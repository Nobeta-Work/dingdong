package cn.nobeta.dingdong.common.rpc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Stable Dubbo contract owned by product-service and consumed by order-service. */
public interface ProductInventoryFacade {
    List<SkuSnapshot> lockInventory(String orderNo, List<LockItem> items);
    void unlockInventory(String orderNo);
    List<SkuSnapshot> getSkuSnapshots(List<Long> skuIds);

    record LockItem(Long skuId, Integer quantity) implements Serializable { }
    record SkuSnapshot(Long skuId, String skuCode, String title, String mainImageUrl,
                       String specJson, BigDecimal price, Integer availableStock) implements Serializable { }
}
