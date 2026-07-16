package cn.nobeta.dingdong.pay.api;
import cn.nobeta.dingdong.pay.domain.PaymentOrder;
import java.math.BigDecimal;import java.time.LocalDateTime;
public record PaymentResponse(String paymentNo,String orderNo,BigDecimal amount,String channel,String status,String transactionNo,LocalDateTime paidAt){public static PaymentResponse from(PaymentOrder p){return new PaymentResponse(p.getPaymentNo(),p.getOrderNo(),p.getAmount(),p.getChannel(),p.getStatus(),p.getTransactionNo(),p.getPaidAt());}}
