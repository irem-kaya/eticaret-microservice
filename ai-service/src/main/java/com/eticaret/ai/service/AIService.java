package com.eticaret.ai.service;

import com.eticaret.ai.dto.ProductDataDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private static final int BATCH_SIZE = 5;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model:gemini-3-flash-preview}")
    private String model;

    private final WebClient geminiClient;
    private final ObjectMapper objectMapper;

    public AIService(ObjectMapper objectMapper) {
        this.geminiClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = objectMapper;
    }

    public List<ProductDataDto> generateProducts(String category, int count) {
        List<ProductDataDto> all = new ArrayList<>();
        int remaining = count;

        while (remaining > 0) {
            int batch = Math.min(remaining, BATCH_SIZE);
            try {
                List<ProductDataDto> batchResult = generateBatch(category, batch);
                all.addAll(batchResult);
                log.info("{} kategorisi: {}/{} ürün üretildi", category, all.size(), count);

                if (remaining - batch > 0) {
                    Thread.sleep(8000);
                }
            } catch (Exception e) {
                log.error("{} kategorisi batch hatası: {}", category, e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    try {
                        log.warn("Rate limit! 60 saniye bekleniyor...");
                        Thread.sleep(60000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            remaining -= batch;
        }

        return all;
    }

    private List<ProductDataDto> generateBatch(String category, int count) throws Exception {
        String prompt = """
            Sen Trendyol ve N11 gibi Türk e-ticaret sitelerindeki gerçek ürünleri bilen bir asistansın.
            "%s" kategorisi için %d adet GERÇEK ürün bilgisi üret.
            
            Şartlar:
            - Gerçek marka ve model adları kullan (Samsung, Apple, Nike, Adidas, Dyson, vb.)
            - Fiyatlar 2024-2025 Türkiye piyasasına uygun olsun (TL cinsinden)
            - Açıklamalar Türkçe, 2-3 cümle, ürünün gerçek özelliklerini içersin
            - imageKeyword: Unsplash'ta bu ürünü bulacak İngilizce anahtar kelime
            - stock: 10-200 arası
            
            SADECE JSON array döndür, başka hiçbir şey yazma:
            [{"name":"Samsung Galaxy S24 128GB Siyah","description":"6.2 inç Dynamic AMOLED ekran, 50MP ana kamera, Snapdragon 8 Gen 3 işlemci. 4000mAh batarya ile uzun kullanım süresi.","price":34999.99,"stock":45,"imageKeyword":"samsung galaxy smartphone"},...]
            """.formatted(category, count);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "maxOutputTokens", 3000,
                        "temperature", 0.8
                )
        );

        String response = geminiClient.post()
                .uri("/v1beta/models/" + model + ":generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(response);

        if (root.has("error")) {
            throw new RuntimeException(root.path("error").path("code").asInt() +
                    " " + root.path("error").path("message").asText());
        }

        String content = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        log.info("Gemini yanıtı: {}", content.substring(0, Math.min(200, content.length())));
        return parseProducts(cleanJson(content));
    }

    private String cleanJson(String s) {
        s = s.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }

    private List<ProductDataDto> parseProducts(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ProductDataDto>>() {});
        } catch (Exception e) {
            log.error("JSON parse hatası: {} \nJSON: {}", e.getMessage(), json);
            return new ArrayList<>();
        }
    }
}