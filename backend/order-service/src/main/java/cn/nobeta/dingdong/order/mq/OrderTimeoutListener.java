package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
import cn.nobeta.dingdong.order.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
@Component @RocketMQMessageListener(topic="${app.rocketmq.order-timeout-topic:order-timeout}",consumerGroup="${app.rocketmq.order-timeout-consumer-group:order-timeout-consumer}")
public class OrderTimeoutListener implements RocketMQListener<OrderTimeoutEvent>{private final OrderService service;public OrderTimeoutListener(OrderService service){this.service=service;}public void onMessage(OrderTimeoutEvent event){service.closeUnpaidOrder(event.orderNo());}}
