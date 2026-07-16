package cn.nobeta.dingdong.order.security;
import cn.nobeta.dingdong.common.exception.BusinessException;
public final class OrderUserContext { private static final ThreadLocal<CurrentOrderUser> HOLDER=new ThreadLocal<>(); private OrderUserContext(){} public static void set(CurrentOrderUser user){HOLDER.set(user);} public static CurrentOrderUser require(){var user=HOLDER.get();if(user==null)throw new BusinessException("AUTH_UNAUTHORIZED","请先登录");return user;} public static void clear(){HOLDER.remove();} }
