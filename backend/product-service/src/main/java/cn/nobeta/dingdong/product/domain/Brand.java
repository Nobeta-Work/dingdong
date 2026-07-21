package cn.nobeta.dingdong.product.domain;

/**
 * 商品品牌实体类
 */
public class Brand {
    /** 品牌主键ID */
    private Long id; 
    /** 品牌名称 */
    private String name; 
    /** 品牌logo图片访问地址 */
    private String logoUrl; 
    /** 排序序号，数值越小展示优先级越高 */
    private Integer sortOrder; 
    /** 品牌状态：1正常启用，0禁用 */
    private Integer status;

    public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
    public String getName() { return name; } 
    public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Integer getSortOrder() { return sortOrder; } 
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; } 
    public void setStatus(Integer status) { this.status = status; }
}
