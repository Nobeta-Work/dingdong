package cn.nobeta.dingdong.order.rpc;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.OrderPaymentFacade;
import cn.nobeta.dingdong.order.domain.MallOrder;
import cn.nobeta.dingdong.order.service.OrderService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service @DubboService
public class OrderPaymentFacadeImpl implements OrderPaymentFacade {
 private final OrderService orderService;public OrderPaymentFacadeImpl(OrderService orderService){this.orderService=orderService;}
 @Override public PayableOrder getPayableOrder(String orderNo,Long userId){MallOrder order=orderService.get(userId,orderNo);if(!"PENDING_PAYMENT".equals(order.getStatus()))throw new BusinessException("ORDER_STATUS_INVALID","当前订单不可支付");return new PayableOrder(order.getOrderNo(),order.getUserId(),order.getTotalAmount(),order.getStatus());}
 @Override public void markPaid(String orderNo,String paymentNo,LocalDateTime paidAt){orderService.markPaid(orderNo,paymentNo,paidAt);}
}
