package cn.nobeta.dingdong.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Event published after a payment record becomes successful. */
public record PaymentSuccessEvent(String paymentNo, String orderNo, Long userId,
                                  BigDecimal amount, LocalDateTime paidAt) implements Serializable { }
