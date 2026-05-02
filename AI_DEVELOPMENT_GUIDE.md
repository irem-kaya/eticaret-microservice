# 🤖 E-Ticaret Platform - AI Araçları Entegrasyonu & Proje Dosyaları Analizi

**AI Tools Integration Guide**  
**Versiyon:** 1.0.0  
**Son Güncelleme:** 1 Mayıs 2026

---

## 📑 İçerik Tablosu

1. [Proje Dosya Yapısı](#proje-dosya-yapısı)
2. [Ürün Çekme Modülleri](#ürün-çekme-modülleri)
3. [AI Service Detaylı Analiz](#ai-service-detaylı-analiz)
4. [Product Service Detaylı Analiz](#product-service-detaylı-analiz)
5. [AI Araçları Entegrasyonu](#ai-araçları-entegrasyonu)
6. [Geliştirme Rehberi](#geliştirme-rehberi)

---

## 📁 Proje Dosya Yapısı

### Root Level Structure

```
eticaret-microservice/
├── 📄 pom.xml                    # Maven Parent POM
├── 📄 README.md                  # Proje özeti
├── 📄 DOCUMENTATION.md           # Detaylı teknik dokümantasyon
├── 📄 SETUP.md                   # Kurulum rehberi
├── 📄 LICENSE                    # MIT License
│
├── 📁 infra/                     # ⭐ Infrastructure yapılandırma
│   ├── docker-compose.yml        # PostgreSQL, RabbitMQ, Redis, Keycloak
│   ├── init-db.sql               # Database initialization
│   └── .env                      # Environment variables
│
├── 📁 common/                    # ⭐ Shared modules (tüm services'ler kullanır)
│   ├── pom.xml
│   └── src/main/
│       └── java/com/eticaret/common/
│           ├── ApiResponse.java          # HTTP response wrapper
│           ├── ErrorResponse.java        # Error handling
│           ├── exception/
│           │   ├── BusinessException.java
│           │   ├── ResourceNotFoundException.java
│           │   └── ValidationException.java
│           ├── dto/
│           │   ├── UserDto.java
│           │   ├── ProductDto.java
│           │   └── OrderDto.java
│           └── util/
│               ├── DateUtil.java
│               ├── StringUtil.java
│               └── ValidationUtil.java
│
├── 📁 api-gateway/               # ⭐ API Gateway (Port 8080)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/gateway/
│   │   │   ├── GatewayApplication.java   # Main class
│   │   │   ├── config/
│   │   │   │   ├── GatewayConfig.java    # Spring Cloud Gateway routes
│   │   │   │   ├── CorsConfig.java       # CORS konfigürasyonu
│   │   │   │   └── SecurityConfig.java   # OAuth2 configuration
│   │   │   ├── filter/
│   │   │   │   ├── AuthenticationFilter.java
│   │   │   │   └── LoggingFilter.java
│   │   │   └── controller/
│   │   │       └── HealthController.java
│   │   └── resources/
│   │       └── application.yml           # Gateway config
│   └── target/                  # Build output
│
├── 📁 user-service/              # ⭐ User Service (Port 8086)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/user/
│   │   │   ├── UserServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── User.java            # Entity
│   │   │   │   ├── UserRepository.java  # Repository (JPA)
│   │   │   │   ├── UserService.java     # Business logic
│   │   │   │   └── UserController.java  # REST API
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   └── UserResponse.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/
│   │   │       ├── AuthenticationService.java
│   │   │       └── JwtTokenProvider.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/         # Flyway scripts
│   └── pom.xml
│
├── 📁 product-service-main/       # ⭐ Product Service (Port 8082)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/product/
│   │   │   ├── ProductServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── Product.java          # Entity
│   │   │   │   ├── Category.java         # Entity
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── ProductService.java   # Business logic
│   │   │   │   └── ProductController.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateProductRequest.java
│   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   ├── ProductResponse.java
│   │   │   │   ├── BulkImportRequest.java
│   │   │   │   └── BulkImportResponse.java
│   │   │   ├── features/
│   │   │   │   ├── search/
│   │   │   │   │   ├── FuzzySearchService.java       # ⭐ %75 Fuzzy search
│   │   │   │   │   └── SearchRepository.java
│   │   │   │   ├── authorization/
│   │   │   │   │   └── StockAuthorizationService.java
│   │   │   │   └── pagination/
│   │   │   │       └── PaginationUtil.java
│   │   │   ├── event/
│   │   │   │   ├── ProductAddedEvent.java
│   │   │   │   └── ProductEventPublisher.java
│   │   │   └── util/
│   │   │       └── ProductMapper.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V001__create_products_table.sql
│   │           ├── V002__create_categories_table.sql
│   │           └── V003__add_search_indexes.sql
│   └── pom.xml
│
├── 📁 cart-service/              # ⭐ Cart Service (Port 8081)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/cart/
│   │   │   ├── CartServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── Cart.java
│   │   │   │   ├── CartItem.java
│   │   │   │   ├── CartRepository.java
│   │   │   │   ├── CartService.java
│   │   │   │   └── CartController.java
│   │   │   └── dto/
│   │   │       ├── AddToCartRequest.java
│   │   │       └── CartResponse.java
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
│
├── 📁 order-service/             # ⭐ Order Service (Port 8083)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/order/
│   │   │   ├── OrderServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── OrderStatus.java (Enum)
│   │   │   ├── facade/
│   │   │   │   └── OrderFacade.java       # CoR pattern
│   │   │   ├── validator/
│   │   │   │   ├── OrderValidator.java
│   │   │   │   ├── InventoryValidator.java
│   │   │   │   └── PaymentValidator.java
│   │   │   └── dto/
│   │   │       ├── CreateOrderRequest.java
│   │   │       └── OrderResponse.java
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
│
├── 📁 payment-service/           # ⭐ Payment Service (Port 8084)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/payment/
│   │   │   ├── PaymentServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── Payment.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   └── PaymentController.java
│   │   │   ├── gateway/
│   │   │   │   ├── PaymentGatewayService.java
│   │   │   │   └── StripeService.java
│   │   │   └── dto/
│   │   │       └── PaymentRequest.java
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
│
├── 📁 notification-service/      # ⭐ Notification Service (Port 8085)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/notification/
│   │   │   ├── NotificationServiceApplication.java
│   │   │   ├── service/
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── SmsService.java
│   │   │   │   └── PushNotificationService.java
│   │   │   ├── event/
│   │   │   │   └── NotificationEventListener.java
│   │   │   └── template/
│   │   │       ├── OrderConfirmationTemplate.java
│   │   │       └── ShippingUpdateTemplate.java
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
│
├── 📁 ai-service/                # ⭐ AI SERVICE (Port 8087) - ÜRÜN ÇEKME
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/ai/
│   │   │   ├── AiServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── AiController.java              # ⭐ REST endpoints
│   │   │   │       ├── POST /api/ai/generate-products
│   │   │   │       └── POST /api/ai/generate-bulk
│   │   │   ├── service/
│   │   │   │   ├── ProductGenerationService.java  # ⭐ Ürün üretim logic
│   │   │   │   │   ├── generateAndSave()          # Async ürün üretimi
│   │   │   │   │   ├── retryLogic()               # 3 deneme
│   │   │   │   │   └── publishEvent()             # RabbitMQ event
│   │   │   │   ├── GeminiApiService.java          # ⭐ Gemini API client
│   │   │   │   │   ├── generateProducts()         # API çağrısı
│   │   │   │   │   └── parseResponse()            # JSON parse
│   │   │   │   └── ImageService.java              # ⭐ Unsplash images
│   │   │   │       └── getProductImage()          # Image fetch
│   │   │   ├── scheduler/
│   │   │   │   └── ProductSyncScheduler.java      # ⭐ Günlük sync (02:00 AM)
│   │   │   │       ├── scheduledProductSync()     # Cron job
│   │   │   │       └── 10 kategori × 100 ürün
│   │   │   ├── dto/
│   │   │   │   ├── GenerateProductsRequest.java
│   │   │   │   ├── GeneratedProduct.java
│   │   │   │   └── GeminiResponse.java
│   │   │   ├── event/
│   │   │   │   ├── ProductAddedEvent.java
│   │   │   │   └── ProductEventPublisher.java
│   │   │   └── util/
│   │   │       ├── ProductDataProcessor.java
│   │   │       └── PriceRandomizer.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── db/migration/
│   │       │   └── V001__create_ai_import_jobs.sql
│   │       └── prompts/
│   │           ├── product_generation.txt         # Gemini prompt
│   │           └── category_list.txt
│   └── pom.xml
│
├── 📁 recommendation-service/    # ⭐ Recommendation Service (Port 8088)
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/eticaret/recommendation/
│   │   │   ├── RecommendationServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── RecommendationController.java
│   │   │   ├── service/
│   │   │   │   ├── RecommendationService.java
│   │   │   │   ├── MCPRecommendationEngine.java   # ⭐ MCP logic
│   │   │   │   └── RecommendationCache.java
│   │   │   ├── event/
│   │   │   │   └── RecommendationEventListener.java
│   │   │   └── dto/
│   │   │       └── RecommendationResponse.java
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
│
├── 📁 frontend/                  # ⭐ FRONTEND (Port 5173) - React 19
│   ├── package.json              # npm dependencies
│   ├── vite.config.js            # Vite configuration
│   ├── tailwind.config.js         # Tailwind CSS
│   ├── postcss.config.js          # PostCSS
│   ├── index.html                 # Entry HTML
│   ├── src/
│   │   ├── main.jsx               # React entry
│   │   ├── App.jsx                # Root component
│   │   ├── index.css              # Global styles
│   │   ├── components/
│   │   │   ├── Navbar.jsx         # Header + search
│   │   │   ├── Sidebar.jsx        # Filters + grid
│   │   │   ├── ProductCard.jsx    # Product display
│   │   │   ├── Pagination.jsx     # Sayfalama
│   │   │   ├── Carousel.jsx       # Benzer ürünler
│   │   │   ├── Toast.jsx          # Notifications
│   │   │   ├── Header.jsx         # Top nav
│   │   │   ├── Footer.jsx         # Bottom section
│   │   │   └── Loading.jsx        # Skeleton
│   │   ├── pages/
│   │   │   ├── ProductListPage.jsx
│   │   │   ├── ProductDetailPage.jsx
│   │   │   ├── CartPage.jsx
│   │   │   ├── CheckoutPage.jsx
│   │   │   ├── LoginPage.jsx
│   │   │   ├── OrdersPage.jsx
│   │   │   └── OrderSuccessPage.jsx
│   │   ├── services/
│   │   │   ├── api.js             # Axios config
│   │   │   ├── productService.js  # Product API
│   │   │   ├── cartService.js     # Cart API
│   │   │   ├── authService.js     # Auth (Keycloak)
│   │   │   ├── orderService.js    # Order API
│   │   │   ├── fuzzySearch.js     # ⭐ Fuzzy search algorithm
│   │   │   └── recommendationService.js
│   │   ├── context/
│   │   │   ├── AuthContext.jsx
│   │   │   └── CartContext.jsx
│   │   ├── hooks/
│   │   │   ├── useAuth.js
│   │   │   └── useCart.js
│   │   └── assets/
│   │       ├── images/
│   │       └── icons/
│   ├── public/
│   │   ├── favicon.svg
│   │   └── icons.svg
│   └── dist/                     # Build output
│
├── 📁 scripts/                    # ⭐ Utility scripts
│   ├── seed-products.js           # Database seed (Node.js)
│   ├── deploy.sh                  # Deployment script
│   └── backup.sh                  # Backup script
│
└── 📁 .github/                    # ⭐ CI/CD (GitHub Actions)
    └── workflows/
        └── ci-cd.yml              # Build, test, deploy pipeline
```

---

## 🎯 Ürün Çekme Modülleri

### Modül Haritası

```
┌─────────────────────────────────────────┐
│      AI Service (Ürün Oluşturma)       │
│          Port: 8087                     │
├─────────────────────────────────────────┤
│                                         │
│  AiController                          │
│  ├─ POST /api/ai/generate-products    │
│  └─ POST /api/ai/generate-bulk        │
│           │                            │
│           ▼                            │
│  ProductGenerationService              │
│  ├─ generateAndSave() [@Async]         │
│  ├─ Retry Logic (3 tries)              │
│  └─ publishEvent() [RabbitMQ]          │
│           │                            │
│           ▼                            │
│  GeminiApiService  + ImageService      │
│  ├─ generateProducts() [API Call]      │
│  ├─ parseResponse() [JSON Parse]       │
│  └─ getProductImage() [Unsplash]       │
│                                         │
│  ProductSyncScheduler                  │
│  └─ @Scheduled Daily (02:00 AM)        │
│     └─ 10 categories × 100 = 1000     │
│                                         │
└─────────────────────────────────────────┘
        │                  │
        ▼                  ▼
    RabbitMQ Events    PostgreSQL
    (Events)          (Data Store)
        │                  │
        ▼                  ▼
Product Service      BulkImport
(Consume Events)     Endpoint
```

---

## 🤖 AI Service - Detaylı Analiz

### 1. AiController.java

**Lokasyon:** `ai-service/src/main/java/com/eticaret/ai/controller/AiController.java`

**Görev:** REST API endpoints sağlama

```java
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI", description = "Yapay zeka servisleri")
public class AiController {

    private final ProductGenerationService productGenerationService;

    // ⭐ Endpoint 1: Manual Ürün Üretimi
    @PostMapping("/generate-products")
    @Operation(summary = "AI ile ürün üret ve kaydet")
    public ResponseEntity<Map<String, Object>> generateProducts(
            @RequestBody GenerateProductsRequest request) {
        
        // Request: {"category": "Elektronik", "categoryId": 1, "count": 20}
        // Response: {"success": true, "message": "20 ürün başarıyla oluşturuldu", "count": 20}
        
        int saved = productGenerationService.generateAndSave(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", saved + " ürün başarıyla oluşturuldu",
                "count", saved
        ));
    }

    // ⭐ Endpoint 2: Toplu Ürün Üretimi
    @PostMapping("/generate-bulk")
    @Operation(summary = "Tüm kategoriler için toplu ürün üret")
    public ResponseEntity<Map<String, Object>> generateBulk() {
        
        // 10 kategori × 100 ürün = 1000 toplam ürün
        List<Map<String, Object>> categories = List.of(
                Map.of("category", "Elektronik", "categoryId", 1L, "count", 100),
                Map.of("category", "Giyim", "categoryId", 2L, "count", 100),
                Map.of("category", "Ev & Yaşam", "categoryId", 3L, "count", 100),
                Map.of("category", "Spor", "categoryId", 4L, "count", 100),
                Map.of("category", "Kitaplar", "categoryId", 5L, "count", 100),
                // ... 5 kategori daha
        );

        int total = 0;
        for (Map<String, Object> cat : categories) {
            GenerateProductsRequest req = new GenerateProductsRequest(
                    (String) cat.get("category"),
                    (Long) cat.get("categoryId"),
                    (Integer) cat.get("count")
            );
            total += productGenerationService.generateAndSave(req);
        }

        // Response: {"success": true, "message": "Toplu üretim tamamlandı", "totalCount": 1000}
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Toplu üretim tamamlandı",
                "totalCount", total
        ));
    }
}
```

**Kullanım Örnekleri:**

```bash
# 1. Manual ürün üretimi
curl -X POST http://localhost:8087/api/ai/generate-products \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Elektronik",
    "categoryId": 1,
    "count": 20
  }'

# 2. Toplu üretim
curl -X POST http://localhost:8087/api/ai/generate-bulk

# 3. API Gateway üzerinden
curl -X POST http://localhost:8080/api/ai/generate-products \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

### 2. ProductGenerationService.java

**Lokasyon:** `ai-service/src/main/java/com/eticaret/ai/service/ProductGenerationService.java`

**Görev:** Ürün üretim iş mantığı

```java
@Service
public class ProductGenerationService {
    
    private final GeminiApiService geminiApiService;
    private final ImageService imageService;
    private final ProductEventPublisher eventPublisher;
    private final ProductRepository productRepository;
    
    private static final int MAX_RETRIES = 3;
    
    // ⭐ Asenkron ürün üretimi ve kaydetme
    @Async
    public int generateAndSave(GenerateProductsRequest request) {
        int saved = 0;
        int retries = 0;
        
        while (retries < MAX_RETRIES) {
            try {
                // Adım 1: Gemini API'den ürün oluştur
                List<GeneratedProduct> generatedProducts = 
                    geminiApiService.generateProducts(
                        request.getCategory(),
                        request.getCount()
                    );
                
                // Adım 2: Her ürün için görsek getir (Unsplash)
                for (GeneratedProduct gp : generatedProducts) {
                    String imageUrl = imageService.getProductImage(gp.getName());
                    gp.setImageUrl(imageUrl);
                }
                
                // Adım 3: Veritabanına kaydet
                List<Product> products = gp.stream()
                    .map(p -> new Product(
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        p.getImageUrl(),
                        request.getCategoryId(),
                        p.getRating()
                    ))
                    .collect(Collectors.toList());
                
                productRepository.saveAll(products);
                saved = products.size();
                
                // Adım 4: Event yayınla (RabbitMQ)
                products.forEach(product -> 
                    eventPublisher.publishProductAdded(product)
                );
                
                return saved; // Başarılı, döngüden çık
                
            } catch (Exception e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    throw new RuntimeException("3 denemeden sonra başarısız: " + e.getMessage());
                }
                // 2 saniye bekle ve tekrar dene
                Thread.sleep(2000);
            }
        }
        
        return saved;
    }
}
```

**İş Akışı:**

```
1. generateAndSave() çağrılır [@Async]
   ├─ Retry loop başlar (max 3)
   │
   ├─ Gemini API çağrısı
   │  └─ "Elektronik kategorisinde 20 ürün oluştur" → JSON
   │
   ├─ Image işlemesi
   │  └─ Her ürün için Unsplash'tan görsel getir
   │
   ├─ Database kaydetme
   │  └─ Product entity'leri PostgreSQL'e kaydet
   │
   ├─ Event yayınlama
   │  └─ RabbitMQ: product.added event
   │
   └─ Başarılı / Hata (retry)
```

---

### 3. GeminiApiService.java

**Lokasyon:** `ai-service/src/main/java/com/eticaret/ai/service/GeminiApiService.java`

**Görev:** Google Gemini API entegrasyonu

```java
@Service
public class GeminiApiService {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url}")
    private String geminiApiUrl; // https://generativelanguage.googleapis.com/v1/models/...
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // ⭐ Gemini API'den ürün oluştur
    public List<GeneratedProduct> generateProducts(String category, int count) {
        
        // Adım 1: Prompt hazırla
        String prompt = buildPrompt(category, count);
        // "Elektronik kategorisinde, her biri detaylı açıklamaya sahip, 20 adet gerçekçi ürün
        // JSON formatında oluştur. Format: [{name, description, price, stock, rating}, ...]"
        
        // Adım 2: API İsteği oluştur
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("temperature", 0.7);
        requestBody.put("maxOutputTokens", 2000);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + geminiApiKey);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            // Adım 3: API çağrısı
            ResponseEntity<String> response = restTemplate.postForEntity(
                geminiApiUrl + "?key=" + geminiApiKey,
                entity,
                String.class
            );
            
            // Adım 4: Response parse et
            String jsonResponse = response.getBody();
            // Example response:
            // {
            //   "candidates": [{
            //     "content": {
            //       "parts": [{
            //         "text": "[{\"name\":\"iPhone 15 Pro\",...}, ...]"
            //       }]
            //     }
            //   }]
            // }
            
            return parseGeminiResponse(jsonResponse);
            
        } catch (Exception e) {
            throw new RuntimeException("Gemini API çağrısı başarısız: " + e.getMessage());
        }
    }
    
    // ⭐ Prompt oluştur
    private String buildPrompt(String category, int count) {
        return """
            Aşağıda verilen kategoride, %s adet gerçekçi ve detaylı ürün ürün oluştur.
            
            Kategori: %s
            Ürün Sayısı: %d
            
            Kurallı:
            1. Her ürün benzersiz ve gerçekçi olmalı
            2. En az 50 karakter açıklama
            3. Hepsi aynı fiyat aralığında olabilir
            4. Stock: 10-100 arası
            5. Rating: 3.5-5.0 arası
            
            Response Format (JSON Array):
            [
              {
                "name": "Ürün Adı",
                "description": "Detaylı açıklama...",
                "price": 1999.99,
                "stock": 50,
                "rating": 4.5
              },
              ...
            ]
            
            Sadece JSON Array'i döndür, başka bir şey ekleme.
            """.formatted(count, category, count);
    }
    
    // ⭐ Response parse et
    private List<GeneratedProduct> parseGeminiResponse(String jsonResponse) throws JsonProcessingException {
        
        // Gemini response'ından text kısmını çıkar
        JsonNode root = objectMapper.readTree(jsonResponse);
        String textContent = root
            .path("candidates")
            .get(0)
            .path("content")
            .path("parts")
            .get(0)
            .path("text")
            .asText();
        
        // JSON Array parse et
        JavaType listType = objectMapper.getTypeFactory()
            .constructCollectionType(List.class, GeneratedProduct.class);
        
        return objectMapper.readValue(textContent, listType);
    }
}
```

**Entegrasyon Adımları:**

```bash
# 1. Gemini API Key al
# https://ai.google.dev/

# 2. application.yml'e ekle
gemini:
  api:
    key: ${GEMINI_API_KEY}  # Environment variable
    url: https://generativelanguage.googleapis.com/v1beta1/models/gemini-1.5-flash:generateContent

# 3. Dependencies ekle (pom.xml)
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### 4. ProductSyncScheduler.java

**Lokasyon:** `ai-service/src/main/java/com/eticaret/ai/scheduler/ProductSyncScheduler.java`

**Görev:** Günlük otomatik ürün senkronizasyonu

```java
@Component
public class ProductSyncScheduler {
    
    private final ProductGenerationService productGenerationService;
    
    private static final Logger logger = LoggerFactory.getLogger(ProductSyncScheduler.class);
    
    // ⭐ Her gün saat 02:00'de çalış
    @Scheduled(cron = "0 0 2 * * ?")  // 02:00 AM every day
    public void scheduledProductSync() {
        
        logger.info("⏱️ Scheduled product sync başlıyor...");
        
        // 10 kategori tanımla
        List<Map<String, Object>> categories = Arrays.asList(
                Map.of("category", "Elektronik", "categoryId", 1L, "count", 100),
                Map.of("category", "Giyim", "categoryId", 2L, "count", 100),
                Map.of("category", "Ev & Yaşam", "categoryId", 3L, "count", 100),
                Map.of("category", "Spor", "categoryId", 4L, "count", 100),
                Map.of("category", "Kitaplar", "categoryId", 5L, "count", 100),
                Map.of("category", "Mobil", "categoryId", 6L, "count", 100),
                Map.of("category", "Kameralar", "categoryId", 7L, "count", 100),
                Map.of("category", "Oyunlar", "categoryId", 8L, "count", 100),
                Map.of("category", "Moda", "categoryId", 9L, "count", 100),
                Map.of("category", "Sağlık", "categoryId", 10L, "count", 100)
        );
        
        int totalProducts = 0;
        
        // Her kategori için ürün oluştur
        for (Map<String, Object> category : categories) {
            try {
                GenerateProductsRequest request = new GenerateProductsRequest(
                        (String) category.get("category"),
                        (Long) category.get("categoryId"),
                        (Integer) category.get("count")
                );
                
                int saved = productGenerationService.generateAndSave(request);
                totalProducts += saved;
                
                logger.info("✅ {} kategorisinde {} ürün oluşturuldu", 
                    category.get("category"), saved);
                
                // Rate limiting: 2 saniye bekle (API limit)
                Thread.sleep(2000);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("❌ Kategoriye ürün oluşturma sırasında hata: {}", 
                    category.get("category"), e);
            }
        }
        
        logger.info("✅ Scheduled sync tamamlandı. Toplam {} ürün oluşturuldu", totalProducts);
    }
}
```

**Cron Expression Açıklaması:**

```
0 0 2 * * ?
│ │ │ │ │ │
│ │ │ │ │ └─ Day of Week (0-7, 0=Sunday) → ? (any)
│ │ │ │ └─── Month (1-12) → * (every month)
│ │ │ └───── Day of Month (1-31) → * (every day)
│ │ └─────── Hour (0-23) → 2 (02:00 AM)
│ └───────── Minute (0-59) → 0 (00 minutes)
└─────────── Second (0-59) → 0 (00 seconds)

Diğer Cron Örnekleri:
"0 0 * * * ?"        → Her saat (00:00)
"0 */6 * * * ?"      → Her 6 saatte bir
"0 0 0 1 * ?"        → Her ayın 1'inde
"0 0 12 ? * MON"     → Her pazartesi 12:00'de
```

---

### 5. ImageService.java

**Lokasyon:** `ai-service/src/main/java/com/eticaret/ai/service/ImageService.java`

**Görev:** Ürün görselleri Unsplash'tan getirme

```java
@Service
public class ImageService {
    
    @Value("${unsplash.api.key}")
    private String unsplashApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String UNSPLASH_API_URL = "https://api.unsplash.com";
    
    // ⭐ Ürün adına göre görsel getir
    public String getProductImage(String productName) {
        
        try {
            // Adım 1: Unsplash API'ye istek yap
            String searchQuery = URLEncoder.encode(productName, "UTF-8");
            String url = UNSPLASH_API_URL + "/search/photos" +
                         "?query=" + searchQuery +
                         "&per_page=1" +
                         "&client_id=" + unsplashApiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            // Adım 2: API çağrısı
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            // Adım 3: Response parse et
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.path("results");
            
            if (results.isArray() && results.size() > 0) {
                String imageUrl = results.get(0)
                    .path("urls")
                    .path("regular")  // 1080px width
                    .asText();
                
                return imageUrl;
            }
            
            // Fallback: Placeholder görsel
            return "https://via.placeholder.com/500x500?text=" + productName;
            
        } catch (Exception e) {
            logger.error("Unsplash görsel getirme başarısız: {}", e.getMessage());
            return "https://via.placeholder.com/500x500?text=Product";
        }
    }
}
```

**Setup:**

```bash
# 1. Unsplash API Key al
# https://unsplash.com/oauth/applications

# 2. application.yml'e ekle
unsplash:
  api:
    key: ${UNSPLASH_API_KEY}

# 3. Dependencies
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

### 6. GenerateProductsRequest.java

**Lokasyon:** `ai-service/src/main/java/com/eticaret/ai/dto/GenerateProductsRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateProductsRequest {
    
    @NotBlank(message = "Kategori adı gerekli")
    private String category;      // "Elektronik"
    
    @NotNull(message = "Kategori ID gerekli")
    private Long categoryId;      // 1L
    
    @Min(1)
    @Max(100)
    private Integer count;        // 1-100 ürün
}
```

---

## 📦 Product Service - Detaylı Analiz

### 1. ProductController.java

**Lokasyon:** `product-service-main/src/main/java/com/eticaret/product/domain/ProductController.java`

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    // ⭐ Toplu import endpoint
    @PostMapping("/bulk-import")
    @Operation(summary = "Toplu ürün import (CSV, JSON)")
    public ResponseEntity<BulkImportResponse> bulkImport(
            @RequestBody List<CreateProductRequest> requests) {
        
        // AI Service'ten gelen ürünleri import et
        BulkImportResponse response = productService.bulkImport(requests);
        
        return ResponseEntity.ok(response);
        // Response: {"totalCount": 100, "successCount": 98, "failureCount": 2}
    }
    
    // ⭐ Ürünleri listele (sayfalanmış, filtrelenmiş)
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        
        // Örnek: GET /api/products?page=0&size=20&categoryId=1&minPrice=100&maxPrice=5000&sortBy=price
        
        Page<ProductResponse> products = productService.getProducts(
            page, size, categoryId, minPrice, maxPrice, sortBy);
        
        return ResponseEntity.ok(products);
    }
    
    // ⭐ Fuzzy search
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        // Örnek: GET /api/products/search?query=laptp&limit=20
        // %75 eşleşme oranıyla ürünleri döndür
        
        List<ProductResponse> results = productService.fuzzySearch(query, limit);
        return ResponseEntity.ok(results);
    }
}
```

---

### 2. ProductService.java

**Lokasyon:** `product-service-main/src/main/java/com/eticaret/product/domain/ProductService.java`

```java
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final FuzzySearchService fuzzySearchService;
    private final ProductEventPublisher eventPublisher;
    private final StockAuthorizationService authService;
    
    // ⭐ Toplu import
    @Transactional
    public BulkImportResponse bulkImport(List<CreateProductRequest> requests) {
        
        int successCount = 0;
        int failureCount = 0;
        
        try {
            List<Product> products = requests.stream()
                .map(req -> {
                    Product product = new Product();
                    product.setName(req.getName());
                    product.setDescription(req.getDescription());
                    product.setPrice(req.getPrice());
                    product.setStock(req.getStock());
                    product.setImageUrl(req.getImageUrl());
                    product.setCategoryId(req.getCategoryId());
                    product.setRating(req.getRating());
                    return product;
                })
                .collect(Collectors.toList());
            
            List<Product> saved = productRepository.saveAll(products);
            successCount = saved.size();
            
            // Event yayınla
            saved.forEach(product -> 
                eventPublisher.publishProductAdded(
                    new ProductAddedEvent(product)
                )
            );
            
        } catch (Exception e) {
            failureCount = requests.size();
        }
        
        return BulkImportResponse.builder()
            .totalCount(requests.size())
            .successCount(successCount)
            .failureCount(failureCount)
            .build();
    }
    
    // ⭐ Fuzzy search (%75 eşleşme)
    public List<ProductResponse> fuzzySearch(String query, int limit) {
        
        // Query normalize et
        String normalizedQuery = normalize(query);
        
        // Tüm ürünleri getir ve filtrele
        List<Product> allProducts = productRepository.findAll();
        
        List<ProductResponse> results = allProducts.stream()
            .map(product -> {
                int similarity = fuzzySearchService
                    .calculateSimilarity(normalizedQuery, product.getName());
                
                // %75+ eşleşme oranı
                if (similarity >= 75) {
                    ProductResponse response = new ProductResponse(product);
                    response.setSimilarityPercentage(similarity);
                    return response;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> b.getSimilarityPercentage() - a.getSimilarityPercentage())
            .limit(limit)
            .collect(Collectors.toList());
        
        return results;
    }
    
    // ⭐ Ürünleri listele (sayfalanmış, filtrelenmiş, sıralanmış)
    public Page<ProductResponse> getProducts(
            int page, int size, Long categoryId, 
            BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, createSort(sortBy));
        
        Page<Product> products;
        
        if (categoryId != null && minPrice != null && maxPrice != null) {
            products = productRepository.findByCategoryIdAndPriceBetween(
                categoryId, minPrice, maxPrice, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        
        // Stock authorization kontrol et
        return products.map(product -> {
            ProductResponse response = new ProductResponse(product);
            
            // Sadece ROLE_ADMIN/ROLE_SELLER stok görebilir
            if (!authService.canViewStock()) {
                response.setStock(null);
            }
            
            return response;
        });
    }
    
    // ⭐ Sıralama oluştur
    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "price" -> Sort.by("price").ascending();
            case "name_asc" -> Sort.by("name").ascending();
            case "name_desc" -> Sort.by("name").descending();
            default -> Sort.by("createdAt").descending(); // En yeni
        };
    }
    
    // ⭐ String normalize (Türkçe karakterler)
    private String normalize(String str) {
        if (str == null) return "";
        return str.toLowerCase()
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u");
    }
}
```

---

### 3. FuzzySearchService.java

**Lokasyon:** `product-service-main/src/main/java/com/eticaret/product/features/search/FuzzySearchService.java`

```java
@Service
public class FuzzySearchService {
    
    private static final int SIMILARITY_THRESHOLD = 75; // %75
    
    // ⭐ İki string arasında benzerlik oranı hesapla (Levenshtein)
    public int calculateSimilarity(String query, String text) {
        if (query == null || text == null) return 0;
        
        query = query.toLowerCase();
        text = text.toLowerCase();
        
        // Exact match
        if (text.contains(query)) {
            return 100;
        }
        
        // Fuzzy match: Levenshtein distance
        int distance = levenshteinDistance(query, text);
        int maxLength = Math.max(query.length(), text.length());
        
        // (1 - normalized_distance) * 100
        return Math.max(0, (int) (100 * (1 - (double) distance / maxLength)));
    }
    
    // ⭐ Levenshtein distance algoritması
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        
        // İlk satır ve sütunu doldur
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        
        // DP tablosunu doldur
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j],       // deletion
                                 dp[i][j - 1]),     // insertion
                                 dp[i - 1][j - 1]); // substitution
                }
            }
        }
        
        return dp[a.length()][b.length()];
    }
    
    // ⭐ Benzerlik kontrol
    public boolean isSimilarEnough(String query, String text) {
        return calculateSimilarity(query, text) >= SIMILARITY_THRESHOLD;
    }
}
```

---

## 🔌 AI Araçları Entegrasyonu

### ChatGPT/OpenAI ile Entegrasyon

```java
@Service
public class OpenAiProductService {
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    @Value("${openai.model}")
    private String model; // "gpt-4-turbo"
    
    private final RestTemplate restTemplate;
    
    public List<GeneratedProduct> generateWithOpenAI(String category, int count) {
        
        String prompt = """
            %d adet %s kategorisinde ürün oluştur.
            JSON Array formatında döndür: [{name, description, price, stock, rating}]
            """.formatted(count, category);
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        request.put("temperature", 0.7);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://api.openai.com/v1/chat/completions",
            new HttpEntity<>(request, headers),
            String.class
        );
        
        // Parse response and return products
        // ...
        
        return new ArrayList<>();
    }
}
```

### Claude (Anthropic) Entegrasyonu

```java
@Service
public class ClaudeProductService {
    
    @Value("${anthropic.api.key}")
    private String claudeApiKey;
    
    private final RestTemplate restTemplate;
    
    public List<GeneratedProduct> generateWithClaude(String category, int count) {
        
        String prompt = """
            %d adet %s kategorisinde ürün oluştur.
            JSON formatında: [{name, description, price, stock, rating}]
            """.formatted(count, category);
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", "claude-3-opus-20240229");
        request.put("max_tokens", 2000);
        request.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", claudeApiKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://api.anthropic.com/v1/messages",
            new HttpEntity<>(request, headers),
            String.class
        );
        
        // Parse response
        return new ArrayList<>();
    }
}
```

### Ollama (Yerel LLM) Entegrasyonu

```bash
# 1. Ollama yükle
# https://ollama.ai

# 2. Model indir
ollama pull mistral

# 3. Çalıştır
ollama serve

# 4. Java'dan kullan
curl http://localhost:11434/api/generate \
  -d '{"model":"mistral","prompt":"..."}'
```

---

## 📊 AI Araçlanı Karşılaştırması

| Tool | Ücret | Hız | Kalite | Setup | Kullanım |
|------|-------|-----|--------|-------|----------|
| **Gemini (Google)** | Ücretsiz (100/day) | Hızlı | Çok İyi | Kolay | `RestTemplate` |
| **ChatGPT (OpenAI)** | $0.015/1K tokens | Hızlı | Mükemmel | Kolay | HTTP REST |
| **Claude (Anthropic)** | $3-15/1M tokens | Orta | Çok İyi | Kolay | HTTP REST |
| **Ollama (Yerel)** | Ücretsiz | Yavaş | İyi | Zor | HTTP |
| **Hugging Face** | Ücretsiz | Değişken | İyi | Orta | SDK/API |

---

## 🚀 Geliştirme Rehberi

### 1. Yeni AI Tool Ekleme

```java
// 1. Service oluştur
@Service
public class NewAiToolService {
    
    @Value("${new-tool.api.key}")
    private String apiKey;
    
    public List<GeneratedProduct> generateProducts(String category, int count) {
        // Implementation
        return new ArrayList<>();
    }
}

// 2. ProductGenerationService'te şart ekle
public List<GeneratedProduct> generateProducts(String category, int count, String aiTool) {
    return switch (aiTool) {
        case "gemini" -> geminiApiService.generateProducts(category, count);
        case "openai" -> openAiService.generateProducts(category, count);
        case "claude" -> claudeService.generateProducts(category, count);
        default -> geminiApiService.generateProducts(category, count);
    };
}

// 3. Request DTO'ya ekle
@Data
public class GenerateProductsRequest {
    private String category;
    private Long categoryId;
    private Integer count;
    private String aiTool; // "gemini", "openai", "claude"
}
```

### 2. Testing

```java
@SpringBootTest
class ProductGenerationServiceTest {
    
    @MockBean
    private GeminiApiService geminiApiService;
    
    @Autowired
    private ProductGenerationService service;
    
    @Test
    void testGenerateProducts() {
        // Mock data
        List<GeneratedProduct> mockProducts = Arrays.asList(
            new GeneratedProduct("iPhone 15", "...", 5999.99, 50, 4.5)
        );
        
        when(geminiApiService.generateProducts("Elektronik", 1))
            .thenReturn(mockProducts);
        
        // Test
        GenerateProductsRequest request = new GenerateProductsRequest(
            "Elektronik", 1L, 1);
        int saved = service.generateAndSave(request);
        
        assertEquals(1, saved);
    }
}
```

### 3. Monitoring & Logging

```java
@Aspect
@Component
public class AiServiceLogging {
    
    @Before("execution(* com.eticaret.ai.service.*.*(..))")
    public void logBeforeAiOperation(JoinPoint joinPoint) {
        logger.info("🤖 AI Operation başlıyor: {}", joinPoint.getSignature());
    }
    
    @After("execution(* com.eticaret.ai.service.*.*(..))")
    public void logAfterAiOperation(JoinPoint joinPoint) {
        logger.info("✅ AI Operation tamamlandı: {}", joinPoint.getSignature());
    }
}
```

---

## 📈 Performance Tuning

```yaml
# application.yml
ai-service:
  gemini:
    api:
      key: ${GEMINI_API_KEY}
      url: https://generativelanguage.googleapis.com/v1beta1/models/gemini-1.5-flash:generateContent
      max-retries: 3
      timeout: 30s
      batch-size: 10
    
    rate-limiting:
      requests-per-minute: 60
      delay-between-requests: 2000ms
    
    performance:
      async-threads: 5
      queue-size: 100
      cache-enabled: true
      cache-ttl: 24h

spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100
```

---

## ✅ Kontrol Listesi

Geliştirme sırasında kontrol etmeniz gerekenler:

- [ ] AI Tool API Key'si alındı
- [ ] RestTemplate bean'i yapılandırıldı
- [ ] Error handling eklendi
- [ ] Retry logic implementasyonu
- [ ] Rate limiting yapılandırması
- [ ] Database connection test edildi
- [ ] Event publishing test edildi
- [ ] Asenkron işleme test edildi
- [ ] Scheduler test edildi
- [ ] Load testing yapıldı
- [ ] Documentation yazıldı
- [ ] CI/CD pipeline updated

---

## 🐛 Hata Debugging

```bash
# Logs görüntüle
docker-compose logs -f ai-service

# Specific error
grep "ERROR" ai-service.log

# API test
curl -X POST http://localhost:8087/api/ai/generate-products \
  -H "Content-Type: application/json" \
  -d '{"category":"Elektronik","categoryId":1,"count":5}' \
  -v

# Database kontrol
psql -h localhost -p 5433 -U postgres -d ai_db \
  -c "SELECT COUNT(*) FROM products;"

# RabbitMQ events
curl -u eticaret:eticaret123 http://localhost:15672/api/queues
```

---

**🎉 Tüm AI Entegrasyon Rehberi Tamamlandı!**

Sorularınız varsa:
- 📖 DOCUMENTATION.md - Teknik detaylar
- 📁 Proje dosyaları - Kod örnekleri
- 🔧 SETUP.md - Kurulum adımları

---

**Version:** 1.0.0  
**Last Updated:** 1 May 2026

