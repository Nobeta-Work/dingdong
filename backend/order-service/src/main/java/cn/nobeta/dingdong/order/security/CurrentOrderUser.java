package cn.nobeta.dingdong.order.security;

/**
 * 当前订单服务用户记录
 * 由 JWT 认证过滤器解析 Token 后构建，包含用户 ID 和角色信息。
 * @param id 用户 ID
 * @param role 用户角色（USER / ADMIN）
 */
public record CurrentOrderUser(Long id, String role) { }
