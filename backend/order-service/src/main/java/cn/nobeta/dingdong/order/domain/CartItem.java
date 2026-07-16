package cn.nobeta.dingdong.order.domain;
import java.time.LocalDateTime;
public class CartItem {
    private Long id; private Long userId; private Long skuId; private Integer quantity; private Boolean selected; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;} public Long getSkuId(){return skuId;} public void setSkuId(Long skuId){this.skuId=skuId;} public Integer getQuantity(){return quantity;} public void setQuantity(Integer quantity){this.quantity=quantity;} public Boolean getSelected(){return selected;} public void setSelected(Boolean selected){this.selected=selected;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
