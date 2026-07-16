package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.order.domain.MallOrder;
import cn.nobeta.dingdong.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;import org.springframework.test.util.ReflectionTestUtils;import java.time.LocalDateTime;import static org.mockito.Mockito.*;
class OrderPaymentTransitionTest {
 @Test void paymentEventConfirmsLockedInventoryAndMovesOrderToPaid(){OrderMapper mapper=mock(OrderMapper.class);CartService cart=mock(CartService.class);ProductInventoryFacade product=mock(ProductInventoryFacade.class);MallOrder order=new MallOrder();order.setId(10L);order.setOrderNo("DD1");order.setStatus("PENDING_PAYMENT");when(mapper.findByOrderNo("DD1")).thenReturn(order);when(mapper.updateStatus("DD1","PENDING_PAYMENT","PAID")).thenReturn(1);OrderService service=new OrderService(mapper,cart);ReflectionTestUtils.setField(service,"productFacade",product);service.markPaid("DD1","PAY1",LocalDateTime.now());verify(product).confirmInventory("DD1");verify(mapper).insertStatusLog(eq(10L),eq("PENDING_PAYMENT"),eq("PAID"),eq("PAYMENT"),isNull(),contains("PAY1"));}
}
