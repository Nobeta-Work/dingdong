package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.api.ProductRequests.SpuRequest;
import cn.nobeta.dingdong.product.mapper.BrandMapper;
import cn.nobeta.dingdong.product.mapper.CategoryMapper;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogServiceTest {
    @Test
    void rejectsSpuWhenCategoryDoesNotExist() {
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        BrandMapper brandMapper = mock(BrandMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        ProductDetailCacheService cacheService = mock(ProductDetailCacheService.class);
        CatalogService service = new CatalogService(categoryMapper, brandMapper, productMapper, cacheService);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.saveSpu(null, new SpuRequest("演示商品", null, null, null, 1L, 2L, 1)));
        assertEquals("PRODUCT_CATEGORY_NOT_FOUND", exception.getCode());
        verifyNoInteractions(brandMapper, productMapper);
    }
}
