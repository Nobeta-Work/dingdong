package cn.nobeta.dingdong.product.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class InventoryRequests {
    private InventoryRequests() { }
    public record AdjustmentRequest(@NotBlank @Size(max=64) String requestId, @NotNull Integer quantityDelta,
                                    @NotBlank @Size(max=255) String reason) { }
}
