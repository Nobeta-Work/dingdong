package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.api.ProductRequests.*;
import cn.nobeta.dingdong.product.api.ProductResponses.*;
import cn.nobeta.dingdong.product.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCatalogController {
    private final CatalogService catalogService;
    public AdminCatalogController(CatalogService catalogService){this.catalogService=catalogService;}
    @GetMapping("/categories") public ApiResponse<List<CategoryResponse>> categories(){return ApiResponse.success(catalogService.categories(false).stream().map(CategoryResponse::from).toList());}
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request){return ApiResponse.success(CategoryResponse.from(catalogService.saveCategory(null,request)));}
    @PutMapping("/categories/{id}") public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequest request){return ApiResponse.success(CategoryResponse.from(catalogService.saveCategory(id,request)));}
    @GetMapping("/brands") public ApiResponse<List<BrandResponse>> brands(){return ApiResponse.success(catalogService.brands(false).stream().map(BrandResponse::from).toList());}
    @PostMapping("/brands") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request){return ApiResponse.success(BrandResponse.from(catalogService.saveBrand(null,request)));}
    @PutMapping("/brands/{id}") public ApiResponse<BrandResponse> updateBrand(@PathVariable Long id,@Valid @RequestBody BrandRequest request){return ApiResponse.success(BrandResponse.from(catalogService.saveBrand(id,request)));}
    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<ProductDetail> createSpu(@Valid @RequestBody SpuRequest request){var spu=catalogService.saveSpu(null,request);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(spu.getId(),false)));}
    @PutMapping("/products/{id}") public ApiResponse<ProductDetail> updateSpu(@PathVariable Long id,@Valid @RequestBody SpuRequest request){var spu=catalogService.saveSpu(id,request);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(id,false)));}
    @GetMapping("/products/{id}") public ApiResponse<ProductDetail> getSpu(@PathVariable Long id){var spu=catalogService.requireSpu(id);return ApiResponse.success(ProductDetail.from(spu,catalogService.skus(id,false)));}
    @PostMapping("/products/{spuId}/skus") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<SkuResponse> createSku(@PathVariable Long spuId,@Valid @RequestBody SkuRequest request){return ApiResponse.success(SkuResponse.from(catalogService.saveSku(spuId,null,request)));}
    @PutMapping("/products/{spuId}/skus/{id}") public ApiResponse<SkuResponse> updateSku(@PathVariable Long spuId,@PathVariable Long id,@Valid @RequestBody SkuRequest request){return ApiResponse.success(SkuResponse.from(catalogService.saveSku(spuId,id,request)));}
}
