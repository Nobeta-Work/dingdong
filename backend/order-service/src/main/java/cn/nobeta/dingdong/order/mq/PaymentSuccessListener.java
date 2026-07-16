package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import cn.nobeta.dingdong.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 支付成功事件消费者
 * <p>监听 RocketMQ 支付成功 Topic，收到事件后调用 {@link OrderService#markPaid}
 * 将订单状态从 PENDING_PAYMENT 流转为 PAID，并触发库存确认扣减。</p>
 */
@Component
@RocketMQMessageListener(
        topic="${app.rocketmq.payment-success-topic:payment-success}",
        consumerGroup="${app.rocketmq.payment-consumer-group:order-payment-consumer}")
public class PaymentSuccessListener implements RocketMQListener<PaymentSuccessEvent> {
    private final OrderService orderService;

    public PaymentSuccessListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 消费支付成功事件，标记订单已支付
     * @param event 支付成功事件
     */
    @Override
    public void onMessage(PaymentSuccessEvent event) {
        orderService.markPaid(event.orderNo(), event.paymentNo(), event.paidAt());
    }
}
