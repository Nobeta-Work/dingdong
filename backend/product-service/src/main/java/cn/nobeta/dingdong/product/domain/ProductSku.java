package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;

/**
 * 商品 SKU 实体
 * <p>对应 product_sku 表，记录每个规格的售价、库存等信息。
 * 价格字段 {@link #price} 是价格校验链路的数据源头，通过 {@link cn.nobeta.dingdong.product.service.CatalogService#saveSku}
 * 写入，经由 {@link cn.nobeta.dingdong.common.rpc.ProductInventoryFacade.SkuSnapshot} 传递给订单服务。</p>
 */
public class ProductSku {
    /** 主键 ID */
    private Long id;
    /** 所属 SPU ID */
    private Long spuId;
    /** SKU 编码（字母数字下划线连字符，4-64 位） */
    private String skuCode;
    /** 规格 JSON（如颜色、尺寸等属性） */
    private String specJson;
    /** 售价（数据库权威价格，订单金额计算以此为准） */
    private BigDecimal price;
    /** 可用库存 */
    private Integer availableStock;
    /** 已锁定库存（下单时预扣） */
    private Integer lockedStock;
    /** 销量 */
    private Integer sales;
    /** 状态：0-下架，1-上架 */
    private Integer status;
    /** 乐观锁版本号 */
    private Integer version;
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