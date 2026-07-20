package cn.nobeta.dingdong.product.api;

import cn.nobeta.dingdong.product.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ProductResponses {
    private ProductResponses() { }
    public record CategoryResponse(Long id,String name,Long parentId,Integer sortOrder,Integer status) { public static CategoryResponse from(Category v){return new CategoryResponse(v.getId(),v.getName(),v.getParentId(),v.getSortOrder(),v.getStatus());} }
    public record BrandResponse(Long id,String name,String logoUrl,Integer sortOrder,Integer status) { public static BrandResponse from(Brand v){return new BrandResponse(v.getId(),v.getName(),v.getLogoUrl(),v.getSortOrder(),v.getStatus());} }
    //SKU
    public record SkuResponse(Long id,String skuCode,String specJson,BigDecimal price,Integer availableStock,Integer lockedStock,Integer sales,Integer status) { public static SkuResponse from(ProductSku v){return new SkuResponse(v.getId(),v.getSkuCode(),v.getSpecJson(),v.getPrice(),v.getAvailableStock(),v.getLockedStock(),v.getSales(),v.getStatus());} }
    public record ProductSummary(Long id,String title,String subtitle,String mainImageUrl,Long categoryId,Long brandId,LocalDateTime createdAt) { public static ProductSummary from(ProductSpu v){return new ProductSummary(v.getId(),v.getTitle(),v.getSubtitle(),v.getMainImageUrl(),v.getCategoryId(),v.getBrandId(),v.getCreatedAt());} }
    public record ProductDetail(Long id,String title,String subtitle,String description,String mainImageUrl,Long categoryId,Long brandId,Integer status,List<SkuResponse> skus) { public static ProductDetail from(ProductSpu v,List<ProductSku> skus){return new ProductDetail(v.getId(),v.getTitle(),v.getSubtitle(),v.getDescription(),v.getMainImageUrl(),v.getCategoryId(),v.getBrandId(),v.getStatus(),skus.stream().map(SkuResponse::from).toList());} }
    public record PageResponse<T>(List<T> records,long total,int page,int size) { }
}
