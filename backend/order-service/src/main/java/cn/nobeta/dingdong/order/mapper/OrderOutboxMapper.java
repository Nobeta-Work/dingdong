package cn.nobeta.dingdong.order.mapper;
import cn.nobeta.dingdong.order.domain.OrderOutbox;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * Outbox 事件数据访问层（MyBatis Mapper）
 * 管理 order_outbox 表的读写操作，支持事件的持久化、发送标记和重试更新。
 */
@Mapper public interface OrderOutboxMapper {

    /**
     * 插入一条待发送的事件记录
     * @param event 事件实体（插入后返回生成的主键 ID）
     * @return 影响行数
     */
    @Insert("insert into order_outbox(event_type,order_no,status,retry_count) values(#{eventType},#{orderNo},#{status},0)")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insert(OrderOutbox event);

    /** 根据 ID 查询事件记录 */
    @Select("select id,event_type,order_no,status,retry_count,created_at from order_outbox where id=#{id}")
    OrderOutbox findById(Long id);

    /** 查询待发送的事件列表（上限 ${limit} 条） */
    @Select("select id,event_type,order_no,status,retry_count,created_at from order_outbox where status='PENDING' order by id limit #{limit}")
    List<OrderOutbox> findPending(int limit);

    /** 标记事件为已发送（CAS 条件：status = PENDING） */
    @Update("update order_outbox set status='SENT',sent_at=current_timestamp,last_error=null where id=#{id} and status='PENDING'")
    int markSent(Long id);

    /** 发送失败时更新重试次数和错误信息 */
    @Update("update order_outbox set retry_count=retry_count+1,last_error=#{error} where id=#{id} and status='PENDING'")
    int markRetry(@Param("id") Long id, @Param("error") String error);
}
