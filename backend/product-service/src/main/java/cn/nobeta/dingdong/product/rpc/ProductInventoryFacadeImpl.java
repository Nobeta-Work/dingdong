package cn.nobeta.dingdong.product.rpc;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.product.domain.InventoryLockRecord;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@DubboService
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {
    private final ProductMapper productMapper;
    public ProductInventoryFacadeImpl(ProductMapper productMapper) { this.productMapper = productMapper; }

    @Override @Transactional
    public List<SkuSnapshot> lockInventory(String orderNo, List<LockItem> items) {
        if (items == null || items.isEmpty()) throw new BusinessException("INVENTORY_EMPTY", "没有需要锁定的商品");
        List<SkuSnapshot> snapshots = new ArrayList<>();
        for (LockItem item : items) {
            if (item.skuId() == null || item.quantity() == null || item.quantity() <= 0) throw new BusinessException("INVENTORY_INVALID_QUANTITY", "商品数量不合法");
            InventorySkuView sku = requireSaleableSku(item.skuId());
            if (productMapper.countActiveLock(orderNo, item.skuId()) == 0) {
                if (productMapper.lockStock(item.skuId(), item.quantity()) == 0) throw new BusinessException("INVENTORY_INSUFFICIENT", "商品库存不足：" + sku.getTitle());
                productMapper.insertInventoryLock(orderNo, item.skuId(), item.quantity());
            }
            snapshots.add(snapshot(sku, item.quantity()));
        }
        return snapshots;
    }

    @Override @Transactional
    public void unlockInventory(String orderNo) {
        List<InventoryLockRecord> locks = productMapper.findActiveLocks(orderNo);
        for (InventoryLockRecord lock : locks) productMapper.unlockStock(lock.getSkuId(), lock.getQuantity());
        productMapper.releaseLocks(orderNo);
    }

    @Override
    public List<SkuSnapshot> getSkuSnapshots(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return List.of();
        List<SkuSnapshot> result = new ArrayList<>();
        for (Long skuId : skuIds) result.add(snapshot(requireSaleableSku(skuId), 0));
        return result;
    }

    private InventorySkuView requireSaleableSku(Long skuId) {
        InventorySkuView sku = productMapper.findInventorySku(skuId);
        if (sku == null || !Integer.valueOf(1).equals(sku.getStatus())) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "SKU 不存在或已下架");
        return sku;
    }
    private SkuSnapshot snapshot(InventorySkuView sku, Integer quantity) { return new SkuSnapshot(sku.getSkuId(), sku.getSkuCode(), sku.getTitle(), sku.getMainImageUrl(), sku.getSpecJson(), sku.getPrice(), sku.getAvailableStock() - quantity); }
}
