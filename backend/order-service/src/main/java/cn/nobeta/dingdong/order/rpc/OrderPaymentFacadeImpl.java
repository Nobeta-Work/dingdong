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
 * 订单支付 Dubbo 服务实现
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
     * 获取可支付订单信息
        * 支付服务创建支付单前调用，校验订单状态是否为 PENDING_PAYMENT。
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @return 可支付订单信息
     * @throws BusinessException 订单不存在或状态不允许支付时抛出
     */
    @Override
    public PayableOrder getPayableOrder(String orderNo, Long userId) {
        MallOrder order = orderService.get(userId, orderNo);
        if (!"PENDING_PAYMENT".equals(order.getStatus()))
            throw new BusinessException("ORDER_STATUS_INVALID", "当前订单不可支付");
        return new PayableOrder(order.getOrderNo(), order.getUserId(), order.getTotalAmount(), order.getStatus());
    }

    /**
     * 标记订单已支付
        * 支付成功后支付服务调用此方法通知订单服务更新状态。
     * @param orderNo 订单号
     * @param paymentNo 支付单号
     * @param paidAt 支付成功时间
     */
    @Override
    public void markPaid(String orderNo, String paymentNo, LocalDateTime paidAt) {
        orderService.markPaid(orderNo, paymentNo, paidAt);
    }
}
