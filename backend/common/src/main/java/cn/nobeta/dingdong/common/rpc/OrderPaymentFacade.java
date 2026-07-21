package cn.nobeta.dingdong.common.rpc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付 Dubbo 契约接口（订单服务提供，支付服务消费）
 * 支付服务（pay-service）通过此接口查询订单可支付信息，避免直接访问订单数据库。
 */
public interface OrderPaymentFacade {

    /**
     * 获取可支付订单信息
        * 支付服务在创建支付单前调用，校验订单状态是否允许支付。
     * @param orderNo 订单号
     * @param userId 用户 ID（用于权限校验）
     * @return 可支付订单信息
     */
    PayableOrder getPayableOrder(String orderNo, Long userId);

    /**
     * 标记订单已支付
        * 支付成功后支付服务调用此方法，通知订单服务更新状态。
     * @param orderNo 订单号
     * @param paymentNo 支付单号（用于关联支付记录）
     * @param paidAt 支付成功时间
     */
    void markPaid(String orderNo, String paymentNo, LocalDateTime paidAt);

    /** 可支付订单信息（仅包含支付所需字段） */
    record PayableOrder(String orderNo, Long userId, BigDecimal amount, String status) implements Serializable { }
}
