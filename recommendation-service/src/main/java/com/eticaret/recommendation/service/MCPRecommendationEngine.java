package com.eticaret.recommendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP (Model Context Protocol) Recommendation Engine
 * Kategorilerdeki ürünleri analiz ederek hedefli öneriler sunar
 */
@Service
public class MCPRecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(MCPRecommendationEngine.class);
    private static final int MIN_PRODUCTS_FOR_RECOMMENDATION = 6;
    private static final int PRICE_TIERS = 4;

    /**
     * Ürün listesini analiz et ve öneriler üret
     */
    public List<RecommendationGroup> analyzeProducts(Long categoryId, List<ProductData> products) {
        log.info("MCP Analizi başlıyor - Kategori: {} ({} ürün)", categoryId, products.size());

        if (products.size() < MIN_PRODUCTS_FOR_RECOMMENDATION) {
            log.warn("Yetersiz ürün ({}) - Minimum {} gerekli", products.size(), MIN_PRODUCTS_FOR_RECOMMENDATION);
            return new ArrayList<>();
        }

        List<RecommendationGroup> groups = new ArrayList<>();

        // 1. En popüler (en çok stok olanlar)
        groups.add(createMostPopularGroup(products));

        // 2. Fiyat bazlı gruplar
        groups.addAll(createPriceTierGroups(products));

        // 3. En iyi değer (fiyat-kalite oranı)
        groups.add(createBestValueGroup(products));

        return groups.stream()
                .filter(g -> g.getItems().size() > 0)
                .collect(Collectors.toList());
    }

    /**
     * En popüler ürünler grubu
     */
    private RecommendationGroup createMostPopularGroup(List<ProductData> products) {
        List<ProductData> popular = products.stream()
                .sorted(Comparator.comparingInt(ProductData::stock).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return new RecommendationGroup(
                "🔥 Çok Satılan Ürünler",
                "En yüksek stok miktarına sahip ürünler",
                95,
                popular
        );
    }

    /**
     * Fiyat seviyelerine göre gruplar
     */
    private List<RecommendationGroup> createPriceTierGroups(List<ProductData> products) {
        List<RecommendationGroup> tierGroups = new ArrayList<>();

        // Fiyat istatistikleri
        OptionalDouble avgPrice = products.stream()
                .mapToDouble(p -> p.price().doubleValue())
                .average();

        if (avgPrice.isPresent()) {
            double avg = avgPrice.getAsDouble();

            // Bütçe dostu (Ortalamanın %50'si altı)
            List<ProductData> budget = products.stream()
                    .filter(p -> p.price().doubleValue() < avg * 0.5)
                    .sorted(Comparator.comparingInt(ProductData::stock).reversed())
                    .limit(4)
                    .collect(Collectors.toList());

            if (budget.size() > 0) {
                tierGroups.add(new RecommendationGroup(
                        "💰 Bütçe Dostu",
                        "En düşük fiyatlı seçenekler",
                        80,
                        budget
                ));
            }

            // Premium (Ortalamanın %150'si üstü)
            List<ProductData> premium = products.stream()
                    .filter(p -> p.price().doubleValue() > avg * 1.5)
                    .sorted(Comparator.comparingInt(ProductData::stock).reversed())
                    .limit(4)
                    .collect(Collectors.toList());

            if (premium.size() > 0) {
                tierGroups.add(new RecommendationGroup(
                        "✨ Premium Seçim",
                        "Yüksek kalite ve özellik",
                        85,
                        premium
                ));
            }
        }

        return tierGroups;
    }

    /**
     * En iyi değer (fiyat-kalite dengelisi)
     */
    private RecommendationGroup createBestValueGroup(List<ProductData> products) {
        // Stok/Fiyat oranı en iyisi
        List<ProductData> bestValue = products.stream()
                .sorted((p1, p2) -> {
                    double ratio1 = (double) p1.stock() / p1.price().doubleValue();
                    double ratio2 = (double) p2.stock() / p2.price().doubleValue();
                    return Double.compare(ratio2, ratio1);
                })
                .limit(5)
                .collect(Collectors.toList());

        return new RecommendationGroup(
                "🎯 En İyi Değer",
                "Fiyat-performans açısından en iyileri",
                90,
                bestValue
        );
    }

    /**
     * Kategori hazır mı kontrol et (minimum 6 ürün)
     */
    public boolean isReadyForRecommendation(int productCount) {
        return productCount >= MIN_PRODUCTS_FOR_RECOMMENDATION;
    }

    /**
     * Tavsiye Grubu
     */
    public static class RecommendationGroup {
        public String title;
        public String description;
        public int score; // 0-100
        public List<ProductData> items;

        public RecommendationGroup(String title, String description, int score, List<ProductData> items) {
            this.title = title;
            this.description = description;
            this.score = score;
            this.items = items;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getScore() { return score; }
        public List<ProductData> getItems() { return items; }
    }

    /**
     * Ürün veri modeli
     */
    public static class ProductData {
        public Long id;
        public String name;
        public BigDecimal price;
        public Integer stock;
        public String imageUrl;

        public ProductData(Long id, String name, BigDecimal price, Integer stock, String imageUrl) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.imageUrl = imageUrl;
        }

        public Long id() { return id; }
        public String name() { return name; }
        public BigDecimal price() { return price; }
        public Integer stock() { return stock; }
        public String imageUrl() { return imageUrl; }
    }
}

