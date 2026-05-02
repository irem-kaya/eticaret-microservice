package com.eticaret.ai.controller;

import com.eticaret.ai.dto.GenerateProductsRequest;
import com.eticaret.ai.dto.SeedResult;
import com.eticaret.ai.service.ProductGenerationService;
import com.eticaret.ai.service.SeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "Yapay zeka servisleri")
public class AiController {

    private final ProductGenerationService productGenerationService;
    private final SeedService seedService;
    private final WebClient geminiClient;

    @Value("${app.ai.api-key}")
    private String apiKey;

    public AiController(ProductGenerationService productGenerationService, SeedService seedService) {
        this.productGenerationService = productGenerationService;
        this.seedService = seedService;
        this.geminiClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @PostMapping("/chat")
    @Operation(summary = "Pınar AI Asistan ile sohbet")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            String userMessage = (String) request.get("message");
            String systemPrompt = """
                Sen TMarket Pro e-ticaret sitesinin AI asistani Pinar'sin.
                Turkce konus, samimi ve yardımsever ol. Kisa ve net cevaplar ver.
                Kategoriler: Elektronik, Giyim & Moda, Ev & Yasam, Spor & Outdoor.
                Urun onerirken: [Urun] - [Fiyat] TL formatini kullan.
                """;

            Map<String, Object> body = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "contents", List.of(
                            Map.of("role", "user", "parts", List.of(Map.of("text", userMessage)))
                    ),
                    "generationConfig", Map.of("maxOutputTokens", 300, "temperature", 0.7)
            );

            String response = geminiClient.post()
                    .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response);
            String reply = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return ResponseEntity.ok(Map.of("success", true, "reply", reply));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "reply", "Su an yanit veremiyorum: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/seed")
    @Operation(summary = "Veritabanini AI ile doldur")
    public ResponseEntity<Map<String, Object>> seed(
            @RequestParam(defaultValue = "3") int productsPerCategory) {
        try {
            SeedResult result = seedService.seedDatabase(productsPerCategory);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "categoriesCreated", result.categoriesCreated(),
                    "productsCreated", result.productsCreated(),
                    "messages", result.messages()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/generate-products")
    @Operation(summary = "AI ile urun ureT ve kaydet")
    public ResponseEntity<Map<String, Object>> generateProducts(
            @RequestBody GenerateProductsRequest request) {
        int saved = productGenerationService.generateAndSave(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", saved + " urun basariyla olusturuldu",
                "count", saved
        ));
    }

    @PostMapping("/generate-bulk")
    @Operation(summary = "Tum kategoriler icin toplu urun ureT")
    public ResponseEntity<Map<String, Object>> generateBulk() {
        List<Map<String, Object>> categories = List.of(
                Map.of("category", "Elektronik", "categoryId", 1L, "count", 3),
                Map.of("category", "Giyim & Moda", "categoryId", 2L, "count", 3),
                Map.of("category", "Ev & Yasam", "categoryId", 3L, "count", 3),
                Map.of("category", "Spor & Outdoor", "categoryId", 4L, "count", 3)
        );
        int total = 0;
        for (Map<String, Object> cat : categories) {
            try {
                GenerateProductsRequest req = new GenerateProductsRequest(
                        (String) cat.get("category"),
                        (Long) cat.get("categoryId"),
                        (Integer) cat.get("count")
                );
                total += productGenerationService.generateAndSave(req);
                Thread.sleep(5000); // rate limit icin bekle
            } catch (Exception e) {
                // devam et
            }
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Toplu uretim tamamlandi",
                "totalCount", total
        ));
    }
}