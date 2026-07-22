package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.config.GitHubImageBedProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

/**
 * GitHub 图床上传服务。
 */
@Service
public class GitHubImageBedService {

    private final GitHubImageBedProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GitHubImageBedService(GitHubImageBedProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "请选择要上传的图片文件");
        }
        if (properties.repo() == null || properties.repo().isBlank()) {
            throw new BusinessException("FILE_UPLOAD_CONFIG", "GitHub 图床仓库未配置");
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
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new UploadResult(buildRawUrl(path), path);
            }
            throw new BusinessException("FILE_UPLOAD_FAILED", extractMessage(response.body()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException("FILE_UPLOAD_FAILED", "图片上传已中断");
        } catch (IOException exception) {
            throw new BusinessException("FILE_UPLOAD_FAILED", "图片上传失败：" + exception.getMessage());
        }
    }

    private String buildPath(MultipartFile file) {
        String extension = resolveExtension(file);
        LocalDate today = LocalDate.now();
        return String.join("/",
                properties.folder(),
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                String.format("%02d", today.getDayOfMonth()),
                UUID.randomUUID().toString().replace("-", "") + extension);
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

    private String buildRawUrl(String path) {
        return properties.rawBaseUrl() + "/" + properties.repo() + "/" + properties.branch() + "/" + encodePath(path);
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