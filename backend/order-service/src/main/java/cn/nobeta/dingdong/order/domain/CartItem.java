package cn.nobeta.dingdong.order.domain;
import java.time.LocalDateTime;

/**
 * 购物车项实体
 * <p>对应 cart_item 表，记录用户加入购物车的商品。
 * 支持选中/未选中状态，方便用户批量结算。</p>
 */
public class CartItem {
    /** 主键 ID */
    private Long id;
    /** 所属用户 ID */
    private Long userId;
    /** 商品 SKU ID */
    private Long skuId;
    /** 购买数量（上限 99） */
    private Integer quantity;
    /** 是否选中（用于批量结算） */
    private Boolean selected;
    /** 添加时间 */
    private LocalDateTime createdAt;

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public Long getSkuId(){return skuId;} public void setSkuId(Long skuId){this.skuId=skuId;}
    public Integer getQuantity(){return quantity;} public void setQuantity(Integer quantity){this.quantity=quantity;}
    public Boolean getSelected(){return selected;} public void setSelected(Boolean selected){this.selected=selected;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
