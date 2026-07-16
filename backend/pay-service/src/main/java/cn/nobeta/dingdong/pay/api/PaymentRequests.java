package cn.nobeta.dingdong.pay.api;
import jakarta.validation.constraints.*;
public final class PaymentRequests {private PaymentRequests(){} public record CreatePaymentRequest(@NotBlank String orderNo){ } public record SimulatePaymentRequest(@NotNull Boolean success){ }}
