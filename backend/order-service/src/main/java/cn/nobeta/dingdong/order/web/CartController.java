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
@RestController @RequestMapping("/api/cart/items")
public class CartController {
 private final CartService cartService;@DubboReference(check=false) private ProductInventoryFacade productFacade;
 public CartController(CartService cartService){this.cartService=cartService;}
 @GetMapping public ApiResponse<List<CartItemResponse>> list(){var items=cartService.list(OrderUserContext.require().id());var snapshots=productFacade.getSkuSnapshots(items.stream().map(i->i.getSkuId()).toList());Map<Long,ProductInventoryFacade.SkuSnapshot> map=new HashMap<>();snapshots.forEach(s->map.put(s.skuId(),s));return ApiResponse.success(items.stream().filter(i->map.containsKey(i.getSkuId())).map(i->CartItemResponse.from(i,map.get(i.getSkuId()))).toList());}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ApiResponse<CartItemResponse> add(@Valid @RequestBody AddCartItemRequest r){var item=cartService.add(OrderUserContext.require().id(),r);var snapshot=productFacade.getSkuSnapshots(List.of(item.getSkuId())).getFirst();return ApiResponse.success(CartItemResponse.from(item,snapshot));}
 @PutMapping("/{id}") public ApiResponse<CartItemResponse> update(@PathVariable Long id,@Valid @RequestBody UpdateCartItemRequest r){var item=cartService.update(OrderUserContext.require().id(),id,r);var snapshot=productFacade.getSkuSnapshots(List.of(item.getSkuId())).getFirst();return ApiResponse.success(CartItemResponse.from(item,snapshot));}
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id){cartService.delete(OrderUserContext.require().id(),id);return ApiResponse.success(null);}
}
