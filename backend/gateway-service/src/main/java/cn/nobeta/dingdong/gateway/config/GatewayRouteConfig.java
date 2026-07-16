package cn.nobeta.dingdong.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {
    @Bean
    RouteLocator dingdongRoutes(RouteLocatorBuilder builder,
                                @Value("${app.routes.user:http://127.0.0.1:8081}") String user,
                                @Value("${app.routes.product:http://127.0.0.1:8082}") String product,
                                @Value("${app.routes.order:http://127.0.0.1:8083}") String order) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/auth/**", "/api/users/**", "/api/addresses/**").uri(user))
                .route("product-service", r -> r.path("/api/categories/**", "/api/brands/**", "/api/products/**", "/api/admin/**").uri(product))
                .route("order-service", r -> r.path("/api/cart/**", "/api/orders/**").uri(order))
                .build();
    }
}
