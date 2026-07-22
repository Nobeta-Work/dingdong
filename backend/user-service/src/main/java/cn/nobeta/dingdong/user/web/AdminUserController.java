package cn.nobeta.dingdong.user.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.user.api.AdminUserRequests;
import cn.nobeta.dingdong.user.api.AdminUserResponses;
import cn.nobeta.dingdong.user.security.CurrentUser;
import cn.nobeta.dingdong.user.security.CurrentUserContext;
import cn.nobeta.dingdong.user.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService service;
    public AdminUserController(AdminUserService service) { this.service = service; }

    @GetMapping
    public ApiResponse<AdminUserResponses.Page<AdminUserResponses.AdminUser>> page(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status, @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireAdmin();
        return ApiResponse.success(service.page(keyword, role, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponses.AdminUser> detail(@PathVariable Long id) {
        requireAdmin();
        return ApiResponse.success(AdminUserResponses.AdminUser.from(service.detail(id)));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<AdminUserResponses.AdminUser> changeStatus(@PathVariable Long id,
            @Valid @RequestBody AdminUserRequests.ChangeStatusRequest request) {
        CurrentUser admin = requireAdmin();
        return ApiResponse.success(AdminUserResponses.AdminUser.from(service.changeStatus(admin.id(), id, request.status())));
    }

    private CurrentUser requireAdmin() {
        CurrentUser current = CurrentUserContext.require();
        if (!"ADMIN".equals(current.role())) throw new BusinessException("AUTH_FORBIDDEN", "需要管理员权限");
        return current;
    }
}
