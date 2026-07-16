package cn.nobeta.dingdong.order.domain;
import java.time.LocalDateTime;
public class OrderOutbox { private Long id;private String eventType;private String orderNo;private String status;private Integer retryCount;private LocalDateTime createdAt;
 public Long getId(){return id;}public void setId(Long v){id=v;}public String getEventType(){return eventType;}public void setEventType(String v){eventType=v;}public String getOrderNo(){return orderNo;}public void setOrderNo(String v){orderNo=v;}public String getStatus(){return status;}public void setStatus(String v){status=v;}public Integer getRetryCount(){return retryCount;}public void setRetryCount(Integer v){retryCount=v;}public LocalDateTime getCreatedAt(){return createdAt;}public void setCreatedAt(LocalDateTime v){createdAt=v;}}
