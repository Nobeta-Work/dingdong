package cn.nobeta.dingdong.pay.mapper;
import cn.nobeta.dingdong.pay.domain.PaymentOrder;
import org.apache.ibatis.annotations.*;
@Mapper public interface PaymentMapper {
 @Select("select id,payment_no,order_no,user_id,amount,channel,status,transaction_no,paid_at,created_at from payment_order where order_no=#{orderNo} and user_id=#{userId}") PaymentOrder findByOrder(@Param("orderNo") String orderNo,@Param("userId") Long userId);
 @Select("select id,payment_no,order_no,user_id,amount,channel,status,transaction_no,paid_at,created_at from payment_order where payment_no=#{paymentNo} and user_id=#{userId}") PaymentOrder findOwned(@Param("paymentNo") String paymentNo,@Param("userId") Long userId);
 @Insert("insert into payment_order(payment_no,order_no,user_id,amount,channel,status) values(#{paymentNo},#{orderNo},#{userId},#{amount},#{channel},#{status})") @Options(useGeneratedKeys=true,keyProperty="id") int insert(PaymentOrder payment);
 @Update("update payment_order set status='SUCCESS',transaction_no=#{transactionNo},paid_at=#{paidAt} where payment_no=#{paymentNo} and status='PENDING'") int markSuccess(@Param("paymentNo") String paymentNo,@Param("transactionNo") String transactionNo,@Param("paidAt") java.time.LocalDateTime paidAt);
 @Update("update payment_order set status='FAILED' where payment_no=#{paymentNo} and status='PENDING'") int markFailed(String paymentNo);
}
