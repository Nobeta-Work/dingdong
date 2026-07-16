package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
import cn.nobeta.dingdong.order.domain.OrderOutbox;
import cn.nobeta.dingdong.order.mapper.OrderOutboxMapper;
import cn.nobeta.dingdong.order.mq.OrderTimeoutPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Service public class OrderOutboxService {private final OrderOutboxMapper mapper;private final OrderTimeoutPublisher publisher;public OrderOutboxService(OrderOutboxMapper mapper,OrderTimeoutPublisher publisher){this.mapper=mapper;this.publisher=publisher;}
 @Transactional public OrderOutbox createTimeout(String orderNo){OrderOutbox event=new OrderOutbox();event.setEventType("ORDER_TIMEOUT");event.setOrderNo(orderNo);event.setStatus("PENDING");mapper.insert(event);return event;}
 @Transactional(propagation=Propagation.REQUIRES_NEW) public void publish(Long id){OrderOutbox event=mapper.findById(id);if(event==null||!"PENDING".equals(event.getStatus()))return;try{publisher.publish(new OrderTimeoutEvent(event.getOrderNo(),event.getCreatedAt()));mapper.markSent(id);}catch(RuntimeException exception){mapper.markRetry(id,exception.getMessage());}}
 @Scheduled(fixedDelayString="${app.outbox.retry-delay-ms:60000}") public void retryPending(){for(OrderOutbox event:mapper.findPending(50))publish(event.getId());}}
