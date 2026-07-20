package cn.nobeta.dingdong.order.web;
import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.*;
import cn.nobeta.dingdong.order.api.OrderResponses.CartItemResponse;
import cn.nobeta.dingdong.order.security.OrderUserContext;
import cn.nobeta.dingdong.order.service.CartService;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 购物车用户端 REST 控制器
 * 提供购物车列表查询、添加商品、更新数量、删除商品等接口。
 * 控制器从安全上下文获取用户 ID，并通过 Dubbo 获取商品快照信息。
 */
@RestController @RequestMapping("/api/cart/items")
public class CartController {
    private final CartService cartService;
    @DubboReference(check=false) private ProductInventoryFacade productFacade;

    public CartController(CartService cartService){this.cartService=cartService;}

    /**
     * 查询当前用户购物车列表
        * 查询购物车项后通过 Dubbo 批量获取商品快照，组装为带商品信息的响应。
     */
    @GetMapping
    public ApiResponse<List<CartItemResponse>> list(){
        var items = cartService.list(OrderUserContext.require().id());
        // 批量获取商品快照（避免 N+1 远程调用）
        var snapshots = productFacade.getSkuSnapshots(items.stream().map(i->i.getSkuId()).toList());
        // 建立索引方便查找
        Map<Long,ProductInventoryFacade.SkuSnapshot> map = new HashMap<>();
        snapshots.forEach(s->map.put(s.skuId(),s));
        // 过滤掉已下架商品并组装响应
        return ApiResponse.success(items.stream()
                .filter(i->map.containsKey(i.getSkuId()))
                .map(i->CartItemResponse.from(i,map.get(i.getSkuId())))
                .toList());
    }

    /** 添加商品到购物车 */
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartItemResponse> add(@Valid @RequestBody AddCartItemRequest r){
        var item = cartService.add(OrderUserContext.require().id(),r);
        // 获取最新商品快照
        var snapshot = productFacade.getSkuSnapshots(List.of(item.getSkuId())).getFirst();
        return ApiResponse.success(CartItemResponse.from(item,snapshot));
    }

    /** 更新购物车项数量与选中状态 */
    @PutMapping("/{id}")
    public ApiResponse<CartItemResponse> update(@PathVariable Long id,@Valid @RequestBody UpdateCartItemRequest r){
        var item = cartService.update(OrderUserContext.require().id(),id,r);
        var snapshot = productFacade.getSkuSnapshots(List.of(item.getSkuId())).getFirst();
        return ApiResponse.success(CartItemResponse.from(item,snapshot));
    }

    /** 删除购物车项 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id){
        cartService.delete(OrderUserContext.require().id(),id);
        return ApiResponse.success(null);
    }
}
