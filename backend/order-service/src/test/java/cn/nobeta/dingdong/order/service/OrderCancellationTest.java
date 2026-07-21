package cn.nobeta.dingdong.order.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.order.domain.MallOrder;
import cn.nobeta.dingdong.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderCancellationTest {
    @Test
    void cancelsOwnedPendingOrderAndReleasesInventory() {
        OrderMapper mapper = mock(OrderMapper.class);
        ProductInventoryFacade product = mock(ProductInventoryFacade.class);
        MallOrder pending = order("PENDING_PAYMENT");
        MallOrder canceled = order("CANCELED");
        when(mapper.findOwned("DD1", 1L)).thenReturn(pending);
        when(mapper.findByOrderNo("DD1")).thenReturn(canceled);
        when(mapper.updateStatus("DD1", "PENDING_PAYMENT", "CANCELED")).thenReturn(1);

        OrderService service = new OrderService(mapper, mock(CartService.class), mock(OrderOutboxService.class));
        ReflectionTestUtils.setField(service, "productFacade", product);

        MallOrder result = service.cancel(1L, "DD1");

        assertEquals("CANCELED", result.getStatus());
        verify(product).unlockInventory("DD1");
        verify(mapper).insertStatusLog(8L, "PENDING_PAYMENT", "CANCELED", "USER", 1L, "用户主动取消订单");
    }

    @Test
    void rejectsCancelWhenOrderIsAlreadyPaid() {
        OrderMapper mapper = mock(OrderMapper.class);
        when(mapper.findOwned("DD1", 1L)).thenReturn(order("PAID"));
        OrderService service = new OrderService(mapper, mock(CartService.class), mock(OrderOutboxService.class));

        BusinessException error = assertThrows(BusinessException.class, () -> service.cancel(1L, "DD1"));

        assertEquals("ORDER_STATUS_INVALID", error.getCode());
        verify(mapper, never()).updateStatus(anyString(), anyString(), anyString());
    }

    private MallOrder order(String status) {
        MallOrder order = new MallOrder();
        order.setId(8L);
        order.setOrderNo("DD1");
        order.setUserId(1L);
        order.setStatus(status);
        return order;
    }
}
