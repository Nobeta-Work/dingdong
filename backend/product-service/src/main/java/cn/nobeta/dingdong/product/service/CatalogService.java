package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.api.ProductRequests.*;
import cn.nobeta.dingdong.product.domain.*;
import cn.nobeta.dingdong.product.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 商品目录管理服务
 * 提供 SPU、SKU、分类、品牌的 CRUD 管理。
 * 价格校验链路：saveSku 将前端传入的价格写入 SKU 实体，
 * 价格合法性已在 SkuRequest#price() 的 Jakarta Validation 注解层完成校验。
 */
@Service
public class CatalogService {
    private final CategoryMapper categoryMapper; 
    private final BrandMapper brandMapper; 
    private final ProductMapper productMapper; 
    private final ProductDetailCacheService detailCacheService;
    public CatalogService(CategoryMapper categoryMapper, BrandMapper brandMapper, ProductMapper productMapper, ProductDetailCacheService detailCacheService) 
    { this.categoryMapper=categoryMapper;this.brandMapper=brandMapper;this.productMapper=productMapper;this.detailCacheService=detailCacheService; }
    /** 查询所有分类（按 enabledOnly 筛选启用/全部） */
    public List<Category> categories(boolean enabledOnly){return enabledOnly?categoryMapper.findEnabled():categoryMapper.findAll();}
    /** 查询所有品牌（按 enabledOnly 筛选启用/全部） */
    public List<Brand> brands(boolean enabledOnly){return enabledOnly?brandMapper.findEnabled():brandMapper.findAll();}
    /** 新增或更新分类（同级分类名不可重复） */
    @Transactional public Category saveCategory(Long id, CategoryRequest request) {
        Category category = id == null ? new Category() : requireCategory(id);
        Long parentId = request.parentId() == null ? 0L : request.parentId();
        if (categoryMapper.countByName(request.name(), parentId) > 0 && (id == null || !request.name().equals(category.getName()) || !java.util.Objects.equals(parentId, category.getParentId()))) 
            throw new BusinessException("PRODUCT_CATEGORY_EXISTS","同级分类名称已存在");
        category.setName(request.name());
        category.setParentId(parentId);
        category.setSortOrder(request.sortOrder()==null?0:request.sortOrder());
        category.setStatus(request.status());
        if(id==null) categoryMapper.insert(category); 
        else categoryMapper.update(category); 
        return requireCategory(category.getId());
    }
    /** 新增或更新品牌（品牌名不可重复） */
    @Transactional public Brand saveBrand(Long id, BrandRequest request) {
        Brand brand=id==null?new Brand():requireBrand(id);
        if(brandMapper.countByName(request.name())>0 && (id==null || !request.name().equals(brand.getName()))) throw new BusinessException("PRODUCT_BRAND_EXISTS","品牌名称已存在");
        brand.setName(request.name());brand.setLogoUrl(request.logoUrl());brand.setSortOrder(request.sortOrder()==null?0:request.sortOrder());brand.setStatus(request.status());
        if(id==null) brandMapper.insert(brand);
        else brandMapper.update(brand);
        return requireBrand(brand.getId());
    }
    /** 新增或更新商品 SPU（校验分类、品牌存在性） */
    @Transactional public ProductSpu saveSpu(Long id, SpuRequest request) {
        ProductSpu spu=id==null?new ProductSpu():requireSpu(id);
        if(categoryMapper.findById(request.categoryId())==null)
            throw new BusinessException("PRODUCT_CATEGORY_NOT_FOUND","商品分类不存在");
        if(brandMapper.findById(request.brandId())==null)
            throw new BusinessException("PRODUCT_BRAND_NOT_FOUND","商品品牌不存在");
        spu.setTitle(request.title());
        spu.setSubtitle(request.subtitle());
        spu.setDescription(request.description());
        spu.setMainImageUrl(request.mainImageUrl());
        spu.setCategoryId(request.categoryId());
        spu.setBrandId(request.brandId());
        spu.setStatus(request.status());
        if(id==null)productMapper.insertSpu(spu);
        else productMapper.updateSpu(spu);
        detailCacheService.evict(spu.getId());
        return requireSpu(spu.getId());
    }

    /**
     * 新增或更新 SKU 规格 —— 价格持久化入口
     * 价格校验链路第一步：将 SkuRequest#price() 写入 SKU 实体。
     * - 价格已在 SkuRequest#price() 的 Jakarta Validation 层完成校验
     *   （@DecimalMin("0.01") @Digits(integer=10,fraction=2)）
     * - 此处直接将校验后的价格持久化写入数据库 product_sku.price 字段
     * - 支付链路中，OrderService#create 会通过 RPC 获取数据库中的快照价格进行金额计算
     * @param spuId 所属 SPU ID
     * @param id SKU ID（为 null 时新增，否则更新）
     * @param request SKU 请求参数（含已校验的价格）
     * @return 持久化后的 SKU 实体
     */
    @Transactional public ProductSku saveSku(Long spuId, Long id, SkuRequest request) {
        requireSpu(spuId);
        ProductSku sku=id==null?new ProductSku():requireSku(spuId,id);
        sku.setSpuId(spuId);
        sku.setSkuCode(request.skuCode());
        sku.setSpecJson(request.specJson());
        sku.setPrice(request.price());
        sku.setAvailableStock(request.availableStock());
        sku.setStatus(request.status());
        if(id==null) productMapper.insertSku(sku); 
        else productMapper.updateSku(sku); 
        detailCacheService.evict(spuId); 
        return requireSku(spuId,sku.getId());
    }

    /** 内部方法：按 ID 查询 SPU（不存在时抛异常） */
    public ProductSpu requireSpu(Long id){
        ProductSpu spu=productMapper.findSpuById(id);
        if(spu==null)throw new BusinessException("PRODUCT_SPU_NOT_FOUND","商品不存在");
        return spu;
    }
    /** 内部方法：按 ID 查询已上架 SPU（不存在或已下架时抛异常） */
    public ProductSpu requirePublishedSpu(Long id){
        ProductSpu spu=productMapper.findPublishedSpuById(id);
        if(spu==null)throw new BusinessException("PRODUCT_SPU_NOT_FOUND","商品不存在或已下架");
        return spu;
    }
    /** 内部方法：按 SPU+SKU ID 查询 SKU（不存在时抛异常） */
    public ProductSku requireSku(Long spuId,Long id){
        ProductSku sku=productMapper.findSku(id,spuId);
        if(sku==null)throw new BusinessException("PRODUCT_SKU_NOT_FOUND","SKU 不存在");
        return sku;
    }
    private Category requireCategory(Long id){
        Category v=categoryMapper.findById(id);
        if(v==null)throw new BusinessException("PRODUCT_CATEGORY_NOT_FOUND","商品分类不存在");
        return v;
    }
    private Brand requireBrand(Long id){
        Brand v=brandMapper.findById(id);
        if(v==null)throw new BusinessException("PRODUCT_BRAND_NOT_FOUND","商品品牌不存在");
        return v;
    }
    /** 查询 SPU 下的 SKU 列表（按 saleable 筛选在售/全部） */
    public List<ProductSku> skus(Long spuId,boolean saleable){return saleable?productMapper.findSaleableSkusBySpuId(spuId):productMapper.findSkusBySpuId(spuId);}
    public List<ProductSpu> adminProducts(AdminProductQuery query){return productMapper.searchAdmin(query);}
    public long countAdminProducts(AdminProductQuery query){return productMapper.countAdmin(query);}
}