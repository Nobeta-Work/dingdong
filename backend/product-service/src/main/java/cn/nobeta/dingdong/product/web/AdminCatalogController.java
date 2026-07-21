package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.ProductRequests.*;
import cn.nobeta.dingdong.product.api.ProductResponses.*;
import cn.nobeta.dingdong.product.domain.AdminProductQuery;
import cn.nobeta.dingdong.product.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 商品管理后台 REST 控制器
 * 提供商品分类、品牌、SPU、SKU 的 CRUD 管理接口。
 * 价格校验链路入口：createSku 和 updateSku 通过 @Valid @RequestBody SkuRequest
 * 触发 SkuRequest#price() 的 Jakarta Validation 校验
 * （@DecimalMin("0.01") @Digits(integer=10,fraction=2)）。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminCatalogController {
    private final CatalogService catalogService;
    public AdminCatalogController(CatalogService catalogService){this.catalogService=catalogService;}
    /** 查询全部分类列表（含未启用） */
    @GetMapping("/categories") public ApiResponse<List<CategoryResponse>> categories(){return ApiResponse.success(catalogService.categories(false).stream().map(CategoryResponse::from).toList());}
    /** 新增分类 */
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request){return ApiResponse.success(CategoryResponse.from(catalogService.saveCategory(null,request)));}
    /** 更新分类 */
    @PutMapping("/categories/{id}") public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequest request){return ApiResponse.success(CategoryResponse.from(catalogService.saveCategory(id,request)));}
    /** 查询全部品牌列表（含未启用） */
    @GetMapping("/brands") public ApiResponse<List<BrandResponse>> brands(){return ApiResponse.success(catalogService.brands(false).stream().map(BrandResponse::from).toList());}
    /** 新增品牌 */
    @PostMapping("/brands") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request){return ApiResponse.success(BrandResponse.from(catalogService.saveBrand(null,request)));}
    /** 更新品牌 */
    @PutMapping("/brands/{id}") public ApiResponse<BrandResponse> updateBrand(@PathVariable Long id,@Valid @RequestBody BrandRequest request){return ApiResponse.success(BrandResponse.from(catalogService.saveBrand(id,request)));}
    @GetMapping("/products") public ApiResponse<PageResponse<ProductSummary>> products(@RequestParam(required=false) String keyword,@RequestParam(required=false) Long categoryId,@RequestParam(required=false) Long brandId,@RequestParam(required=false) Integer status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){int p=Math.max(1,page),s=Math.min(100,Math.max(1,size));AdminProductQuery query=new AdminProductQuery(keyword,categoryId,brandId,status,s,(p-1)*s);long total=catalogService.countAdminProducts(query);return ApiResponse.success(new PageResponse<>(catalogService.adminProducts(query).stream().map(ProductSummary::from).toList(),total,p,s,(total+s-1)/s));}
    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<ProductDetail> createSpu(@Valid @RequestBody SpuRequest request){var spu=catalogService.saveSpu(null,request);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(spu.getId(),false)));}
    /** 更新商品 SPU */
    @PutMapping("/products/{id}") public ApiResponse<ProductDetail> updateSpu(@PathVariable Long id,@Valid @RequestBody SpuRequest request){var spu=catalogService.saveSpu(id,request);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(id,false)));}
    /** 查询商品 SPU 详情（含所有 SKU） */
    @GetMapping("/products/{id}") public ApiResponse<ProductDetail> getSpu(@PathVariable Long id){var spu=catalogService.requireSpu(id);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(id,false)));}

    /**
     * 新增 SKU 规格 —— 价格校验入口
     * 通过 @Valid 触发 SkuRequest#price() 的 Jakarta Validation 校验，
     * 确保价格 >= 0.01 且格式合法（最多 10 位整数 + 2 位小数），
     * 校验通过后委托 CatalogService#saveSku 持久化。
     */
    @PostMapping("/products/{spuId}/skus") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<SkuResponse> createSku(@PathVariable Long spuId,@Valid @RequestBody SkuRequest request){return ApiResponse.success(SkuResponse.from(catalogService.saveSku(spuId,null,request)));}

    /**
     * 更新 SKU 规格 —— 价格校验入口
     * 同 createSku，更新时同样触发价格字段的 Jakarta Validation 校验。
     */
    @PutMapping("/products/{spuId}/skus/{id}") public ApiResponse<SkuResponse> updateSku(@PathVariable Long spuId,@PathVariable Long id,@Valid @RequestBody SkuRequest request){return ApiResponse.success(SkuResponse.from(catalogService.saveSku(spuId,id,request)));}
}