package cn.nobeta.dingdong.product.domain;
import java.time.LocalDateTime;

/**
 * 商品SPU实体
 */
public class ProductSpu {
    /** 主键id */
    private Long id;
    /** 商品标题 */
    private String title;
    /** 商品副标题 */
    private String subtitle;
    /** 商品详情描述 */
    private String description;
    /** 商品主图地址 */
    private String mainImageUrl;
    /** 分类id */
    private Long categoryId;
    /** 品牌id */
    private Long brandId;
    /** 上下架状态 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
