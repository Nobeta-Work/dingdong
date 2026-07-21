package cn.nobeta.dingdong.order.api;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade.SkuSnapshot;
import cn.nobeta.dingdong.order.domain.*;
import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.List;

/**
 * 订单模块的响应 DTO 集合
 * <p>提供 from 静态工厂方法，将领域对象转换为前端友好的扁平结构。</p>
 */
public final class OrderResponses { private OrderResponses(){}

    /** 购物车商品响应 —— 包含实时的商品快照信息 */
    public record CartItemResponse(Long id,Long skuId,Integer quantity,Boolean selected,String productTitle,String mainImageUrl,String specJson,BigDecimal unitPrice,Integer availableStock){public static CartItemResponse from(CartItem c,SkuSnapshot s){return new CartItemResponse(c.getId(),c.getSkuId(),c.getQuantity(),c.getSelected(),s.title(),s.mainImageUrl(),s.specJson(),s.price(),s.availableStock());}}

    /** 订单项响应 —— 反映下单时刻的商品快照 */
    public record OrderItemResponse(Long skuId,String skuCode,String productTitle,String mainImageUrl,String specJson,BigDecimal unitPrice,Integer quantity,BigDecimal totalAmount){public static OrderItemResponse from(OrderItem i){return new OrderItemResponse(i.getSkuId(),i.getSkuCode(),i.getProductTitle(),i.getProductImageUrl(),i.getSpecJson(),i.getUnitPrice(),i.getQuantity(),i.getTotalAmount());}}

    /** 订单主表响应 */
    public record OrderResponse(String orderNo,String status,BigDecimal totalAmount,String receiverName,String receiverPhone,String receiverAddress,String carrier,String trackingNo,LocalDateTime shippedAt,LocalDateTime createdAt,List<OrderItemResponse> items){public static OrderResponse from(MallOrder o,List<OrderItem> items){return new OrderResponse(o.getOrderNo(),o.getStatus(),o.getTotalAmount(),o.getReceiverName(),o.getReceiverPhone(),o.getReceiverAddress(),o.getCarrier(),o.getTrackingNo(),o.getShippedAt(),o.getCreatedAt(),items.stream().map(OrderItemResponse::from).toList());}}

    /** 管理员订单响应，额外包含下单用户 ID。 */
    public record AdminOrderResponse(String orderNo,Long userId,String status,BigDecimal totalAmount,String receiverName,String receiverPhone,String receiverAddress,String carrier,String trackingNo,LocalDateTime shippedAt,LocalDateTime createdAt,List<OrderItemResponse> items){public static AdminOrderResponse from(MallOrder o,List<OrderItem> items){return new AdminOrderResponse(o.getOrderNo(),o.getUserId(),o.getStatus(),o.getTotalAmount(),o.getReceiverName(),o.getReceiverPhone(),o.getReceiverAddress(),o.getCarrier(),o.getTrackingNo(),o.getShippedAt(),o.getCreatedAt(),items.stream().map(OrderItemResponse::from).toList());}}

    public record TopProductResponse(Long skuId,String productTitle,String productImageUrl,long quantity,BigDecimal salesAmount){public static TopProductResponse from(TopProductStat v){return new TopProductResponse(v.getSkuId(),v.getProductTitle(),v.getProductImageUrl(),v.getQuantity(),v.getSalesAmount());}}

    public record DashboardOverview(long todayOrderCount,BigDecimal todayPaidAmount,long pendingShipmentCount,List<TopProductResponse> topProducts){}

    /** 分页响应包装 */
    public record PageResponse<T>(List<T> items,long total,int page,int pageSize,long pages){}
}
