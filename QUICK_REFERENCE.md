# ⚡ E-Ticaret Platform - Quick Reference Card

**Hızlı Referans Kartı - Geliştiriciler İçin**

---

## 🚀 30 Saniyede Başlangıç

```bash
# 1. Repo klonla
git clone <repo-url>
cd eticaret-microservice

# 2. Docker başlat
cd infra && docker-compose up -d && cd ..

# 3. Backend build
mvn clean package -DskipTests

# 4. Her servis için terminal aç ve çalıştır
cd api-gateway && mvn spring-boot:run        # Terminal 1
cd user-service && mvn spring-boot:run       # Terminal 2
cd product-service-main && mvn spring-boot:run # Terminal 3
# ... (diğer 6 servis)

# 5. Frontend
cd frontend && npm install && npm run dev    # Terminal n+1

# 6. Tarayıcıda aç
open http://localhost:5173
```

---

## 🔗 Önemli URL'ler

| Uygulama | URL | Username | Password |
|----------|-----|----------|----------|
| **Frontend** | http://localhost:5173 | - | - |
| **API Gateway** | http://localhost:8080 | - | - |
| **Keycloak** | http://localhost:8180 | admin | admin123 |
| **RabbitMQ** | http://localhost:15672 | eticaret | eticaret123 |
| **PgAdmin** | http://localhost:5050 | admin@example.com | admin123 |

---

## 📊 Ports Haritası

```
Docker Services:
├── PostgreSQL       5433
├── RabbitMQ AMQP    5672
├── RabbitMQ Web     15672
├── Redis            6380
├── Keycloak         8180
└── PgAdmin          5050

Spring Services:
├── API Gateway      8080
├── User Service     8086
├── Product Svc      8082
├── Cart Service     8081
├── Order Service    8083
├── Payment Svc      8084
├── Notification     8085
├── AI Service       8087
└── Recommendation   8088

Frontend:
└── React App        5173
```

---

## 📁 Proje Yapısı (Tree)

```
eticaret-microservice/
├── README.md                    ← START HERE
├── INDEX.md                     ← Dokümantasyon rehberi
├── SETUP.md                     ← Kurulum
├── DOCUMENTATION.md             ← Teknik detaylar
├── AI_DEVELOPMENT_GUIDE.md      ← AI araçları
│
├── api-gateway/                 ← 8080 (routing)
├── user-service/                ← 8086 (auth)
├── product-service-main/        ← 8082 (ürünler)
├── cart-service/                ← 8081 (sepet)
├── order-service/               ← 8083 (siparişler)
├── payment-service/             ← 8084 (ödeme)
├── notification-service/        ← 8085 (bildirimler)
├── ai-service/                  ← 8087 (ürün üretimi)
├── recommendation-service/      ← 8088 (öneriler)
├── common/                      ← Shared utilities
│
├── frontend/                    ← 5173 (React)
├── infra/                       ← Docker Compose
│   ├── docker-compose.yml
│   └── init-db.sql
└── scripts/                     ← Utility scripts
```

---

## 🤖 AI Ürün Çekme - 3 API

### 1. Manual Ürün Üretimi

```bash
# 20 adet Elektronik ürün üret
curl -X POST http://localhost:8080/api/ai/generate-products \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Elektronik",
    "categoryId": 1,
    "count": 20
  }'

# Response
{
  "success": true,
  "message": "20 ürün başarıyla oluşturuldu",
  "count": 20
}
```

### 2. Toplu Üretim (1000 ürün)

```bash
# 10 kategori × 100 ürün = 1000 ürün
curl -X POST http://localhost:8080/api/ai/generate-bulk

# Response
{
  "success": true,
  "message": "Toplu üretim tamamlandı",
  "totalCount": 1000
}
```

### 3. Otomatik Günlük Sync

```
⏰ Her gün 02:00 AM'de otomatik çalışır
📊 10 kategori × 100 ürün = 1000 ürün/gün
🔄 Asenkron işleme
📡 RabbitMQ event yayınlama
```

---

## 🔍 API Endpoints - Hızlı Referans

### Products

```bash
# Ürünleri listele (20/sayfa)
GET /api/products?page=0&size=20&categoryId=1&minPrice=100&maxPrice=5000

# Fuzzy Search (%75 eşleşme)
GET /api/products/search?query=laptop&limit=10

# Ürün detayı
GET /api/products/{id}

# Kategoriler
GET /api/categories
```

### Cart

```bash
# Sepeti getir
GET /api/cart

# Ürün ekle
POST /api/cart/items
{
  "productId": 1,
  "quantity": 2
}

# Ürün sil
DELETE /api/cart/items/{itemId}
```

### Orders

```bash
# Sipariş oluştur
POST /api/orders

# Siparişleri listele
GET /api/orders

# Sipariş durumu güncelle
PATCH /api/orders/{id}/status?status=SHIPPED

# En çok satılan ürünler
GET /api/orders/best-sellers?limit=10
```

### Recommendations

```bash
# Kategori önerileri
GET /api/recommendations/category/{categoryId}

# 4 tavsiye grubu:
# 🔥 Çok Satılan
# 💰 Bütçe Dostu
# ✨ Premium
# 🎯 En İyi Değer
```

---

## 🐛 Hatalı Olduğunda

```bash
# 1. Servisleri kontrol et
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8082/actuator/health  # Product Service

# 2. Logs görüntüle
docker-compose -f infra/docker-compose.yml logs -f postgres
docker-compose -f infra/docker-compose.yml logs -f rabbitmq

# 3. PostgreSQL kontrol
docker exec eticaret-postgres psql -U postgres -c "SELECT 1"

# 4. Redis kontrol
redis-cli -p 6380 ping

# 5. RabbitMQ kontrol
curl -u eticaret:eticaret123 http://localhost:15672/api/overview
```

---

## 🔑 Önemli Dosyalar

| Dosya | Amaç |
|-------|------|
| `ai-service/src/main/java/com/eticaret/ai/controller/AiController.java` | Ürün çekme API |
| `ai-service/src/main/java/com/eticaret/ai/service/ProductGenerationService.java` | Ürün üretim logic |
| `ai-service/src/main/java/com/eticaret/ai/service/GeminiApiService.java` | Gemini API client |
| `ai-service/src/main/java/com/eticaret/ai/scheduler/ProductSyncScheduler.java` | Günlük sync scheduler |
| `product-service-main/src/main/java/com/eticaret/product/domain/ProductService.java` | Ürün yönetimi |
| `product-service-main/src/main/java/com/eticaret/product/features/search/FuzzySearchService.java` | Fuzzy search |
| `frontend/src/services/fuzzySearch.js` | Frontend fuzzy search |
| `frontend/src/services/productService.js` | Product API calls |

---

## 💾 Database Commands

```bash
# PostgreSQL'e bağlan
psql -h localhost -p 5433 -U postgres -d postgres

# Veritabanları listele
\l

# E-Ticaret veritabanlarını kontrol et
SELECT datname FROM pg_database 
WHERE datname LIKE '%db%';

# product_db'ye bağlan
\c product_db

# Ürünleri say
SELECT COUNT(*) FROM products;

# İlk 10 ürün göster
SELECT id, name, price, stock FROM products LIMIT 10;

# Kategori dağılımı
SELECT category_id, COUNT(*) FROM products GROUP BY category_id;
```

---

## 🧪 Quick Tests

```bash
# Frontend açılıyor mu?
curl -s http://localhost:5173 | head -20

# API Gateway çalışıyor mu?
curl -s http://localhost:8080/actuator/health | jq .status

# Ürünleri getir
curl -s http://localhost:8080/api/products?page=0&size=5 | jq .

# Fuzzy search yap
curl -s "http://localhost:8080/api/products/search?query=samsung" | jq .

# Ürün detayı
curl -s http://localhost:8080/api/products/1 | jq .
```

---

## 📊 System Resources

```bash
# CPU & Memory kullanımı
docker stats --no-stream

# Disk kullanımı
df -h

# Network kontrol
netstat -an | grep LISTEN | grep -E ":(8080|8082|5173|5433)"

# Java process'leri
jps -l

# Maven cache boyutu
du -sh ~/.m2/repository
```

---

## 🔧 Common Commands

```bash
# Build
mvn clean install -DskipTests
mvn clean package -DskipTests

# Test
mvn test
mvn test -pl product-service-main

# Coverage
mvn clean test jacoco:report

# Frontend build
npm run build

# Frontend lint
npm run lint

# Docker komutlar
docker ps                                    # Çalışan containers
docker logs -f <container-name>             # Live logs
docker exec -it <container> bash            # Shell aç
docker-compose ps                           # Compose status
docker-compose restart <service>            # Hizmet yeniden başlat
docker system prune                         # Temizle
```

---

## 📚 Dokümantasyon Hızlı Erişim

```bash
# Tüm dokümantasyon dosyalarını aç (VS Code)
code README.md SETUP.md DOCUMENTATION.md AI_DEVELOPMENT_GUIDE.md INDEX.md

# Hangi dosyada ararsam?
# - Kurulum? → SETUP.md
# - Sistem mimarisi? → DOCUMENTATION.md
# - AI Tools? → AI_DEVELOPMENT_GUIDE.md
# - Genel? → README.md
# - Rehber? → INDEX.md
```

---

## ⚡ Performance Tips

```bash
# JVM optimization
export JAVA_OPTS="-Xmx2048m -Xms1024m -XX:+UseG1GC"

# Maven parallel build
mvn clean package -T 1C  # 1 thread per core

# Disable Docker logging
docker-compose -f infra/docker-compose.yml up -d --quiet-pull

# Skip tests
mvn clean package -DskipTests
```

---

## 🔐 Security Notes

```bash
# Passwords (Local development only!)
PostgreSQL: postgres123
RabbitMQ: eticaret123
Keycloak: admin123

# Production: Environment variables kullan!
export POSTGRES_PASSWORD=<strong-password>
export RABBITMQ_PASSWORD=<strong-password>
export KEYCLOAK_ADMIN_PASSWORD=<strong-password>
```

---

## 🎯 Development Workflow

```
1. README.md oku                    (5 min)
2. SETUP.md takip et ve kur        (60 min)
3. Frontend açı ve test et         (5 min)
4. Backend service'i kes (Ctrl+C)
5. Değişiklik yap
6. Test et (curl commands)
7. Git commit & push
8. CI/CD workflow'u kontrol et
```

---

## 📞 Help & Support

```bash
# GitHub Issues açabil
# Dokümantasyonda ara
# Logs'a bak
# curl ile test et
# Database'i kontrol et
```

---

## 🆚 Microservices Port Özeti

```
┌─────────────────────────────────────┐
│   API GATEWAY (8080)                │
│   ↓ Gateway tüm istekleri yönlendir │
├──────────────┬──────────────┬───────┤
│ User (8086)  │ Product(8082)│Order  │
│              │              │(8083) │
│ Cart (8081)  │ AI (8087)    │Payment│
│              │              │(8084) │
│Recommend.    │Notification │       │
│(8088)        │ (8085)       │       │
└──────────────┴──────────────┴───────┘
```

---

## ✅ Pre-Deployment Checklist

- [ ] Tüm services health check'ler PASS
- [ ] Frontend açılıyor
- [ ] API requests çalışıyor
- [ ] Database migration'lar tamam
- [ ] Environment variable'lar set
- [ ] Logs temiz (error yok)
- [ ] Load testing passed
- [ ] Security scan passed
- [ ] Documentation updated
- [ ] .env dosyası gitignore'da

---

## 🚀 One-Command Setup (macOS/Linux)

```bash
#!/bin/bash

# Clone
git clone <repo> && cd eticaret-microservice

# Docker
cd infra && docker-compose up -d && cd ..

# Build
mvn clean package -DskipTests &

# Wait for build
wait

# All services in background
mvn -pl api-gateway spring-boot:run &
mvn -pl user-service spring-boot:run &
mvn -pl product-service-main spring-boot:run &
mvn -pl cart-service spring-boot:run &
mvn -pl order-service spring-boot:run &
mvn -pl payment-service spring-boot:run &
mvn -pl ai-service spring-boot:run &
mvn -pl recommendation-service spring-boot:run &
mvn -pl notification-service spring-boot:run &

# Frontend
(cd frontend && npm install && npm run dev) &

echo "✅ All services started!"
echo "Frontend: http://localhost:5173"
echo "API Gateway: http://localhost:8080"
```

---

## 📱 Mobile Responsive

Frontend Tailwind'le responsive yapılmış:
- ✅ Mobile (320px+)
- ✅ Tablet (768px+)
- ✅ Desktop (1024px+)

---

**Version:** 1.0.0  
**Last Updated:** 1 May 2026

💡 **Pro Tip:** Bu dosyayı bookmark'la! (~5 KB, terminal'de de okuyabilirsin)

```bash
# Terminal'de oku
less QUICK_REFERENCE.md
```

---

