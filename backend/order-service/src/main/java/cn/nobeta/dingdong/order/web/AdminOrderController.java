package cn.nobeta.dingdong.order.web;
import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.order.api.OrderRequests.ShipmentRequest;
import cn.nobeta.dingdong.order.api.OrderResponses.OrderResponse;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理员控制器
 * <p>提供管理员专用的订单发货接口，需 ADMIN 角色权限。</p>
 */
@RestController @RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 订单发货
     * <p>仅管理员可调用，将已支付订单状态从 PAID 流转为 SHIPPED。</p>
     * @param orderNo 订单号
     * @param request 发货请求（快递公司、单号）
     * @return 更新后的订单详情
     */
    @PostMapping("/{orderNo}/shipment")
    public ApiResponse<OrderResponse> ship(@PathVariable String orderNo,
                                           @Valid @RequestBody ShipmentRequest request) {
        var user = OrderUserContext.require();
        if(!"ADMIN".equals(user.role()))
            throw new BusinessException("AUTH_FORBIDDEN", "需要管理员权限");
        var order = orderService.ship(orderNo, request.carrier(), request.trackingNo(), user.id());
        return ApiResponse.success(OrderResponse.from(order, orderService.items(order.getId())));
    }
}
