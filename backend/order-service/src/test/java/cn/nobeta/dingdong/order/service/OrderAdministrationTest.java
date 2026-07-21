package cn.nobeta.dingdong.order.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.order.domain.TopProductStat;
import cn.nobeta.dingdong.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderAdministrationTest {
    @Test
    void buildsDashboardOverviewFromOrderAggregates() {
        OrderMapper mapper = mock(OrderMapper.class);
        when(mapper.countTodayOrders()).thenReturn(6L);
        when(mapper.sumTodayPaidAmount()).thenReturn(new BigDecimal("399.00"));
        when(mapper.countPendingShipment()).thenReturn(2L);
        TopProductStat top = new TopProductStat();
        top.setSkuId(9L);
        top.setProductTitle("演示商品");
        top.setQuantity(3L);
        top.setSalesAmount(new BigDecimal("99.00"));
        when(mapper.findTopProducts(10)).thenReturn(List.of(top));
        OrderService service = new OrderService(mapper, mock(CartService.class), mock(OrderOutboxService.class));

        var overview = service.dashboardOverview();

        assertEquals(6L, overview.todayOrderCount());
        assertEquals(new BigDecimal("399.00"), overview.todayPaidAmount());
        assertEquals(1, overview.topProducts().size());
    }

    @Test
    void rejectsUnknownAdminOrderStatus() {
        OrderService service = new OrderService(mock(OrderMapper.class), mock(CartService.class), mock(OrderOutboxService.class));
        BusinessException error = assertThrows(BusinessException.class,
                () -> service.adminPage(null, null, "UNKNOWN", 1, 20));
        assertEquals("ORDER_STATUS_INVALID", error.getCode());
    }
}
