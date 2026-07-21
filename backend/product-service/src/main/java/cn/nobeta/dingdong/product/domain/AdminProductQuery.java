package cn.nobeta.dingdong.product.domain;

public record AdminProductQuery(String keyword, Long categoryId, Long brandId, Integer status, int size, int offset) { }
