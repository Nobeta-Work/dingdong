package cn.nobeta.dingdong.order.mapper;
import cn.nobeta.dingdong.order.domain.CartItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 购物车数据访问层（MyBatis Mapper）
 * 提供购物车项的增删改查操作，支持批量删除（软删除）机制。
 */
@Mapper
public interface CartMapper {

    /** 查询用户所有购物车项（按 ID 倒序，排除已删除项） */
    @Select("select id,user_id,sku_id,quantity,selected,created_at from cart_item where user_id=#{userId} and deleted=0 order by id desc")
    List<CartItem> findByUserId(Long userId);

    /** 查询指定 ID 的购物车项（需属于当前用户且未删除） */
    @Select("select id,user_id,sku_id,quantity,selected,created_at from cart_item where id=#{id} and user_id=#{userId} and deleted=0")
    CartItem findOwned(@Param("id") Long id, @Param("userId") Long userId);

    /** 查询用户指定 SKU 的购物车项（用于合并数量） */
    @Select("select id,user_id,sku_id,quantity,selected,created_at from cart_item where user_id=#{userId} and sku_id=#{skuId} and deleted=0")
    CartItem findBySku(@Param("userId") Long userId, @Param("skuId") Long skuId);

    /** 插入新的购物车项 */
    @Insert("insert into cart_item(user_id,sku_id,quantity,selected) values(#{userId},#{skuId},#{quantity},#{selected})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insert(CartItem item);

    /** 更新购物车项数量与选中状态 */
    @Update("update cart_item set quantity=#{quantity},selected=#{selected} where id=#{id} and user_id=#{userId} and deleted=0")
    int update(CartItem item);

    /** 删除单个购物车项（软删除） */
    @Update("update cart_item set deleted=1 where id=#{id} and user_id=#{userId} and deleted=0")
    int deleteOwned(@Param("id") Long id, @Param("userId") Long userId);

    /** 批量删除购物车项（软删除） */
    @Update("<script>update cart_item set deleted=1 where user_id=#{userId} and id in <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteBatch(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
