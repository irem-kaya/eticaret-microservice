package com.eticaret.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8081"))

                .route("product-service", r -> r
                        .path("/api/products/**", "/api/categories/**")
                        .uri("http://localhost:8083"))

                .route("cart-service", r -> r
                        .path("/api/cart/**")
                        .uri("http://localhost:8089"))

                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("http://localhost:8084"))

                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("http://localhost:8085"))

                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8086"))

                .route("ai-service", r -> r
                        .path("/api/ai/**")
                        .uri("http://localhost:8087"))

                .build();
    }
}