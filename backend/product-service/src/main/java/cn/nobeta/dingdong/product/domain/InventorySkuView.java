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
    
     /**
     * 获取SKU主键ID
     * @return sku唯一主键id
     */
    public Long getSkuId() { return skuId; }
    /**
     * 设置SKU主键ID
     * @param skuId 商品规格主键
     */
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    
    /**
     * 获取业务SKU编码
     * @return 外部交互唯一sku编码
     */
    public String getSkuCode() { return skuCode; }
    /**
     * 设置业务SKU编码
     * @param skuCode 商品规格业务编码
     */
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    
    /**
     * 获取商品SPU标题
     * @return 商品展示标题
     */
    public String getTitle() { return title; }
    /**
     * 设置商品SPU标题
     * @param title 商品名称标题
     */
    public void setTitle(String title) { this.title = title; }
    
    /**
     * 获取商品主图地址
     * @return 商品封面图片链接
     */
    public String getMainImageUrl() { return mainImageUrl; }
    /**
     * 设置商品主图地址
     * @param mainImageUrl 图片资源url
     */
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    
    /**
     * 获取SKU规格json字符串
     * @return 规格信息json文本
     */
    public String getSpecJson() { return specJson; }
    /**
     * 设置SKU规格json字符串
     * @param specJson 存储规格键值的json字符串
     */
    public void setSpecJson(String specJson) { this.specJson = specJson; }
    
    /**
     * 获取商品销售单价
     * @return 数据库权威售价，订单计价基准
     */
    public BigDecimal getPrice() { return price; }
    /**
     * 设置商品销售单价
     * @param price sku标准售价
     */
    public void setPrice(BigDecimal price) { this.price = price; }
    
    /**
     * 获取当前可用库存数量
     * @return 可下单剩余库存
     */
    public Integer getAvailableStock() { return availableStock; }
    /**
     * 设置可用库存数量
     * @param availableStock 剩余可售库存
     */
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    
    /**
     * 获取商品上下架状态
     * @return 0下架/1上架
     */
    public Integer getStatus() { return status; }
    /**
     * 设置商品上下架状态
     * @param status 商品销售状态标识
     */
    public void setStatus(Integer status) { this.status = status; }
}
