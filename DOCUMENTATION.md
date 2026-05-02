# 🏢 E-Ticaret Microservice Projesi - Detaylı Dokumantasyon

**Versiyon:** 1.0.0  
**Son Güncelleme:** Mayıs 2026  
**Teknik Stack:** Spring Boot 3.2.5 + React 19 + Vite + Tailwind CSS

---

## 📑 İçerik Tablosu

1. [Proje Özeti](#-proje-özeti)
2. [Sistem Mimarisi](#-sistem-mimarisi)
3. [Teknoloji Stack](#-teknoloji-stack)
4. [Microservisler](#-microservisler)
5. [Frontend](#-frontend)
6. [Database](#-database)
7. [API Endpoints](#-api-endpoints)
8. [Kurulum & Çalıştırma](#-kurulum--çalıştırma)
9. [CI/CD Pipeline](#-cicd-pipeline)
10. [Deployment](#-deployment)

---

## 🎯 Proje Özeti

**E-Ticaret Microservice**, modern bir e-ticaret platformudur ve şu özelliklere sahiptir:

### ✨ Temel Özellikleri

| Özellik | Açıklama |
|---------|----------|
| **Distributed Architecture** | 9 bağımsız microservice |
| **API Gateway** | Spring Cloud Gateway (8080) |
| **Authentication** | OAuth2 + Keycloak (8180) |
| **Database** | PostgreSQL (multi-database) |
| **Message Queue** | RabbitMQ (event-driven) |
| **Caching** | Redis (6380) |
| **AI Integration** | Ürün üretimi, öneriler |
| **Frontend** | React 19 + Tailwind CSS |
| **Search** | Fuzzy Search (%75 eşleşme) |
| **Recommendation** | MCP-tabanlı AI öneriler |

---

## 🏗️ Sistem Mimarisi

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend                             │
│                    (React 19 + Vite)                        │
│              http://localhost:5173                           │
└──────────────────────┬──────────────────────────────────────┘
                       │ Ajax Requests
┌──────────────────────▼──────────────────────────────────────┐
│                     API Gateway                             │
│              (Spring Cloud Gateway)                          │
│                  Port: 8080                                  │
│     Route: /api/** → Microservices                          │
└──────────────┬──────────────────────────┬────────────────────┘
               │                          │
    ┌──────────▼──────┬─────────────┐     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    │                 │             │     │
    └─ User Service   │ Product     │ Cart
      Port: 8086      │ Service     │ Port: 8081
      - Register      │ Port: 8082  │ - Add Item
      - Login         │ - Product   │ - Remove Item
      - Profile       │   CRUD      │ - Get Cart
                      │ - Search    │
                      │ - Filter    │
                      │ - Pagination│
                      │              
                      └─ AI Service Order Service Payment Service
                        Port: 8087  Port: 8083    Port: 8084
                        - Generate  - Create      - Process
                          Products  Order        Payment
                        - Sync      - List       - Webhook
                        - Scheduler Orders      - Refund

┌─────────────────────────────────────────────────────────────┐
│           Infrastructure (Docker Compose)                  │
├──────────┬─────────────┬──────────┬─────────────────────────┤
│PostgreSQL│  RabbitMQ   │  Redis   │      Keycloak          │
│5433      │   5672      │   6380   │       8180             │
│Multi-DB  │ Management: │  Cache   │    OAuth2 IAM          │
│          │   15672     │          │                        │
└──────────┴─────────────┴──────────┴─────────────────────────┘
```

---

## 🛠️ Teknoloji Stack

### Backend Stack

| Teknoloji | Versiyon | Amaç |
|-----------|----------|------|
| **Java** | 17+ | Backend dili |
| **Spring Boot** | 3.2.5 | Framework |
| **Spring Cloud** | 2023.0.1 | Microservices |
| **PostgreSQL** | 16 | Primary Database |
| **RabbitMQ** | 3.13 | Message Broker |
| **Redis** | 7.2 | Cache & Session |
| **Keycloak** | 24.0 | OAuth2/OIDC |
| **Flyway** | 9.16 | Database Migration |
| **FuzzyWuzzy** | 1.4.0 | Fuzzy Search |

### Frontend Stack

| Teknoloji | Versiyon | Amaç |
|-----------|----------|------|
| **React** | 19.2.5 | UI Framework |
| **Vite** | 8.0.10 | Build Tool |
| **Tailwind CSS** | 3.3.0 | Styling |
| **Axios** | 1.15.2 | HTTP Client |
| **React Router** | 7.14.2 | Routing |
| **PostCSS** | 8.4.31 | CSS Processing |

### DevOps Stack

| Teknoloji | Amaç |
|-----------|------|
| **Docker** | Containerization |
| **Docker Compose** | Orchestration |
| **Maven** | Build Tool |
| **Git** | Version Control |

---

## 📦 Microservisler

### 1. API Gateway (Port: 8080)

**Amaç:** Tüm client isteklerini ilgili microservice'lere yönlendirme

**Sorumluluklar:**
- Request routing
- Load balancing
- Authentication pass-through
- Rate limiting (opsiyonel)
- CORS handling

**Key Dependencies:**
- `spring-cloud-starter-gateway`
- `spring-boot-starter-security`
- `spring-security-oauth2-jose`

**Routes:**
```yaml
/api/users/** → User Service (8086)
/api/products/** → Product Service (8082)
/api/cart/** → Cart Service (8081)
/api/orders/** → Order Service (8083)
/api/payments/** → Payment Service (8084)
/api/ai/** → AI Service (8087)
/api/recommendations/** → Recommendation Service (8088)
/api/notifications/** → Notification Service (8085)
```

---

### 2. User Service (Port: 8086)

**Amaç:** Kullanıcı yönetimi, kimlik doğrulama, profil

**Veritabanı:** PostgreSQL - `user_db`

**Temel Özellikler:**
- ✅ User registration & login
- ✅ Profile management
- ✅ Role-based access control (RBAC)
- ✅ JWT token generation
- ✅ Keycloak integration

**Ana Endpointler:**
```
POST   /api/users/register          # Kayıt
POST   /api/users/login             # Giriş
GET    /api/users/{id}              # Profil
PUT    /api/users/{id}              # Güncelle
GET    /api/users/{id}/orders       # Siparişler
DELETE /api/users/{id}              # Sil (Admin)
```

**Entity:**
```java
public class User {
    Long id;
    String email;
    String username;
    String password;
    String firstName;
    String lastName;
    Set<Role> roles;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

---

### 3. Product Service (Port: 8082)

**Amaç:** Ürün yönetimi, arama, filtreleme

**Veritabanı:** PostgreSQL - `product_db`

**Temel Özellikler:**
- ✅ CRUD operations
- ✅ Category management
- ✅ Fuzzy search (%75 eşleşme)
- ✅ Price filtering
- ✅ Stock management
- ✅ Pagination (20 ürün/sayfa)
- ✅ Image management (Unsplash API)

**Ana Endpointler:**
```
GET    /api/products                # Listele (sayfalanmış)
GET    /api/products/{id}           # Detay
POST   /api/products                # Oluştur (Admin)
PUT    /api/products/{id}           # Güncelle (Seller)
DELETE /api/products/{id}           # Sil (Admin)
GET    /api/products/search         # Fuzzy search
POST   /api/products/bulk-import    # Toplu import
GET    /api/categories              # Kategoriler
```

**Query Parameters:**
```
?page=0&size=20                     # Pagination
?sortBy=price|name_asc|name_desc    # Sıralama
?categoryId=1                       # Kategori filtresi
?minPrice=100&maxPrice=5000         # Fiyat aralığı
?search=laptop                      # Arama (Fuzzy)
```

**Entity:**
```java
public class Product {
    Long id;
    String name;
    String description;
    BigDecimal price;
    Integer stock;
    String imageUrl;
    Category category;
    User seller;
    Integer rating;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

**Önemli Özellikler:**

**a) Fuzzy Search:**
```java
// Levenshtein distance algoritması
// Query: "laptp" → "laptop" (%75+ eşleşme)
public boolean isSimilarEnough(String query, String text) {
    return calculateSimilarity(query, text) >= SIMILARITY_THRESHOLD; // 75
}
```

**b) Stock Authorization (Rol-tabanı):**
```java
public boolean canViewStock() {
    // Sadece ROLE_ADMIN / ROLE_SELLER stok görebilir
    // Normal users → stock = null
}
```

**c) Pagination:**
```
Default: 20 ürün/sayfa
Max: 100
Page numbering: 0-indexed
```

---

### 4. Cart Service (Port: 8081)

**Amaç:** Sepet yönetimi

**Veritabanı:** PostgreSQL - `cart_db`

**Temel Özellikler:**
- ✅ Add to cart
- ✅ Remove from cart
- ✅ Update quantity
- ✅ Get cart
- ✅ Clear cart
- ✅ Cart persistence

**Ana Endpointler:**
```
GET    /api/cart                    # Sepeti getir
POST   /api/cart/items              # Ürün ekle
PUT    /api/cart/items/{id}         # Miktar güncelle
DELETE /api/cart/items/{id}         # Ürün sil
DELETE /api/cart                    # Sepeti temizle
GET    /api/cart/total              # Toplam tutar
```

**Entity:**
```java
public class Cart {
    Long id;
    User user;
    List<CartItem> items;
    BigDecimal totalPrice;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

public class CartItem {
    Long id;
    Product product;
    Integer quantity;
    BigDecimal unitPrice;
}
```

---

### 5. Order Service (Port: 8083)

**Amaç:** Sipariş yönetimi, takip

**Veritabanı:** PostgreSQL - `order_db`

**Temel Özellikler:**
- ✅ Create order (Chain of Responsibility pattern)
- ✅ Order tracking
- ✅ Status management
- ✅ Best sellers report
- ✅ Order history

**Ana Endpointler:**
```
POST   /api/orders                  # Sipariş oluştur
GET    /api/orders                  # Siparişleri listele
GET    /api/orders/{id}             # Sipariş detayı
PATCH  /api/orders/{id}/status      # Durumu güncelle
GET    /api/orders/best-sellers     # Top ürünler
```

**Sipariş Durumları:**
```
PENDING     → Beklemede
CONFIRMED   → Onaylandı
SHIPPED     → Kargoya verildi
DELIVERED   → Teslim edildi
CANCELLED   → İptal edildi
```

**Entity:**
```java
public class Order {
    Long id;
    User user;
    List<OrderItem> items;
    BigDecimal totalAmount;
    OrderStatus status;
    ShippingAddress shippingAddress;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

**Chain of Responsibility Pattern:**
```
OrderValidator → InventoryValidator → PaymentValidator → OrderCreator
```

---

### 6. Payment Service (Port: 8084)

**Amaç:** Ödeme işlemleri

**Veritabanı:** PostgreSQL - `payment_db`

**Temel Özellikler:**
- ✅ Process payment
- ✅ Webhook handling
- ✅ Refund management
- ✅ Payment history
- ✅ Invoice generation

**Ana Endpointler:**
```
POST   /api/payments                # Ödeme işle
GET    /api/payments/{id}           # Ödeme detayı
POST   /api/payments/{id}/refund    # İade et
POST   /api/payments/webhook        # Webhook
GET    /api/payments/invoices/{id}  # Fatura
```

---

### 7. Notification Service (Port: 8085)

**Amaç:** Email, SMS, push notifications

**Temel Özellikler:**
- ✅ Email notifications
- ✅ Order status updates
- ✅ Payment confirmations
- ✅ User alerts
- ✅ Template engine

**Ana Endpointler:**
```
POST   /api/notifications/email     # Email gönder
POST   /api/notifications/sms       # SMS gönder
GET    /api/notifications/{id}      # Bildirim detayı
```

---

### 8. AI Service (Port: 8087)

**Amaç:** Yapay zeka tabanlı ürün üretimi, tavsiyeler

**Veritabanı:** PostgreSQL - `ai_db`

**Temel Özellikler:**
- ✅ AI-powered product generation
- ✅ Asynchronous processing
- ✅ Retry logic (3 deneme)
- ✅ Scheduled syncing (02:00 AM günlük)
- ✅ Bulk import (10 kategori × 100 ürün = 1000 ürün)
- ✅ Image integration (Unsplash API)

**Ana Endpointler:**
```
POST   /api/ai/generate-products    # Manual ürün üret
POST   /api/ai/generate-bulk        # Toplu üretim (tüm kategoriler)
GET    /api/ai/import-jobs          # İthalat durumu
```

**Şimdiki Çalışma Mantığı:**

**a) Manual Generation:**
```java
POST /api/ai/generate-products
{
  "category": "Elektronik",
  "categoryId": 1,
  "count": 20
}
```

**b) Bulk Generation:**
```java
POST /api/ai/generate-bulk
// 10 kategori × 100 ürün = 1000 ürün
// Kategoriler:
// 1. Elektronik (20)
// 2. Giyim (20)
// 3. Ev & Yaşam (20)
// 4. Spor (20)
```

**c) Scheduled Sync:**
```java
@Scheduled(cron = "0 0 2 * * ?")  // Her gün 02:00 AM
public void scheduledProductSync() {
    // 1000 ürün otomatik üret ve kaydet
}
```

**Asenkron İşleme:**
```java
@Async
public void generateAndSaveAsync() {
    // 3 deneme ile retry
    // RabbitMQ event yayınla
}
```

---

### 9. Recommendation Service (Port: 8088)

**Amaç:** MCP tabanlı AI öneriler

**Veritabanı:** MongoDB (opsiyonel)  
**Cache:** Redis (24h TTL)

**Temel Özellikler:**
- ✅ MCP recommendation engine
- ✅ 4 tavsiye grubu:
  - 🔥 Çok Satılan Ürünler
  - 💰 Bütçe Dostu
  - ✨ Premium Seçim
  - 🎯 En İyi Değer
- ✅ Event-driven updates
- ✅ Redis caching
- ✅ REST API

**Ana Endpointler:**
```
GET    /api/recommendations/category/{id}  # Kategori önerileri
POST   /api/recommendations/refresh/{id}   # Yenile (Admin)
DELETE /api/recommendations/category/{id}  # Sil (Admin)
GET    /api/recommendations/trending       # Trend öneriler
```

**MCP Engine Logic:**
```
Best Sellers: 
  → Order Service'den en çok satılan ürünleri al
  → Kategori filtresi uygula
  → Redis'e cache et

Budget Friendly:
  → Price < avg_price && rating >= 4.0
  → Stock > 10
  → Katgori-spesifik

Premium Choice:
  → Price > 2 * avg_price && rating >= 4.5
  → Seller verified
  → Trend setters

Best Value:
  → Price/Rating ratio optimize et
  → (Rating * 100) / Price
```

---

## 🎨 Frontend

### Proje Yapısı

```
frontend/
├── src/
│   ├── components/
│   │   ├── Navbar.jsx              # Header + arama + kategori bar
│   │   ├── Sidebar.jsx             # Filters + grid ayarları
│   │   ├── Toast.jsx               # Notifications
│   │   ├── Pagination.jsx          # Sayfalama
│   │   ├── Carousel.jsx            # Slider (benzer ürünler)
│   │   ├── ProductCard.jsx         # Ürün kartı
│   │   ├── Header.jsx              # Top navigation
│   │   ├── Footer.jsx              # Alt kısım
│   │   └── Loading.jsx             # Skeleton loader
│   ├── pages/
│   │   ├── ProductListPage.jsx     # Ana sayfa
│   │   ├── ProductDetailPage.jsx   # Ürün detayı
│   │   ├── CartPage.jsx            # Sepet
│   │   ├── CheckoutPage.jsx        # Ödeme
│   │   ├── LoginPage.jsx           # Giriş
│   │   ├── RegisterPage.jsx        # Kayıt
│   │   ├── OrdersPage.jsx          # Siparişler
│   │   ├── OrderSuccessPage.jsx    # Başarı
│   │   └── ProfilePage.jsx         # Profil
│   ├── services/
│   │   ├── api.js                  # Axios config
│   │   ├── productService.js       # Ürün API
│   │   ├── cartService.js          # Sepet API
│   │   ├── authService.js          # Auth (Keycloak)
│   │   ├── orderService.js         # Sipariş API
│   │   ├── userService.js          # Kullanıcı API
│   │   └── fuzzySearch.js          # Arama algoritması
│   ├── context/
│   │   ├── AuthContext.jsx         # Auth state
│   │   └── CartContext.jsx         # Cart state
│   ├── hooks/
│   │   ├── useAuth.js              # Auth hook
│   │   ├── useCart.js              # Cart hook
│   │   ├── useToast.js             # Toast hook
│   │   └── usePagination.js        # Pagination hook
│   ├── App.jsx                     # Root component
│   ├── main.jsx                    # Entry point
│   └── index.css                   # Global styles
├── tailwind.config.js              # Tailwind konfigürasyonu
├── postcss.config.js               # PostCSS konfigürasyonu
├── vite.config.js                  # Vite konfigürasyonu
└── package.json                    # Dependencies

```

### React Component Hiyerarşisi

```
App
├── Header / Navbar
│   ├── Logo
│   ├── SearchBar (Fuzzy Search)
│   ├── CategoryBar
│   ├── UserMenu
│   └── CartIcon (Badge)
│
├── Layout
│   ├── Sidebar
│   │   ├── CategoryFilter
│   │   ├── PriceFilter
│   │   └── GridSettings (2/3/4)
│   │
│   └── MainContent
│       ├── Banner (Reklam Panosu)
│       │   ├── Card 1 (Elektronik)
│       │   ├── Card 2 (Giyim)
│       │   ├── Card 3 (Ev)
│       │   ├── Card 4 (Spor)
│       │   └── Card 5 (Teknoloji)
│       │
│       ├── ProductGrid
│       │   ├── ProductCard
│       │   │   ├── Image
│       │   │   ├── Title
│       │   │   ├── Price
│       │   │   ├── Rating
│       │   │   ├── Stock Badge
│       │   │   └── Add to Cart Button
│       │   └── (20 ürün/sayfa)
│       │
│       └── Pagination
│           ├── Prev Button
│           ├── Page Numbers
│           └── Next Button
│
└── Toast Notifications
    └── (Sepete ekleme, bilgiler, hatalar)
```

### Tailwind CSS Konfigürasyonu

```javascript
// tailwind.config.js
{
  colors: {
    red: { 50, 100, 600, 700 },        // Trendyol Red (#ec1c24)
    primary: { 50, 100, 500, 600, 700 }, // Brand colors
    secondary: { 400, 500, 600 },
    success: { 500, 600 },
    error: { 500, 600 },
    neutral: { 50-900 },
    gray: { 50-900 }
  },
  spacing: {
    nav: "80px",    // Navbar yüksekliği
    sidebar: "220px" // Sidebar genişliği
  },
  animations: {
    fade-in: "0.3s ease-out",
    slide-in: "0.4s ease-out",
    pulse: "2s cubic-bezier(0.4, 0, 0.6, 1) infinite"
  }
}
```

### API Integration (Axios)

```javascript
// services/api.js
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor - token ekleme
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - hata yönetimi
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Yeniden giriş yap
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### State Management

**AuthContext:**
```javascript
{
  user: { id, email, username, roles },
  token: "jwt_token",
  isAuthenticated: boolean,
  login: (email, password) => Promise,
  logout: () => void,
  register: (data) => Promise
}
```

**CartContext:**
```javascript
{
  items: [
    { id, productId, name, price, quantity, imageUrl },
    ...
  ],
  total: number,
  addItem: (product, quantity) => void,
  removeItem: (productId) => void,
  updateQuantity: (productId, quantity) => void,
  clearCart: () => void
}
```

### Fuzzy Search Algoritması

```javascript
// services/fuzzySearch.js
function calculateSimilarity(query, text) {
  // Türkçe karakter normalizasyonu
  const normalize = (str) => str
    .toLowerCase()
    .replace(/ç/g, 'c')
    .replace(/ğ/g, 'g')
    .replace(/ı/g, 'i')
    .replace(/ö/g, 'o')
    .replace(/ş/g, 's')
    .replace(/ü/g, 'u');

  const q = normalize(query);
  const t = normalize(text);

  // Levenshtein distance
  const dp = Array(q.length + 1).fill(0)
    .map(() => Array(t.length + 1).fill(0));

  for (let i = 1; i <= q.length; i++) {
    dp[i][0] = i;
  }
  for (let j = 1; j <= t.length; j++) {
    dp[0][j] = j;
  }

  for (let i = 1; i <= q.length; i++) {
    for (let j = 1; j <= t.length; j++) {
      if (q[i - 1] === t[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1];
      } else {
        dp[i][j] = 1 + Math.min(
          dp[i - 1][j],      // deletion
          dp[i][j - 1],      // insertion
          dp[i - 1][j - 1]   // substitution
        );
      }
    }
  }

  const distance = dp[q.length][t.length];
  const maxLength = Math.max(q.length, t.length);
  
  return Math.round((1 - distance / maxLength) * 100);
}

// Kullanım
const similarity = calculateSimilarity("laptp", "laptop"); // 85%
const isSimilar = similarity >= 75; // true
```

---

## 💾 Database

### Database Architecture

```
┌─────────────┐
│ PostgreSQL  │
│ Port: 5433  │
└────┬────────┘
     │
     ├── user_db              (User Service)
     ├── product_db           (Product Service)
     ├── cart_db              (Cart Service)
     ├── order_db             (Order Service)
     ├── payment_db           (Payment Service)
     ├── notification_db      (Notification Service)
     ├── ai_db                (AI Service)
     └── keycloak_db          (Keycloak)
```

### Flyway Migrations

**Lokasyon:** `src/main/resources/db/migration/`

**Format:** `V###__description.sql`

**Örnek Migrations:**

```sql
-- V001__create_ai_import_jobs_table.sql
CREATE TABLE IF NOT EXISTS ai_import_jobs (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    products_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V001__add_search_indexes.sql
CREATE INDEX idx_product_name ON products(LOWER(name));
CREATE INDEX idx_product_category_id ON products(category_id);
CREATE INDEX idx_product_price ON products(price);
CREATE INDEX idx_product_created_at ON products(created_at DESC);
```

### Redis Cache

**Amaç:** 
- Product recommendations caching (24h TTL)
- Session storage
- Rate limiting

**Key Patterns:**
```
recommendations:category:{id}      # Category-wise recommendations
recommendations:user:{userId}      # User-specific recommendations
search:cache:{query}               # Search results cache
session:{sessionId}                # User sessions
cart:{userId}                      # Cart data (optional)
```

---

## 📡 API Endpoints

### Complete API Reference

#### Authentication

```
POST   /api/users/register
POST   /api/users/login
POST   /api/users/refresh-token
POST   /api/users/logout
```

#### Products

```
GET    /api/products?page=0&size=20&sortBy=&categoryId=&minPrice=&maxPrice=
GET    /api/products/{id}
POST   /api/products                    (Admin/Seller)
PUT    /api/products/{id}               (Seller)
DELETE /api/products/{id}               (Admin)
GET    /api/products/search?query=&limit=10
POST   /api/products/bulk-import        (Admin)
GET    /api/categories
```

#### Cart

```
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{itemId}
DELETE /api/cart/items/{itemId}
DELETE /api/cart
GET    /api/cart/total
```

#### Orders

```
POST   /api/orders
GET    /api/orders?page=0&size=10
GET    /api/orders/{id}
PATCH  /api/orders/{id}/status?status=SHIPPED
GET    /api/orders/best-sellers?limit=10
POST   /api/orders/{id}/cancel           (User/Admin)
GET    /api/orders/{id}/invoice
```

#### Payments

```
POST   /api/payments
GET    /api/payments/{id}
POST   /api/payments/{id}/refund
POST   /api/payments/webhook
GET    /api/payments/invoices/{id}
```

#### AI Service

```
POST   /api/ai/generate-products
POST   /api/ai/generate-bulk
GET    /api/ai/import-jobs
GET    /api/ai/import-jobs/{jobId}/status
```

#### Recommendations

```
GET    /api/recommendations/category/{categoryId}
POST   /api/recommendations/refresh/{categoryId}   (Admin)
DELETE /api/recommendations/category/{categoryId}  (Admin)
GET    /api/recommendations/trending
```

#### Notifications

```
POST   /api/notifications/email
POST   /api/notifications/sms
GET    /api/notifications/{id}
GET    /api/notifications?page=0&size=20
```

---

## 🚀 Kurulum & Çalıştırma

### Ön Koşullar

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Git
- Maven 3.8+
- npm/yarn

### Adım 1: Repository Klonla

```bash
git clone https://github.com/your-repo/eticaret-microservice.git
cd eticaret-microservice
```

### Adım 2: Environment Variables Ayarla

```bash
# .env dosyası oluştur (root directory)
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
RABBITMQ_USER=eticaret
RABBITMQ_PASSWORD=eticaret123
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
PGADMIN_EMAIL=admin@example.com
PGADMIN_PASSWORD=admin123
```

### Adım 3: Docker Services Başlat

```bash
cd infra
docker-compose up -d

# Services kontrol et
docker-compose ps

# Logs görmek için
docker-compose logs -f postgres    # PostgreSQL
docker-compose logs -f rabbitmq    # RabbitMQ
docker-compose logs -f redis       # Redis
docker-compose logs -f keycloak    # Keycloak
```

### Adım 4: Veritabanı Migrate Et

```bash
# Flyway otomatik çalışacak (Spring Boot startup'ta)
# Manuel olarak çalıştırmak için:
mvn flyway:migrate -pl product-service-main
mvn flyway:migrate -pl ai-service
```

### Adım 5: Backend Microservices Başlat

```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run
# http://localhost:8080

# Terminal 2 - User Service
cd user-service
mvn spring-boot:run
# http://localhost:8086

# Terminal 3 - Product Service
cd product-service-main
mvn spring-boot:run
# http://localhost:8082

# Terminal 4 - Cart Service
cd cart-service
mvn spring-boot:run
# http://localhost:8081

# Terminal 5 - Order Service
cd order-service
mvn spring-boot:run
# http://localhost:8083

# Terminal 6 - Payment Service
cd payment-service
mvn spring-boot:run
# http://localhost:8084

# Terminal 7 - AI Service
cd ai-service
mvn spring-boot:run
# http://localhost:8087

# Terminal 8 - Recommendation Service
cd recommendation-service
mvn spring-boot:run
# http://localhost:8088

# Terminal 9 - Notification Service
cd notification-service
mvn spring-boot:run
# http://localhost:8085
```

### Adım 6: Frontend Başlat

```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

### Adım 7: Keycloak Konfigürasyonu

```
1. http://localhost:8180 gir
2. Username: admin
3. Password: admin123
4. Realm oluştur: "eticaret"
5. Client oluştur: "eticaret-app"
6. Redirect URIs ayarla:
   - http://localhost:5173/*
   - http://localhost:8080/*
```

---

## 📊 Service Health Checks

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8086/actuator/health

# Product Service
curl http://localhost:8082/actuator/health

# All services
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo "Port $port:"
  curl -s http://localhost:$port/actuator/health | jq .status
done
```

---

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

**File:** `.github/workflows/ci-cd.yml`

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2/repository
        key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    
    - name: Build Backend
      run: mvn clean package -DskipTests
    
    - name: Setup Node
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
    
    - name: Build Frontend
      run: |
        cd frontend
        npm install
        npm run build
    
    - name: Run Tests
      run: mvn test
    
    - name: SonarQube Analysis (optional)
      run: |
        mvn sonar:sonar \
          -Dsonar.projectKey=eticaret-microservice \
          -Dsonar.sources=. \
          -Dsonar.host.url=https://sonarcloud.io
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    
    - name: Build Docker Images
      run: |
        docker build -t eticaret/api-gateway:latest -f api-gateway/Dockerfile .
        docker build -t eticaret/product-service:latest -f product-service-main/Dockerfile .
        # ... other services
    
    - name: Push to Docker Registry
      run: |
        echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
        docker push eticaret/api-gateway:latest
        # ... other services
    
    - name: Deploy to Staging
      if: github.ref == 'refs/heads/develop'
      run: |
        # Deployment script
        kubectl apply -f k8s/staging/
    
    - name: Deploy to Production
      if: github.ref == 'refs/heads/main'
      run: |
        # Deployment script
        kubectl apply -f k8s/production/
```

---

## 🐳 Docker & Deployment

### Docker Build

```bash
# Her microservice için Dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/service-name-1.0.0.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: eticaret/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: production
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

---

## 📝 Monitoring & Logging

### Actuator Endpoints

```
/actuator/health              # Service health
/actuator/metrics             # Application metrics
/actuator/prometheus          # Prometheus metrics
/actuator/info                # Application info
/actuator/env                 # Environment variables
```

### Logging Configuration

```yml
logging:
  level:
    com.eticaret: DEBUG
    org.springframework: INFO
    org.springframework.data: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
```

---

## 🧪 Testing

### Backend Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn test -P integration

# Coverage report
mvn clean test jacoco:report
```

### Frontend Testing (Optional)

```bash
# Setup testing
cd frontend
npm install --save-dev vitest @testing-library/react

# Run tests
npm run test

# Coverage
npm run test -- --coverage
```

---

## 🔒 Security

### Authentication & Authorization

- **OAuth2 + Keycloak**: Merkezi kimlik doğrulama
- **JWT Tokens**: Stateless authentication
- **Role-Based Access Control (RBAC)**:
  - `ROLE_ADMIN`: Tüm operasyonlar
  - `ROLE_SELLER`: Ürün yönetimi
  - `ROLE_USER`: Alışveriş

### API Security

```java
// Spring Security Configuration
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/seller/**").hasRole("SELLER")
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### CORS Configuration

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://app.example.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## 📚 API Documentation (Swagger)

### Swagger UI Endpoints

```
API Gateway:        http://localhost:8080/swagger-ui.html
Product Service:    http://localhost:8082/swagger-ui.html
Order Service:      http://localhost:8083/swagger-ui.html
AI Service:         http://localhost:8087/swagger-ui.html
```

### OpenAPI Annotations

```java
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Ürün yönetimi")
public class ProductController {
    
    @GetMapping
    @Operation(summary = "Ürünleri listele")
    @Parameters({
        @Parameter(name = "page", example = "0"),
        @Parameter(name = "size", example = "20"),
        @Parameter(name = "sortBy", example = "price")
    })
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // ...
    }
}
```

---

## 🤝 Contribution Guidelines

### Git Workflow

```bash
# Feature branch oluştur
git checkout -b feature/feature-name

# Commit et
git add .
git commit -m "feat: description"

# Push et
git push origin feature/feature-name

# Pull Request aç
```

### Commit Message Convention

```
feat:    New feature
fix:     Bug fix
docs:    Documentation
style:   Formatting
refactor: Code restructuring
test:    Test additions
chore:   Build/dependency updates
```

---

## 📈 Performance Optimization

### Caching Strategy

```
├── Database Layer
│   └── Query caching (Spring Data properties)
├── Service Layer
│   └── @Cacheable annotations
├── API Gateway
│   └── Response caching
└── Frontend
    └── Browser caching
```

### Database Indexes

```sql
-- Product search optimization
CREATE INDEX idx_product_name ON products(LOWER(name));
CREATE INDEX idx_product_category_id ON products(category_id);
CREATE INDEX idx_product_price ON products(price);
CREATE INDEX idx_product_created_at ON products(created_at DESC);
CREATE INDEX idx_product_stock ON products(stock);
```

### Query Optimization

```java
// N+1 Problem Solution
@Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
List<Product> findAllWithCategory();

// Pagination
Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
Page<Product> products = productRepository.findAll(pageable);
```

---

## 🐛 Troubleshooting

### Sık Karşılaşılan Sorunlar

| Sorun | Çözüm |
|-------|-------|
| **Port 5433 zaten kullanımda** | `lsof -i :5433` ve process kill et |
| **Docker containers fail** | `docker-compose down && docker-compose up --build` |
| **Maven build hatası** | `mvn clean install -DskipTests` |
| **Node modules conflict** | `rm -rf node_modules && npm install` |
| **CORS hatası** | CorsConfig'de allowed origins kontrol et |
| **Keycloak token expired** | `localStorage.removeItem('access_token')` ve relogin |
| **RabbitMQ connection refused** | RabbitMQ container'ın çalıştığını kontrol et |

---

## 📞 Support & Contact

**İssue Reporting:** GitHub Issues  
**Documentation:** Wiki  
**Contact:** team@example.com

---

## 📄 License

MIT License - Kopyala, modifika et ve dağıt.

---

## 🎓 İleri Konular

### Message Queue (RabbitMQ)

**Event Publishing:**
```java
@Component
public class ProductEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishProductAdded(Product product) {
        rabbitTemplate.convertAndSend(
            "product-events",
            "product.added",
            new ProductAddedEvent(product)
        );
    }
}
```

**Event Listening:**
```java
@Component
public class ProductEventListener {
    
    @RabbitListener(queues = "product-queue")
    public void handleProductAdded(ProductAddedEvent event) {
        // Process event
    }
}
```

### Asynchronous Processing

```java
@Service
public class ProductGenerationService {
    
    @Async
    public void generateAndSaveAsync(GenerateProductsRequest request) {
        // Long-running operation
        // Non-blocking call
    }
}
```

### Chain of Responsibility Pattern (Orders)

```
OrderValidator
    ↓
InventoryValidator
    ↓
PaymentValidator
    ↓
OrderCreator (Success)
```

---

**Dokuman Son Güncelleme:** 1 Mayıs 2026

---

