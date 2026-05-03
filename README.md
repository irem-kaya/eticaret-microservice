# E-Ticaret Microservice Platform

[![CI/CD Pipeline](https://github.com/irem-kaya/eticaret-microservice/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/irem-kaya/eticaret-microservice/actions/workflows/ci-cd.yml)
[![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/AWS-Deployed-FF9900?logo=amazonaws&logoColor=white)](https://aws.amazon.com/)
[![License](https://img.shields.io/badge/License-MIT-22c55e)](LICENSE)

> **N11 Backend Talent Hub — Bitirme Projesi**  
> Spring Boot 3 + React 19 tabanlı, Trendyol/N11 tarzında gerçek dünya e-ticaret platformu. 9 bağımsız microservice, event-driven mimari, AI entegrasyonu ve tam CI/CD pipeline.

---

## 📑 İçindekiler

- [Proje Hakkında](#-proje-hakkında)
- [Mimari Kararlar](#-mimari-kararlar)
- [Tasarım Kalıpları & Yazılım Prensipleri](#-tasarım-kalıpları--yazılım-prensipleri)
- [Sistem Mimarisi](#-sistem-mimarisi)
- [Teknoloji Stack](#-teknoloji-stack)
- [Microservisler](#-microservisler)
- [Güvenlik Mimarisi](#-güvenlik-mimarisi)
- [Kurulum](#-kurulum)
- [API Referansı](#-api-referansı)
- [Frontend](#-frontend)
- [DevOps & CI/CD](#-devops--cicd)
- [Testler](#-testler)
- [Performans](#-performans)
- [Proje Gereksinimleri](#-proje-gereksinimleri)

---

## 📖 Proje Hakkında

Bu proje, **N11 Backend Talent Hub** programı kapsamında geliştirilen bitirme projesidir. Ürün listeleme, sepet yönetimi, sipariş oluşturma ve ödeme işlemlerini kapsayan, üretime hazır bir e-ticaret platformu sunar.

Projenin temel hedefi; gerçek dünya yazılım geliştirme pratiklerini yansıtmaktır. Bu doğrultuda monolitik mimari yerine **microservice mimarisi**, senkron iletişim yerine **event-driven asenkron iletişim** ve geleneksel deployment yerine **CI/CD otomasyonu** tercih edilmiştir.

### Öne Çıkan Yönler

- **9 bağımsız microservice** — her servis kendi veritabanına, bağımlılıklarına ve deploy sürecine sahip
- **AI entegrasyonu** — günlük 1.000 ürün otomatik üretimi ve MCP tabanlı öneri motoru
- **Fuzzy Search** — Levenshtein distance algoritması ile %75 eşleşme toleranslı arama
- **OAuth2 + Keycloak** — kurumsal düzeyde kimlik doğrulama ve RBAC
- **RabbitMQ** — Order → Notification akışında event-driven asenkron iletişim
- **Redis Cache** — 24 saatlik TTL ile ürün ve kategori önbelleklemesi

---

## 🏛️ Mimari Kararlar

Bu bölüm, projedeki kritik mimari seçimleri ve gerekçelerini açıklar.

### Neden Microservice?

| Kriter | Monolitik | Microservice (Tercih) |
|---|---|---|
| Bağımsız deploy | ❌ Tüm uygulama yeniden deploy edilir | ✅ Sadece değişen servis deploy edilir |
| Ölçekleme | ❌ Tüm uygulama ölçeklenir | ✅ Yalnızca yoğun servis ölçeklenir |
| Teknoloji çeşitliliği | ❌ Tek stack zorunlu | ✅ Her servis farklı teknoloji kullanabilir |
| Hata izolasyonu | ❌ Bir hata tüm sistemi etkiler | ✅ Hata ilgili servisle sınırlı kalır |
| Takım bağımsızlığı | ❌ Ortak codebase çakışmaları | ✅ Ekipler bağımsız çalışır |

**Karar:** Üretim ölçeğinde bir e-ticaret platformunda her domain (ürün, sipariş, ödeme) farklı yük profiline sahiptir. Microservice mimarisi bu gerçeği yansıtır.

### Neden Event-Driven İletişim?

Sipariş servisinin bildirim servisini **doğrudan HTTP çağrısı** ile tetiklemesi yerine **RabbitMQ** tercih edildi:

- Sipariş servisi, bildirim servisinin cevabını bekleme**z** → latency düşer
- Bildirim servisi geçici olarak çevrimdışı olursa mesajlar kuyrukta bekler → **dayanıklılık artar**
- İleride farklı consumer'lar (SMS, push notification) kolayca eklenebilir → **genişletilebilirlik**

### Neden Redis Cache?

Ürün listesi ve kategori verileri **okuma ağırlıklı** verilerdir. Her istekte PostgreSQL'e gitmek gereksiz yük oluşturur. 24 saatlik TTL ile Redis önbelleği bu yükü ortadan kaldırır.

### Neden Keycloak?

JWT üretimini ve kimlik doğrulamayı her serviste yeniden yazmak yerine Keycloak ile bu sorumluluk dışarıya taşındı. OAuth2 / OpenID Connect standardını tam destekler, refresh token yönetimini üstlenir ve RBAC konfigürasyonunu merkezi tutar.

### Neden Jib?

Dockerfile yazmak, katman optimizasyonu ve güvenlik güncellemeleri için ek bakım gerektirir. Jib, Maven plugin'i olarak çalışır; minimal, katmanlı ve tekrarlanabilir Docker imajları üretir.

---

## 🧩 Tasarım Kalıpları & Yazılım Prensipleri

### Mimari Kalıplar

**API Gateway Pattern**
Tüm dış istekler tek giriş noktasından geçer. İstemci 9 servisi ayrı ayrı bilmek zorunda değildir. JWT doğrulama, CORS ve rate limiting burada merkezi olarak çözülür; servisler bu cross-cutting concern'lerden arındırılmış kalır.

**Database per Service**
Her microservice yalnızca kendi veritabanına erişir. Cart Service, Product tablosuna doğrudan SQL atamaz; veri paylaşımı yalnızca API üzerinden gerçekleşir. Bu yaklaşım servis bağımsızlığını korur ve şema değişikliklerinin yayılmasını engeller.

**Event-Driven / Publisher-Subscriber**
Order Service, sipariş oluştuğunda RabbitMQ'ya `order.created` eventi yayınlar. Notification Service bu eventi bağımsız olarak tüketir. İkisi birbirini doğrudan çağırmaz — biri çökse diğeri etkilenmez, yeni consumer'lar mevcut kodu değiştirmeden eklenebilir.

```
Order Service ──publish──► [order.created] ──consume──► Notification Service
                             RabbitMQ                    (e-posta / SMS)
```

---

### GoF Tasarım Kalıpları

**Repository Pattern**
Veri erişim mantığı servis katmanından soyutlanır. `ProductRepository extends JpaRepository` ile servis katmanı veritabanı detaylarını bilmez; sadece repository arayüzüne bağımlıdır. PostgreSQL'den MongoDB'ye geçilse servis kodu değişmez.

```java
// Servis katmanı — veritabanı teknolojisinden bağımsız
public Product findById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
}
```

**Strategy Pattern**
Recommendation Service'teki 4 öneri kategorisi Strategy Pattern ile modellenmiştir. Her strateji farklı sıralama / filtreleme mantığı uygular ama aynı arayüzü implement eder. Yeni bir kategori eklemek mevcut kodu değiştirmeyi gerektirmez.

```java
// Her strateji aynı arayüzü uygular
interface RecommendationStrategy {
    List<Product> recommend(Long userId);
}

class BestSellerStrategy implements RecommendationStrategy { ... }  // 🔥 Çok Satılan
class BudgetFriendlyStrategy implements RecommendationStrategy { ... } // 💰 Bütçe Dostu
class PremiumStrategy implements RecommendationStrategy { ... }     // ✨ Premium
class BestValueStrategy implements RecommendationStrategy { ... }   // 🎯 En İyi Değer
```

**Observer Pattern**
RabbitMQ tabanlı event akışı Observer Pattern'in mesaj kuyruğu üzerindeki uygulamasıdır. Order Service (subject) durumunu değiştirir ve event yayınlar; Notification Service (observer) bu değişikliği dinleyerek tepki verir.

**Builder Pattern**
DTO ve entity oluşturma süreçlerinde Lombok `@Builder` ile uygulanır. Çok alanlı nesnelerde constructor karmaşasını önler, okunabilirliği artırır ve isteğe bağlı alanları net biçimde yönetir.

```java
ProductDTO dto = ProductDTO.builder()
    .id(product.getId())
    .name(product.getName())
    .price(product.getPrice())
    .stock(product.getStock())
    .build();
```

**Singleton Pattern**
Spring'deki tüm `@Service`, `@Repository`, `@Component` bean'leri varsayılan olarak singleton scope'ta yönetilir. Her bean JVM başına bir kez oluşturulur, bu bellek verimliliği ve durum tutarlılığı sağlar.

---

### Yazılım Prensipleri (SOLID)

| Prensip | Uygulama |
|---|---|
| **S** — Single Responsibility | Her servis tek domain'den sorumlu: Cart Service yalnızca sepet, Order Service yalnızca sipariş işleri |
| **O** — Open/Closed | Yeni öneri stratejisi eklemek için mevcut strateji sınıfları değiştirilmez, yeni sınıf yazılır |
| **L** — Liskov Substitution | Strategy implementasyonları temel arayüzle yer değiştirilebilir |
| **I** — Interface Segregation | Repository arayüzleri yalnızca ilgili metotları içerir, şişirilmez |
| **D** — Dependency Inversion | Servisler concrete sınıflara değil arayüzlere bağımlıdır; Spring DI bunu sağlar |

**DRY (Don't Repeat Yourself)**
`common` modülü bu prensip için bilinçli olarak oluşturulmuştur. Paylaşılan DTO'lar, exception sınıfları ve utility metodlar tek yerde tanımlanır; her serviste kopyalanmaz.

**Separation of Concerns — Katmanlı Mimari**

```
Controller  →  HTTP isteği alır, DTO ile yanıt döner
Service     →  iş mantığı; transaction yönetimi burada
Repository  →  yalnızca veri erişimi; SQL / JPA sorgular
DTO         →  katmanlar arası veri transferi (entity dışarı sızmaz)
Entity      →  veritabanı tablo eşlemesi; servis dışına çıkmaz
```

---

### Bilinen Mimari Kısıtlar

**Distributed Transaction / Saga Pattern**
Microservice'ler arası klasik ACID transaction uygulanamaz. Örneğin ödeme başarısız olduğunda siparişi geri almak için Saga Pattern gerekir. Bu projede tam Saga implementasyonu bulunmuyor; ödeme hatası durumunda kompanzasyon işlemi manuel yönetilmektedir. Gerçek üretim ortamı için Choreography-based Saga uygulanması planlanmaktadır.

**Servis Keşfi (Service Discovery)**
Servis adresleri şu an statik konfigürasyonla yönetilmektedir. Ölçeklenebilir bir ortamda Eureka veya Consul gibi bir service registry entegre edilmesi gerekir.

---

```
┌──────────────────────────────────────────────────────────────┐
│                  Frontend (React 19 + Vite)                  │
│                    http://localhost:5173                      │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTPS
┌────────────────────────────▼─────────────────────────────────┐
│                  API Gateway  :8080                          │
│        Spring Cloud Gateway — JWT Doğrulama,                 │
│            Yönlendirme, CORS, Rate Limiting                  │
└──┬─────────┬─────────┬──────────┬──────────┬─────────────────┘
   │         │         │          │          │
   ▼         ▼         ▼          ▼          ▼
┌──────┐ ┌──────┐ ┌────────┐ ┌───────┐ ┌──────┐
│User  │ │Prod. │ │ Cart   │ │ Order │ │ Pay  │
│Svc   │ │ Svc  │ │  Svc   │ │  Svc  │ │ Svc  │
│:8086 │ │:8082 │ │ :8081  │ │ :8083 │ │:8084 │
└──────┘ └──────┘ └────────┘ └───┬───┘ └──────┘
                                  │ publish event
┌──────┐ ┌──────┐          ┌──────▼───────┐
│  AI  │ │Recom.│          │   RabbitMQ   │
│ Svc  │ │ Svc  │          └──────┬───────┘
│:8087 │ │:8088 │                 │ consume event
└──────┘ └──────┘          ┌──────▼───────┐
                            │Notification  │
                            │Svc  :8085    │
                            └──────────────┘

─────────────── Infrastructure (Docker Compose) ───────────────
  PostgreSQL :5433  │  Redis :6380  │  RabbitMQ :5672/:15672
  Keycloak :8180    │  pgAdmin :5050
```

### Servisler Arası İletişim Stratejisi

```
┌─────────────────────────────────────────────────────────────┐
│  Senkron (REST / HTTP)                                       │
│  ─────────────────────                                       │
│  Kullanım: Kullanıcı isteğinin yanıtını beklemesi gereken    │
│  akışlar (ürün sorgulama, sepet işlemleri, sipariş detayı)  │
│                                                              │
│  Asenkron (RabbitMQ)                                         │
│  ─────────────────────                                       │
│  Kullanım: Sipariş oluşturulduğunda bildirim tetikleme.      │
│  Order Service → [order.created] → Notification Service      │
│  Avantaj: Bildirim gecikmesi sipariş akışını bloklamaz.      │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Teknoloji Stack

### Backend

| Katman | Teknoloji | Versiyon | Tercih Gerekçesi |
|---|---|---|---|
| Dil | Java | 17+ | Virtual Threads (Project Loom) desteği |
| Framework | Spring Boot | 3.2.5 | Olgun ekosistem, production-ready |
| API Gateway | Spring Cloud Gateway | — | Reaktif, yüksek verimli yönlendirme |
| ORM | Spring Data JPA / Hibernate | — | Standart Java kalıcılık katmanı |
| Veritabanı | PostgreSQL | 15 | ACID, JSON desteği, olgun ekosistem |
| Cache | Redis | 7 | Sub-millisecond latency, TTL desteği |
| Message Broker | RabbitMQ | 3 | Güvenilir mesaj kuyruğu, AMQP standardı |
| Auth | Keycloak + OAuth2 | 24 | Kurumsal IAM, OpenID Connect |
| Dokümantasyon | Swagger / OpenAPI 3 | — | Otomatik, interaktif API dokümantasyonu |
| Test | JUnit 5, Mockito, Testcontainers | — | Gerçek PostgreSQL ile entegrasyon testi |
| Build & Image | Maven + Jib | 3.8+ / 3.4.0 | Dockerfile gerektirmeyen, optimize imaj üretimi |

### Frontend

| Katman | Teknoloji | Versiyon |
|---|---|---|
| Framework | React | 19 |
| Build Tool | Vite | — |
| Styling | Tailwind CSS | 3 |
| HTTP Client | Axios | — |
| State | React Hooks (useState, useEffect, custom) | — |
| Auth | Keycloak-JS | — |

### DevOps & Cloud

| Araç | Rol |
|---|---|
| Docker + Docker Compose | Container ve lokal orkestrasyon |
| Jib (Maven Plugin) | Dockerfile'sız, katmanlı imaj üretimi |
| GitHub Actions | CI/CD pipeline otomasyonu |
| AWS EC2 | Uygulama sunucusu (product-service + frontend) |
| Slack Webhook | Deploy bildirimleri (çalışıyor ✅) |

---

## 📦 Microservisler

### 1. API Gateway — `:8080`

Tüm dış isteklerin tek giriş noktası. Sorumlulukları:

- JWT token doğrulama (Keycloak ile entegre)
- İlgili servise yönlendirme (routing)
- CORS politikası yönetimi
- Rate limiting (istek sınırlama)

### 2. User Service — `:8086`

Kullanıcı kaydı, profil yönetimi ve adres yönetimi. Keycloak ile federe kimlik doğrulama.

### 3. Product Service — `:8082`

Ürün CRUD, kategori yönetimi, sayfalı listeleme (20 ürün/sayfa), stok takibi. Redis önbelleği ile hızlandırılmış veri sunumu.

**Fuzzy Search — Levenshtein Distance:**
```java
// Yazım hatası toleranslı arama: "laptp" → "laptop" sonucunu döndürür
double similarity = calculateSimilarity(query, productName);
if (similarity >= 0.75) {
    results.add(product); // %75 eşleşme eşiği
}
```

### 4. Cart Service — `:8081`

Kullanıcıya özel sepet yönetimi. Ürün ekleme, çıkarma, miktar güncelleme ve anlık toplam fiyat hesaplama.

### 5. Order Service — `:8083`

Sipariş oluşturma ve durum makinesi yönetimi. Sipariş oluşturulduğunda RabbitMQ'ya event yayınlar.

```
Sipariş Durum Akışı:
PENDING → CONFIRMED → SHIPPED → DELIVERED
```

### 6. Payment Service — `:8084`

Iyzico ödeme altyapısı entegrasyonu. Ödeme başlatma, 3D doğrulama ve iade işlemleri.

### 7. AI Service — `:8087`

Yapay zeka destekli ürün açıklaması ve içerik üretimi. Cron job ile günlük 02:00'de otomatik bulk senkronizasyon.

```
POST /api/ai/generate-bulk
→ 10 kategori × 100 ürün = 1.000 ürün / gün (otomatik)
```

### 8. Recommendation Service — `:8088`

MCP tabanlı kişiselleştirilmiş öneri motoru. Dört öneri kategorisi:

- 🔥 **Çok Satılan** — satış hacmine göre
- 💰 **Bütçe Dostu** — fiyat/performans oranına göre
- ✨ **Premium** — kalite skoruna göre
- 🎯 **En İyi Değer** — çok boyutlu sıralama

### 9. Notification Service — `:8085`

RabbitMQ'dan gelen `order.created`, `order.shipped` gibi olayları dinler. E-posta ve SMS bildirimleri gönderir. Order Service ile **asenkron** — sipariş akışını bloklama**z**.

---

## 🔒 Güvenlik Mimarisi

```
İstek
  ↓
API Gateway ──► JWT Token Doğrulama (Keycloak Public Key)
  ↓
Keycloak    ──► OAuth2 / OpenID Connect token yönetimi
  ↓
Microservice ──► Spring Security — Rol Tabanlı Erişim Kontrolü (RBAC)
```

### Rol Yetki Matrisi

| Yetki | `ROLE_ADMIN` | `ROLE_SELLER` | `ROLE_USER` |
|---|:---:|:---:|:---:|
| Ürün CRUD (tüm) | ✅ | — | — |
| Kendi ürünlerini yönetme | ✅ | ✅ | — |
| Sepet / Sipariş oluşturma | ✅ | ✅ | ✅ |
| Kullanıcı yönetimi | ✅ | — | — |
| Raporlar | ✅ | — | — |

### Güvenlik Özellikleri

- **JWT Access Token** — 15 dakika geçerlilik süresi
- **Refresh Token** — 7 günlük yenileme süresi
- **HTTPS** — Production ortamında TLS zorunlu
- **CORS** — Yalnızca izin verilen origin'ler
- **Rate Limiting** — API Gateway seviyesinde istek sınırlama
- **BCrypt** — Şifre hashleme (Keycloak üzerinde)

---

## ⚙️ Kurulum

### Ön Koşullar

```
✅ Java 17+
✅ Node.js 18+
✅ Docker & Docker Compose
✅ Maven 3.8+
✅ Git
```

### 1. Repoyu Klonla

```bash
git clone https://github.com/irem-kaya/eticaret-microservice.git
cd eticaret-microservice
```

### 2. Environment Değişkenlerini Ayarla

```bash
cat > .env << EOF
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
RABBITMQ_USER=eticaret
RABBITMQ_PASSWORD=eticaret123
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
PGADMIN_EMAIL=admin@example.com
PGADMIN_PASSWORD=admin123
EOF
```

### 3. Infrastructure Servislerini Başlat

```bash
cd infra
docker-compose up -d
docker-compose ps   # Tüm servislerin healthy olduğunu doğrula
```

| Servis | Port | Yönetim Arayüzü |
|---|---|---|
| PostgreSQL | 5433 | pgAdmin → :5050 |
| Redis | 6380 | — |
| RabbitMQ | 5672 | Management UI → :15672 |
| Keycloak | 8180 | Admin Console → :8180 |

### 4. Keycloak Yapılandırması

```
URL: http://localhost:8180
Kullanıcı: admin / admin123

1. Realm oluştur: "eticaret"
2. Client oluştur: "eticaret-app" (confidential)
3. Redirect URIs:
   → http://localhost:5173/*
   → http://localhost:8080/*
4. Rol ekle: ROLE_ADMIN, ROLE_SELLER, ROLE_USER
```

### 5. Backend Servislerini Başlat

```bash
# Tüm modülleri derle
mvn clean package -DskipTests

# Her servis için ayrı terminal
mvn spring-boot:run -pl api-gateway             # :8080
mvn spring-boot:run -pl user-service            # :8086
mvn spring-boot:run -pl product-service-main    # :8082
mvn spring-boot:run -pl cart-service            # :8081
mvn spring-boot:run -pl order-service           # :8083
mvn spring-boot:run -pl payment-service         # :8084
mvn spring-boot:run -pl ai-service              # :8087
mvn spring-boot:run -pl recommendation-service  # :8088
mvn spring-boot:run -pl notification-service    # :8085
```

### 6. Frontend Başlat

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### Swagger UI

Her servisin OpenAPI dokümantasyonu API Gateway üzerinden tek noktada erişilebilir:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔌 API Referansı

> Base URL: `http://localhost:8080`  
> Korumalı endpoint'ler için: `Authorization: Bearer <token>`

### Ürünler

```bash
# Sayfalı listeleme (varsayılan: 20 ürün/sayfa)
GET /api/products?page=0&size=20

# Kategoriye göre filtreleme
GET /api/products?categoryId=1

# Fuzzy arama — yazım hataları tolere edilir
GET /api/products/search?query=laptp

# Fiyata göre artan sıralama
GET /api/products?sortBy=price&order=asc

# Ürün detayı
GET /api/products/{id}
```

### Sepet

```bash
# Sepeti getir
GET /api/cart

# Ürün ekle
POST /api/cart/items
{ "productId": 1, "quantity": 2 }

# Miktar güncelle
PUT /api/cart/items/{itemId}
{ "quantity": 5 }

# Ürünü çıkar
DELETE /api/cart/items/{itemId}

# Sepeti temizle
DELETE /api/cart
```

### Siparişler

```bash
# Sipariş oluştur
POST /api/orders
{ "addressId": 1, "paymentMethod": "CREDIT_CARD" }

# Sipariş geçmişi
GET /api/orders

# Sipariş detayı
GET /api/orders/{id}
```

### Kullanıcı

```bash
# Kayıt
POST /api/users/register
{ "email": "user@example.com", "password": "...", "name": "..." }

# Profil
GET /api/users/me

# Adres ekle
POST /api/users/addresses
{ "title": "Ev", "city": "İstanbul", "district": "Kadıköy", "detail": "..." }
```

### AI ve Öneri

```bash
# Tekil ürün üretimi
POST /api/ai/generate-products
{ "category": "Elektronik", "count": 20 }

# Bulk üretim (1.000 ürün)
POST /api/ai/generate-bulk

# Kategori bazlı öneri
GET /api/recommendations/category/{categoryId}

# Kullanıcıya özel öneri
GET /api/recommendations/user/{userId}
```

---

## 🎨 Frontend

### Proje Yapısı

```
frontend/
├── src/
│   ├── components/
│   │   ├── Navbar/        # Header, arama çubuğu, kategori navigasyonu
│   │   ├── Sidebar/       # Filtreler, grid görünüm ayarları
│   │   ├── ProductCard/   # Ürün kartı bileşeni
│   │   ├── Pagination/    # Sayfa navigasyon bileşeni
│   │   ├── Carousel/      # Benzer ürünler slider
│   │   ├── Toast/         # Kullanıcı bildirim sistemi
│   │   └── Footer/
│   ├── pages/
│   │   ├── Home/          # Ana sayfa + öne çıkan ürünler
│   │   ├── ProductList/   # Listeleme, filtreleme, sıralama
│   │   ├── ProductDetail/ # Ürün detay, resim carousel, stok
│   │   ├── Cart/          # Sepet yönetimi
│   │   ├── Order/         # Sipariş oluşturma ve takip
│   │   └── Profile/       # Kullanıcı profili ve adresler
│   ├── hooks/             # Custom React hooks
│   ├── services/          # Axios API katmanı
│   └── context/           # Auth ve Cart context
└── package.json
```

### Custom Hook Örneği

```javascript
// Ürün listesi yönetimi
const [products, setProducts] = useState([]);
const [loading, setLoading] = useState(true);
const [currentPage, setCurrentPage] = useState(0);

useEffect(() => {
  fetchProducts(currentPage);
}, [currentPage]);
```

---

## 🚀 DevOps & CI/CD

### GitHub Actions Pipeline

```
git push → main
    │
    ▼
① Checkout + Java 17 Setup
    │
    ▼
② Maven Build
   mvn clean package -DskipTests
    │
    ▼
③ Unit & Integration Testler
   mvn test
    │
    ▼
④ Jib — Docker Image Build
   (Dockerfile gerektirmez)
    │
    ▼
⑤ AWS EC2 Deploy
   Product Service + Frontend (JAR + static build)
    │
    ▼
⑥ Slack Bildirimi
   ✅ Başarılı / ❌ Hata
```

### Jib Konfigürasyonu

```xml
<plugin>
  <groupId>com.google.cloud.tools</groupId>
  <artifactId>jib-maven-plugin</artifactId>
  <version>3.4.0</version>
  <configuration>
    <to>
      <image>eticaret/${project.artifactId}:${project.version}</image>
    </to>
  </configuration>
</plugin>
```

**Jib vs Dockerfile:** Jib, minimal distroless base imaj kullanır; katmanları (dependencies, resources, classes) ayrı tutar, böylece sadece değişen katman push edilir. Güvenlik açıklarına daha az maruz kalır.

### AWS Deployment

Deployment sürecinde tek EC2 instance kullanıldı. Free tier sınırları nedeniyle 9 servisin tamamı aynı anda ayağa kaldırılamadı; **product-service** ve **frontend** başarıyla deploy edildi. Diğer servisler lokal Docker Compose ortamında çalışmaktadır.

| Bileşen | Durum | Notlar |
|---|:---:|---|
| Product Service | ✅ EC2'da çalışıyor | Ana iş servisi |
| Frontend (React) | ✅ EC2'da çalışıyor | Nginx ile serve ediliyor |
| Diğer 7 servis | 🔧 Lokal | EC2 RAM yetersizliği (t2.micro) |
| CI/CD Tetikleyici | ✅ | GitHub Actions |
| Slack Deploy Bildirimi | ✅ | Webhook ile çalışıyor |

> **Not:** Tüm 9 servisin production'da çalışması için minimum `t3.large` (8 GB RAM) EC2 instance gerekir. Gerçek bir üretim ortamında her servis ayrı container veya ECS task olarak deploy edilir.

---

## 🧪 Testler

```bash
# Tüm testleri çalıştır
mvn test

# Belirli bir servis
mvn test -pl product-service-main

# JaCoCo coverage raporu
mvn clean test jacoco:report
# → target/site/jacoco/index.html
```

### Test Stratejisi

```
product-service/
├── unit/
│   ├── ProductServiceTest.java      # İş mantığı — Mockito ile izole test
│   ├── ProductRepositoryTest.java   # Veri erişim katmanı
│   └── SearchServiceTest.java       # Fuzzy search algoritması
└── integration/
    ├── ProductControllerIT.java     # REST endpoint — MockMvc
    └── CartServiceIT.java           # Sepet akışı — Testcontainers
```

**Araçlar:** JUnit 5, Mockito, Testcontainers (gerçek PostgreSQL container), MockMvc

---

## 📈 Performans

| Optimizasyon | Yöntem | Kazanım |
|---|---|---|
| Veritabanı sorgu | Index'ler, lazy loading, N+1 koruması | Daha az I/O |
| API yanıt boyutu | Projection DTO'ları, pagination (20/sayfa) | Daha az veri transferi |
| Önbellekleme | Redis — 24 saatlik TTL | PostgreSQL yükü azalır |
| Asenkron işleme | RabbitMQ — non-blocking notification | Sipariş latency azalır |
| Thread yönetimi | Spring Boot 3 Virtual Threads (Project Loom) | Daha yüksek eş zamanlılık |

---

## 📁 Proje Yapısı

```
eticaret-microservice/
├── api-gateway/                # Spring Cloud Gateway
├── user-service/               # Kullanıcı yönetimi
├── product-service-main/       # Ürün & arama
├── cart-service/               # Sepet işlemleri
├── order-service/              # Sipariş yönetimi + event publisher
├── payment-service/            # Iyzico ödeme entegrasyonu
├── ai-service/                 # AI ürün üretimi
├── recommendation-service/     # MCP öneri motoru
├── notification-service/       # Email/SMS — event consumer
├── common/                     # Paylaşımlı DTO ve utilities
├── frontend/                   # React 19 + Vite
├── infra/                      # Docker Compose (infrastructure)
├── scripts/                    # Yardımcı betikler
├── .github/workflows/          # GitHub Actions CI/CD
├── pom.xml                     # Root Maven POM
├── ci-cd.yml                   # Pipeline tanımı
├── DOCUMENTATION.md            # Detaylı teknik dokümantasyon
└── SETUP.md                    # Kurulum kılavuzu
```

---

## ✅ Proje Gereksinimleri

### Backend

| Gereksinim | Durum | Uygulama |
|---|:---:|---|
| RESTful Web Servisi | ✅ | 9 servis, tam CRUD API'leri |
| PostgreSQL Veritabanı | ✅ | Her servis kendi schema'sına sahip |
| Pagination | ✅ | `Page<ProductDTO>`, 20 ürün/sayfa |
| Sepet İşlemleri | ✅ | Cart Service — ekleme, çıkarma, güncelleme |
| Sipariş Yönetimi | ✅ | Order Service — durum makinesi |
| Ödeme Entegrasyonu | ✅ | Iyzico SDK |
| JWT / Güvenlik | ✅ | Keycloak OAuth2 + RBAC |
| Unit & Integration Test | ✅ | JUnit 5 + Mockito + Testcontainers |
| Swagger/OpenAPI | ✅ | Her serviste otomatik dokümantasyon |
| Loglama | ✅ | SLF4J + Logback + MDC korelasyon ID'si |

### Frontend

| Gereksinim | Durum | Uygulama |
|---|:---:|---|
| Ürün Listeleme & Detay | ✅ | Kategori, filtre, carousel, stok |
| React Hooks | ✅ | useState, useEffect, custom hooks |
| Pagination UI | ✅ | Sayfa navigasyon bileşeni |
| Sepet UI | ✅ | Sepet sayfası ve header göstergesi |
| API Entegrasyonu | ✅ | Axios servis katmanı |
| Hata Yönetimi | ✅ | Toast bildirimleri, loading state |

### DevOps

| Gereksinim | Durum | Uygulama |
|---|:---:|---|
| Docker | ✅ | Dockerfile + docker-compose.yml |
| Jib | ✅ | Maven Jib plugin |
| CI/CD (GitHub Actions) | ✅ | Build → Test → Deploy pipeline |
| Jenkins Karşılaştırması | ✅ | `ci-cd.yml` içinde açıklama |
| AWS Deployment | ✅ | EC2 — product-service + frontend deploy edildi |
| Slack Bildirimleri | ✅ | Deploy sonucu webhook ile çalışıyor |

### Ek (İnisiyatif) Özellikler

| Özellik | Açıklama |
|---|---|
| 🤖 AI Service | Günlük 1.000 ürün otomatik üretimi |
| 🔍 Fuzzy Search | Levenshtein distance — %75 eşleşme toleransı |
| 🎯 Öneri Motoru | MCP tabanlı kişiselleştirilmiş öneriler |
| 💾 Redis Cache | 24 saatlik TTL ile ürün/kategori önbelleği |
| 📨 Event-Driven Bildirim | RabbitMQ asenkron notification akışı |
| 🌐 API Gateway | Merkezi yönlendirme, auth ve rate limiting |

---

## 🔗 Kaynaklar

- [Spring Boot Dokümantasyonu](https://spring.io/projects/spring-boot)
- [React Dokümantasyonu](https://react.dev)
- [Keycloak Dokümantasyonu](https://www.keycloak.org/documentation)
- [RabbitMQ Dokümantasyonu](https://www.rabbitmq.com/documentation.html)
- [Iyzico Java SDK](https://github.com/iyzico/iyzipay-java)
- [Detaylı Teknik Dokümantasyon](DOCUMENTATION.md)
- [Hızlı Kurulum Rehberi](SETUP.md)

---

## 📄 Lisans

MIT Lisansı — ayrıntılar için [LICENSE](LICENSE) dosyasına bakın.

---

<div align="center">

**🎓 N11 Backend Talent Hub — Bitirme Projesi**

*Spring Boot 3 · React 19 · PostgreSQL · Redis · RabbitMQ · Keycloak · Docker · AWS*

</div>
