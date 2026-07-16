package cn.nobeta.dingdong.order.mq;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
public interface OrderTimeoutPublisher { void publish(OrderTimeoutEvent event); }
