package cn.nobeta.dingdong.product.security;
import cn.nobeta.dingdong.common.exception.BusinessException;
public final class ProductUserContext {
    private static final ThreadLocal<CurrentProductUser> HOLDER = new ThreadLocal<>();
    private ProductUserContext() { }
    public static void set(CurrentProductUser user){HOLDER.set(user);}
    public static CurrentProductUser require(){CurrentProductUser user=HOLDER.get();if(user==null)throw new BusinessException("AUTH_UNAUTHORIZED","请先登录");return user;}
    public static void clear(){HOLDER.remove();}
}
