package cn.nobeta.dingdong.product.domain;

import java.math.BigDecimal;

/**
 * 库存业务视图
 * <p>联表查询 product_sku + product_spu 的聚合视图，用于库存锁定/解锁/确认操作。
 * 价格字段 {@link #price} 来自数据库 {@code product_sku.price}，
 * 是价格校验链路中返回给订单服务的权威价格来源。</p>
 */
public class InventorySkuView {
    /** SKU ID */
    private Long skuId;
    /** SKU 编码 */
    private String skuCode;
    /** 商品标题（来自 SPU） */
    private String title;
    /** 商品主图 URL（来自 SPU） */
    private String mainImageUrl;
    /** 规格 JSON */
    private String specJson;
    /** 售价（数据库权威价格，订单服务以此为准） */
    private BigDecimal price;
    /** 可用库存 */
    private Integer availableStock;
    /** 状态：0-下架，1-上架 */
    private Integer status;
    public Long getSkuId() { return skuId; } public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; } public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getMainImageUrl() { return mainImageUrl; } public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public String getSpecJson() { return specJson; } public void setSpecJson(String specJson) { this.specJson = specJson; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getAvailableStock() { return availableStock; } public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}