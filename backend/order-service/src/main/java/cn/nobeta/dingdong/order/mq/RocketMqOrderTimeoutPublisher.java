package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
@Component public class RocketMqOrderTimeoutPublisher implements OrderTimeoutPublisher {private final RocketMQTemplate template;private final String topic;public RocketMqOrderTimeoutPublisher(RocketMQTemplate template,@Value("${app.rocketmq.order-timeout-topic:order-timeout}")String topic){this.template=template;this.topic=topic;}public void publish(OrderTimeoutEvent event){template.syncSend(topic, MessageBuilder.withPayload(event).build(),3000,16);}}
