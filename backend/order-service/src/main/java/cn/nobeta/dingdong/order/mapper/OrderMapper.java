package cn.nobeta.dingdong.order.mapper;
import cn.nobeta.dingdong.order.domain.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 订单数据访问层（MyBatis Mapper）
 * 提供订单主表、订单项、状态日志的 CRUD 操作。
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单主表记录
     * @param order 订单实体（插入后返回生成的主键 ID）
     * @return 影响行数
     */
    @Insert("insert into mall_order(order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status) values(#{orderNo},#{userId},#{receiverName},#{receiverPhone},#{receiverAddress},#{totalAmount},#{status})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insertOrder(MallOrder order);

    /**
     * 插入订单项记录
     * @param item 订单项实体
     * @return 影响行数
     */
    @Insert("insert into order_item(order_id,sku_id,sku_code,product_title,product_image_url,spec_json,unit_price,quantity,total_amount) values(#{orderId},#{skuId},#{skuCode},#{productTitle},#{productImageUrl},#{specJson},#{unitPrice},#{quantity},#{totalAmount})")
    int insertItem(OrderItem item);

    /**
     * 查询用户拥有的指定订单号订单
        * 该查询用于订单详情链路，必须同时满足订单号匹配和用户归属校验，避免越权读取。
     * @param orderNo 订单号
     * @param userId 用户 ID（权限校验）
     * @return 订单实体，不存在返回 null
     */
    @Select("select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at from mall_order where order_no=#{orderNo} and user_id=#{userId}")
    MallOrder findOwned(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    /**
     * 根据订单号查询订单（无权限校验，内部使用）
     * @param orderNo 订单号
     * @return 订单实体，不存在返回 null
     */
    @Select("select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at from mall_order where order_no=#{orderNo}")
    MallOrder findByOrderNo(String orderNo);

    /**
     * 分页查询用户订单
     * @param userId 用户 ID
     * @param size 每页大小
     * @param offset 偏移量
     * @return 订单列表
     */
    @Select("select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at from mall_order where user_id=#{userId} order by id desc limit #{size} offset #{offset}")
    List<MallOrder> findPage(@Param("userId") Long userId, @Param("size") int size, @Param("offset") int offset);

    /**
     * 统计用户订单总数
     * @param userId 用户 ID
     * @return 订单数量
     */
    @Select("select count(1) from mall_order where user_id=#{userId}")
    long countByUserId(Long userId);

    @Select("""
      <script>select id,order_no,user_id,receiver_name,receiver_phone,receiver_address,total_amount,status,carrier,tracking_no,shipped_at,created_at
      from mall_order where 1=1
      <if test='orderNo != null and !orderNo.isBlank()'>and order_no like concat('%',#{orderNo},'%')</if>
      <if test='userId != null'>and user_id=#{userId}</if>
      <if test='status != null and !status.isBlank()'>and status=#{status}</if>
      order by id desc limit #{size} offset #{offset}</script>
      """)
    List<MallOrder> findAdminPage(@Param("orderNo") String orderNo,@Param("userId") Long userId,@Param("status") String status,@Param("size") int size,@Param("offset") int offset);

    @Select("""
      <script>select count(1) from mall_order where 1=1
      <if test='orderNo != null and !orderNo.isBlank()'>and order_no like concat('%',#{orderNo},'%')</if>
      <if test='userId != null'>and user_id=#{userId}</if>
      <if test='status != null and !status.isBlank()'>and status=#{status}</if></script>
      """)
    long countAdmin(@Param("orderNo") String orderNo,@Param("userId") Long userId,@Param("status") String status);

    @Select("select count(1) from mall_order where created_at >= current_date")
    long countTodayOrders();

    @Select("select coalesce(sum(total_amount),0) from mall_order where created_at >= current_date and status in ('PAID','SHIPPED','COMPLETED')")
    java.math.BigDecimal sumTodayPaidAmount();

    @Select("select count(1) from mall_order where status='PAID'")
    long countPendingShipment();

    @Select("""
      select i.sku_id,i.product_title,max(i.product_image_url) as product_image_url,
      sum(i.quantity) as quantity,sum(i.total_amount) as sales_amount
      from order_item i join mall_order o on o.id=i.order_id
      where o.status in ('PAID','SHIPPED','COMPLETED')
      group by i.sku_id,i.product_title order by quantity desc,i.sku_id desc limit #{limit}
      """)
    List<TopProductStat> findTopProducts(int limit);

    /**
     * 查询订单的所有订单项
        * 订单详情页会根据主订单 ID 补充商品明细，这里返回按 ID 排序的订单项集合。
     * @param orderId 订单 ID
     * @return 订单项列表
     */
    @Select("select id,order_id,sku_id,sku_code,product_title,product_image_url,spec_json,unit_price,quantity,total_amount from order_item where order_id=#{orderId} order by id")
    List<OrderItem> findItems(Long orderId);

    /**
     * 乐观锁更新订单状态
     * @param orderNo 订单号
     * @param currentStatus 当前状态（CAS 条件）
     * @param nextStatus 目标状态
     * @return 影响行数（0 表示状态不匹配）
     */
    @Update("update mall_order set status=#{nextStatus} where order_no=#{orderNo} and status=#{currentStatus}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("currentStatus") String currentStatus, @Param("nextStatus") String nextStatus);

    /**
     * 发货操作：更新订单状态为 SHIPPED，并记录物流信息
     * @param orderNo 订单号
     * @param carrier 快递公司名称
     * @param trackingNo 快递单号
     * @return 影响行数（0 表示订单状态不是 PAID）
     */
    @Update("update mall_order set status='SHIPPED', carrier=#{carrier}, tracking_no=#{trackingNo}, shipped_at=current_timestamp where order_no=#{orderNo} and status='PAID'")
    int ship(@Param("orderNo") String orderNo, @Param("carrier") String carrier, @Param("trackingNo") String trackingNo);

    /**
     * 插入订单状态变更日志
     * @param orderId 订单 ID
     * @param fromStatus 变更前状态
     * @param toStatus 变更后状态
     * @param operatorType 操作者类型：USER / ADMIN / SYSTEM / PAYMENT
     * @param operatorId 操作者 ID
     * @param remark 备注信息
     * @return 影响行数
     */
    @Insert("insert into order_status_log(order_id,from_status,to_status,operator_type,operator_id,remark) values(#{orderId},#{fromStatus},#{toStatus},#{operatorType},#{operatorId},#{remark})")
    int insertStatusLog(@Param("orderId") Long orderId, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus, @Param("operatorType") String operatorType, @Param("operatorId") Long operatorId, @Param("remark") String remark);
}
