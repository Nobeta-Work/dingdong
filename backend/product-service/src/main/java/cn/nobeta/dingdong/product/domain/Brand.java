package cn.nobeta.dingdong.product.domain;
public class Brand {
    private Long id; private String name; private String logoUrl; private Integer sortOrder; private Integer status;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; } public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Integer getSortOrder() { return sortOrder; } public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
}
