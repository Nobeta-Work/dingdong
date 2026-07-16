package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.common.rpc.UserAddressFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.CreateOrderRequest;
import cn.nobeta.dingdong.order.domain.*;
import cn.nobeta.dingdong.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;import java.util.List;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.*;import static org.mockito.Mockito.*;
class OrderServiceTest {
 @Test void createsOrderFromRemotePriceAndAddressSnapshot(){
  OrderMapper mapper=mock(OrderMapper.class);CartService cart=mock(CartService.class);ProductInventoryFacade product=mock(ProductInventoryFacade.class);UserAddressFacade address=mock(UserAddressFacade.class);
  CartItem cartItem=new CartItem();cartItem.setId(5L);cartItem.setSkuId(9L);cartItem.setQuantity(2);when(cart.itemsForOrder(1L,List.of(5L))).thenReturn(List.of(cartItem));when(address.getAddressSnapshot(1L,3L)).thenReturn(new UserAddressFacade.AddressSnapshot(3L,"张三","13800138000","陕西省","西安市","雁塔区","科技路 1 号"));when(product.lockInventory(anyString(),anyList())).thenReturn(List.of(new ProductInventoryFacade.SkuSnapshot(9L,"SKU-9","快照商品","image", "{}",new BigDecimal("12.50"),8)));
  doAnswer(i->{((MallOrder)i.getArgument(0)).setId(7L);return 1;}).when(mapper).insertOrder(any());when(mapper.findOwned(anyString(),eq(1L))).thenAnswer(i->{MallOrder o=new MallOrder();o.setId(7L);o.setOrderNo(i.getArgument(0));o.setTotalAmount(new BigDecimal("25.00"));o.setStatus("PENDING_PAYMENT");return o;});
  OrderService service=new OrderService(mapper,cart);ReflectionTestUtils.setField(service,"productFacade",product);ReflectionTestUtils.setField(service,"addressFacade",address);
  MallOrder created=service.create(1L,new CreateOrderRequest(3L,List.of(5L)));
  assertEquals(new BigDecimal("25.00"),created.getTotalAmount());verify(mapper).insertItem(argThat(i->i.getSkuId().equals(9L)&&i.getQuantity()==2&&i.getTotalAmount().equals(new BigDecimal("25.00"))));verify(cart).removeAfterOrder(1L,List.of(5L));
 }
}
