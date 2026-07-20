package cn.nobeta.dingdong.user.mapper;

import cn.nobeta.dingdong.user.domain.MallUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    /**
     * 按用户名查询未删除的用户 —— 登录链路数据库查询入口
     * 登录时通过此方法根据用户名获取完整用户信息（含 BCrypt 密码哈希），
     * 再由 UserService 完成密码比对。条件 deleted=0 确保逻辑删除的用户不可登录。
     */
    @Select("select id, username, password_hash, nickname, phone, email, avatar_url, role, status, created_at, updated_at from mall_user where username = #{username} and deleted = 0")
    MallUser findByUsername(String username);

    /**
     * 按用户 ID 查询未删除用户 —— 用于 token 校验时确认用户仍存在且未被禁用，以及用户资料查询/修改时加载用户信息
     */
    @Select("select id, username, password_hash, nickname, phone, email, avatar_url, role, status, created_at, updated_at from mall_user where id = #{id} and deleted = 0")
    MallUser findById(Long id);

    /**
     * 统计未删除用户中指定手机号的数量（> 0 表示已占用）
     * 注册时用于校验手机号唯一性，修改资料时用于校验新手机号是否被其他用户占用
     */
    @Select("select count(1) from mall_user where phone = #{value} and deleted = 0")
    int countByPhone(String value);

    /**
     * 统计未删除用户中指定邮箱的数量（> 0 表示已占用）
     * 注册时用于校验邮箱唯一性，修改资料时用于校验新邮箱是否被其他用户占用
     */
    @Select("select count(1) from mall_user where email = #{value} and deleted = 0")
    int countByEmail(String value);

    /**
     * 插入新用户记录
     * 注册时将构建好的用户实体持久化到 mall_user 表
     */
    @Insert("insert into mall_user(username, password_hash, nickname, phone, email, avatar_url, role, status) values(#{username}, #{passwordHash}, #{nickname}, #{phone}, #{email}, #{avatarUrl}, #{role}, #{status})")
    int insert(MallUser user);

    /**
     * 更新用户个人资料（昵称、手机号、邮箱、头像 URL）
     * 以用户 ID 为条件更新，仅作用于未删除的记录（deleted=0）
     * 由 UserService.updateProfile() 在完成唯一性校验后调用
     */
    @Update("update mall_user set nickname=#{nickname}, phone=#{phone}, email=#{email}, avatar_url=#{avatarUrl} where id=#{id} and deleted=0")
    int updateProfile(MallUser user);
}
