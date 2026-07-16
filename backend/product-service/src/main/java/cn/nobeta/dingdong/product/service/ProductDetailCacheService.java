package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.api.ProductResponses.ProductDetail;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

/** Redis cache with database fallback; Redis unavailability must not block product browsing. */
@Service
public class ProductDetailCacheService {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;
    public ProductDetailCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, ProductMapper productMapper) { this.redisTemplate = redisTemplate; this.objectMapper = objectMapper; this.productMapper = productMapper; }
    public ProductDetail get(Long spuId) {
        String key = key(spuId);
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) return objectMapper.readValue(cached, ProductDetail.class);
        } catch (Exception ignored) { }
        var spu = productMapper.findPublishedSpuById(spuId);
        if (spu == null) throw new BusinessException("PRODUCT_SPU_NOT_FOUND", "商品不存在或已下架");
        ProductDetail detail = ProductDetail.from(spu, productMapper.findSaleableSkusBySpuId(spuId));
        try { redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(detail), TTL); } catch (Exception ignored) { }
        return detail;
    }
    public void evict(Long spuId) { try { redisTemplate.delete(key(spuId)); } catch (Exception ignored) { } }
    private String key(Long spuId) { return "dingdong:product:detail:" + spuId; }
}
