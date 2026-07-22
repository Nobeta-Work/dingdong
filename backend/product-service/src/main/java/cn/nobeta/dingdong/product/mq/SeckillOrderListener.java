package cn.nobeta.dingdong.product.mq;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.event.SeckillOrderEvent;
import cn.nobeta.dingdong.product.service.SeckillPersistenceService;
import cn.nobeta.dingdong.product.service.SeckillService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic=SeckillService.TOPIC,consumerGroup="dingdong-seckill-persist-consumer")
public class SeckillOrderListener implements RocketMQListener<SeckillOrderEvent> {
    private final SeckillPersistenceService persistence; private final SeckillService service;
    public SeckillOrderListener(SeckillPersistenceService persistence,SeckillService service){this.persistence=persistence;this.service=service;}
    @Override public void onMessage(SeckillOrderEvent event){
        try{persistence.persist(event);service.markSuccess(event.requestId());}
        catch(BusinessException ex){service.compensate(event,"FAILED_DB");}
    }
}
