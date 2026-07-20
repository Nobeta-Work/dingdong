package cn.nobeta.dingdong.user.mapper;

import cn.nobeta.dingdong.user.domain.UserAddress;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AddressMapper {
    /**
     * 查询用户的收货地址列表 —— 用于地址列表展示
     * 仅返回未删除的记录（deleted=0），按默认地址优先、ID 倒序排列
     * @param userId 用户 ID
     * @return 该用户的所有有效地址列表
     */
    @Select("select id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address from user_address where user_id=#{userId} and deleted=0 order by default_address desc, id desc")
    List<UserAddress> findByUserId(Long userId);

    /**
     * 按 ID + 用户 ID 联合查询单条地址 —— 用于地址归属校验
     * 同时校验 id 和 userId，确保只能操作自己的地址，防止越权
     * @param id     地址主键
     * @param userId 用户 ID
     * @return 匹配的地址记录，不存在时返回 null
     */
    @Select("select id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address from user_address where id=#{id} and user_id=#{userId} and deleted=0")
    UserAddress findOwned(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 插入新的收货地址记录
     * 使用 useGeneratedKeys 自动回填自增主键 id
     */
    @Insert("insert into user_address(user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address) values(#{userId},#{receiverName},#{receiverPhone},#{province},#{city},#{district},#{detailAddress},#{defaultAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAddress address);

    /**
     * 更新收货地址信息（收件人、手机号、省市区、详细地址、是否默认）
     * 以 id + userId 为条件更新，确保只能修改自己的地址
     */
    @Update("update user_address set receiver_name=#{receiverName}, receiver_phone=#{receiverPhone}, province=#{province}, city=#{city}, district=#{district}, detail_address=#{detailAddress}, default_address=#{defaultAddress} where id=#{id} and user_id=#{userId} and deleted=0")
    int update(UserAddress address);

    /**
     * 清除用户的默认地址标记 —— 将指定用户所有地址的 default_address 置为 0
     * 在新增/修改默认地址时调用，确保一个用户只有一个默认地址
     * @param userId 用户 ID
     */
    @Update("update user_address set default_address=0 where user_id=#{userId} and deleted=0")
    int clearDefault(Long userId);

    /**
     * 逻辑删除地址 —— 设置 deleted=1 并清除默认标记
     * 以 id + userId 为条件，影响行数为 0 表示地址不存在或不属于当前用户
     * @param id     地址主键
     * @param userId 用户 ID
     * @return 影响的记录行数
     */
    @Update("update user_address set deleted=1, default_address=0 where id=#{id} and user_id=#{userId} and deleted=0")
    int deleteOwned(@Param("id") Long id, @Param("userId") Long userId);
}
