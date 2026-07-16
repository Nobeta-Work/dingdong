package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;

/**
 * 订单超时事件发布器接口
 * <p>定义订单超时事件的发布契约，具体实现为 {@link RocketMqOrderTimeoutPublisher}。</p>
 */
public interface OrderTimeoutPublisher {
    /**
     * 发布订单超时事件到消息队列
     * @param event 超时事件
     */
    void publish(OrderTimeoutEvent event);
}
