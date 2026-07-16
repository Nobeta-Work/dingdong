package cn.nobeta.dingdong.pay.channel;
import cn.nobeta.dingdong.pay.domain.PaymentOrder;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component public class MockPaymentChannel implements PaymentChannel { public String code(){return "MOCK";} public ChannelResult pay(PaymentOrder payment,boolean success){return success?new ChannelResult(true,"MOCK-"+UUID.randomUUID(),"模拟支付成功"):new ChannelResult(false,null,"模拟支付失败");} }
