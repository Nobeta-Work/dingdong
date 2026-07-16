package cn.nobeta.dingdong.product.domain;

import java.math.BigDecimal;

public class InventorySkuView {
    private Long skuId; private String skuCode; private String title; private String mainImageUrl; private String specJson; private BigDecimal price; private Integer availableStock; private Integer status;
    public Long getSkuId() { return skuId; } public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; } public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getMainImageUrl() { return mainImageUrl; } public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public String getSpecJson() { return specJson; } public void setSpecJson(String specJson) { this.specJson = specJson; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getAvailableStock() { return availableStock; } public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
