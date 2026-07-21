package cn.nobeta.dingdong.order.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.order.api.OrderResponses.DashboardOverview;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final OrderService orderService;

    public AdminDashboardController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverview> overview() {
        if (!"ADMIN".equals(OrderUserContext.require().role())) {
            throw new BusinessException("AUTH_FORBIDDEN", "需要管理员权限");
        }
        return ApiResponse.success(orderService.dashboardOverview());
    }
}
