package cn.nobeta.dingdong.user.security;

/**
 * 当前登录用户信息记录 —— 登录链路中 JWT 解析后的内存载体
 * 由 JwtTokenService.create() 构造并编码进 JWT Payload，
 * 后续由 JwtTokenService.parse() 或 JwtGatewayFilter 解码还原
 *
 * @param id       用户唯一标识（对应 mall_user.id）
 * @param username 用户名
 * @param role     角色（USER / ADMIN），用于权限判断
 */
public record CurrentUser(Long id, String username, String role) { }
