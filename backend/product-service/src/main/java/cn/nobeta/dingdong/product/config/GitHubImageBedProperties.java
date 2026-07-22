package cn.nobeta.dingdong.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GitHub 图床上传配置。
 */
@ConfigurationProperties(prefix = "dingdong.image-bed.github")
public record GitHubImageBedProperties(
        String repo,
        String token,
        String branch,
        String folder,
        String rawBaseUrl) {

    public String branch() {
        return branch == null || branch.isBlank() ? "main" : branch;
    }

    public String folder() {
        return folder == null || folder.isBlank() ? "product-images" : folder;
    }

    public String rawBaseUrl() {
        return rawBaseUrl == null || rawBaseUrl.isBlank() ? "https://raw.githubusercontent.com" : rawBaseUrl;
    }
}