package cn.nobeta.dingdong.user.security;

import cn.nobeta.dingdong.common.exception.BusinessException;

/**
 * 当前用户上下文 —— 基于 ThreadLocal 的请求级用户信息容器
 * 登录链路的后续请求中，JwtAuthenticationFilter 解析 token 后将 CurrentUser 存入此上下文，
 * 业务方法通过 require() 安全获取当前用户，无需逐层透传用户信息。
 * 每个请求结束时由 Filter 的 finally 块调用 clear() 防止内存泄漏。
 */
public final class CurrentUserContext {
    /** ThreadLocal 存储当前请求的用户，保证同一线程内任意位置可获取 */
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();
    private CurrentUserContext() { }

    /** 由 JwtAuthenticationFilter 在认证成功后调用，将用户信息绑定到当前线程 */
    public static void set(CurrentUser user) { HOLDER.set(user); }

    /**
     * 获取当前请求的登录用户
     * @return CurrentUser
     * @throws BusinessException 当前请求未携带有效 token 时抛出 AUTH_UNAUTHORIZED
     */
    public static CurrentUser require() {
        CurrentUser currentUser = HOLDER.get();
        if (currentUser == null) throw new BusinessException("AUTH_UNAUTHORIZED", "请先登录");
        return currentUser;
    }

    /** 请求结束时清理 ThreadLocal，防止线程复用时数据串扰和内存泄漏 */
    public static void clear() { HOLDER.remove(); }
}
