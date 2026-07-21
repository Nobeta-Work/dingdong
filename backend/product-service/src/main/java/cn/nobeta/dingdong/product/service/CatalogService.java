package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.api.ProductRequests.*;
import cn.nobeta.dingdong.product.domain.*;
import cn.nobeta.dingdong.product.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CatalogService {
    private final CategoryMapper categoryMapper; private final BrandMapper brandMapper; private final ProductMapper productMapper; private final ProductDetailCacheService detailCacheService;
    public CatalogService(CategoryMapper categoryMapper, BrandMapper brandMapper, ProductMapper productMapper, ProductDetailCacheService detailCacheService) { this.categoryMapper=categoryMapper;this.brandMapper=brandMapper;this.productMapper=productMapper;this.detailCacheService=detailCacheService; }
    public List<Category> categories(boolean enabledOnly){return enabledOnly?categoryMapper.findEnabled():categoryMapper.findAll();}
    public List<Brand> brands(boolean enabledOnly){return enabledOnly?brandMapper.findEnabled():brandMapper.findAll();}
    @Transactional public Category saveCategory(Long id, CategoryRequest request) {
        Category category = id == null ? new Category() : requireCategory(id);
        Long parentId = request.parentId() == null ? 0L : request.parentId();
        if (categoryMapper.countByName(request.name(), parentId) > 0 && (id == null || !request.name().equals(category.getName()) || !java.util.Objects.equals(parentId, category.getParentId()))) throw new BusinessException("PRODUCT_CATEGORY_EXISTS","同级分类名称已存在");
        category.setName(request.name());category.setParentId(parentId);category.setSortOrder(request.sortOrder()==null?0:request.sortOrder());category.setStatus(request.status());
        if(id==null) categoryMapper.insert(category); else categoryMapper.update(category); return requireCategory(category.getId());
    }
    @Transactional public Brand saveBrand(Long id, BrandRequest request) {
        Brand brand=id==null?new Brand():requireBrand(id);
        if(brandMapper.countByName(request.name())>0 && (id==null || !request.name().equals(brand.getName()))) throw new BusinessException("PRODUCT_BRAND_EXISTS","品牌名称已存在");
        brand.setName(request.name());brand.setLogoUrl(request.logoUrl());brand.setSortOrder(request.sortOrder()==null?0:request.sortOrder());brand.setStatus(request.status());
        if(id==null) brandMapper.insert(brand);else brandMapper.update(brand);return requireBrand(brand.getId());
    }
    @Transactional public ProductSpu saveSpu(Long id, SpuRequest request) {
        ProductSpu spu=id==null?new ProductSpu():requireSpu(id);
        if(categoryMapper.findById(request.categoryId())==null)throw new BusinessException("PRODUCT_CATEGORY_NOT_FOUND","商品分类不存在");
        if(brandMapper.findById(request.brandId())==null)throw new BusinessException("PRODUCT_BRAND_NOT_FOUND","商品品牌不存在");
        spu.setTitle(request.title());spu.setSubtitle(request.subtitle());spu.setDescription(request.description());spu.setMainImageUrl(request.mainImageUrl());spu.setCategoryId(request.categoryId());spu.setBrandId(request.brandId());spu.setStatus(request.status());
        if(id==null)productMapper.insertSpu(spu);else productMapper.updateSpu(spu);detailCacheService.evict(spu.getId());return requireSpu(spu.getId());
    }
    @Transactional public ProductSku saveSku(Long spuId, Long id, SkuRequest request) {
        requireSpu(spuId); ProductSku sku=id==null?new ProductSku():requireSku(spuId,id);
        sku.setSpuId(spuId);sku.setSkuCode(request.skuCode());sku.setSpecJson(request.specJson());sku.setPrice(request.price());sku.setAvailableStock(request.availableStock());sku.setStatus(request.status());
        if(id==null) productMapper.insertSku(sku); else productMapper.updateSku(sku); detailCacheService.evict(spuId); return requireSku(spuId,sku.getId());
    }
    public ProductSpu requireSpu(Long id){ProductSpu spu=productMapper.findSpuById(id);if(spu==null)throw new BusinessException("PRODUCT_SPU_NOT_FOUND","商品不存在");return spu;}
    public ProductSpu requirePublishedSpu(Long id){ProductSpu spu=productMapper.findPublishedSpuById(id);if(spu==null)throw new BusinessException("PRODUCT_SPU_NOT_FOUND","商品不存在或已下架");return spu;}
    public ProductSku requireSku(Long spuId,Long id){ProductSku sku=productMapper.findSku(id,spuId);if(sku==null)throw new BusinessException("PRODUCT_SKU_NOT_FOUND","SKU 不存在");return sku;}
    private Category requireCategory(Long id){Category v=categoryMapper.findById(id);if(v==null)throw new BusinessException("PRODUCT_CATEGORY_NOT_FOUND","商品分类不存在");return v;}
    private Brand requireBrand(Long id){Brand v=brandMapper.findById(id);if(v==null)throw new BusinessException("PRODUCT_BRAND_NOT_FOUND","商品品牌不存在");return v;}
    public List<ProductSku> skus(Long spuId,boolean saleable){return saleable?productMapper.findSaleableSkusBySpuId(spuId):productMapper.findSkusBySpuId(spuId);}
    public List<ProductSpu> adminProducts(AdminProductQuery query){return productMapper.searchAdmin(query);}
    public long countAdminProducts(AdminProductQuery query){return productMapper.countAdmin(query);}
}
