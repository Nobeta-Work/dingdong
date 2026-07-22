package cn.nobeta.dingdong.product.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class SeckillRequests {
    private SeckillRequests() { }
    public record CreateActivityRequest(@NotBlank @Size(max=128) String name, @NotNull Long skuId,
            @NotNull @DecimalMin("0.01") BigDecimal seckillPrice, @NotNull @Min(1) Integer totalStock,
            @NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime) { }
    public record PurchaseRequest(@NotBlank @Size(max=64) String requestId) { }
}
