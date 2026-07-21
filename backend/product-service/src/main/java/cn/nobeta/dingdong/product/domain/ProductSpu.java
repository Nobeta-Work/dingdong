package cn.nobeta.dingdong.product.domain;
import java.time.LocalDateTime;
import java.math.BigDecimal;
public class ProductSpu {
    private Long id; private String title; private String subtitle; private String description; private String mainImageUrl; private Long categoryId; private Long brandId; private Integer status; private BigDecimal minPrice; private Integer sales; private LocalDateTime createdAt;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; } public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getMainImageUrl() { return mainImageUrl; } public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public Long getCategoryId() { return categoryId; } public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getBrandId() { return brandId; } public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public BigDecimal getMinPrice() { return minPrice; } public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public Integer getSales() { return sales; } public void setSales(Integer sales) { this.sales = sales; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
