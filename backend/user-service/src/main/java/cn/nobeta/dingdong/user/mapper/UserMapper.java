package cn.nobeta.dingdong.user.mapper;

import cn.nobeta.dingdong.user.domain.MallUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    /**
     * 按用户名查询未删除的用户
     * 注册时用于校验用户名唯一性，以及插入后回查完整数据
     */
    @Select("select id, username, password_hash, nickname, phone, email, avatar_url, role, status, created_at, updated_at from mall_user where username = #{username} and deleted = 0")
    MallUser findByUsername(String username);

    @Select("select id, username, password_hash, nickname, phone, email, avatar_url, role, status, created_at, updated_at from mall_user where id = #{id} and deleted = 0")
    MallUser findById(Long id);

    /**
     * 统计未删除用户中指定手机号的数量（> 0 表示已占用）
     * 注册时用于校验手机号唯一性
     */
    @Select("select count(1) from mall_user where phone = #{value} and deleted = 0")
    int countByPhone(String value);

    /**
     * 统计未删除用户中指定邮箱的数量（> 0 表示已占用）
     * 注册时用于校验邮箱唯一性
     */
    @Select("select count(1) from mall_user where email = #{value} and deleted = 0")
    int countByEmail(String value);

    /**
     * 插入新用户记录
     * 注册时将构建好的用户实体持久化到 mall_user 表
     */
    @Insert("insert into mall_user(username, password_hash, nickname, phone, email, avatar_url, role, status) values(#{username}, #{passwordHash}, #{nickname}, #{phone}, #{email}, #{avatarUrl}, #{role}, #{status})")
    int insert(MallUser user);

    @Update("update mall_user set nickname=#{nickname}, phone=#{phone}, email=#{email}, avatar_url=#{avatarUrl} where id=#{id} and deleted=0")
    int updateProfile(MallUser user);
}
