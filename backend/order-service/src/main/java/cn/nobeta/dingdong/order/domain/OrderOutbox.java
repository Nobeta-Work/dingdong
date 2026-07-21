package cn.nobeta.dingdong.order.domain;
import java.time.LocalDateTime;

/**
 * Outbox 事件实体
 * 对应 order_outbox 表，实现可靠消息投递模式（Outbox Pattern）。
 * 将待发送的事件先持久化到此表，确保消息不丢失，支持重试机制。
 */
public class OrderOutbox {
    /** 主键 ID */
    private Long id;
    /** 事件类型：ORDER_TIMEOUT（订单超时） */
    private String eventType;
    /** 关联的订单号 */
    private String orderNo;
    /** 发送状态：PENDING（待发送）/ SENT（已发送） */
    private String status;
    /** 重试次数（发送失败时递增） */
    private Integer retryCount;
    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public String getOrderNo(){return orderNo;} public void setOrderNo(String v){orderNo=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Integer getRetryCount(){return retryCount;} public void setRetryCount(Integer v){retryCount=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
