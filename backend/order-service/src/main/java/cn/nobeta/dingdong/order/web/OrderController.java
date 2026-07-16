package cn.nobeta.dingdong.order.web;
import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.order.api.OrderRequests.CreateOrderRequest;
import cn.nobeta.dingdong.order.api.OrderResponses.*;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders")
public class OrderController {
 private final OrderService orderService;public OrderController(OrderService orderService){this.orderService=orderService;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest r){var o=orderService.create(OrderUserContext.require().id(),r);return ApiResponse.success(OrderResponse.from(o,orderService.items(o.getId())));}
 @GetMapping public ApiResponse<PageResponse<OrderResponse>> page(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));Long userId=OrderUserContext.require().id();return ApiResponse.success(new PageResponse<>(orderService.page(userId,p,s).stream().map(o->OrderResponse.from(o,orderService.items(o.getId()))).toList(),orderService.count(userId),p,s));}
 @GetMapping("/{orderNo}") public ApiResponse<OrderResponse> detail(@PathVariable String orderNo){var o=orderService.get(OrderUserContext.require().id(),orderNo);return ApiResponse.success(OrderResponse.from(o,orderService.items(o.getId())));}
}
