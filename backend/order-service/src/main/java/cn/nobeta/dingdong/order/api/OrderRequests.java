package cn.nobeta.dingdong.order.api;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 订单模块的请求 DTO 集合
 * <p>内部以静态 record 定义，避免顶层文件膨胀；所有构造函数参数均可通过
 * {@link jakarta.validation.Valid} 校验。</p>
 */
public final class OrderRequests { private OrderRequests(){}

    /** 添加购物车项请求 */
    public record AddCartItemRequest(@NotNull Long skuId,@NotNull @Min(1) @Max(99) Integer quantity){ }

    /** 更新购物车项请求 */
    public record UpdateCartItemRequest(@NotNull @Min(1) @Max(99) Integer quantity,@NotNull Boolean selected){ }

    /**
     * 创建订单请求
     * @param addressId  收件地址 ID（对应 user_address 表）
     * @param cartItemIds 待结算的购物车项 ID 列表；为 {@code null} 或空列表时结算当前用户所有已选中商品
     */
    public record CreateOrderRequest(@NotNull Long addressId,List<Long> cartItemIds){ }

    /** 管理员发货请求 */
    public record ShipmentRequest(@NotBlank @Size(max=64) String carrier,@NotBlank @Size(max=64) String trackingNo){ }
}
