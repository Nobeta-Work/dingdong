package cn.nobeta.dingdong.order.mapper;
import cn.nobeta.dingdong.order.domain.CartItem;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper
public interface CartMapper {
 @Select("select id,user_id,sku_id,quantity,selected,created_at from cart_item where user_id=#{userId} and deleted=0 order by id desc") List<CartItem> findByUserId(Long userId);
 @Select("select id,user_id,sku_id,quantity,selected,created_at from cart_item where id=#{id} and user_id=#{userId} and deleted=0") CartItem findOwned(@Param("id") Long id,@Param("userId") Long userId);
 @Select("select id,user_id,sku_id,quantity,selected,created_at from cart_item where user_id=#{userId} and sku_id=#{skuId} and deleted=0") CartItem findBySku(@Param("userId") Long userId,@Param("skuId") Long skuId);
 @Insert("insert into cart_item(user_id,sku_id,quantity,selected) values(#{userId},#{skuId},#{quantity},#{selected})") @Options(useGeneratedKeys=true,keyProperty="id") int insert(CartItem item);
 @Update("update cart_item set quantity=#{quantity},selected=#{selected} where id=#{id} and user_id=#{userId} and deleted=0") int update(CartItem item);
 @Update("update cart_item set deleted=1 where id=#{id} and user_id=#{userId} and deleted=0") int deleteOwned(@Param("id") Long id,@Param("userId") Long userId);
 @Update("<script>update cart_item set deleted=1 where user_id=#{userId} and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>") int deleteBatch(@Param("userId") Long userId,@Param("ids") List<Long> ids);
}
