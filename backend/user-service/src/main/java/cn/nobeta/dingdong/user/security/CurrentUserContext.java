package cn.nobeta.dingdong.user.security;

import cn.nobeta.dingdong.common.exception.BusinessException;

public final class CurrentUserContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();
    private CurrentUserContext() { }
    public static void set(CurrentUser user) { HOLDER.set(user); }
    public static CurrentUser require() {
        CurrentUser currentUser = HOLDER.get();
        if (currentUser == null) throw new BusinessException("AUTH_UNAUTHORIZED", "请先登录");
        return currentUser;
    }
    public static void clear() { HOLDER.remove(); }
}
