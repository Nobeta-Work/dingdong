package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;
public record ProductQuery(String keyword, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice, String sort, int size, int offset) { }
