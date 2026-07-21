package cn.nobeta.dingdong.order.rpc;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.OrderPaymentFacade;
import cn.nobeta.dingdong.order.domain.MallOrder;
import cn.nobeta.dingdong.order.service.OrderService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 订单支付 Dubbo 服务实现。
 * 向支付服务（pay-service）提供订单查询和状态更新能力，使支付服务无需直接访问订单数据库。
 */
@Service
@DubboService
public class OrderPaymentFacadeImpl implements OrderPaymentFacade {
    private final OrderService orderService;

    public OrderPaymentFacadeImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取可支付订单信息。
        * 支付服务创建支付单前调用，先校验订单是否存在，再确认订单状态是否仍为待支付。
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @return 可支付订单信息
     * @throws BusinessException 订单不存在或状态不允许支付时抛出
     */
    @Override
    public PayableOrder getPayableOrder(String orderNo, Long userId) {
        MallOrder order = orderService.get(userId, orderNo);
        // 仅允许待支付订单进入支付链路，其他状态直接拒绝。
        if (!"PENDING_PAYMENT".equals(order.getStatus()))
            throw new BusinessException("ORDER_STATUS_INVALID", "当前订单不可支付");
        // 返回支付服务所需的最小订单信息，避免暴露不必要的订单字段。
        return new PayableOrder(order.getOrderNo(), order.getUserId(), order.getTotalAmount(), order.getStatus());
    }

    /**
     * 标记订单已支付。
        * 支付成功后由支付服务调用，通知订单服务推进订单状态流转。
     *
     * @param orderNo 订单号
     * @param paymentNo 支付单号
     * @param paidAt 支付成功时间
     */
    @Override
    public void markPaid(String orderNo, String paymentNo, LocalDateTime paidAt) {
        // 具体的状态校验、幂等处理和库存确认由订单服务统一完成。
        orderService.markPaid(orderNo, paymentNo, paidAt);
    }
}
