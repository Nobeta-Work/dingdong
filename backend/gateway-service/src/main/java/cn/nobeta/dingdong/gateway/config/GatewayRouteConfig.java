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
                .route("user-service", route -> route.path("/api/auth/**", "/api/users/**", "/api/addresses/**")
                        .uri("lb://user-service"))
                .route("user-admin-service", route -> route.path("/api/admin/users/**")
                        .uri("lb://user-service"))
                .route("order-admin-service", route -> route.path("/api/admin/orders/**", "/api/admin/dashboard/**")
                        .uri("lb://order-service"))
                .route("product-service", route -> route.path("/api/categories/**", "/api/brands/**", "/api/products/**",
                                "/api/seckill/**", "/api/files", "/api/files/**", "/api/admin/**")
                        .uri("lb://product-service"))
                .route("order-service", route -> route.path("/api/cart/**", "/api/orders/**")
                        .uri("lb://order-service"))
                .route("pay-service", route -> route.path("/api/payments/**")
                        .uri("lb://pay-service"))
                .build();
    }
}
