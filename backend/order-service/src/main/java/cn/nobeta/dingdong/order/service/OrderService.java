package cn.nobeta.dingdong.order.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.common.rpc.UserAddressFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.CreateOrderRequest;
import cn.nobeta.dingdong.order.domain.*;
import cn.nobeta.dingdong.order.mapper.OrderMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单核心业务服务
 * 实现了订单的生命周期管理：创建、支付、发货、收货、关闭等。
 * 使用 Dubbo RPC 远程调用产品服务的库存锁定、用户服务的地址快照，事务一致。
 */
@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final OrderOutboxService outboxService;
    @DubboReference(check = false) private ProductInventoryFacade productFacade;
    @DubboReference(check = false) private UserAddressFacade addressFacade;

    public OrderService(OrderMapper orderMapper, CartService cartService, OrderOutboxService outboxService) {
        this.orderMapper = orderMapper;
        this.cartService = cartService;
        this.outboxService = outboxService;
    }

    /**
     * 创建订单 — 核心下单流程，共 10 个步骤
     * 价格校验链路核心说明：
     * - 步骤 4 通过 RPC 调用 ProductInventoryFacadeImpl#lockInventory
     *   获取包含数据库权威价格的商品快照
     * - 步骤 5 使用快照价格计算订单总金额，拒绝信任客户端传入的价格
     * - 步骤 7 将快照价格写入订单项（unit_price / total_amount），作为历史快照保存
     * 具体步骤：
     *   1. 从购物车获取待结算商品
     *   2. 通过 RPC 获取收件地址快照
     *   3. 生成唯一订单号
     *   4. 调用产品服务锁定库存（分布式事务起点）
     *   5. 基于快照价格计算订单总金额
     *   6. 写入订单主表（mall_order）
     *   7. 写入订单项（order_item）并保存商品快照
     *   8. 删除已结算的购物车项
     *   9. 创建超时关闭事件（Outbox 模式）
     *  10. 提交事务后发布超时事件（保障一致性）
     * @param userId 当前用户 ID
     * @param request 创建订单请求（含地址 ID、购物车项 ID 列表）
     * @return 新创建的订单实体（包含数据库生成的 ID）
     * @throws BusinessException 业务校验失败，如商品不可售、购物车项不存在
     */
    @Transactional
    public MallOrder create(Long userId, CreateOrderRequest request) {
        // 1. 从购物车获取待结算商品
        List<CartItem> carts = cartService.itemsForOrder(userId, request.cartItemIds());
        // 2. 获取地址快照：这里直接拿下单时刻的收货信息，避免后续用户修改地址影响历史订单展示
        var address = addressFacade.getAddressSnapshot(userId, request.addressId());
        // 3. 生成订单号（格式：DD + yyyyMMddHHmmssSSS + 3位随机数）
        String orderNo = nextOrderNo();
        boolean locked = false;
        try {
            // 4. 准备锁定库存的请求参数
            List<ProductInventoryFacade.LockItem> locks = carts.stream().map(i -> new ProductInventoryFacade.LockItem(i.getSkuId(), i.getQuantity())).toList();
            // 调用产品服务锁定库存（返回商品快照用于计算金额）
            // 价格校验链路：lockInventory 返回的 SkuSnapshot.price 是数据库中的权威价格
            List<ProductInventoryFacade.SkuSnapshot> snapshots = productFacade.lockInventory(orderNo, locks);
            locked = true;
            // 建立商品快照索引（skuId → snapshot）
            Map<Long, ProductInventoryFacade.SkuSnapshot> snapshotMap = new HashMap<>();
            snapshots.forEach(s -> snapshotMap.put(s.skuId(), s));
            // 5. 基于快照价格计算订单总金额（使用下单时的价格，拒绝客户端传入的值）
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem cart : carts) {
                var snapshot = snapshotMap.get(cart.getSkuId());
                if (snapshot == null) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "商品不可售");
                // 价格校验：累加快照价格 × 数量，确保总金额由服务端权威价格计算得出
                total = total.add(snapshot.price().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
            // 6. 构建订单主表实体
            MallOrder order = new MallOrder();
            order.setOrderNo(orderNo); order.setUserId(userId);
            // 订单收件人信息直接来源于地址快照，保证订单落库时保存的是历史状态
            order.setReceiverName(address.receiverName());
            order.setReceiverPhone(address.receiverPhone());
            order.setReceiverAddress(String.join(" ", address.province(), address.city(), address.district(), address.detailAddress()));
            order.setTotalAmount(total);
            order.setStatus("PENDING_PAYMENT"); // 初始状态：待支付
            orderMapper.insertOrder(order); // 插入订单主表记录

            // 7. 写入订单项（保存商品快照）
            for (CartItem cart : carts) {
                var snapshot = snapshotMap.get(cart.getSkuId());
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setSkuId(snapshot.skuId());
                item.setSkuCode(snapshot.skuCode());
                item.setProductTitle(snapshot.title());
                item.setProductImageUrl(snapshot.mainImageUrl());
                item.setSpecJson(snapshot.specJson());
                // 价格校验：写入下单时的快照单价（unitPrice），用于历史追溯
                item.setUnitPrice(snapshot.price());
                item.setQuantity(cart.getQuantity());
                // 价格校验：计算该项小计金额（快照价格 × 数量）
                item.setTotalAmount(snapshot.price().multiply(BigDecimal.valueOf(cart.getQuantity())));
                orderMapper.insertItem(item); // 插入订单项记录
            }
            // 8. 删除已结算的购物车项
            cartService.removeAfterOrder(userId, carts.stream().map(CartItem::getId).toList());
            // 9. 创建超时关闭事件（Outbox 模式）
            OrderOutbox timeout = outboxService.createTimeout(orderNo);
            // 10. 事务提交后发布超时事件
            publishTimeoutAfterCommit(timeout.getId());
            // 返回查询到的订单实体（包含数据库生成的 ID）
            return orderMapper.findOwned(orderNo, userId);
        } catch (RuntimeException exception) {
            // 异常回滚：若已锁定库存则释放
            if (locked) productFacade.unlockInventory(orderNo);
            throw exception;
        }
    }

    /** 查询当前用户指定订单号订单，不存在时抛出业务异常 */
    public MallOrder get(Long userId, String orderNo) {
        MallOrder order = orderMapper.findOwned(orderNo, userId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        return order;
    }

    /** 查询指定订单的所有订单项 */
    public List<OrderItem> items(Long orderId) { return orderMapper.findItems(orderId); }

    /** 分页查询当前用户订单 */
    public List<MallOrder> page(Long userId, int page, int size) {
        return orderMapper.findPage(userId, size, (page - 1) * size);
    }

    /** 统计当前用户订单总数 */
    public long count(Long userId) { return orderMapper.countByUserId(userId); }

    /**
     * 标记订单已支付 — 由 PaymentSuccessListener 消费支付成功事件后调用
     * 将订单状态从 PENDING_PAYMENT 流转为 PAID，同时确认锁定库存为实际扣减。
     * @param orderNo 订单号
     * @param paymentNo 支付单号（用于关联支付记录）
     * @param paidAt 支付成功时间
     */
    @Transactional
    public void markPaid(String orderNo, String paymentNo, LocalDateTime paidAt) {
        MallOrder order = requireOrder(orderNo);
        if ("PAID".equals(order.getStatus())) return; // 幂等处理
        if (!"PENDING_PAYMENT".equals(order.getStatus())) throw new BusinessException("ORDER_STATUS_INVALID", "当前订单状态不允许支付");
        // 乐观锁更新状态
        if (orderMapper.updateStatus(orderNo, "PENDING_PAYMENT", "PAID") == 0) return;
        // 通知产品服务确认库存扣减
        productFacade.confirmInventory(orderNo);
        // 记录状态变更日志
        orderMapper.insertStatusLog(order.getId(), "PENDING_PAYMENT", "PAID", "PAYMENT", null, "支付单：" + paymentNo);
    }

    /**
     * 关闭未支付订单 — 由 OrderTimeoutListener 消费超时事件后调用
     * 将订单状态从 PENDING_PAYMENT 流转为 CANCELED，释放已锁定的库存。
     * @param orderNo 订单号
     */
    @Transactional
    public void closeUnpaidOrder(String orderNo) {
        MallOrder order = requireOrder(orderNo);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) return; // 订单可能已支付
        // 乐观锁更新状态
        if (orderMapper.updateStatus(orderNo, "PENDING_PAYMENT", "CANCELED") == 0) return;
        // 释放库存（取消锁定）
        productFacade.unlockInventory(orderNo);
        // 记录状态变更日志
        orderMapper.insertStatusLog(order.getId(), "PENDING_PAYMENT", "CANCELED", "SYSTEM", null, "支付超时自动关闭");
    }

    /** 管理员发货（仅支持 PAID 状态的订单） */
    @Transactional
    public MallOrder ship(String orderNo, String carrier, String trackingNo, Long adminId) {
        MallOrder order = requireOrder(orderNo);
        if (orderMapper.ship(orderNo, carrier, trackingNo) == 0) throw new BusinessException("ORDER_STATUS_INVALID", "仅已支付订单可发货");
        orderMapper.insertStatusLog(order.getId(), "PAID", "SHIPPED", "ADMIN", adminId, "模拟发货：" + carrier + "/" + trackingNo);
        return requireOrder(orderNo);
    }

    /** 用户确认收货（仅支持 SHIPPED 状态的订单） */
    @Transactional
    public MallOrder confirmReceipt(Long userId, String orderNo) {
        MallOrder order = get(userId, orderNo);
        if (orderMapper.updateStatus(orderNo, "SHIPPED", "COMPLETED") == 0) throw new BusinessException("ORDER_STATUS_INVALID", "仅已发货订单可确认收货");
        orderMapper.insertStatusLog(order.getId(), "SHIPPED", "COMPLETED", "USER", userId, "确认收货");
        return requireOrder(orderNo);
    }

    /** 内部工具方法：根据订单号查询订单，不存在时抛业务异常 */
    public MallOrder requireOrder(String orderNo) {
        MallOrder order = orderMapper.findByOrderNo(orderNo);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在");
        return order;
    }

    /**
     * 事务提交后发布超时事件（Outbox 模式）
     * 若当前无活跃事务，立即发布；否则注册一个事务同步回调，在 afterCommit() 中执行。
     * @param eventId order_outbox 记录 ID
     */
    private void publishTimeoutAfterCommit(Long eventId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            outboxService.publish(eventId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                outboxService.publish(eventId);
            }
        });
    }

    /** 生成下一订单号：DD + yyyyMMddHHmmssSSS + 3位随机数（0~999） */
    private String nextOrderNo() {
        return "DD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + String.format("%03d", new Random().nextInt(1000));
    }
}