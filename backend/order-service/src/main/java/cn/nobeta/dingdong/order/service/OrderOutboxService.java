package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.event.OrderTimeoutEvent;
import cn.nobeta.dingdong.order.domain.OrderOutbox;
import cn.nobeta.dingdong.order.mapper.OrderOutboxMapper;
import cn.nobeta.dingdong.order.mq.OrderTimeoutPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单 Outbox 事件服务
 * <p>实现可靠消息投递模式（Outbox Pattern）：将待发布的事件先持久化到数据库，
 * 再通过定时任务轮询发送，确保订单超时关闭事件不丢失。</p>
 * <ul>
 * <li>{@link #createTimeout} — 事务内创建待发送事件</li>
 * <li>{@link #publish} — 独立事务发布事件到 RocketMQ</li>
 * <li>{@link #retryPending} — 定时重试失败事件</li>
 * </ul>
 */
@Service public class OrderOutboxService {
    private final OrderOutboxMapper mapper;
    private final OrderTimeoutPublisher publisher;

    public OrderOutboxService(OrderOutboxMapper mapper, OrderTimeoutPublisher publisher){
        this.mapper=mapper;
        this.publisher=publisher;
    }

    /**
     * 创建订单超时事件记录（状态：PENDING）
     * <p>该方法通常在创建订单的同一事务中调用，确保订单与事件记录原子性写入。</p>
     * @param orderNo 订单号
     * @return 新创建的 Outbox 事件记录
     */
    @Transactional public OrderOutbox createTimeout(String orderNo){
        OrderOutbox event=new OrderOutbox();
        event.setEventType("ORDER_TIMEOUT");
        event.setOrderNo(orderNo);
        event.setStatus("PENDING");
        mapper.insert(event);
        return event;
    }

    /**
     * 发布 Outbox 事件到 RocketMQ（独立事务）
     * <p>使用 {@link Propagation#REQUIRES_NEW} 确保即使主事务回滚，已发送的事件仍可追踪重试。</p>
     * @param id Outbox 记录 ID
     */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void publish(Long id){
        OrderOutbox event=mapper.findById(id);
        // 校验事件存在且状态为待发送
        if(event==null||!"PENDING".equals(event.getStatus()))return;
        try{
            // 发布到 RocketMQ
            publisher.publish(new OrderTimeoutEvent(event.getOrderNo(),event.getCreatedAt()));
            // 标记为已发送
            mapper.markSent(id);
        }catch(RuntimeException exception){
            // 记录错误信息，等待定时任务重试
            mapper.markRetry(id,exception.getMessage());
        }
    }

    /**
     * 定时重试待发送事件（每 60 秒执行一次）
     * <p>通过 {@link Scheduled} 注解配置，默认延迟 60 秒，可通过 {@code app.outbox.retry-delay-ms} 覆盖。</p>
     */
    @Scheduled(fixedDelayString="${app.outbox.retry-delay-ms:60000}")
    public void retryPending(){
        // 每次最多处理 50 条待发送事件
        for(OrderOutbox event:mapper.findPending(50))
            publish(event.getId());
    }
}
