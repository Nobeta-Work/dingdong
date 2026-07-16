package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import cn.nobeta.dingdong.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
@Component
@RocketMQMessageListener(topic="${app.rocketmq.payment-success-topic:payment-success}",consumerGroup="${app.rocketmq.payment-consumer-group:order-payment-consumer}")
public class PaymentSuccessListener implements RocketMQListener<PaymentSuccessEvent> {
 private final OrderService orderService;public PaymentSuccessListener(OrderService orderService){this.orderService=orderService;}
 @Override public void onMessage(PaymentSuccessEvent event){orderService.markPaid(event.orderNo(),event.paymentNo(),event.paidAt());}
}
