package cn.nobeta.dingdong.order.web;
import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.order.api.OrderRequests.ShipmentRequest;
import cn.nobeta.dingdong.order.api.OrderResponses.OrderResponse;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/orders")
public class AdminOrderController {
 private final OrderService orderService;public AdminOrderController(OrderService orderService){this.orderService=orderService;}
 @PostMapping("/{orderNo}/shipment") public ApiResponse<OrderResponse> ship(@PathVariable String orderNo,@Valid @RequestBody ShipmentRequest request){var user=OrderUserContext.require();if(!"ADMIN".equals(user.role()))throw new BusinessException("AUTH_FORBIDDEN","需要管理员权限");var order=orderService.ship(orderNo,request.carrier(),request.trackingNo(),user.id());return ApiResponse.success(OrderResponse.from(order,orderService.items(order.getId())));}
}
