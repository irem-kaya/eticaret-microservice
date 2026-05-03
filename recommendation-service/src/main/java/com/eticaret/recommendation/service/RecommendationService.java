package com.eticaret.recommendation.service;

import com.eticaret.recommendation.service.MCPRecommendationEngine.ProductData;
import com.eticaret.recommendation.service.MCPRecommendationEngine.RecommendationGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Recommendation Service - Öneri Yönetimi
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final long CACHE_DURATION_HOURS = 24;
    private static final String CACHE_KEY_PREFIX = "recommendations:category:";

    private final MCPRecommendationEngine engine;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient productClient;

    public RecommendationService(
            MCPRecommendationEngine engine,
            RedisTemplate<String, Object> redisTemplate,
            WebClient.Builder webClientBuilder) {
        this.engine = engine;
        this.redisTemplate = redisTemplate;
        this.productClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    /**
     * RabbitMQ'dan gelen product.added event'i dinle
     */
    @RabbitListener(queues = "product.added.queue")
    public void handleProductAdded(ProductAddedEvent event) {
        log.info("📦 Ürün eklendi event alındı: Kategori {}, Ürün ID {}", event.categoryId(), event.productId());
        
        // Asenkron olarak önerileri güncelle
        CompletableFuture.runAsync(() -> updateRecommendations(event.categoryId()));
    }

    /**
     * Kategori için önerileri güncelle
     */
    public void updateRecommendations(Long categoryId) {
        log.info("🔄 Kategori {} için öneriler güncelleniyor...", categoryId);

        try {
            // Product Service'ten ürünleri çek
            List<ProductData> products = fetchProductsForCategory(categoryId);
            
            if (products.size() < 6) {
                log.info("⏸️  Kategori {} için yetersiz ürün ({}/6)", categoryId, products.size());
                return;
            }

            // MCP analizi yap
            List<RecommendationGroup> recommendations = engine.analyzeProducts(categoryId, products);

            if (recommendations.isEmpty()) {
                log.warn("⚠️  Kategori {} için öneri üretilemedi", categoryId);
                return;
            }

            // Redis'e kaydet (24 saat TTL)
            String cacheKey = CACHE_KEY_PREFIX + categoryId;
            redisTemplate.opsForValue().set(
                    cacheKey,
                    recommendations,
                    Duration.ofHours(CACHE_DURATION_HOURS)
            );

            log.info("✅ Kategori {} için {} öneri grubu oluşturuldu ve cache'e kaydedildi",
                    categoryId, recommendations.size());

        } catch (Exception e) {
            log.error("❌ Kategori {} için öneri güncellemesi başarısız: {}", categoryId, e.getMessage());
        }
    }

    /**
     * Product Service'ten kategori ürünlerini çek
     */
    private List<ProductData> fetchProductsForCategory(Long categoryId) {
        try {
            String response = productClient.get()
                    .uri("/api/products?categoryId={id}&size=100", categoryId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON parse ve ProductData'ya dönüştür
            // Not: Gerçek uygulamada ObjectMapper kullanılmalı
            log.debug("Product Service'ten {} kategorisi ürünleri çekildi", categoryId);
            
            return new ArrayList<>(); // TODO: JSON parsing
        } catch (Exception e) {
            log.error("Product Service'ten ürün çekilirken hata: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Kategori için önerileri getir (cache'den)
     */
    public List<RecommendationGroup> getRecommendations(Long categoryId) {
        String cacheKey = CACHE_KEY_PREFIX + categoryId;
        
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("💾 Kategori {} için öneriler cache'den alındı", categoryId);
            return (List<RecommendationGroup>) cached;
        }

        log.info("📭 Kategori {} için cache'de öneri yok", categoryId);
        return new ArrayList<>();
    }

    /**
     * Kategori önerilerini temizle
     */
    public void clearRecommendations(Long categoryId) {
        String cacheKey = CACHE_KEY_PREFIX + categoryId;
        redisTemplate.delete(cacheKey);
        log.info("🗑️  Kategori {} önerileri temizlendi", categoryId);
    }

    public void refreshAllRecommendations() {
    }

    /**
     * Product Added Event
     */
    public static class ProductAddedEvent {
        private Long categoryId;
        private Long productId;
        private String productName;

        public Long categoryId() { return categoryId; }
        public Long productId() { return productId; }
        public String productName() { return productName; }
    }
}

