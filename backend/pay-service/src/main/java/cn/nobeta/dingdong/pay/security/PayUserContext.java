package cn.nobeta.dingdong.pay.security;

import cn.nobeta.dingdong.common.exception.BusinessException;

/**
 * 支付服务用户上下文 —— 基于 ThreadLocal 的请求级用户信息容器
 * 登录链路的后续请求中，PayJwtFilter 解析 token 后将 CurrentPayUser 存入此上下文，
 * 支付相关业务方法通过 require() 安全获取当前用户，无需逐层透传用户信息。
 * 每个请求结束时由 Filter 的 finally 块调用 clear() 防止内存泄漏。
 */
public final class PayUserContext {
    /** ThreadLocal 存储当前请求的支付用户，保证同一线程内任意位置可获取 */
    private static final ThreadLocal<CurrentPayUser> H = new ThreadLocal<>();

    private PayUserContext() {}

    /** 由 PayJwtFilter 在认证成功后调用，将用户信息绑定到当前线程 */
    public static void set(CurrentPayUser u) { H.set(u); }

    /**
     * 获取当前请求的登录用户（支付操作需校验所属买家身份）
     * @return CurrentPayUser
     * @throws BusinessException 当前请求未携带有效 token 时抛出 AUTH_UNAUTHORIZED
     */
    public static CurrentPayUser require() {
        var u = H.get();
        if (u == null) throw new BusinessException("AUTH_UNAUTHORIZED", "请先登录");
        return u;
    }

    /** 请求结束时清理 ThreadLocal，防止线程复用时数据串扰和内存泄漏 */
    public static void clear() { H.remove(); }
}
