package cn.nobeta.dingdong.user.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class AdminUserRequests {
    private AdminUserRequests() { }
    public record ChangeStatusRequest(@NotNull @Min(0) @Max(1) Integer status) { }
}
