package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;
public class ProductSku {
    private Long id; private Long spuId; private String skuCode; private String specJson; private BigDecimal price; private Integer availableStock; private Integer lockedStock; private Integer sales; private Integer status; private Integer version;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getSpuId() { return spuId; } public void setSpuId(Long spuId) { this.spuId = spuId; }
    public String getSkuCode() { return skuCode; } public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSpecJson() { return specJson; } public void setSpecJson(String specJson) { this.specJson = specJson; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getAvailableStock() { return availableStock; } public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getLockedStock() { return lockedStock; } public void setLockedStock(Integer lockedStock) { this.lockedStock = lockedStock; }
    public Integer getSales() { return sales; } public void setSales(Integer sales) { this.sales = sales; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Integer getVersion() { return version; } public void setVersion(Integer version) { this.version = version; }
}
