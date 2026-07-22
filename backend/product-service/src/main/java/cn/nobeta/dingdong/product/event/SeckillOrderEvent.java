package cn.nobeta.dingdong.product.event;
import java.io.Serializable;
public record SeckillOrderEvent(String requestId, Long activityId, Long userId, Long skuId) implements Serializable { }
