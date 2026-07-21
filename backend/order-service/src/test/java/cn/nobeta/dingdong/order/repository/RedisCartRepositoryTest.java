package cn.nobeta.dingdong.order.repository;

import cn.nobeta.dingdong.order.domain.CartItem;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class RedisCartRepositoryTest {
    @Test
    @SuppressWarnings("unchecked")
    void storesAndReadsCartItemsFromUserHash() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hash = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hash);
        var mapper = JsonMapper.builder().findAndAddModules().build();
        RedisCartRepository repository = new RedisCartRepository(redis, mapper, "dingdong:cart:");
        CartItem item = new CartItem();
        item.setUserId(1L);
        item.setSkuId(9L);
        item.setQuantity(2);
        item.setSelected(true);

        repository.save(item);
        verify(hash).put(eq("dingdong:cart:1"), eq("9"), contains("\"quantity\":2"));

        when(hash.entries("dingdong:cart:1")).thenReturn(Map.of("9", mapper.writeValueAsString(item)));
        var result = repository.findByUserId(1L);
        assertEquals(1, result.size());
        assertEquals(9L, result.getFirst().getId());
    }
}
