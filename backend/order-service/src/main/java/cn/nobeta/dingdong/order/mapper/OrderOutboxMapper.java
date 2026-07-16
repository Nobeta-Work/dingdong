package cn.nobeta.dingdong.order.mapper;
import cn.nobeta.dingdong.order.domain.OrderOutbox;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper public interface OrderOutboxMapper {
 @Insert("insert into order_outbox(event_type,order_no,status,retry_count) values(#{eventType},#{orderNo},#{status},0)") @Options(useGeneratedKeys=true,keyProperty="id") int insert(OrderOutbox event);
 @Select("select id,event_type,order_no,status,retry_count,created_at from order_outbox where id=#{id}") OrderOutbox findById(Long id);
 @Select("select id,event_type,order_no,status,retry_count,created_at from order_outbox where status='PENDING' order by id limit #{limit}") List<OrderOutbox> findPending(int limit);
 @Update("update order_outbox set status='SENT',sent_at=current_timestamp,last_error=null where id=#{id} and status='PENDING'") int markSent(Long id);
 @Update("update order_outbox set retry_count=retry_count+1,last_error=#{error} where id=#{id} and status='PENDING'") int markRetry(@Param("id") Long id,@Param("error") String error);
}
