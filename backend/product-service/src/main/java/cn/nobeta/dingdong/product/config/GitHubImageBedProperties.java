package cn.nobeta.dingdong.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GitHub 图床上传配置。
 */
@ConfigurationProperties(prefix = "github")
public record GitHubImageBedProperties(
        String repo,
        String token,
        String branch,
        String cdnBaseUrl,
        String sslTrustStoreType) {

    public String branch() {
        return branch == null || branch.isBlank() ? "main" : branch;
    }

    public String cdnBaseUrl() {
        return cdnBaseUrl == null || cdnBaseUrl.isBlank() ? "https://cdn.jsdelivr.net/gh" : stripTrailingSlash(cdnBaseUrl);
    }

    public String sslTrustStoreType() {
        return sslTrustStoreType == null ? "" : sslTrustStoreType.trim();
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
