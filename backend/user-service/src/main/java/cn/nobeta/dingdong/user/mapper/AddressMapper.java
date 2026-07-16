package cn.nobeta.dingdong.user.mapper;

import cn.nobeta.dingdong.user.domain.UserAddress;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AddressMapper {
    @Select("select id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address from user_address where user_id=#{userId} and deleted=0 order by default_address desc, id desc")
    List<UserAddress> findByUserId(Long userId);
    @Select("select id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address from user_address where id=#{id} and user_id=#{userId} and deleted=0")
    UserAddress findOwned(@Param("id") Long id, @Param("userId") Long userId);
    @Insert("insert into user_address(user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address) values(#{userId},#{receiverName},#{receiverPhone},#{province},#{city},#{district},#{detailAddress},#{defaultAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAddress address);
    @Update("update user_address set receiver_name=#{receiverName}, receiver_phone=#{receiverPhone}, province=#{province}, city=#{city}, district=#{district}, detail_address=#{detailAddress}, default_address=#{defaultAddress} where id=#{id} and user_id=#{userId} and deleted=0")
    int update(UserAddress address);
    @Update("update user_address set default_address=0 where user_id=#{userId} and deleted=0")
    int clearDefault(Long userId);
    @Update("update user_address set deleted=1, default_address=0 where id=#{id} and user_id=#{userId} and deleted=0")
    int deleteOwned(@Param("id") Long id, @Param("userId") Long userId);
}
