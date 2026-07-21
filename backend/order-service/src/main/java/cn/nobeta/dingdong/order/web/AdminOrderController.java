package cn.nobeta.dingdong.order.web;
import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.order.api.OrderRequests.ShipmentRequest;
import cn.nobeta.dingdong.order.api.OrderResponses.AdminOrderResponse;
import cn.nobeta.dingdong.order.api.OrderResponses.PageResponse;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理员控制器
 * 提供管理员专用的订单发货接口，需 ADMIN 角色权限。
 */
@RestController @RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminOrderResponse>> page(@RequestParam(required=false) String orderNo,
                                                              @RequestParam(required=false) Long userId,
                                                              @RequestParam(required=false) String status,
                                                              @RequestParam(defaultValue="1") int page,
                                                              @RequestParam(defaultValue="20") int size) {
        requireAdmin();
        int normalizedPage=Math.max(1,page), normalizedSize=Math.min(100,Math.max(1,size));
        long total=orderService.countAdmin(orderNo,userId,status);
        var items=orderService.adminPage(orderNo,userId,status,normalizedPage,normalizedSize).stream()
                .map(order->AdminOrderResponse.from(order,orderService.items(order.getId())))
                .toList();
        return ApiResponse.success(new PageResponse<>(items,total,normalizedPage,normalizedSize,(total+normalizedSize-1)/normalizedSize));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<AdminOrderResponse> detail(@PathVariable String orderNo) {
        requireAdmin();
        var order=orderService.requireOrder(orderNo);
        return ApiResponse.success(AdminOrderResponse.from(order,orderService.items(order.getId())));
    }

    /**
     * 订单发货
        * 仅管理员可调用，将已支付订单状态从 PAID 流转为 SHIPPED。
     * @param orderNo 订单号
     * @param request 发货请求（快递公司、单号）
     * @return 更新后的订单详情
     */
    @PostMapping("/{orderNo}/shipment")
    public ApiResponse<AdminOrderResponse> ship(@PathVariable String orderNo,
                                           @Valid @RequestBody ShipmentRequest request) {
        var user = requireAdmin();
        var order = orderService.ship(orderNo, request.carrier(), request.trackingNo(), user.id());
        return ApiResponse.success(AdminOrderResponse.from(order, orderService.items(order.getId())));
    }

    private cn.nobeta.dingdong.order.security.CurrentOrderUser requireAdmin() {
        var user=OrderUserContext.require();
        if(!"ADMIN".equals(user.role())) throw new BusinessException("AUTH_FORBIDDEN", "需要管理员权限");
        return user;
    }
}
