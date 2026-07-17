package cn.nobeta.dingdong.user.domain;

import java.time.LocalDateTime;

/**
 * 商城用户领域实体，映射 mall_user 表 —— 登录链路中的核心数据载体
 * 登录时通过 username 查库获得此实体，其中 passwordHash 用于 BCrypt 密码比对，
 * id / username / role 用于构造 JWT Payload，status 用于账号禁用校验。
 * 注册时至少需要 username、passwordHash、nickname，phone 和 email 为可选但全局唯一。
 */
public class MallUser {
    private Long id;
    /** 用户名，全局唯一标识，注册时必须提供，4-32 位字母数字或下划线 */
    private String username;
    /** BCrypt 加密后的密码哈希值，注册时由服务端生成 */
    private String passwordHash;
    /** 用户昵称，注册时必填，展示用 */
    private String nickname;
    /** 手机号，可选但全局唯一，注册后可用于登录或找回密码 */
    private String phone;
    /** 邮箱，可选但全局唯一，注册后可用于登录或找回密码 */
    private String email;
    /** 头像 URL，注册时默认为空 */
    private String avatarUrl;
    /** 用户角色，注册默认赋值为 "USER" */
    private String role;
    /** 账号状态：1 - 正常，0 - 禁用，注册默认赋值为 1 */
    private Integer status;
    /** 记录创建时间，数据库自动填充 */
    private LocalDateTime createdAt;
    /** 记录最近更新时间，数据库自动填充 */
    private LocalDateTime updatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
