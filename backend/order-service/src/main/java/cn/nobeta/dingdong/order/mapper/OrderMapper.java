package cn.nobeta.dingdong.order.mapper;
import cn.nobeta.dingdong.order.domain.*;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper
public interface OrderMapper {
 @Insert("insert into mall_order(order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status) values(#{orderNo},#{userId},#{receiverName},#{receiverPhone},#{receiverAddress},#{totalAmount},#{status})") @Options(useGeneratedKeys=true,keyProperty="id") int insertOrder(MallOrder order);
 @Insert("insert into order_item(order_id,sku_id,sku_code,product_title,product_image_url,spec_json,unit_price,quantity,total_amount) values(#{orderId},#{skuId},#{skuCode},#{productTitle},#{productImageUrl},#{specJson},#{unitPrice},#{quantity},#{totalAmount})") int insertItem(OrderItem item);
 @Select("select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at from mall_order where order_no=#{orderNo} and user_id=#{userId}") MallOrder findOwned(@Param("orderNo") String orderNo,@Param("userId") Long userId);
 @Select("select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at from mall_order where order_no=#{orderNo}") MallOrder findByOrderNo(String orderNo);
 @Select("select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at from mall_order where user_id=#{userId} order by id desc limit #{size} offset #{offset}") List<MallOrder> findPage(@Param("userId") Long userId,@Param("size") int size,@Param("offset") int offset);
 @Select("select count(1) from mall_order where user_id=#{userId}") long countByUserId(Long userId);
 @Select("select id,order_id,sku_id,sku_code,product_title,product_image_url,spec_json,unit_price,quantity,total_amount from order_item where order_id=#{orderId} order by id") List<OrderItem> findItems(Long orderId);
 @Update("update mall_order set status=#{nextStatus} where order_no=#{orderNo} and status=#{currentStatus}") int updateStatus(@Param("orderNo") String orderNo,@Param("currentStatus") String currentStatus,@Param("nextStatus") String nextStatus);
 @Update("update mall_order set status='SHIPPED', carrier=#{carrier}, tracking_no=#{trackingNo}, shipped_at=current_timestamp where order_no=#{orderNo} and status='PAID'") int ship(@Param("orderNo") String orderNo,@Param("carrier") String carrier,@Param("trackingNo") String trackingNo);
 @Insert("insert into order_status_log(order_id,from_status,to_status,operator_type,operator_id,remark) values(#{orderId},#{fromStatus},#{toStatus},#{operatorType},#{operatorId},#{remark})") int insertStatusLog(@Param("orderId") Long orderId,@Param("fromStatus") String fromStatus,@Param("toStatus") String toStatus,@Param("operatorType") String operatorType,@Param("operatorId") Long operatorId,@Param("remark") String remark);
}
