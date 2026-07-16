package cn.nobeta.dingdong.product.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class ProductRequests {
    private ProductRequests() { }
    public record CategoryRequest(@NotBlank @Size(max=64) String name, Long parentId, @Min(0) @Max(9999) Integer sortOrder, @NotNull @Min(0) @Max(1) Integer status) { }
    public record BrandRequest(@NotBlank @Size(max=64) String name, @Size(max=512) String logoUrl, @Min(0) @Max(9999) Integer sortOrder, @NotNull @Min(0) @Max(1) Integer status) { }
    public record SpuRequest(@NotBlank @Size(max=128) String title, @Size(max=255) String subtitle, @Size(max=10000) String description, @Size(max=512) String mainImageUrl, @NotNull Long categoryId, @NotNull Long brandId, @NotNull @Min(0) @Max(1) Integer status) { }
    public record SkuRequest(@NotBlank @Pattern(regexp="^[A-Za-z0-9_-]{4,64}$", message="SKU 编码应为 4-64 位字母、数字、下划线或连字符") String skuCode, @NotBlank @Size(max=1000) String specJson, @NotNull @DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal price, @NotNull @Min(0) Integer availableStock, @NotNull @Min(0) @Max(1) Integer status) { }
}
