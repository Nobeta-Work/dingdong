package cn.nobeta.dingdong.order.domain;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体
 * 对应 mall_order 表，记录每笔订单的核心信息。
 * 订单状态流转：PENDING_PAYMENT → PAID → SHIPPED → COMPLETED / CANCELED。
 */
public class MallOrder {
    /** 主键 ID */
    private Long id;
    /** 订单号（DD + 时间戳 + 随机数） */
    private String orderNo;
    /** 下单用户 ID */
    private Long userId;
    /** 收件人姓名（下单时快照） */
    private String receiverName;
    /** 收件人电话（下单时快照） */
    private String receiverPhone;
    /** 收件地址（下单时快照，含省/市/区/详细地址） */
    private String receiverAddress;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 订单状态：PENDING_PAYMENT / PAID / SHIPPED / COMPLETED / CANCELED */
    private String status;
    /** 快递公司名称（发货后填写） */
    private String carrier;
    /** 快递单号（发货后填写） */
    private String trackingNo;
    /** 发货时间 */
    private LocalDateTime shippedAt;
    /** 创建时间（下单时间） */
    private LocalDateTime createdAt;

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getOrderNo(){return orderNo;} public void setOrderNo(String orderNo){this.orderNo=orderNo;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public String getReceiverName(){return receiverName;} public void setReceiverName(String receiverName){this.receiverName=receiverName;}
    public String getReceiverPhone(){return receiverPhone;} public void setReceiverPhone(String receiverPhone){this.receiverPhone=receiverPhone;}
    public String getReceiverAddress(){return receiverAddress;} public void setReceiverAddress(String receiverAddress){this.receiverAddress=receiverAddress;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal totalAmount){this.totalAmount=totalAmount;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    public String getCarrier(){return carrier;} public void setCarrier(String carrier){this.carrier=carrier;}
    public String getTrackingNo(){return trackingNo;} public void setTrackingNo(String trackingNo){this.trackingNo=trackingNo;}
    public LocalDateTime getShippedAt(){return shippedAt;} public void setShippedAt(LocalDateTime shippedAt){this.shippedAt=shippedAt;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
