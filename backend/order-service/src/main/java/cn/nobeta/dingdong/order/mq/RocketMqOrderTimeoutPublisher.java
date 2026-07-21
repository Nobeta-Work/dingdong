package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 订单超时事件发布器
 * 使用 RocketMQ 的延迟消息机制，将订单超时事件发送到指定 Topic。
 * 消费方（OrderTimeoutListener）在指定延迟后收到消息，执行订单关闭逻辑。
 */
@Component
public class RocketMqOrderTimeoutPublisher implements OrderTimeoutPublisher {
    private final RocketMQTemplate template;
    private final String topic;

    public RocketMqOrderTimeoutPublisher(RocketMQTemplate template,
                                         @Value("${app.rocketmq.order-timeout-topic:order-timeout}") String topic) {
        this.template = template;
        this.topic = topic;
    }

    /**
     * 同步发送超时事件（延迟级别 16，约 30 分钟）
     * @param event 订单超时事件
     */
    public void publish(OrderTimeoutEvent event) {
        template.syncSend(topic, MessageBuilder.withPayload(event).build(), 3000, 16);
    }
}
