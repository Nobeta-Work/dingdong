package cn.nobeta.dingdong.product.rpc;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.product.domain.InventoryLockRecord;
import cn.nobeta.dingdong.product.domain.InventoryChangeLog;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * 产品库存 RPC 服务实现
 * 作为 Dubbo 服务提供者，向订单服务（order-service）暴露库存锁定/释放/确认接口。
 * 价格校验链路关键环节：lockInventory 返回的 SkuSnapshot 中包含
 * 数据库中的权威价格，订单服务基于此快照价格计算订单金额，而非信任客户端传入的价格。
 */
@Service
@DubboService
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {
    private final ProductMapper productMapper;
    public ProductInventoryFacadeImpl(ProductMapper productMapper) { this.productMapper = productMapper; }

    /**
     * 锁定库存 —— 下单时由订单服务调用
     * 价格校验链路核心环节，共 5 个子步骤：
     *   1. 验证每个 SKU 存在且为在售状态（requireSaleableSku）
     *   2. 乐观锁扣减可用库存（校验 available_stock >= quantity）
     *   3. 插入库存锁定流水记录（幂等防重）
     *   4. 返回 SkuSnapshot，其中包含数据库中的权威价格，
     *      订单服务使用此价格计算总金额，而非客户端提交的价格
     * @param orderNo 订单号（用于关联库存锁定记录）
     * @param items 锁定项列表（SKU ID + 数量）
     * @return 商品快照列表（含价格、标题、规格等，用于订单金额计算）
     * @throws BusinessException 库存不足 / SKU 不存在或已下架 / 数量不合法
     */
    @Override @Transactional
    public List<SkuSnapshot> lockInventory(String orderNo, List<LockItem> items) {
        // 订单没有任何待锁定项时，直接拒绝，避免创建空锁单。
        if (items == null || items.isEmpty()) throw new BusinessException("INVENTORY_EMPTY", "没有需要锁定的商品");
        List<SkuSnapshot> snapshots = new ArrayList<>();
        for (LockItem item : items) {
            // 基础参数校验：SKU 和数量必须同时有效，且数量必须大于 0。
            if (item.skuId() == null || item.quantity() == null || item.quantity() <= 0) throw new BusinessException("INVENTORY_INVALID_QUANTITY", "商品数量不合法");
            // 先读取数据库中的 SKU 视图，后续无论锁定成功与否都用它返回服务端权威价格。
            InventorySkuView sku = requireSaleableSkuForUpdate(item.skuId());
            // 幂等防重：同一订单同一 SKU 只允许锁定一次，重复请求直接跳过库存扣减。
            boolean newlyLocked = productMapper.countActiveLock(orderNo, item.skuId()) == 0;
            if (newlyLocked) {
                // 通过乐观锁扣减可用库存，失败说明当前库存不足或状态不满足在售条件。
                if (productMapper.lockStock(item.skuId(), item.quantity()) == 0) throw new BusinessException("INVENTORY_INSUFFICIENT", "商品库存不足：" + sku.getTitle());
                // 写入锁定流水，后续取消/支付确认都依赖这条订单级别的锁定记录。
                productMapper.insertInventoryLock(orderNo, item.skuId(), item.quantity());
                recordChange(sku, "ORDER_LOCK:" + orderNo + ":" + item.skuId(), "ORDER_LOCK", orderNo,
                        -item.quantity(), item.quantity(), 0, "订单锁定库存");
            }
            // 无论是否命中幂等分支，都返回同一份快照给订单服务用于金额计算。
            snapshots.add(snapshot(sku, newlyLocked ? item.quantity() : 0));
        }
        return snapshots;
    }

    /**
     * 释放库存 —— 订单超时关闭或取消时调用
     * 将锁定库存返还至可用库存，并将锁定流水状态标记为 RELEASED。
     * @param orderNo 订单号
     */
    @Override @Transactional
    public void unlockInventory(String orderNo) {
        // 先读取仍处于 LOCKED 状态的库存明细，再逐条回滚可用库存。
        List<InventoryLockRecord> locks = productMapper.findActiveLocks(orderNo);
        for (InventoryLockRecord lock : locks) {
            InventorySkuView sku = requireSkuForUpdate(lock.getSkuId());
            String key = "ORDER_RELEASE:" + orderNo + ":" + lock.getSkuId();
            if (productMapper.countInventoryChange(key) == 0) {
                if (productMapper.unlockStock(lock.getSkuId(), lock.getQuantity()) == 0) throw new BusinessException("INVENTORY_RELEASE_FAILED", "库存释放失败");
                recordChange(sku, key, "ORDER_RELEASE", orderNo, lock.getQuantity(), -lock.getQuantity(), 0, "订单取消或超时释放");
            }
        }
        // 库存回滚完成后，再把锁定流水统一标记为已释放，避免重复释放。
        productMapper.releaseLocks(orderNo);
    }

    /**
     * 确认库存扣减 —— 订单支付成功时调用
     * 将锁定库存转换为销量（locked_stock -> sales），不可再释放。
     * @param orderNo 订单号
     */
    @Override @Transactional
    public void confirmInventory(String orderNo) {
        // 只确认仍处于 LOCKED 状态的库存记录，避免对已处理的订单重复核销。
        List<InventoryLockRecord> locks = productMapper.findActiveLocks(orderNo);
        for (InventoryLockRecord lock : locks) {
            InventorySkuView sku = requireSkuForUpdate(lock.getSkuId());
            String key = "ORDER_CONFIRM:" + orderNo + ":" + lock.getSkuId();
            if (productMapper.countInventoryChange(key) > 0) continue;
            // 将锁定库存转为销量；任意一条失败都说明数据状态异常，需要回滚。
            if (productMapper.confirmStock(lock.getSkuId(), lock.getQuantity()) == 0) {
                throw new BusinessException("INVENTORY_CONFIRM_FAILED", "库存锁定记录异常");
            }
            recordChange(sku, key, "ORDER_CONFIRM", orderNo, 0, -lock.getQuantity(), lock.getQuantity(), "支付成功核销锁定库存");
        }
        // 库存核销成功后，再统一把流水状态改为 CONFIRMED。
        productMapper.confirmLocks(orderNo);
    }

    /**
     * 获取商品 SKU 快照 —— 加入购物车时调用
     * 用于校验商品存在性并获取实时信息（价格、标题、库存等）。
     * 不执行库存锁定操作。
     * @param skuIds SKU ID 列表
     * @return 商品快照列表（含价格）
     */
    @Override
    public List<SkuSnapshot> getSkuSnapshots(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return List.of();
        List<SkuSnapshot> result = new ArrayList<>();
        for (Long skuId : skuIds) result.add(snapshot(requireSaleableSku(skuId), 0));
        return result;
    }

    /**
     * 校验并获取在售 SKU 视图
     * 价格校验链路辅助方法：从数据库查询 SKU + 关联 SPU 信息，
     * 验证 SKU 存在且处于上架状态（status=1）。
     * @param skuId SKU ID
     * @return 库存视图对象（含数据库中的实时价格）
     * @throws BusinessException 当 SKU 不存在或已下架时抛出
     */
    private InventorySkuView requireSaleableSku(Long skuId) {
        // 商品快照必须从数据库读取，确保价格、库存、上下架状态都是实时值。
        InventorySkuView sku = productMapper.findInventorySku(skuId);
        if (sku == null || !Integer.valueOf(1).equals(sku.getStatus())) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "SKU 不存在或已下架");
        return sku;
    }

    private InventorySkuView requireSaleableSkuForUpdate(Long skuId) {
        InventorySkuView sku = requireSkuForUpdate(skuId);
        if (!Integer.valueOf(1).equals(sku.getStatus())) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "SKU 不存在或已下架");
        return sku;
    }

    private InventorySkuView requireSkuForUpdate(Long skuId) {
        InventorySkuView sku = productMapper.findInventorySkuForUpdate(skuId);
        if (sku == null) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "SKU 不存在");
        return sku;
    }

    private void recordChange(InventorySkuView before, String key, String type, String referenceNo,
                              int availableDelta, int lockedDelta, int salesDelta, String remark) {
        InventoryChangeLog log = new InventoryChangeLog();
        log.setSkuId(before.getSkuId()); log.setBusinessKey(key); log.setBusinessType(type); log.setReferenceNo(referenceNo);
        log.setChangeAvailable(availableDelta); log.setChangeLocked(lockedDelta); log.setChangeSales(salesDelta);
        log.setBeforeAvailable(before.getAvailableStock()); log.setAfterAvailable(before.getAvailableStock() + availableDelta);
        log.setBeforeLocked(before.getLockedStock()); log.setAfterLocked(before.getLockedStock() + lockedDelta);
        log.setBeforeSales(before.getSales()); log.setAfterSales(before.getSales() + salesDelta); log.setRemark(remark);
        productMapper.insertInventoryChange(log);
    }

    /**
     * 构建 SKU 快照 —— 价格对外输出的关键方法
     * 价格校验链路：将数据库中的实时价格封装进 SkuSnapshot，
     * 返回给订单服务用于金额计算。该价格是服务端权威价格，客户端无法篡改。
     * @param sku 库存视图对象（来自数据库）
     * @param quantity 已锁定数量（用于计算剩余可用库存）
     * @return SKU 快照（含价格）
     */
    private SkuSnapshot snapshot(InventorySkuView sku, Integer quantity) {
        // 返回给订单服务的快照只用于展示和金额计算，库存值按当前可用库存减去本次锁定数量计算。
        return new SkuSnapshot(sku.getSkuId(), sku.getSkuCode(), sku.getTitle(), sku.getMainImageUrl(), sku.getSpecJson(), sku.getPrice(), sku.getAvailableStock() - quantity);
    }
}
