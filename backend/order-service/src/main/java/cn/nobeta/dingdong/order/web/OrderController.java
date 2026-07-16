package cn.nobeta.dingdong.order.web;
import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.order.api.OrderRequests.CreateOrderRequest;
import cn.nobeta.dingdong.order.api.OrderResponses.*;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 订单相关的用户端 REST 控制器
 * 提供创建订单、订单列表分页、订单详情、确认收货等接口
 */
@RestController @RequestMapping("/api/orders")
public class OrderController {
 private final OrderService orderService;public OrderController(OrderService orderService){this.orderService=orderService;}

    /**
     * 创建订单 — 用户下单入口
     * 从安全上下文中获取当前用户 ID，委托 {@link OrderService#create} 执行核心业务逻辑，
     * 并将返回的领域对象组装为 {@link OrderResponse} 响应体
     */
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest r){var o=orderService.create(OrderUserContext.require().id(),r);return ApiResponse.success(OrderResponse.from(o,orderService.items(o.getId())));}

    /**
     * 分页查询当前用户的订单列表，每页记录数上限 100
     */
 @GetMapping public ApiResponse<PageResponse<OrderResponse>> page(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));Long userId=OrderUserContext.require().id();return ApiResponse.success(new PageResponse<>(orderService.page(userId,p,s).stream().map(o->OrderResponse.from(o,orderService.items(o.getId()))).toList(),orderService.count(userId),p,s));}

    /**
     * 查询当前用户指定订单号的订单详情
     */
 @GetMapping("/{orderNo}") public ApiResponse<OrderResponse> detail(@PathVariable String orderNo){var o=orderService.get(OrderUserContext.require().id(),orderNo);return ApiResponse.success(OrderResponse.from(o,orderService.items(o.getId())));}

    /**
     * 确认收货 — 将指定订单从 SHIPPED 流转为 COMPLETED
     */
 @PostMapping("/{orderNo}/confirm-receipt") public ApiResponse<OrderResponse> confirmReceipt(@PathVariable String orderNo){var o=orderService.confirmReceipt(OrderUserContext.require().id(),orderNo);return ApiResponse.success(OrderResponse.from(o,orderService.items(o.getId())));}
}
