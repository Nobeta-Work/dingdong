package cn.nobeta.dingdong.pay.mq;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
public interface PaymentEventPublisher { void publishSuccess(PaymentSuccessEvent event); }
