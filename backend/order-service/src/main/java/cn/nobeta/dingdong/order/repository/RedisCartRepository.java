package cn.nobeta.dingdong.order.repository;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.order.domain.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Repository
public class RedisCartRepository implements CartRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;

    public RedisCartRepository(StringRedisTemplate redisTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.cart.key-prefix:dingdong:cart:}") String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public List<CartItem> findByUserId(Long userId) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key(userId));
            return entries.values().stream()
                    .map(this::deserialize)
                    .sorted(Comparator.comparing(CartItem::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        } catch (RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public CartItem findOwned(Long userId, Long id) {
        try {
            Object value = redisTemplate.opsForHash().get(key(userId), field(id));
            return value == null ? null : deserialize(value);
        } catch (RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public CartItem findBySku(Long userId, Long skuId) {
        return findOwned(userId, skuId);
    }

    @Override
    public CartItem save(CartItem item) {
        if (item.getId() == null) item.setId(item.getSkuId());
        if (item.getCreatedAt() == null) item.setCreatedAt(LocalDateTime.now());
        try {
            redisTemplate.opsForHash().put(key(item.getUserId()), field(item.getId()), serialize(item));
            return item;
        } catch (RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public boolean deleteOwned(Long userId, Long id) {
        try {
            return redisTemplate.opsForHash().delete(key(userId), field(id)) > 0;
        } catch (RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public void deleteBatch(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        try {
            Object[] fields = ids.stream().map(this::field).toArray();
            redisTemplate.opsForHash().delete(key(userId), fields);
        } catch (RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    private String key(Long userId) {
        return keyPrefix + userId;
    }

    private String field(Long id) {
        return String.valueOf(id);
    }

    private String serialize(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception exception) {
            throw new IllegalStateException("无法序列化购物车数据", exception);
        }
    }

    private CartItem deserialize(Object value) {
        try {
            return objectMapper.readValue(String.valueOf(value), CartItem.class);
        } catch (Exception exception) {
            throw new IllegalStateException("无法读取购物车数据", exception);
        }
    }

    private BusinessException unavailable(RuntimeException cause) {
        BusinessException exception = new BusinessException("CART_STORAGE_UNAVAILABLE", "购物车服务暂不可用，请稍后重试");
        exception.initCause(cause);
        return exception;
    }
}
