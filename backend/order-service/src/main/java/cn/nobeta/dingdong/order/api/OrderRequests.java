package cn.nobeta.dingdong.order.api;
import jakarta.validation.constraints.*;
import java.util.List;
public final class OrderRequests { private OrderRequests(){} public record AddCartItemRequest(@NotNull Long skuId,@NotNull @Min(1) @Max(99) Integer quantity){ } public record UpdateCartItemRequest(@NotNull @Min(1) @Max(99) Integer quantity,@NotNull Boolean selected){ } public record CreateOrderRequest(@NotNull Long addressId,List<Long> cartItemIds){ } }
