package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.ProductResponses.*;
import cn.nobeta.dingdong.product.domain.ProductQuery;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import cn.nobeta.dingdong.product.service.CatalogService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final CatalogService catalogService; private final ProductMapper productMapper;
    public ProductController(CatalogService catalogService, ProductMapper productMapper){this.catalogService=catalogService;this.productMapper=productMapper;}
    @GetMapping("/categories") public ApiResponse<List<CategoryResponse>> categories(){return ApiResponse.success(catalogService.categories(true).stream().map(CategoryResponse::from).toList());}
    @GetMapping("/brands") public ApiResponse<List<BrandResponse>> brands(){return ApiResponse.success(catalogService.brands(true).stream().map(BrandResponse::from).toList());}
    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductSummary>> list(@RequestParam(required=false) String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(required=false) Long brandId,@RequestParam(required=false) BigDecimal minPrice,@RequestParam(required=false) BigDecimal maxPrice,@RequestParam(defaultValue="newest") String sort,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){
        int normalizedPage=Math.max(page,1), normalizedSize=Math.min(Math.max(size,1),100);
        String orderBy=switch(sort){case "price-asc"->"k.price asc, s.id desc";case "price-desc"->"k.price desc, s.id desc";case "sales"->"k.sales desc, s.id desc";default->"s.created_at desc, s.id desc";};
        ProductQuery query=new ProductQuery(keyword,categoryId,brandId,minPrice,maxPrice,orderBy,normalizedSize,(normalizedPage-1)*normalizedSize);
        return ApiResponse.success(new PageResponse<>(productMapper.search(query).stream().map(ProductSummary::from).toList(),productMapper.countSearch(query),normalizedPage,normalizedSize));
    }
    @GetMapping("/products/{id}") public ApiResponse<ProductDetail> detail(@PathVariable Long id){var spu=catalogService.requirePublishedSpu(id);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(id,true)));}
}
