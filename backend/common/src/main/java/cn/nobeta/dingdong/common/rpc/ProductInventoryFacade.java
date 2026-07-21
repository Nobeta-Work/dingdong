package cn.nobeta.dingdong.common.rpc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品库存 Dubbo 契约接口（产品服务提供，订单服务消费）
 * 用于跨服务调用，支持库存锁定、释放和确认扣减，保障订单与库存的一致性。
 */
public interface ProductInventoryFacade {

    /**
     * 锁定库存 — 下单时调用
     * 从可用库存中预扣减指定数量的商品，并返回下单时刻的商品快照信息（用于订单金额计算）。
     * @param orderNo 订单号（用于关联库存锁定记录）
     * @param items 锁定项列表
     * @return 每个 SKU 的快照信息（标题、图片、规格、价格、可用库存）
     */
    List<SkuSnapshot> lockInventory(String orderNo, List<LockItem> items);

    /**
     * 释放库存 — 订单超时关闭或取消时调用
     * 释放之前锁定的库存，使其恢复为可用库存。
     * @param orderNo 订单号
     */
    void unlockInventory(String orderNo);

    /**
     * 确认库存扣减 — 订单支付成功后调用
     * 将锁定的库存正式扣除，不再可释放。
     * @param orderNo 订单号
     */
    void confirmInventory(String orderNo);

    /**
     * 获取商品 SKU 快照 — 加入购物车时调用
     * 用于校验商品存在性并获取实时信息。
     * @param skuIds SKU ID 列表
     * @return 商品快照列表
     */
    List<SkuSnapshot> getSkuSnapshots(List<Long> skuIds);

    /** 库存锁定请求项 */
    record LockItem(Long skuId, Integer quantity) implements Serializable { }

    /** 商品 SKU 快照信息（价格校验链路关键数据载体） */
    record SkuSnapshot(Long skuId, String skuCode, String title, String mainImageUrl,
                       String specJson, BigDecimal price, Integer availableStock) implements Serializable { }
}