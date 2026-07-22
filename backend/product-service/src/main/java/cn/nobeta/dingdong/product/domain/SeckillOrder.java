package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class SeckillOrder {
    private Long id; private String requestId; private Long activityId; private Long userId; private Long skuId;
    private Integer quantity; private BigDecimal seckillPrice; private String status; private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getRequestId(){return requestId;} public void setRequestId(String v){requestId=v;}
    public Long getActivityId(){return activityId;} public void setActivityId(Long v){activityId=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public Long getSkuId(){return skuId;} public void setSkuId(Long v){skuId=v;} public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
    public BigDecimal getSeckillPrice(){return seckillPrice;} public void setSeckillPrice(BigDecimal v){seckillPrice=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
