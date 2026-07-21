package cn.nobeta.dingdong.pay.security;

/**
 * 当前支付服务用户记录 —— 登录链路中 JWT 解析后的支付服务用户载体
 * 由 PayJwtFilter 解析 token 后从 JWT Payload 中提取构建，
 * 存入 PayUserContext，供支付相关业务方法通过 require() 获取当前用户身份
 *
 * @param id   用户唯一标识（对应 mall_user.id），支付时必须校验是否为订单所属买家
 * @param role 用户角色（USER / ADMIN），用于支付操作权限判断
 */
public record CurrentPayUser(Long id, String role) {}
