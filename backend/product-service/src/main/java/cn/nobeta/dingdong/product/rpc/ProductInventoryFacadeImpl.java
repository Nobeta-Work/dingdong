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
        if (items == null || items.isEmpty()) throw new BusinessException("INVENTORY_EMPTY", "没有需要锁定的商品");
        List<SkuSnapshot> snapshots = new ArrayList<>();
        for (LockItem item : items) {
            // 校验锁定数量 >= 0
            if (item.skuId() == null || item.quantity() == null || item.quantity() <= 0) throw new BusinessException("INVENTORY_INVALID_QUANTITY", "商品数量不合法");
            // 1. 校验 SKU 存在且在售：获取数据库中的实时价格信息
            InventorySkuView sku = requireSaleableSku(item.skuId());
            // 2. 幂等防重：若该订单已锁定过该 SKU，则跳过锁定操作
            if (productMapper.countActiveLock(orderNo, item.skuId()) == 0) {
                // 3. 乐观锁扣减库存（校验可用库存 >= 需求量）
                if (productMapper.lockStock(item.skuId(), item.quantity()) == 0) throw new BusinessException("INVENTORY_INSUFFICIENT", "商品库存不足：" + sku.getTitle());
                // 4. 记录库存锁定流水
                productMapper.insertInventoryLock(orderNo, item.skuId(), item.quantity());
            }
            // 5. 构建快照（含数据库中最新价格），返回给订单服务
            snapshots.add(snapshot(sku, item.quantity()));
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
        List<InventoryLockRecord> locks = productMapper.findActiveLocks(orderNo);
        for (InventoryLockRecord lock : locks) productMapper.unlockStock(lock.getSkuId(), lock.getQuantity());
        productMapper.releaseLocks(orderNo);
    }

    /**
     * 确认库存扣减 —— 订单支付成功时调用
     * 将锁定库存转换为销量（locked_stock -> sales），不可再释放。
     * @param orderNo 订单号
     */
    @Override @Transactional
    public void confirmInventory(String orderNo) {
        List<InventoryLockRecord> locks = productMapper.findActiveLocks(orderNo);
        for (InventoryLockRecord lock : locks) {
            if (productMapper.confirmStock(lock.getSkuId(), lock.getQuantity()) == 0) {
                throw new BusinessException("INVENTORY_CONFIRM_FAILED", "库存锁定记录异常");
            }
        }
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
        InventorySkuView sku = productMapper.findInventorySku(skuId);
        if (sku == null || !Integer.valueOf(1).equals(sku.getStatus())) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "SKU 不存在或已下架");
        return sku;
    }

    /**
     * 构建 SKU 快照 —— 价格对外输出的关键方法
     * 价格校验链路：将数据库中的实时价格封装进 SkuSnapshot，
     * 返回给订单服务用于金额计算。该价格是服务端权威价格，客户端无法篡改。
     * @param sku 库存视图对象（来自数据库）
     * @param quantity 已锁定数量（用于计算剩余可用库存）
     * @return SKU 快照（含价格）
     */
    private SkuSnapshot snapshot(InventorySkuView sku, Integer quantity) { return new SkuSnapshot(sku.getSkuId(), sku.getSkuCode(), sku.getTitle(), sku.getMainImageUrl(), sku.getSpecJson(), sku.getPrice(), sku.getAvailableStock() - quantity); }
}