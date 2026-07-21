package cn.nobeta.dingdong.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {
    @Bean
    RouteLocator dingdongRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", route -> route.path("/api/auth/**", "/api/users/**", "/api/addresses/**")
                        .uri("lb://user-service"))
                .route("order-admin-service", route -> route.path("/api/admin/orders/**", "/api/admin/dashboard/**")
                        .uri("lb://order-service"))
                .route("product-service", route -> route.path("/api/categories/**", "/api/brands/**", "/api/products/**", "/api/admin/**")
                        .uri("lb://product-service"))
                .route("order-service", route -> route.path("/api/cart/**", "/api/orders/**")
                        .uri("lb://order-service"))
                .route("pay-service", route -> route.path("/api/payments/**")
                        .uri("lb://pay-service"))
                .build();
    }
}
