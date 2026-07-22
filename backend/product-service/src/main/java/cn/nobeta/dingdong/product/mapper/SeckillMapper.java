package cn.nobeta.dingdong.product.mapper;

import cn.nobeta.dingdong.product.domain.SeckillActivity;
import cn.nobeta.dingdong.product.domain.SeckillOrder;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SeckillMapper {
    @Insert("insert into seckill_activity(name,sku_id,seckill_price,total_stock,available_stock,status,start_time,end_time) values(#{name},#{skuId},#{seckillPrice},#{totalStock},#{totalStock},#{status},#{startTime},#{endTime})")
    @Options(useGeneratedKeys=true,keyProperty="id") int insertActivity(SeckillActivity activity);
    @Select("select id,name,sku_id,seckill_price,total_stock,available_stock,status,start_time,end_time,created_at,updated_at from seckill_activity where id=#{id}")
    SeckillActivity findActivity(Long id);
    @Select("select id,name,sku_id,seckill_price,total_stock,available_stock,status,start_time,end_time,created_at,updated_at from seckill_activity order by id desc")
    List<SeckillActivity> findAllActivities();
    @Select("select id,name,sku_id,seckill_price,total_stock,available_stock,status,start_time,end_time,created_at,updated_at from seckill_activity where status='ACTIVE' and start_time&lt;=current_timestamp and end_time&gt;current_timestamp order by start_time,id")
    List<SeckillActivity> findActiveActivities();
    @Update("update seckill_activity set status='ACTIVE',version=version+1 where id=#{id} and status in ('DRAFT','ACTIVE')") int activate(Long id);
    @Update("update seckill_activity set status='ENDED',version=version+1 where id=#{id} and status='ACTIVE'") int end(Long id);
    @Update("update seckill_activity set available_stock=available_stock-1,version=version+1 where id=#{id} and available_stock&gt;0") int decreaseDbStock(Long id);
    @Insert("insert into seckill_order(request_id,activity_id,user_id,sku_id,quantity,seckill_price,status) values(#{requestId},#{activityId},#{userId},#{skuId},1,#{seckillPrice},'SUCCESS')") int insertOrder(SeckillOrder order);
    @Select("select id,request_id,activity_id,user_id,sku_id,quantity,seckill_price,status,created_at from seckill_order where request_id=#{requestId}") SeckillOrder findOrderByRequest(String requestId);
    @Select("select id,request_id,activity_id,user_id,sku_id,quantity,seckill_price,status,created_at from seckill_order where request_id=#{requestId} and user_id=#{userId}") SeckillOrder findOwnedOrder(@Param("requestId") String requestId,@Param("userId") Long userId);
    @Select("select count(1) from seckill_order where activity_id=#{activityId} and status='SUCCESS'") long countSuccess(Long activityId);
}
