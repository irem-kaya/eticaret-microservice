# 📚 Swagger/OpenAPI API Dokümantasyonu - Kurulum ve Kullanım Rehberi

**Versiyon:** 1.0  
**Status:** Production Ready  
**Framework:** Spring Boot 3.2.5 + SpringDoc OpenAPI

---

## 📋 İçerik

1. [Neden Swagger?](#neden-swagger)
2. [Kurulum](#kurulum)
3. [Konfigürasyon](#konfigürasyon)
4. [Controller Annotations](#controller-annotations)
5. [DTO Dokumentasyonu](#dto-dokumentasyonu)
6. [Security & Authorization](#security--authorization)
7. [Swagger UI Erişim](#swagger-ui-erişim)
8. [Curl Örnekleri](#curl-örnekleri)
9. [Advanced Features](#advanced-features)
10. [Troubleshooting](#troubleshooting)

---

## 🤔 Neden Swagger?

```
Swagger/OpenAPI Benefits:
✅ Otomatik API dokümantasyonu
✅ Interactive API testing
✅ Client SDK generation
✅ Endpoint discovery
✅ Request/Response examples
✅ Real-time API validation
✅ Team collaboration
✅ API versioning
```

---

## 🚀 Kurulum

### Adım 1: pom.xml'e Dependency Ekle

**Root pom.xml **:
```xml
<!-- ...existing code... -->
<dependencyManagement>
    <dependencies>
        <!-- ...existing code... -->
        
        <!-- SpringDoc OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.0.2</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Her Microservice'in pom.xml'ine:**
```xml
<dependencies>
    <!-- ...existing code... -->
    
    <!-- Swagger/OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

**Verify:**
```bash
mvn dependency:tree | grep springdoc
# Output: org.springdoc:springdoc-openapi-starter-webmvc-ui:jar:2.0.2
```

---

### Adım 2: application.yml Konfigürasyonu

**Her service'in application.yml:**
```yaml
# ...existing code...

springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    deepLinking: true
    showRequestHeaders: true
    filter: true
    syntaxHighlight:
      activate: true
      theme: monokai
  api-docs:
    path: /v3/api-docs
    groups:
      enabled: true
  show-actuator: true

# OpenAPI Info
springdoc.swagger-ui.apis-sort: tags
springdoc.swagger-ui.operations-sort: method
springdoc.swagger-ui.use-root-path: true
```

---

### Adım 3: OpenAPI Configuration Bean Oluştur

**src/main/java/com/eticaret/config/OpenApiConfig.java:**
```java
package com.eticaret.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Ticaret Microservice Platform - API")
                .description("Comprehensive e-commerce platform built with Spring Boot microservices")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-Ticaret Team")
                    .email("team@eticaret.com")
                    .url("https://eticaret.com")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token from /api/auth/login")
                )
            );
    }
}
```

---

## 🔧 Konfigürasyon

### Application Properties (Alternative)

**application.properties:**
```properties
# Swagger/OpenAPI
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.deepLinking=true
springdoc.api-docs.path=/v3/api-docs
springdoc.api-docs.groups.enabled=true

# Security
springdoc.swagger-ui.default-models-expand-depth=1
springdoc.swagger-ui.show-extensions=true
springdoc.swagger-ui.filter=true
```

---

## 📝 Controller Annotations

### Basit Product Controller Örneği

```java
package com.eticaret.product.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(
    name = "Product Management",
    description = "API endpoints for managing e-commerce products"
)
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    // ...dependencies...

    /**
     * Get all products with pagination and filtering
     */
    @GetMapping
    @Operation(
        summary = "List all products",
        description = "Retrieve paginated list of products with optional filters",
        tags = {"Product"}
    )
    @Parameters({
        @Parameter(
            name = "page",
            description = "Page number (0-indexed)",
            example = "0",
            schema = @Schema(type = "integer", defaultValue = "0")
        ),
        @Parameter(
            name = "size",
            description = "Page size (max 100)",
            example = "20",
            schema = @Schema(type = "integer", defaultValue = "20")
        ),
        @Parameter(
            name = "sortBy",
            description = "Sort field (createdAt, price, name_asc, name_desc)",
            example = "createdAt",
            schema = @Schema(
                type = "string",
                allowableValues = {"createdAt", "price", "name_asc", "name_desc"},
                defaultValue = "createdAt"
            )
        ),
        @Parameter(
            name = "categoryId",
            description = "Filter by category ID",
            example = "1"
        ),
        @Parameter(
            name = "keyword",
            description = "Search keyword (fuzzy search %75)",
            example = "laptop"
        ),
        @Parameter(
            name = "minPrice",
            description = "Minimum price filter",
            example = "100"
        ),
        @Parameter(
            name = "maxPrice",
            description = "Maximum price filter",
            example = "5000"
        )
    })
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "name": "Laptop",
                              "price": 2999.99,
                              "stock": 50,
                              "rating": 4.5
                            }
                          ],
                          "totalPages": 50,
                          "totalElements": 1000,
                          "currentPage": 0,
                          "pageSize": 20
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters",
            content = @Content(schema = @Schema(name = "ErrorResponse"))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Token required"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        List<Product> products = productService.findAll(page, size, sortBy, 
            categoryId, keyword, minPrice, maxPrice);
        
        return ResponseEntity.ok(new Page<>(products));
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get product details",
        description = "Retrieve detailed information about a specific product"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        
        Product product = productService.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        return ResponseEntity.ok(new ProductResponse(product));
    }

    /**
     * Create new product (Admin/Seller only)
     */
    @PostMapping
    @Operation(
        summary = "Create new product",
        description = "Create a new product (requires ADMIN or SELLER role)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions"
        )
    })
    public ResponseEntity<ProductResponse> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Product data",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateProductRequest.class))
            )
            @RequestBody CreateProductRequest request) {
        
        Product product = productService.create(request);
        return ResponseEntity.status(201).body(new ProductResponse(product));
    }

    /**
     * Update product
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update product",
        description = "Update an existing product",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Not the owner"
        )
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        
        Product product = productService.update(id, request);
        return ResponseEntity.ok(new ProductResponse(product));
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete product",
        description = "Delete a product (Admin only)"
    )
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search products with fuzzy matching
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Search products using fuzzy matching (75% similarity threshold)"
    )
    @Parameters({
        @Parameter(
            name = "query",
            description = "Search query",
            required = true,
            example = "laptp"
        ),
        @Parameter(
            name = "limit",
            description = "Maximum results",
            example = "10"
        )
    })
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Product> results = productService.fuzzySearch(query, limit);
        return ResponseEntity.ok(results.stream()
            .map(ProductResponse::new)
            .toList());
    }
}
```

---

## 📋 DTO Dokumentasyonu

### DTO Annotations

```java
package com.eticaret.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(
    name = "ProductResponse",
    description = "Product information response model"
)
public class ProductResponse {
    
    @Schema(
        description = "Unique product identifier",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
        description = "Product name",
        example = "MacBook Pro 14-inch",
        minLength = 3,
        maxLength = 255
    )
    private String name;

    @Schema(
        description = "Product description",
        example = "High-performance laptop for professionals",
        maxLength = 1000
    )
    private String description;

    @Schema(
        description = "Product price in TL",
        example = "2999.99",
        minimum = "0",
        exclusiveMinimum = true
    )
    private BigDecimal price;

    @Schema(
        description = "Available stock quantity",
        example = "50",
        minimum = "0"
    )
    private Integer stock;

    @Schema(
        description = "Product image URL",
        example = "https://images.unsplash.com/photo-...",
        format = "uri"
    )
    private String imageUrl;

    @Schema(
        description = "Product category",
        example = "Electronics",
        enumAsRef = true
    )
    private CategoryResponse category;

    @Schema(
        description = "Average rating (0-5)",
        example = "4.5",
        minimum = "0",
        maximum = "5"
    )
    private Double rating;

    @Schema(
        description = "Creation timestamp",
        example = "2024-05-01T10:30:00Z",
        format = "date-time",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;
}

@Schema(
    name = "CreateProductRequest",
    description = "Request body for creating a new product"
)
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Schema(description = "Product name", example = "Laptop", minLength = 3, maxLength = 255)
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Product description", maxLength = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin("0.01")
    @Schema(description = "Product price", example = "2999.99")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(0)
    @Schema(description = "Available stock", example = "50")
    private Integer stock;

    @URL(message = "Invalid image URL")
    @Schema(description = "Product image URL", format = "uri")
    private String imageUrl;

    @NotNull(message = "Category is required")
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
}
```

---

## 🔐 Security & Authorization

### SecurityScheme Konfigürasyonu

```java
@Configuration
public class OpenApiSecurityConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT token without 'Bearer ' prefix")
                )
                .addSecuritySchemes("api-key", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-KEY")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
            .addSecurityItem(new SecurityRequirement().addList("api-key"));
    }
}
```

### Protected Endpoint Örneği

```java
@PostMapping
@Operation(
    summary = "Create admin content",
    security = {
        @SecurityRequirement(name = "bearer-jwt"),
        @SecurityRequirement(name = "api-key")
    }
)
@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
public ResponseEntity<Response> createAdminContent() {
    // Only ADMIN role can access
    return ResponseEntity.ok(new Response("Created"));
}
```

---

## 🌐 Swagger UI Erişim

### URLs

| Service | URL | Port |
|---------|-----|------|
| **Product Service** | http://localhost:8082/swagger-ui.html | 8082 |
| **Cart Service** | http://localhost:8081/swagger-ui.html | 8081 |
| **Order Service** | http://localhost:8083/swagger-ui.html | 8083 |
| **Payment Service** | http://localhost:8084/swagger-ui.html | 8084 |
| **User Service** | http://localhost:8086/swagger-ui.html | 8086 |
| **AI Service** | http://localhost:8087/swagger-ui.html | 8087 |
| **Notification Service** | http://localhost:8085/swagger-ui.html | 8085 |
| **Recommendation Service** | http://localhost:8088/swagger-ui.html | 8088 |
| **API Gateway** | http://localhost:8080/swagger-ui.html | 8080 |

### JSON/YAML API Docs

```
http://localhost:8082/v3/api-docs
http://localhost:8082/v3/api-docs.yaml
```

---

## 🔄 Curl Örnekleri

### Get All Products

```bash
curl -X GET "http://localhost:8080/api/products?page=0&size=20&sortBy=createdAt" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Create Product

```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 2999.99,
    "stock": 50,
    "imageUrl": "https://images.unsplash.com/...",
    "categoryId": 1
  }'
```

### Search Products

```bash
curl -X GET "http://localhost:8080/api/products/search?query=laptp&limit=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Product by ID

```bash
curl -X GET "http://localhost:8080/api/products/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🚀 Advanced Features

### 1. Custom Swagger Configuration

```java
@Configuration
public class SwaggerUICustomConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        
        registry
            .addResourceHandler("/v3/api-docs/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
```

### 2. Group API by Tags

```java
@Configuration
public class OpenApiGroupsConfig {

    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
            .group("products")
            .pathsToMatch("/api/products/**")
            .packagesToScan("com.eticaret.product")
            .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
            .group("orders")
            .pathsToMatch("/api/orders/**")
            .packagesToScan("com.eticaret.order")
            .build();
    }

    @Bean
    public GroupedOpenApi cartApi() {
        return GroupedOpenApi.builder()
            .group("cart")
            .pathsToMatch("/api/cart/**")
            .packagesToScan("com.eticaret.cart")
            .build();
    }
}
```

### 3. Swagger UI Theme & Settings

```yaml
springdoc:
  swagger-ui:
    # UI Theme
    theme: dark                    # or light
    
    # Sorting
    operationsSorter: method       # or alpha
    tagsSorter: alpha
    
    # Features
    deepLinking: true
    persistAuthorization: true
    displayOperationId: true
    displayRequestDuration: true
    filter: true
    showCommonExtensions: true
    showExtensions: true
    
    # Layout
    layout: BaseLayout             # or StandaloneLayout
    docExpansion: list             # or full, none
    defaultModelsExpandDepth: 1
```

---

## 📊 OpenAPI Export

### JSON Export
```bash
curl http://localhost:8082/v3/api-docs > api-docs.json
```

### YAML Export
```bash
curl http://localhost:8082/v3/api-docs.yaml > api-docs.yaml
```

### Postman Import
1. Postman açarak "Import" tıkla
2. "Link" tab'ında URL yapıştır: `http://localhost:8082/v3/api-docs`
3. Collection otomatik oluşturulur

---

## 🐛 Troubleshooting

### Problem: Swagger UI görünmüyor (404)

**Çözüm:**
```bash
# 1. Dependency kontrol et
mvn dependency:tree | grep springdoc

# 2. pom.xml'de starter-webmvc-ui var mı?
# 3. Browser cache temizle (Ctrl+Shift+Delete)
# 4. Application restart
```

### Problem: Endpoints Swagger'da görünmüyor

**Çözüm:**
```java
// @RestController kullanmamış mı?
@RestController  // Not @Component
@RequestMapping("/api/...")
public class MyController {
    @GetMapping
    public ResponseEntity<?> getData() { }
}

// RequestMapping eksik mi?
@RestController
@RequestMapping("/api/products")  // REQUIRED
public class ProductController { }
```

### Problem: "No endpoints found" hatası

**Çözüm:**
```yaml
# application.yml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true  # Ensure enabled
  api-docs:
    path: /v3/api-docs  # Check path
```

### Problem: JWT Token vermiş olduğum halde 401

**Çözüm:**
```bash
# 1. "Bearer " prefix ekleme (Swagger otomatik ekliyor)
# 2. Token geçerliliğini kontrol et
# 3. SecurityConfig'de permitAll endpoints ekle

# Swagger UI herself authenticate değildir
# Test yapmak için "/api/public/**" endpoints ekle
```

---

## 📚 Complete Example: All Services

### Product Service Swagger

```
http://localhost:8082/swagger-ui.html

Endpoints:
├── GET    /api/products              (List with pagination)
├── GET    /api/products/{id}         (Get by ID)
├── GET    /api/products/search       (Fuzzy search)
├── POST   /api/products              (Create - Admin/Seller)
├── PUT    /api/products/{id}         (Update - Seller)
├── DELETE /api/products/{id}         (Delete - Admin)
└── GET    /api/categories            (Categories list)
```

### Order Service Swagger

```
http://localhost:8083/swagger-ui.html

Endpoints:
├── GET    /api/orders                (List - paginated)
├── GET    /api/orders/{id}           (Get by ID)
├── POST   /api/orders                (Create order)
├── PATCH  /api/orders/{id}/status    (Update status)
├── POST   /api/orders/{id}/cancel    (Cancel order)
└── GET    /api/orders/{id}/invoice   (Get invoice)
```

---

## ✨ Best Practices

```
1. Her endpoint'e @Operation annotation'ı ekle
2. Request/Response DTO'larını @Schema ile dokumante et
3. ApiResponse'ları tüm status codes için ekle
4. Security requirements belirt (@SecurityRequirement)
5. Parameter örnekleri (examples) ver
6. Validation annotations kullan (e.g., @NotNull, @Min)
7. Türkçe açıklamalar kullan ama isimler İngilizce kal
8. BaseUrl'i konfigürasyonda set et
9. API versioning planla (v1, v2, vb)
10. Regular update et (API değişirse dokümantasyonu güncelle)
```

---

## 🎯 Verification Checklist

```
☑️ Dependency pom.xml'e eklendi
☑️ OpenApiConfig.java oluşturuldu
☑️ application.yml konfigüre edildi
☑️ Controllers @Tag, @Operation annotations'a sahip
☑️ DTOs @Schema annotations'a sahip
☑️ Security configuration (JWT/API Key) tanımlandı
☑️ Swagger UI accessible (http://localhost:8082/swagger-ui.html)
☑️ /v3/api-docs endpoint'i çalışıyor
☑️ Curl test'leri çalıştırıldı
☑️ Postman'da import test edildi
```

---

## 📞 Quick Links

- **SpringDoc OpenAPI Docs:** https://springdoc.org/
- **OpenAPI 3.0 Spec:** https://spec.openapis.org/
- **Swagger UI:** https://swagger.io/tools/swagger-ui/
- **JSON Example:** http://localhost:8082/v3/api-docs

