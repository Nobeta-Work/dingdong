package cn.nobeta.dingdong.product.domain;

import java.time.LocalDateTime;

public class InventoryChangeLog {
    private Long id;
    private Long skuId;
    private String businessKey;
    private String businessType;
    private String referenceNo;
    private Integer changeAvailable;
    private Integer changeLocked;
    private Integer changeSales;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private Integer beforeLocked;
    private Integer afterLocked;
    private Integer beforeSales;
    private Integer afterSales;
    private String remark;
    private LocalDateTime createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getSkuId(){return skuId;} public void setSkuId(Long v){skuId=v;}
    public String getBusinessKey(){return businessKey;} public void setBusinessKey(String v){businessKey=v;}
    public String getBusinessType(){return businessType;} public void setBusinessType(String v){businessType=v;}
    public String getReferenceNo(){return referenceNo;} public void setReferenceNo(String v){referenceNo=v;}
    public Integer getChangeAvailable(){return changeAvailable;} public void setChangeAvailable(Integer v){changeAvailable=v;}
    public Integer getChangeLocked(){return changeLocked;} public void setChangeLocked(Integer v){changeLocked=v;}
    public Integer getChangeSales(){return changeSales;} public void setChangeSales(Integer v){changeSales=v;}
    public Integer getBeforeAvailable(){return beforeAvailable;} public void setBeforeAvailable(Integer v){beforeAvailable=v;}
    public Integer getAfterAvailable(){return afterAvailable;} public void setAfterAvailable(Integer v){afterAvailable=v;}
    public Integer getBeforeLocked(){return beforeLocked;} public void setBeforeLocked(Integer v){beforeLocked=v;}
    public Integer getAfterLocked(){return afterLocked;} public void setAfterLocked(Integer v){afterLocked=v;}
    public Integer getBeforeSales(){return beforeSales;} public void setBeforeSales(Integer v){beforeSales=v;}
    public Integer getAfterSales(){return afterSales;} public void setAfterSales(Integer v){afterSales=v;}
    public String getRemark(){return remark;} public void setRemark(String v){remark=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
