package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;

/**
 * 商品 SKU 实体
 * 对应 product_sku 表，记录每个规格的售价、库存等信息。
 * 价格字段 {@link #price} 是价格校验链路的数据源头，通过 {@link cn.nobeta.dingdong.product.service.CatalogService#saveSku}
 * 写入，经由 {@link cn.nobeta.dingdong.common.rpc.ProductInventoryFacade.SkuSnapshot} 传递给订单服务。
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

    /**
     * 获取主键ID
     * @return id
     */
    public Long getId() { return id; }

    /**
     * 设置主键ID
     * @param id
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取所属SPU ID
     * @return spuId
     */
    public Long getSpuId() { return spuId; }

    /**
     * 设置所属SPU ID
     * @param spuId
     */
    public void setSpuId(Long spuId) { this.spuId = spuId; }

    /**
     * 获取SKU编码
     * @return skuCode
     */
    public String getSkuCode() { return skuCode; }

    /**
     * 设置SKU编码
     * @param skuCode
     */
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    /**
     * 获取规格属性JSON
     * @return specJson
     */
    public String getSpecJson() { return specJson; }

    /**
     * 设置规格属性JSON
     * @param specJson
     */
    public void setSpecJson(String specJson) { this.specJson = specJson; }

    /**
     * 获取商品售价
     * @return price
     */
    public BigDecimal getPrice() { return price; }

    /**
     * 设置商品售价
     * @param price
     */
    public void setPrice(BigDecimal price) { this.price = price; }

    /**
     * 获取可用库存数量
     * @return availableStock
     */
    public Integer getAvailableStock() { return availableStock; }

    /**
     * 设置可用库存数量
     * @param availableStock
     */
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }

    /**
     * 获取已锁定库存数量
     * @return lockedStock
     */
    public Integer getLockedStock() { return lockedStock; }

    /**
     * 设置已锁定库存数量
     * @param lockedStock
     */
    public void setLockedStock(Integer lockedStock) { this.lockedStock = lockedStock; }

    /**
     * 获取SKU累计销量
     * @return sales
     */
    public Integer getSales() { return sales; }

    /**
     * 设置SKU累计销量
     * @param sales
     */
    public void setSales(Integer sales) { this.sales = sales; }

    /**
     * 获取SKU上下架状态
     * @return status 0-下架，1-上架
     */
    public Integer getStatus() { return status; }

    /**
     * 设置SKU上下架状态
     * @param status 0-下架，1-上架
     */
    public void setStatus(Integer status) { this.status = status; }

    /**
     * 获取乐观锁版本号
     * @return version
     */
    public Integer getVersion() { return version; }

    /**
     * 设置乐观锁版本号
     * @param version
     */
    public void setVersion(Integer version) { this.version = version; }
}