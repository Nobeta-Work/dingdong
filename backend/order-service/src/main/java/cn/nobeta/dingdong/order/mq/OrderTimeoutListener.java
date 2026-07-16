package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
import cn.nobeta.dingdong.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时事件消费者
 * <p>监听 RocketMQ 订单超时 Topic，收到事件后调用 {@link OrderService#closeUnpaidOrder}
 * 执行未支付订单的自动关闭逻辑。</p>
 */
@Component
@RocketMQMessageListener(
        topic="${app.rocketmq.order-timeout-topic:order-timeout}",
        consumerGroup="${app.rocketmq.order-timeout-consumer-group:order-timeout-consumer}")
public class OrderTimeoutListener implements RocketMQListener<OrderTimeoutEvent> {
    private final OrderService service;

    public OrderTimeoutListener(OrderService service) {
        this.service = service;
    }

    /**
     * 消费超时事件，关闭未支付订单
     * @param event 订单超时事件
     */
    public void onMessage(OrderTimeoutEvent event) {
        service.closeUnpaidOrder(event.orderNo());
    }
}
