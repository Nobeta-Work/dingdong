package cn.nobeta.dingdong.product.domain;
/**库存锁记录类 */
public class InventoryLockRecord {
    private Long skuId; 
    private Integer quantity;
    public Long getSkuId() { return skuId; } 
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQuantity() { return quantity; } 
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
