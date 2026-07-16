package cn.nobeta.dingdong.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付成功事件
 * <p>支付服务完成支付后发布到 RocketMQ，订单服务消费此事件后将订单状态从
 * PENDING_PAYMENT 流转为 PAID。</p>
 * @param paymentNo 支付单号
 * @param orderNo 关联订单号
 * @param userId 支付用户 ID
 * @param amount 支付金额
 * @param paidAt 支付成功时间
 */
public record PaymentSuccessEvent(String paymentNo, String orderNo, Long userId,
                                  BigDecimal amount, LocalDateTime paidAt) implements Serializable { }
