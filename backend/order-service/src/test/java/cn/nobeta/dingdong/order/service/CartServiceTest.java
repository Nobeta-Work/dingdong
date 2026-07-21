package cn.nobeta.dingdong.order.service;

import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.AddCartItemRequest;
import cn.nobeta.dingdong.order.api.OrderRequests.UpdateCartItemRequest;
import cn.nobeta.dingdong.order.domain.CartItem;
import cn.nobeta.dingdong.order.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {
    @Test
    void createsRedisCartItemUsingSkuIdAsStableId() {
        CartRepository repository = mock(CartRepository.class);
        ProductInventoryFacade product = mock(ProductInventoryFacade.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CartService service = new CartService(repository);
        ReflectionTestUtils.setField(service, "productFacade", product);

        CartItem result = service.add(1L, new AddCartItemRequest(9L, 2));

        assertEquals(9L, result.getId());
        assertEquals(2, result.getQuantity());
        assertTrue(result.getSelected());
        verify(product).getSkuSnapshots(List.of(9L));
        verify(repository).save(result);
    }

    @Test
    void updatesExistingRedisCartItem() {
        CartRepository repository = mock(CartRepository.class);
        CartItem item = item();
        when(repository.findOwned(1L, 9L)).thenReturn(item);
        when(repository.save(item)).thenReturn(item);
        CartService service = new CartService(repository);

        CartItem result = service.update(1L, 9L, new UpdateCartItemRequest(4, false));

        assertEquals(4, result.getQuantity());
        assertEquals(false, result.getSelected());
    }

    private CartItem item() {
        CartItem item = new CartItem();
        item.setId(9L);
        item.setUserId(1L);
        item.setSkuId(9L);
        item.setQuantity(1);
        item.setSelected(true);
        return item;
    }
}
