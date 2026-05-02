package com.eticaret.ai.scheduler;

import com.eticaret.ai.dto.GenerateProductsRequest;
import com.eticaret.ai.service.ProductGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Asenkron Ürün Senkronizasyon Zamanlayıcısı
 * Günlük 2 AM'de tüm kategoriler için ürün üretimi başlatır
 */
@Component
public class ProductSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncScheduler.class);

    private final ProductGenerationService productGenerationService;

    // Kategori konfigürasyonu: kategoriAdı, kategoriId, üretilecekÜrünSayısı
    private static final List<CategoryConfig> CATEGORIES = List.of(
            new CategoryConfig("Elektronik", 1L, 100),
            new CategoryConfig("Giyim", 2L, 100),
            new CategoryConfig("Ev & Yaşam", 3L, 100),
            new CategoryConfig("Spor", 4L, 100),
            new CategoryConfig("Kitaplar", 5L, 100),
            new CategoryConfig("Oyuncaklar", 6L, 100),
            new CategoryConfig("Kozmetik", 7L, 100),
            new CategoryConfig("Mobilya", 8L, 100),
            new CategoryConfig("Otomotiv", 9L, 100),
            new CategoryConfig("Bahçe", 10L, 100)
    );

    public ProductSyncScheduler(ProductGenerationService productGenerationService) {
        this.productGenerationService = productGenerationService;
    }

    /**
     * Günlük senkronizasyon - Her gün 02:00 AM'de çalışır
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
    public void syncProductsDaily() {
        log.info("📅 Günlük ürün senkronizasyonu başlıyor...");
        long startTime = System.currentTimeMillis();

        int totalSaved = 0;
        int failedCount = 0;

        for (CategoryConfig category : CATEGORIES) {
            try {
                log.info("▶️  {} kategorisi işlenmektedir... (Hedef: {} ürün)",
                        category.name, category.count);

                GenerateProductsRequest request = new GenerateProductsRequest(
                        category.name,
                        category.categoryId,
                        category.count
                );

                int saved = productGenerationService.generateAndSave(request);
                totalSaved += saved;

                log.info("✅ {} kategorisi tamamlandı: {} ürün kaydedildi",
                        category.name, saved);

                // Rate limiting - API'ye aşırı yük bindirmemek için
                Thread.sleep(2000);

            } catch (Exception e) {
                log.error("❌ {} kategorisinde hata: {}", category.name, e.getMessage());
                failedCount++;
            }
        }

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        log.info("🏁 Senkronizasyon tamamlandı! Toplam: {} ürün kaydedildi, {} kategori başarısız, " +
                "Süre: {} saniye", totalSaved, failedCount, duration);
    }

    /**
     * Manual tetikleme için endpoint (opsiyonel)
     * REST controller'dan çağrılabilir
     */
    public void syncProductsManual(Long categoryId) {
        CATEGORIES.stream()
                .filter(cat -> categoryId == null || cat.categoryId.equals(categoryId))
                .forEach(category -> {
                    log.info("🔄 Manuel senkronizasyon başlatılıyor: {}", category.name);
                    try {
                        GenerateProductsRequest request = new GenerateProductsRequest(
                                category.name,
                                category.categoryId,
                                category.count
                        );
                        int saved = productGenerationService.generateAndSave(request);
                        log.info("✅ {} kategorisiinden {} ürün kaydedildi", category.name, saved);
                    } catch (Exception e) {
                        log.error("❌ Manuel senkronizasyon başarısız: {}", category.name, e);
                    }
                });
    }

    /**
     * Kategori konfigürasyonu
     */
    public static class CategoryConfig {
        public String name;
        public Long categoryId;
        public int count;

        public CategoryConfig(String name, Long categoryId, int count) {
            this.name = name;
            this.categoryId = categoryId;
            this.count = count;
        }
    }
}

