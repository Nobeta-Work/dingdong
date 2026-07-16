package cn.nobeta.dingdong.pay.service;

import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.OrderPaymentFacade;
import cn.nobeta.dingdong.pay.channel.PaymentChannel;
import cn.nobeta.dingdong.pay.domain.PaymentOrder;
import cn.nobeta.dingdong.pay.mapper.PaymentMapper;
import cn.nobeta.dingdong.pay.mq.PaymentEventPublisher;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class PaymentService {
    private final PaymentMapper mapper;
    private final PaymentChannel channel;
    private final PaymentEventPublisher publisher;
    @DubboReference(check = false) private OrderPaymentFacade orderFacade;
    public PaymentService(PaymentMapper mapper, PaymentChannel channel, PaymentEventPublisher publisher) { this.mapper = mapper; this.channel = channel; this.publisher = publisher; }

    @Transactional
    public PaymentOrder create(Long userId, String orderNo) {
        PaymentOrder existing = mapper.findByOrder(orderNo, userId);
        if (existing != null) return existing;
        var order = orderFacade.getPayableOrder(orderNo, userId);
        PaymentOrder payment = new PaymentOrder();
        payment.setPaymentNo(nextNo()); payment.setOrderNo(order.orderNo()); payment.setUserId(userId); payment.setAmount(order.amount()); payment.setChannel(channel.code()); payment.setStatus("PENDING");
        mapper.insert(payment);
        return mapper.findOwned(payment.getPaymentNo(), userId);
    }

    @Transactional
    public PaymentOrder simulate(Long userId, String paymentNo, boolean success) {
        PaymentOrder payment = require(userId, paymentNo);
        if ("SUCCESS".equals(payment.getStatus())) return payment;
        if (!"PENDING".equals(payment.getStatus())) throw new BusinessException("PAYMENT_STATUS_INVALID", "支付单不可重复处理");
        var result = channel.pay(payment, success);
        if (!result.success()) { mapper.markFailed(paymentNo); return require(userId, paymentNo); }
        LocalDateTime paidAt = LocalDateTime.now();
        if (mapper.markSuccess(paymentNo, result.transactionNo(), paidAt) == 0) return require(userId, paymentNo);
        PaymentOrder paid = require(userId, paymentNo);
        publishAfterCommit(new PaymentSuccessEvent(paid.getPaymentNo(), paid.getOrderNo(), paid.getUserId(), paid.getAmount(), paidAt));
        return paid;
    }

    public PaymentOrder require(Long userId, String paymentNo) {
        PaymentOrder payment = mapper.findOwned(paymentNo, userId);
        if (payment == null) throw new BusinessException("PAYMENT_NOT_FOUND", "支付单不存在");
        return payment;
    }
    private void publishAfterCommit(PaymentSuccessEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { publisher.publishSuccess(event); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { @Override public void afterCommit() { publisher.publishSuccess(event); } });
    }
    private String nextNo() { return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + String.format("%03d", new Random().nextInt(1000)); }
}
