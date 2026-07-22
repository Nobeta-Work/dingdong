package cn.nobeta.dingdong.product.domain;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class SeckillActivity {
    private Long id; private String name; private Long skuId; private BigDecimal seckillPrice;
    private Integer totalStock; private Integer availableStock; private String status;
    private LocalDateTime startTime; private LocalDateTime endTime; private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getName(){return name;} public void setName(String v){name=v;}
    public Long getSkuId(){return skuId;} public void setSkuId(Long v){skuId=v;} public BigDecimal getSeckillPrice(){return seckillPrice;} public void setSeckillPrice(BigDecimal v){seckillPrice=v;}
    public Integer getTotalStock(){return totalStock;} public void setTotalStock(Integer v){totalStock=v;} public Integer getAvailableStock(){return availableStock;} public void setAvailableStock(Integer v){availableStock=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getStartTime(){return startTime;} public void setStartTime(LocalDateTime v){startTime=v;}
    public LocalDateTime getEndTime(){return endTime;} public void setEndTime(LocalDateTime v){endTime=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
