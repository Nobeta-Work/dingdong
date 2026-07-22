package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;

/**
 * 商品分页检索查询条件
 * 价格校验链路：{@link #minPrice} 和 {@link #maxPrice} 用于 SQL 价格区间筛选，
 * 在 {@link cn.nobeta.dingdong.product.mapper.ProductMapper#search} 中
 * 通过 {@code k.price >= #{minPrice}} 和 {@code k.price <= #{maxPrice}} 实现过滤。
 */
public record ProductQuery(
    String keyword, 
    Long categoryId, 
    Long brandId, 
    BigDecimal minPrice, 
    BigDecimal maxPrice, 
    String sort, 
    int size, 
    int offset
) { }