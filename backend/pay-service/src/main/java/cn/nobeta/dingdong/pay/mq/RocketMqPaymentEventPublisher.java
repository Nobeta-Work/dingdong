package cn.nobeta.dingdong.pay.mq;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component public class RocketMqPaymentEventPublisher implements PaymentEventPublisher { private final RocketMQTemplate template;private final String topic;public RocketMqPaymentEventPublisher(RocketMQTemplate template,@Value("${app.rocketmq.payment-success-topic:payment-success}") String topic){this.template=template;this.topic=topic;}public void publishSuccess(PaymentSuccessEvent event){template.convertAndSend(topic,event);} }
