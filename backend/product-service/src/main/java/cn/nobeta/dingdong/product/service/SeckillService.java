package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.api.SeckillRequests;
import cn.nobeta.dingdong.product.api.SeckillResponses;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.domain.SeckillActivity;
import cn.nobeta.dingdong.product.domain.SeckillOrder;
import cn.nobeta.dingdong.product.event.SeckillOrderEvent;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import cn.nobeta.dingdong.product.mapper.SeckillMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class SeckillService {
    public static final String TOPIC = "dingdong-seckill-order";
    private static final DefaultRedisScript<Long> PRE_DEDUCT = new DefaultRedisScript<>("""
        if redis.call('exists', KEYS[3]) == 1 then return -3 end
        if redis.call('exists', KEYS[2]) == 1 then return -2 end
        local stock=tonumber(redis.call('get',KEYS[1]) or '-1')
        if stock <= 0 then return -1 end
        local remain=redis.call('decr',KEYS[1])
        redis.call('set',KEYS[2],ARGV[1]); redis.call('set',KEYS[3],'PENDING')
        return remain
        """, Long.class);
    private static final DefaultRedisScript<Long> COMPENSATE = new DefaultRedisScript<>("""
        if redis.call('get',KEYS[3]) == 'PENDING' then
          redis.call('incr',KEYS[1]); redis.call('del',KEYS[2]); redis.call('set',KEYS[3],ARGV[1]); return 1
        end
        return 0
        """, Long.class);
    private final SeckillMapper mapper; private final ProductMapper productMapper;
    private final StringRedisTemplate redis; private final RocketMQTemplate rocketMQ;
    public SeckillService(SeckillMapper mapper, ProductMapper productMapper, StringRedisTemplate redis, RocketMQTemplate rocketMQ){this.mapper=mapper;this.productMapper=productMapper;this.redis=redis;this.rocketMQ=rocketMQ;}

    public List<SeckillActivity> active(){return mapper.findActiveActivities();}
    public List<SeckillActivity> all(){return mapper.findAllActivities();}
    public SeckillActivity requireActivity(Long id){SeckillActivity a=mapper.findActivity(id);if(a==null)throw new BusinessException("SECKILL_ACTIVITY_NOT_FOUND","秒杀活动不存在");return a;}

    public SeckillActivity create(SeckillRequests.CreateActivityRequest request){
        if(!request.endTime().isAfter(request.startTime()))throw new BusinessException("SECKILL_TIME_INVALID","结束时间必须晚于开始时间");
        InventorySkuView sku=productMapper.findInventorySku(request.skuId());
        if(sku==null||!Integer.valueOf(1).equals(sku.getStatus()))throw new BusinessException("PRODUCT_SKU_NOT_FOUND","SKU 不存在或已下架");
        if(request.totalStock()>sku.getAvailableStock())throw new BusinessException("SECKILL_STOCK_INVALID","活动库存不能超过当前可用库存");
        SeckillActivity a=new SeckillActivity();a.setName(request.name());a.setSkuId(request.skuId());a.setSeckillPrice(request.seckillPrice());
        a.setTotalStock(request.totalStock());a.setAvailableStock(request.totalStock());a.setStatus("DRAFT");a.setStartTime(request.startTime());a.setEndTime(request.endTime());
        mapper.insertActivity(a);return requireActivity(a.getId());
    }

    public SeckillActivity activate(Long id){requireActivity(id);warmup(id);mapper.activate(id);return requireActivity(id);}
    public SeckillActivity end(Long id){mapper.end(id);return requireActivity(id);}
    public void warmup(Long id){SeckillActivity a=requireActivity(id);String current=redis.opsForValue().get(stockKey(id));if(current!=null&&Long.parseLong(current)!=a.getAvailableStock())throw new BusinessException("SECKILL_MESSAGES_PENDING","库存尚未收敛，禁止重建 Redis 库存");redis.opsForValue().set(stockKey(id),String.valueOf(a.getAvailableStock()));expire(a,id);}

    public SeckillResponses.Accepted purchase(Long userId,Long activityId,String requestId){
        SeckillActivity a=requireActivity(activityId);LocalDateTime now=LocalDateTime.now();
        if(!"ACTIVE".equals(a.getStatus())||now.isBefore(a.getStartTime())||!now.isBefore(a.getEndTime()))throw new BusinessException("SECKILL_NOT_ACTIVE","秒杀活动未开始或已结束");
        if(Boolean.FALSE.equals(redis.hasKey(stockKey(activityId))))throw new BusinessException("SECKILL_NOT_WARMED","活动尚未完成 Redis 预热");
        Long result=redis.execute(PRE_DEDUCT,List.of(stockKey(activityId),userKey(activityId,userId),resultKey(requestId)),requestId);
        if(result==null)throw new BusinessException("SECKILL_REDIS_UNAVAILABLE","秒杀繁忙，请稍后重试");
        if(result==-3)return new SeckillResponses.Accepted(requestId,status(requestId),redisStock(activityId));
        if(result==-2)throw new BusinessException("SECKILL_DUPLICATE_USER","每位用户限购一件");
        if(result==-1)throw new BusinessException("SECKILL_SOLD_OUT","活动库存已售罄");
        expire(a,activityId);
        Duration ttl=Duration.ofSeconds(Math.max(Duration.between(LocalDateTime.now(),a.getEndTime().plusDays(1)).toSeconds(),3600));
        redis.expire(userKey(activityId,userId),ttl);redis.expire(resultKey(requestId),ttl);
        SeckillOrderEvent event=new SeckillOrderEvent(requestId,activityId,userId,a.getSkuId());
        try{rocketMQ.syncSend(TOPIC,event,3000);}catch(RuntimeException ex){compensate(event,"FAILED_MQ");throw new BusinessException("SECKILL_QUEUE_UNAVAILABLE","排队失败，库存已返还");}
        return new SeckillResponses.Accepted(requestId,"PENDING",result);
    }

    public SeckillResponses.Result result(Long userId,String requestId){SeckillOrder order=mapper.findOwnedOrder(requestId,userId);if(order!=null)return SeckillResponses.Result.success(order);return new SeckillResponses.Result(requestId,status(requestId),null);}
    public SeckillResponses.Consistency consistency(Long id){SeckillActivity a=requireActivity(id);long redisStock=redisStock(id);long success=mapper.countSuccess(id);long pending=Math.max(0,a.getAvailableStock()-redisStock);boolean ok=redisStock==a.getAvailableStock()&&a.getAvailableStock()==a.getTotalStock()-success;return new SeckillResponses.Consistency(id,a.getTotalStock(),redisStock,a.getAvailableStock(),success,pending,ok);}
    public void markSuccess(String requestId){redis.opsForValue().set(resultKey(requestId),"SUCCESS",Duration.ofDays(1));}
    public void compensate(SeckillOrderEvent e,String state){redis.execute(COMPENSATE,List.of(stockKey(e.activityId()),userKey(e.activityId(),e.userId()),resultKey(e.requestId())),state);}
    private String status(String requestId){String s=redis.opsForValue().get(resultKey(requestId));return s==null?"NOT_FOUND":s;}
    private long redisStock(Long id){String v=redis.opsForValue().get(stockKey(id));return v==null?-1:Long.parseLong(v);}
    private void expire(SeckillActivity a,Long id){long seconds=Math.max(Duration.between(LocalDateTime.now(),a.getEndTime().plusDays(1)).toSeconds(),3600);redis.expire(stockKey(id),Duration.ofSeconds(seconds));}
    private String stockKey(Long id){return "seckill:stock:"+id;} private String userKey(Long id,Long user){return "seckill:user:"+id+":"+user;} private String resultKey(String request){return "seckill:result:"+request;}
}
