package com.eticaret.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.product-service-url:http://localhost:8083}")
    private String productServiceUrl;

    @Bean
    public WebClient productWebClient() {
        return WebClient.builder()
            .baseUrl(productServiceUrl)
            .build();
    }
}