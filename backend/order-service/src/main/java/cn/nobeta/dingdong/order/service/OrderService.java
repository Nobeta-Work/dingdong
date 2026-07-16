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

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final OrderOutboxService outboxService;
    @DubboReference(check = false) private ProductInventoryFacade productFacade;
    @DubboReference(check = false) private UserAddressFacade addressFacade;
    public OrderService(OrderMapper orderMapper, CartService cartService, OrderOutboxService outboxService) { this.orderMapper = orderMapper; this.cartService = cartService; this.outboxService = outboxService; }

    @Transactional
    public MallOrder create(Long userId, CreateOrderRequest request) {
        List<CartItem> carts = cartService.itemsForOrder(userId, request.cartItemIds());
        var address = addressFacade.getAddressSnapshot(userId, request.addressId());
        String orderNo = nextOrderNo();
        boolean locked = false;
        try {
            List<ProductInventoryFacade.LockItem> locks = carts.stream().map(i -> new ProductInventoryFacade.LockItem(i.getSkuId(), i.getQuantity())).toList();
            List<ProductInventoryFacade.SkuSnapshot> snapshots = productFacade.lockInventory(orderNo, locks);
            locked = true;
            Map<Long, ProductInventoryFacade.SkuSnapshot> snapshotMap = new HashMap<>(); snapshots.forEach(s -> snapshotMap.put(s.skuId(), s));
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem cart : carts) { var snapshot = snapshotMap.get(cart.getSkuId()); if (snapshot == null) throw new BusinessException("PRODUCT_SKU_NOT_FOUND", "商品不可售"); total = total.add(snapshot.price().multiply(BigDecimal.valueOf(cart.getQuantity()))); }
            MallOrder order = new MallOrder();
            order.setOrderNo(orderNo); order.setUserId(userId); order.setReceiverName(address.receiverName()); order.setReceiverPhone(address.receiverPhone()); order.setReceiverAddress(String.join(" ", address.province(), address.city(), address.district(), address.detailAddress())); order.setTotalAmount(total); order.setStatus("PENDING_PAYMENT");
            orderMapper.insertOrder(order);
            for (CartItem cart : carts) {
                var snapshot = snapshotMap.get(cart.getSkuId()); OrderItem item = new OrderItem();
                item.setOrderId(order.getId()); item.setSkuId(snapshot.skuId()); item.setSkuCode(snapshot.skuCode()); item.setProductTitle(snapshot.title()); item.setProductImageUrl(snapshot.mainImageUrl()); item.setSpecJson(snapshot.specJson()); item.setUnitPrice(snapshot.price()); item.setQuantity(cart.getQuantity()); item.setTotalAmount(snapshot.price().multiply(BigDecimal.valueOf(cart.getQuantity()))); orderMapper.insertItem(item);
            }
            cartService.removeAfterOrder(userId, carts.stream().map(CartItem::getId).toList());
            OrderOutbox timeout = outboxService.createTimeout(orderNo);
            publishTimeoutAfterCommit(timeout.getId());
            return orderMapper.findOwned(orderNo, userId);
        } catch (RuntimeException exception) { if (locked) productFacade.unlockInventory(orderNo); throw exception; }
    }

    public MallOrder get(Long userId, String orderNo) { MallOrder order = orderMapper.findOwned(orderNo, userId); if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在"); return order; }
    public List<OrderItem> items(Long orderId) { return orderMapper.findItems(orderId); }
    public List<MallOrder> page(Long userId, int page, int size) { return orderMapper.findPage(userId, size, (page - 1) * size); }
    public long count(Long userId) { return orderMapper.countByUserId(userId); }

    @Transactional
    public void markPaid(String orderNo, String paymentNo, LocalDateTime paidAt) {
        MallOrder order = requireOrder(orderNo);
        if ("PAID".equals(order.getStatus())) return;
        if (!"PENDING_PAYMENT".equals(order.getStatus())) throw new BusinessException("ORDER_STATUS_INVALID", "当前订单状态不允许支付");
        if (orderMapper.updateStatus(orderNo, "PENDING_PAYMENT", "PAID") == 0) return;
        productFacade.confirmInventory(orderNo);
        orderMapper.insertStatusLog(order.getId(), "PENDING_PAYMENT", "PAID", "PAYMENT", null, "支付单：" + paymentNo);
    }

    @Transactional
    public void closeUnpaidOrder(String orderNo) {
        MallOrder order = requireOrder(orderNo);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) return;
        if (orderMapper.updateStatus(orderNo, "PENDING_PAYMENT", "CANCELED") == 0) return;
        productFacade.unlockInventory(orderNo);
        orderMapper.insertStatusLog(order.getId(), "PENDING_PAYMENT", "CANCELED", "SYSTEM", null, "支付超时自动关闭");
    }

    @Transactional
    public MallOrder ship(String orderNo, String carrier, String trackingNo, Long adminId) { MallOrder order = requireOrder(orderNo); if (orderMapper.ship(orderNo, carrier, trackingNo) == 0) throw new BusinessException("ORDER_STATUS_INVALID", "仅已支付订单可发货"); orderMapper.insertStatusLog(order.getId(), "PAID", "SHIPPED", "ADMIN", adminId, "模拟发货：" + carrier + "/" + trackingNo); return requireOrder(orderNo); }
    @Transactional
    public MallOrder confirmReceipt(Long userId, String orderNo) { MallOrder order = get(userId, orderNo); if (orderMapper.updateStatus(orderNo, "SHIPPED", "COMPLETED") == 0) throw new BusinessException("ORDER_STATUS_INVALID", "仅已发货订单可确认收货"); orderMapper.insertStatusLog(order.getId(), "SHIPPED", "COMPLETED", "USER", userId, "确认收货"); return requireOrder(orderNo); }
    public MallOrder requireOrder(String orderNo) { MallOrder order = orderMapper.findByOrderNo(orderNo); if (order == null) throw new BusinessException("ORDER_NOT_FOUND", "订单不存在"); return order; }
    private void publishTimeoutAfterCommit(Long eventId) { if (!TransactionSynchronizationManager.isSynchronizationActive()) { outboxService.publish(eventId); return; } TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { @Override public void afterCommit() { outboxService.publish(eventId); } }); }
    private String nextOrderNo() { return "DD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + String.format("%03d", new Random().nextInt(1000)); }
}
