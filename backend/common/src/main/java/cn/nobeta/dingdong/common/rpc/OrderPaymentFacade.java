package cn.nobeta.dingdong.common.rpc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Payment-facing order contract; pay-service never accesses the order database directly. */
public interface OrderPaymentFacade {
    PayableOrder getPayableOrder(String orderNo, Long userId);
    void markPaid(String orderNo, String paymentNo, LocalDateTime paidAt);

    record PayableOrder(String orderNo, Long userId, BigDecimal amount, String status) implements Serializable { }
}
