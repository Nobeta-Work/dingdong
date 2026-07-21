package cn.nobeta.dingdong.order.domain;
import java.math.BigDecimal;

/**
 * 订单项实体
 * 对应 order_item 表，记录订单中每个商品的快照信息。
 * 采用快照设计：保存下单时刻的商品名称、图片、规格、单价，避免后续商品修改影响历史订单。
 */
public class OrderItem {
    /** 主键 ID */
    private Long id;
    /** 所属订单 ID */
    private Long orderId;
    /** 商品 SKU ID */
    private Long skuId;
    /** 商品 SKU 编码 */
    private String skuCode;
    /** 商品标题（快照） */
    private String productTitle;
    /** 商品主图 URL（快照） */
    private String productImageUrl;
    /** 商品规格 JSON（快照，记录颜色/尺寸等属性） */
    private String specJson;
    /** 单价（下单时快照） */
    private BigDecimal unitPrice;
    /** 购买数量 */
    private Integer quantity;
    /** 该项小计金额（单价 × 数量） */
    private BigDecimal totalAmount;

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getOrderId(){return orderId;} public void setOrderId(Long orderId){this.orderId=orderId;}
    public Long getSkuId(){return skuId;} public void setSkuId(Long skuId){this.skuId=skuId;}
    public String getSkuCode(){return skuCode;} public void setSkuCode(String skuCode){this.skuCode=skuCode;}
    public String getProductTitle(){return productTitle;} public void setProductTitle(String productTitle){this.productTitle=productTitle;}
    public String getProductImageUrl(){return productImageUrl;} public void setProductImageUrl(String productImageUrl){this.productImageUrl=productImageUrl;}
    public String getSpecJson(){return specJson;} public void setSpecJson(String specJson){this.specJson=specJson;}
    public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal unitPrice){this.unitPrice=unitPrice;}
    public Integer getQuantity(){return quantity;} public void setQuantity(Integer quantity){this.quantity=quantity;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal totalAmount){this.totalAmount=totalAmount;}
}
