package com.eticaret.ai.service;

import com.eticaret.ai.dto.GenerateProductsRequest;
import com.eticaret.ai.dto.SeedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SeedService {

    private static final Logger log = LoggerFactory.getLogger(SeedService.class);

    private static final List<String[]> CATEGORIES = List.of(
        new String[]{"Elektronik", "Telefon, bilgisayar, TV ve elektronik ürünler"},
        new String[]{"Giyim", "Kadın, erkek ve çocuk giyim ürünleri"},
        new String[]{"Ev & Yaşam", "Mobilya, dekorasyon ve ev tekstili"},
        new String[]{"Spor", "Spor ekipmanları, kıyafetleri ve aksesuarlar"},
        new String[]{"Kitap & Kırtasiye", "Kitaplar, dergiler ve kırtasiye malzemeleri"},
        new String[]{"Oyuncak & Hobi", "Oyuncaklar, hobi malzemeleri ve oyunlar"},
        new String[]{"Kozmetik & Kişisel Bakım", "Makyaj, cilt bakımı ve parfüm"},
        new String[]{"Mutfak", "Mutfak aletleri, yemek takımları ve pişirme ekipmanları"},
        new String[]{"Bahçe & Yapı Market", "Bahçe aletleri, yapı malzemeleri ve hırdavat"},
        new String[]{"Otomobil & Aksesuar", "Araç aksesuarları, bakım ürünleri ve yedek parça"}
    );

    private final ProductGenerationService generationService;
    private final WebClient productWebClient;

    public SeedService(ProductGenerationService generationService, WebClient productWebClient) {
        this.generationService = generationService;
        this.productWebClient = productWebClient;
    }

    public SeedResult seedDatabase(int productsPerCategory) {
        List<String> messages = new ArrayList<>();
        int categoriesCreated = 0;
        int productsCreated = 0;
        int productsFailed = 0;

        // Step 1: Get or create categories
        List<Map<String, Object>> existingCategories = fetchExistingCategories();
        List<Map<String, Object>> categoryMap = new ArrayList<>();

        for (String[] catDef : CATEGORIES) {
            String catName = catDef[0];
            String catDesc = catDef[1];

            Map<String, Object> existing = existingCategories.stream()
                .filter(c -> catName.equals(c.get("name")))
                .findFirst()
                .orElse(null);

            if (existing != null) {
                categoryMap.add(existing);
                messages.add("Kategori mevcut: " + catName + " (ID: " + existing.get("id") + ")");
            } else {
                Map<String, Object> created = createCategory(catName, catDesc);
                if (created != null) {
                    categoryMap.add(created);
                    categoriesCreated++;
                    messages.add("Kategori oluşturuldu: " + catName + " (ID: " + created.get("id") + ")");
                } else {
                    messages.add("Kategori oluşturulamadı: " + catName);
                }
            }
        }

        // Step 2: Generate and create products for each category
        for (int i = 0; i < CATEGORIES.size() && i < categoryMap.size(); i++) {
            String categoryName = CATEGORIES.get(i)[0];
            Long categoryId = ((Number) categoryMap.get(i).get("id")).longValue();

            log.info("{} kategorisi için {} ürün üretiliyor...", categoryName, productsPerCategory);
            GenerateProductsRequest req = new GenerateProductsRequest(categoryName, categoryId, productsPerCategory);
            int count = generationService.generateAndSave(req);
            productsCreated += count;
            messages.add(categoryName + ": " + count + " ürün oluşturuldu");
        }

        return new SeedResult(categoriesCreated, productsCreated, productsFailed, messages);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchExistingCategories() {
        try {
            Map response = productWebClient.get()
                .uri("/api/categories")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            Object data = response.get("data");
            if (data instanceof List<?> list) {
                return (List<Map<String, Object>>) list;
            }
        } catch (Exception e) {
            log.error("Failed to fetch categories: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createCategory(String name, String description) {
        try {
            Map response = productWebClient.post()
                .uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", name, "description", description))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            return (Map<String, Object>) response.get("data");
        } catch (Exception e) {
            log.error("Failed to create category {}: {}", name, e.getMessage());
            return null;
        }
    }

}