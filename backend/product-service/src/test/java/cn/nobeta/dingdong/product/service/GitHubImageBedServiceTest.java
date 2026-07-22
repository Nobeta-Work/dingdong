package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.product.config.GitHubImageBedProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubImageBedServiceTest {

    private final GitHubImageBedService service = new GitHubImageBedService(
            new GitHubImageBedProperties(
                    "Y-tan-max/-",
                    "test-token",
                    "main",
                    "https://cdn.jsdelivr.net/gh",
                    ""),
            new ObjectMapper());

    @Test
    void imageIsStoredAtRepositoryRootAndUsesConfiguredCdn() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "商品图片.png", "image/png", new byte[]{1});

        String path = service.buildPath(file);

        assertThat(path)
                .endsWith(".png")
                .doesNotContain("/")
                .doesNotContain("product-images");
        assertThat(service.buildPublicUrl(path))
                .isEqualTo("https://cdn.jsdelivr.net/gh/Y-tan-max/-@main/" + path);
    }
}
