package cn.nobeta.dingdong.product.domain;

/**
 * 商品分类实体类
 * 对应数据库商品分类表，存储分类基础信息
 */
public class Category {

    /** 分类主键ID */
    private Long id;
    /** 分类名称 */
    private String name;
    /** 上级分类ID，顶级分类该字段为null */
    private Long parentId;
    /** 排序序号，数值越小展示越靠前 */
    private Integer sortOrder;
    /** 分类状态：1正常启用，0禁用 */
    private Integer status;
    
    public Long getId() { return id; } 
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; } 
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; } 
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; } 
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; } 
    public void setStatus(Integer status) { this.status = status; }
}
