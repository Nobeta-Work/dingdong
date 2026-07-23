package cn.nobeta.dingdong.pay.channel;

import cn.nobeta.dingdong.pay.domain.PaymentOrder;

public interface PaymentChannel {
    String code();

    ChannelResult pay(PaymentOrder payment, boolean success);

    record ChannelResult(boolean success, String transactionNo, String message) {
    }
}
