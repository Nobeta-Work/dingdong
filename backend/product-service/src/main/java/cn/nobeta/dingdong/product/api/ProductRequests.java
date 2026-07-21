package cn.nobeta.dingdong.product.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品模块的请求 DTO 集合
 * 内部以静态 record 定义，避免顶层文件膨胀；所有构造函数参数均可通过
 * jakarta.validation.Valid 校验。
 */
public final class ProductRequests {
    private ProductRequests() { }

    /** 商品分类请求（名称、父级 ID、排序、状态） */
    public record CategoryRequest
    (@NotBlank @Size(max=64) String name, Long parentId, 
    @Min(0) @Max(9999) Integer sortOrder, 
    @NotNull @Min(0) @Max(1) Integer status) { }

    /** 品牌请求（名称、Logo、排序、状态） */
    public record BrandRequest
    (@NotBlank @Size(max=64) String name,
    @Size(max=512) String logoUrl,
    @Min(0) @Max(9999) Integer sortOrder,
    @NotNull @Min(0) @Max(1) Integer status) { }

    /** 商品 SPU 请求（标题、副标题、描述、主图、分类、品牌、状态） */
    public record SpuRequest
    (@NotBlank @Size(max=128) String title,
    @Size(max=255) String subtitle, 
    @Size(max=10000) String description, 
    @Size(max=512) String mainImageUrl, 
    @NotNull Long categoryId, 
    @NotNull Long brandId, 
    @NotNull @Min(0) @Max(1) Integer status) { }

    /**
     * SKU 规格请求
     * 价格校验入口：通过 jakarta.validation 注解约束价格的合法性。
     * - @DecimalMin("0.01") — 价格必须大于 0，不允许免费或负数
     * - @Digits(integer=10, fraction=2) — 整数部分最多 10 位，小数部分最多 2 位
     * 此处的价格校验属于"参数层校验"（Input Validation），
     * 后续在 CatalogService#saveSku 中持久化写入数据库，
     * 并在 OrderService#create 中通过库存快照进行一致性校验。
     */
    public record SkuRequest
    (@NotBlank @Pattern(regexp="^[A-Za-z0-9_-]{4,64}$", message="SKU 编码应为 4-64 位字母、数字、下划线或连字符") String skuCode, 
    @NotBlank @Size(max=1000) String specJson, 
    @NotNull @DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal price, 
    @NotNull @Min(0) Integer availableStock, 
    @NotNull @Min(0) @Max(1) Integer status) { }
}