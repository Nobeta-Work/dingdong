package cn.nobeta.dingdong.order.repository;

import cn.nobeta.dingdong.order.domain.CartItem;

import java.util.List;

public interface CartRepository {
    List<CartItem> findByUserId(Long userId);
    CartItem findOwned(Long userId, Long id);
    CartItem findBySku(Long userId, Long skuId);
    CartItem save(CartItem item);
    boolean deleteOwned(Long userId, Long id);
    void deleteBatch(Long userId, List<Long> ids);
}
