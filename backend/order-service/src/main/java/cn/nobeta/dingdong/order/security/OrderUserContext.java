package cn.nobeta.dingdong.order.security;
import cn.nobeta.dingdong.common.exception.BusinessException;

/**
 * 订单服务用户上下文工具类
 * 使用 {@link ThreadLocal} 存储当前请求的认证用户信息，由 {@link OrderJwtFilter} 在过滤器中设置。
 * 同一请求线程内可通过 {@link #require()} 安全获取当前用户。
 */
public final class OrderUserContext {
    /** 线程本地变量，存储当前用户信息 */
    private static final ThreadLocal<CurrentOrderUser> HOLDER = new ThreadLocal<>();

    private OrderUserContext() {}

    /** 设置当前用户（过滤器调用） */
    public static void set(CurrentOrderUser user) { HOLDER.set(user); }

    /**
     * 获取当前用户，不存在时抛出业务异常
     * @return 当前认证用户
     * @throws BusinessException 用户未登录时抛出
     */
    public static CurrentOrderUser require() {
        var user = HOLDER.get();
        if (user == null) throw new BusinessException("AUTH_UNAUTHORIZED", "请先登录");
        return user;
    }

    /** 清理当前用户上下文（请求结束时调用） */
    public static void clear() { HOLDER.remove(); }
}
