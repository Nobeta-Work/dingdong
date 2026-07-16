package cn.nobeta.dingdong.user.mapper;

import cn.nobeta.dingdong.user.domain.MallUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("select id, username, password_hash, nickname, phone, email, avatar_url, role, status, created_at, updated_at from mall_user where username = #{username} and deleted = 0")
    MallUser findByUsername(String username);

    @Select("select id, username, password_hash, nickname, phone, email, avatar_url, role, status, created_at, updated_at from mall_user where id = #{id} and deleted = 0")
    MallUser findById(Long id);

    @Select("select count(1) from mall_user where phone = #{value} and deleted = 0")
    int countByPhone(String value);

    @Select("select count(1) from mall_user where email = #{value} and deleted = 0")
    int countByEmail(String value);

    @Insert("insert into mall_user(username, password_hash, nickname, phone, email, avatar_url, role, status) values(#{username}, #{passwordHash}, #{nickname}, #{phone}, #{email}, #{avatarUrl}, #{role}, #{status})")
    int insert(MallUser user);

    @Update("update mall_user set nickname=#{nickname}, phone=#{phone}, email=#{email}, avatar_url=#{avatarUrl} where id=#{id} and deleted=0")
    int updateProfile(MallUser user);
}
