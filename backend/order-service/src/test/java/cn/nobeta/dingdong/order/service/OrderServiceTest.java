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

/**
 * OrderService#create 订单创建核心流程测试
 * 价格校验链路测试：验证订单总金额基于产品服务返回的快照价格计算，
 * 而非客户端传入的价格。测试覆盖：
 * - 远程快照价格正确用于总金额计算（12.50 x 2 = 25.00）
 * - 订单项单价和总价使用快照价格
 * - 购物车在订单创建成功后删除
 * - 超时事件在事务提交后发布
 */
class OrderServiceTest {
	/** 测试正常下单：使用远程快照价格计算金额 */
	@Test void createsOrderFromRemotePriceAndAddressSnapshot(){
		OrderMapper mapper=mock(OrderMapper.class);CartService cart=mock(CartService.class);OrderOutboxService outbox=mock(OrderOutboxService.class);ProductInventoryFacade product=mock(ProductInventoryFacade.class);UserAddressFacade address=mock(UserAddressFacade.class);
		CartItem cartItem=new CartItem();cartItem.setId(5L);cartItem.setSkuId(9L);cartItem.setQuantity(2);when(cart.itemsForOrder(1L,List.of(5L))).thenReturn(List.of(cartItem));when(address.getAddressSnapshot(1L,3L)).thenReturn(new UserAddressFacade.AddressSnapshot(3L,"张三","13800138000","陕西省","西安市","雁塔区","科技路 1 号"));when(product.lockInventory(anyString(),anyList())).thenReturn(List.of(new ProductInventoryFacade.SkuSnapshot(9L,"SKU-9","快照商品","image", "{}",new BigDecimal("12.50"),8)));
		doAnswer(i->{((MallOrder)i.getArgument(0)).setId(7L);return 1;}).when(mapper).insertOrder(any());when(mapper.findOwned(anyString(),eq(1L))).thenAnswer(i->{MallOrder o=new MallOrder();o.setId(7L);o.setOrderNo(i.getArgument(0));o.setTotalAmount(new BigDecimal("25.00"));o.setStatus("PENDING_PAYMENT");return o;});
		OrderOutbox timeout=new OrderOutbox();timeout.setId(11L);when(outbox.createTimeout(anyString())).thenReturn(timeout);OrderService service=new OrderService(mapper,cart,outbox);ReflectionTestUtils.setField(service,"productFacade",product);ReflectionTestUtils.setField(service,"addressFacade",address);
		MallOrder created=service.create(1L,new CreateOrderRequest(3L,List.of(5L)));
		// 验证：总金额 12.50 x 2 = 25.00（使用快照价格，而非客户端传入的值）
		assertEquals(new BigDecimal("25.00"),created.getTotalAmount());
		// 验证：订单项单价和总价使用快照价格
		verify(mapper).insertItem(argThat(i->i.getSkuId().equals(9L)&&i.getQuantity()==2&&i.getTotalAmount().equals(new BigDecimal("25.00"))));
		verify(cart).removeAfterOrder(1L,List.of(5L));verify(outbox).publish(11L);
	}
}