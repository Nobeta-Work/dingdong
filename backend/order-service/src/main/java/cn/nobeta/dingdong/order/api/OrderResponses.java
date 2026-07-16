package cn.nobeta.dingdong.order.api;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade.SkuSnapshot;
import cn.nobeta.dingdong.order.domain.*;
import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.List;
public final class OrderResponses { private OrderResponses(){}
 public record CartItemResponse(Long id,Long skuId,Integer quantity,Boolean selected,String title,String mainImageUrl,String specJson,BigDecimal price,Integer availableStock){public static CartItemResponse from(CartItem c,SkuSnapshot s){return new CartItemResponse(c.getId(),c.getSkuId(),c.getQuantity(),c.getSelected(),s.title(),s.mainImageUrl(),s.specJson(),s.price(),s.availableStock());}}
 public record OrderItemResponse(Long skuId,String skuCode,String productTitle,String productImageUrl,String specJson,BigDecimal unitPrice,Integer quantity,BigDecimal totalAmount){public static OrderItemResponse from(OrderItem i){return new OrderItemResponse(i.getSkuId(),i.getSkuCode(),i.getProductTitle(),i.getProductImageUrl(),i.getSpecJson(),i.getUnitPrice(),i.getQuantity(),i.getTotalAmount());}}
 public record OrderResponse(String orderNo,String status,BigDecimal totalAmount,String receiverName,String receiverPhone,String receiverAddress,LocalDateTime createdAt,List<OrderItemResponse> items){public static OrderResponse from(MallOrder o,List<OrderItem> items){return new OrderResponse(o.getOrderNo(),o.getStatus(),o.getTotalAmount(),o.getReceiverName(),o.getReceiverPhone(),o.getReceiverAddress(),o.getCreatedAt(),items.stream().map(OrderItemResponse::from).toList());}}
 public record PageResponse<T>(List<T> records,long total,int page,int size){}
}
