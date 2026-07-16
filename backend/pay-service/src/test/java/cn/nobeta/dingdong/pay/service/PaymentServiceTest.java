package cn.nobeta.dingdong.pay.service;
import cn.nobeta.dingdong.common.event.PaymentSuccessEvent;
import cn.nobeta.dingdong.pay.channel.PaymentChannel;
import cn.nobeta.dingdong.pay.domain.PaymentOrder;
import cn.nobeta.dingdong.pay.mapper.PaymentMapper;
import cn.nobeta.dingdong.pay.mq.PaymentEventPublisher;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.*;import static org.mockito.Mockito.*;
class PaymentServiceTest {
 @Test void simulatedSuccessMarksPaymentAndPublishesEvent(){
  PaymentMapper mapper=mock(PaymentMapper.class);PaymentChannel channel=mock(PaymentChannel.class);PaymentEventPublisher publisher=mock(PaymentEventPublisher.class);PaymentOrder payment=new PaymentOrder();payment.setPaymentNo("PAY1");payment.setOrderNo("DD1");payment.setUserId(1L);payment.setAmount(new BigDecimal("19.90"));payment.setStatus("PENDING");when(mapper.findOwned("PAY1",1L)).thenReturn(payment);when(channel.pay(payment,true)).thenReturn(new PaymentChannel.ChannelResult(true,"MOCK-1","ok"));doAnswer(i->{payment.setStatus("SUCCESS");payment.setTransactionNo(i.getArgument(1));payment.setPaidAt(i.getArgument(2));return 1;}).when(mapper).markSuccess(eq("PAY1"),anyString(),any(LocalDateTime.class));
  PaymentOrder result=new PaymentService(mapper,channel,publisher).simulate(1L,"PAY1",true);
  assertEquals("SUCCESS",result.getStatus());verify(publisher).publishSuccess(argThat(e->e instanceof PaymentSuccessEvent event&&event.orderNo().equals("DD1")&&event.amount().equals(new BigDecimal("19.90"))));
 }
 @Test void simulatedFailureDoesNotPublishEvent(){
  PaymentMapper mapper=mock(PaymentMapper.class);PaymentChannel channel=mock(PaymentChannel.class);PaymentEventPublisher publisher=mock(PaymentEventPublisher.class);PaymentOrder payment=new PaymentOrder();payment.setPaymentNo("PAY1");payment.setStatus("PENDING");when(mapper.findOwned("PAY1",1L)).thenReturn(payment);when(channel.pay(payment,false)).thenReturn(new PaymentChannel.ChannelResult(false,null,"failed"));
  new PaymentService(mapper,channel,publisher).simulate(1L,"PAY1",false);
  verify(mapper).markFailed("PAY1");verifyNoInteractions(publisher);
 }
}
