package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.domain.InventoryChangeLog;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventoryService {
    private final ProductMapper mapper;
    public InventoryService(ProductMapper mapper) { this.mapper = mapper; }

    public Page page(Long skuId, String businessType, int page, int size) {
        int p = Math.max(page, 1), s = Math.min(Math.max(size, 1), 100);
        return new Page(mapper.findInventoryChanges(skuId, businessType, (p - 1) * s, s),
                mapper.countInventoryChanges(skuId, businessType), p, s);
    }

    @Transactional
    public InventoryChangeLog adjust(Long skuId, String requestId, Integer delta, String reason) {
        if (delta == 0) throw new BusinessException("INVENTORY_ADJUSTMENT_EMPTY", "库存调整量不能为 0");
        InventorySkuView before = mapper.findInventorySkuForUpdate(skuId);
        if (before == null) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "SKU 不存在");
        String key = "ADMIN_ADJUST:" + requestId;
        if (mapper.countInventoryChange(key) > 0) return mapper.findInventoryChanges(skuId, null, 0, 100).stream()
                .filter(item -> key.equals(item.getBusinessKey())).findFirst().orElseThrow();
        if (mapper.adjustAvailableStock(skuId, delta) == 0) throw new BusinessException("INVENTORY_ADJUSTMENT_INVALID", "调整后库存不能小于 0");
        InventoryChangeLog log = new InventoryChangeLog();
        log.setSkuId(skuId); log.setBusinessKey(key); log.setBusinessType("ADMIN_ADJUST"); log.setReferenceNo(requestId);
        log.setChangeAvailable(delta); log.setChangeLocked(0); log.setChangeSales(0);
        log.setBeforeAvailable(before.getAvailableStock()); log.setAfterAvailable(before.getAvailableStock() + delta);
        log.setBeforeLocked(before.getLockedStock()); log.setAfterLocked(before.getLockedStock());
        log.setBeforeSales(before.getSales()); log.setAfterSales(before.getSales()); log.setRemark(reason);
        mapper.insertInventoryChange(log);
        return mapper.findInventoryChanges(skuId, null, 0, 1).getFirst();
    }

    public record Page(List<InventoryChangeLog> items, long total, int page, int size) { }
}
