package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.ProductResponses.*;
import cn.nobeta.dingdong.product.domain.ProductQuery;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import cn.nobeta.dingdong.product.service.CatalogService;
import cn.nobeta.dingdong.product.service.ProductDetailCacheService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品前台 REST 控制器
 * 提供商品浏览、搜索、详情等面向用户的接口。
 * 价格校验链路中，list 方法支持按价格区间筛选商品。
 */
@RestController
@RequestMapping("/api")
public class ProductController {
    private final CatalogService catalogService; private final ProductMapper productMapper; private final ProductDetailCacheService detailCacheService;
    public ProductController(CatalogService catalogService, ProductMapper productMapper, ProductDetailCacheService detailCacheService){this.catalogService=catalogService;this.productMapper=productMapper;this.detailCacheService=detailCacheService;}
    /** 查询所有启用的分类列表 */
    @GetMapping("/categories") public ApiResponse<List<CategoryResponse>> categories(){return ApiResponse.success(catalogService.categories(true).stream().map(CategoryResponse::from).toList());}
    /** 查询所有启用的品牌列表 */
    @GetMapping("/brands") public ApiResponse<List<BrandResponse>> brands(){return ApiResponse.success(catalogService.brands(true).stream().map(BrandResponse::from).toList());}

    /**
     * 商品列表分页搜索
     * 支持按关键词、分类、品牌、价格区间筛选，支持多种排序方式。
     * 价格校验链路：minPrice / maxPrice 参数用于数据库价格区间过滤，
     * SQL 中通过 k.price >= #{minPrice} 和 k.price <= #{maxPrice} 实现。
     * @param keyword 搜索关键词（模糊匹配标题/副标题）
     * @param categoryId 分类 ID 筛选
     * @param brandId 品牌 ID 筛选
     * @param minPrice 最低价格（含边界）
     * @param maxPrice 最高价格（含边界）
     * @param sort 排序方式：newest（默认）/ price-asc / price-desc / sales
     * @param page 页码（从 1 开始，默认 1）
     * @param size 每页条数（默认 20，上限 100）
     * @return 分页商品摘要列表
     */
    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductSummary>> list(@RequestParam(required=false) String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(required=false) Long brandId,@RequestParam(required=false) BigDecimal minPrice,@RequestParam(required=false) BigDecimal maxPrice,@RequestParam(defaultValue="newest") String sort,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){
        int normalizedPage=Math.max(page,1), normalizedSize=Math.min(Math.max(size,1),100);
        String orderBy=switch(sort){case "price-asc"->"k.price asc, s.id desc";case "price-desc"->"k.price desc, s.id desc";case "sales"->"k.sales desc, s.id desc";default->"s.created_at desc, s.id desc";};
        ProductQuery query=new ProductQuery(keyword,categoryId,brandId,minPrice,maxPrice,orderBy,normalizedSize,(normalizedPage-1)*normalizedSize);
        return ApiResponse.success(new PageResponse<>(productMapper.search(query).stream().map(ProductSummary::from).toList(),productMapper.countSearch(query),normalizedPage,normalizedSize));
    }

    /**
     * 商品详情（含缓存，10 分钟 TTL）
     * 返回商品 SPU 信息及所有在售 SKU 列表（含价格、库存等）。
     */
    @GetMapping("/products/{id}") public ApiResponse<ProductDetail> detail(@PathVariable Long id){return ApiResponse.success(detailCacheService.get(id));}
}