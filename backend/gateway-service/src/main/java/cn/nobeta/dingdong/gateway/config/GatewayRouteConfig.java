package cn.nobeta.dingdong.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 路由配置 —— 登录链路中请求分发的路由规则
 * 将前端统一请求路径按前缀匹配路由到对应的微服务（先经 JwtGatewayFilter 认证拦截）
 */
@Configuration
public class GatewayRouteConfig {
    /**
     * 定义叮咚商城各微服务的路由映射
     * /api/auth/** → user-service（登录/注册接口由此进入）
     */
    @Bean
    RouteLocator dingdongRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务：认证（登录/注册）、用户信息、收货地址 → 8081
                .route("user-service", r -> r.path("/api/auth/**", "/api/users/**", "/api/addresses/**").uri(user))
                // 订单管理后台 → order-service:8083
                .route("order-admin-service", r -> r.path("/api/admin/orders/**").uri(order))
                // 商品服务：分类、品牌、商品、文件上传、管理后台 → 8082
                .route("product-service", r -> r.path("/api/categories/**", "/api/brands/**", "/api/products/**", "/api/files", "/api/admin/**").uri(product))
                // 订单服务：购物车、订单 → 8083
                .route("order-service", r -> r.path("/api/cart/**", "/api/orders/**").uri(order))
                // 支付服务 → 8084
                .route("pay-service", r -> r.path("/api/payments/**").uri(pay))
                .build();
    }
}
