package com.eticaret.ai.service;

import com.eticaret.ai.dto.ProductDataDto;
import com.eticaret.ai.dto.GenerateProductsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ProductGenerationService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 15000; // 15 saniye — rate limit için

    @Value("${app.unsplash.access-key:AIzaSyBkd4TTu5rLNbsLOA2Ux4fS2p8WehfNmJs}")
    private String unsplashKey;

    private final AIService aiService;
    private final WebClient productClient;

    public ProductGenerationService(AIService aiService, WebClient.Builder webClientBuilder) {
        this.aiService = aiService;
        // 8083 — product-service doğru port
        this.productClient = webClientBuilder.baseUrl("http://localhost:8083").build();
    }

    @Async
    public CompletableFuture<Integer> generateAndSaveAsync(GenerateProductsRequest request) {
        return CompletableFuture.supplyAsync(() -> generateAndSave(request));
    }

    public int generateAndSave(GenerateProductsRequest request) {
        log.info("{} kategorisi için {} ürün üretiliyor...", request.category(), request.count());

        List<ProductDataDto> products = aiService.generateProducts(request.category(), request.count());
        int saved = 0;
        int failed = 0;

        for (ProductDataDto product : products) {
            try {
                if (saveProductWithRetry(product, request.categoryId())) {
                    saved++;
                } else {
                    failed++;
                }
                // Her ürün arasında 2 saniye bekle
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("Ürün kaydedilemedi: {} - {}", product.name(), e.getMessage());
                failed++;
            }
        }

        log.info("{} kategorisi tamamlandı: {}/{} ürün kaydedildi, {} başarısız",
                request.category(), saved, products.size(), failed);
        return saved;
    }

    private boolean saveProductWithRetry(ProductDataDto product, Long categoryId) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String imageUrl = fetchUnsplashImage(product.imageKeyword());

                Map<String, Object> productRequest = Map.of(
                        "name", product.name(),
                        "description", product.description(),
                        "price", product.price(),
                        "stock", product.stock(),
                        "categoryId", categoryId,
                        "imageUrl", imageUrl != null ? imageUrl : ""
                );

                productClient.post()
                        .uri("/api/products")
                        .header("Content-Type", "application/json")
                        .bodyValue(productRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(java.time.Duration.ofSeconds(10))
                        .block();

                log.info("✓ Ürün kaydedildi: {}", product.name());
                return true;

            } catch (Exception e) {
                if (attempt < MAX_RETRIES) {
                    log.warn("Ürün kayıt başarısız (attempt {}/{}), {} ms sonra yeniden deneniyor: {}",
                            attempt, MAX_RETRIES, RETRY_DELAY_MS * attempt, product.name());
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("Ürün kaydedilemedi (tüm denemeler başarısız): {} - {}", product.name(), e.getMessage());
                }
            }
        }
        return false;
    }

    private String fetchUnsplashImage(String keyword) {
        if (unsplashKey == null || unsplashKey.equals("your-key-here") || unsplashKey.isBlank()) {
            // Unsplash key yoksa keyword bazlı Unsplash URL üret
            return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&q=80";
        }
        try {
            String response = WebClient.builder()
                    .baseUrl("https://api.unsplash.com")
                    .build()
                    .get()
                    .uri("/search/photos?query=" + keyword + "&per_page=1&orientation=squarish")
                    .header("Authorization", "Client-ID " + unsplashKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response);
            String imageUrl = root.path("results").get(0).path("urls").path("regular").asText();
            return imageUrl.isEmpty() ? null : imageUrl;
        } catch (Exception e) {
            log.debug("Unsplash görsel çekme başarısız: {}", keyword);
            return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&q=80";
        }
    }
}