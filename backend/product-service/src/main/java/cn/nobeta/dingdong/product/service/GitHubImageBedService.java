package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.config.GitHubImageBedProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

/**
 * GitHub 图床上传服务。
 */
@Service
public class GitHubImageBedService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final GitHubImageBedProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GitHubImageBedService(GitHubImageBedProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = buildHttpClient(properties.sslTrustStoreType());
    }

    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "请选择要上传的图片文件");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BusinessException("FILE_TYPE_INVALID", "仅支持上传图片文件");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("FILE_TOO_LARGE", "图片大小不能超过 5MB");
        }
        if (properties.repo() == null || !properties.repo().matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) {
            throw new BusinessException("FILE_UPLOAD_CONFIG", "GitHub 图床仓库应配置为 owner/repository");
        }
        if (properties.token() == null || properties.token().isBlank()) {
            throw new BusinessException("FILE_UPLOAD_CONFIG", "GitHub 图床访问令牌未配置");
        }

        String path = buildPath(file);
        String content = Base64.getEncoder().encodeToString(readBytes(file));
        String requestBody = buildRequestBody(path, content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + properties.repo() + "/contents/" + encodePath(path)))
                .header("Authorization", "Bearer " + properties.token())
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "dingdong-product-service")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new UploadResult(buildPublicUrl(path), path);
            }
            throw new BusinessException("FILE_UPLOAD_FAILED", extractMessage(response.body()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("FILE_UPLOAD_FAILED", "图片上传已中断");
        } catch (IOException exception) {
            throw new BusinessException("FILE_UPLOAD_FAILED", "图片上传失败：" + exception.getMessage());
        }
    }

    String buildPath(MultipartFile file) {
        String extension = resolveExtension(file);
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String resolveExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                return originalName.substring(dotIndex).toLowerCase();
            }
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return ".png";
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".png";
        };
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException("FILE_UPLOAD_FAILED", "读取图片内容失败");
        }
    }

    private String buildRequestBody(String path, String content) {
        try {
            return objectMapper.writeValueAsString(new GitHubContentsRequest(
                    "upload image: " + path,
                    content,
                    properties.branch()));
        } catch (IOException exception) {
            throw new BusinessException("FILE_UPLOAD_FAILED", "构建上传请求失败");
        }
    }

    String buildPublicUrl(String path) {
        return properties.cdnBaseUrl()
                + "/" + properties.repo()
                + "@" + UriUtils.encodePathSegment(properties.branch(), StandardCharsets.UTF_8)
                + "/" + encodePath(path);
    }

    /**
     * 开发环境使用 Windows 系统根证书库，以兼容企业代理或本机已安装的证书链。
     * 未配置时仍使用 JVM 默认信任库；任何情况下都不会绕过 HTTPS 证书校验。
     */
    private HttpClient buildHttpClient(String trustStoreType) {
        if (trustStoreType == null || trustStoreType.isBlank()) {
            return HttpClient.newBuilder().build();
        }
        try {
            KeyStore trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(null, null);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return HttpClient.newBuilder().sslContext(sslContext).build();
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("无法加载 HTTPS 信任库：" + trustStoreType, exception);
        }
    }

    private String encodePath(String path) {
        return Arrays.stream(path.split("/"))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .reduce((left, right) -> left + "/" + right)
                .orElse(path);
    }

    private String extractMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode message = root.get("message");
            if (message != null && !message.isNull() && !message.asText().isBlank()) {
                return "GitHub 上传失败：" + message.asText();
            }
        } catch (Exception ignored) {
        }
        return body == null || body.isBlank() ? "GitHub 上传失败" : body;
    }

    public record UploadResult(String url, String path) { }

    private record GitHubContentsRequest(String message, String content, String branch) { }
}
