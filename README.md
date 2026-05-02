# 🏢 E-Ticaret Microservice Platform

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-17+-red)
![React](https://img.shields.io/badge/React-19.2-blue)

**Trendyol/N11 tarzında modern, ölçeklenebilir e-ticaret platformu**

[🚀 Hızlı Başlangıç](#hızlı-başlangıç) • [📖 Dokümantasyon](./DOCUMENTATION.md) • [🤝 Katıl](#katılım) • [📞 İletişim](#iletişim)

</div>

---

## 🎯 Proje Özeti

**E-Ticaret Microservice** modern teknolojiler kullanarak inşa edilmiş, tam özellikli bir e-ticaret platformudur.

### ✨ Temel Özellikler

- 🏗️ **Microservice Architecture** - 9 bağımsız servis
- 🔒 **OAuth2 + Keycloak** - Güvenli kimlik doğrulama
- 🤖 **AI Integration** - Ürün üretimi & öneriler (MCP)
- 🔍 **Fuzzy Search** - %75 eşleşme oranı
- 💳 **Complete Payment** - Ödeme işlemleri
- 📱 **Modern Frontend** - React 19 + Tailwind CSS
- 🚀 **Event-Driven** - RabbitMQ ile asenkron işleme
- 💾 **Redis Caching** - Yüksek performans
- 🐳 **Docker Ready** - Container orchestration
- 📊 **Comprehensive APIs** - Swagger/OpenAPI

---

## 🚀 Hızlı Başlangıç

### Ön Koşullar

```bash
✅ Java 17+
✅ Node.js 18+
✅ Docker & Docker Compose
✅ Maven 3.8+
✅ Git
```

### 1️⃣ Repository Klonla & Setup

```bash
# Klonla
git clone https://github.com/your-repo/eticaret-microservice.git
cd eticaret-microservice

# Environment variables ayarla
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

### 2️⃣ Docker Services Başlat

```bash
cd infra
docker-compose up -d

# Kontrol et
docker-compose ps
```

### 3️⃣ Backend Services Başlat

```bash
# Tüm servisleri build et
mvn clean package -DskipTests

# Terminal'ler aç ve servisleri başlat:
mvn spring-boot:run -pl api-gateway         # 8080
mvn spring-boot:run -pl user-service        # 8086
mvn spring-boot:run -pl product-service-main # 8082
mvn spring-boot:run -pl cart-service        # 8081
mvn spring-boot:run -pl order-service       # 8083
mvn spring-boot:run -pl payment-service     # 8084
mvn spring-boot:run -pl ai-service          # 8087
mvn spring-boot:run -pl recommendation-service # 8088
mvn spring-boot:run -pl notification-service # 8085
```

### 4️⃣ Frontend Başlat

```bash
cd frontend
npm install
npm run dev

# Tarayıcıda aç: http://localhost:5173
```

### 5️⃣ Keycloak Konfigürasyonu

```
URL: http://localhost:8180
Username: admin
Password: admin123

1. Realm oluştur: "eticaret"
2. Client oluştur: "eticaret-app"
3. Redirect URIs ayarla:
   - http://localhost:5173/*
   - http://localhost:8080/*
```

---

## 📊 Sistem Mimarisi

```
Frontend (React 19)
    ↓
API Gateway (8080)
    ↓
┌─────────────────────────────────────────────┐
│         Microservices (Spring Boot)         │
├─────────┬─────────┬─────────┬─────────┬─────┤
│ User    │Product  │ Cart    │ Order   │Pay  │
│Service  │Service  │Service  │Service  │Svc  │
│(8086)   │(8082)   │(8081)   │(8083)   │8084 │
└─────────┴─────────┴─────────┴─────────┴─────┘
    ↓
┌─────────────────────────────────────────────┐
│    Infrastructure (Docker Compose)          │
├─────────┬─────────┬──────────┬──────────────┤
│PostgreSQL│RabbitMQ│ Redis    │  Keycloak   │
│  5433   │ 5672   │  6380    │    8180     │
└─────────┴─────────┴──────────┴──────────────┘
```

---

## 📦 Microservices

| Service | Port | Açıklama |
|---------|------|----------|
| **API Gateway** | 8080 | Request routing |
| **User Service** | 8086 | Kullanıcı yönetimi |
| **Product Service** | 8082 | Ürün yönetimi, arama |
| **Cart Service** | 8081 | Sepet yönetimi |
| **Order Service** | 8083 | Sipariş yönetimi |
| **Payment Service** | 8084 | Ödeme işlemleri |
| **AI Service** | 8087 | Ürün üretimi, tavsiyeler |
| **Recommendation Service** | 8088 | MCP öneriler |
| **Notification Service** | 8085 | Email/SMS bildirimleri |

---

## 🎨 Frontend Özelikleri

### Components

- ✅ **Navbar** - Header + arama + kategori bar
- ✅ **Sidebar** - Filtreler + grid ayarları
- ✅ **Product Cards** - Modern tasarım
- ✅ **Pagination** - Sayfalama (20 ürün/sayfa)
- ✅ **Carousel** - Benzer ürünler slider
- ✅ **Toast** - Bildirimler
- ✅ **Footer** - Alt kısım

### Styling

- 🎨 **Tailwind CSS** - Modern CSS framework
- 🔴 **Trendyol Renkler** - Brand consistency
- 📱 **Responsive Design** - Mobile-first
- ✨ **Smooth Animations** - Professional feel

---

## 🔍 Önemli Özellikler

### 1. Fuzzy Search (%75 Eşleşme)

```javascript
// Levenshtein distance algoritması
const similarity = calculateSimilarity("laptp", "laptop"); // 85%
const isSimilar = similarity >= 75; // true
```

### 2. AI Ürün Üretimi

```
POST /api/ai/generate-bulk
→ 10 kategori × 100 ürün = 1000 ürün
→ Günlük 02:00 AM otomatik sync
```

### 3. MCP Öneriler

```
GET /api/recommendations/category/{id}
→ 🔥 Çok Satılan
→ 💰 Bütçe Dostu
→ ✨ Premium
→ 🎯 En İyi Değer
```

### 4. Rol Tabanlı Erişim

```
ROLE_ADMIN    → Tüm operasyonlar
ROLE_SELLER   → Ürün yönetimi
ROLE_USER     → Alışveriş
```

---

## 📊 API Endpoints

### Products

```bash
# Listele (20 ürün/sayfa)
curl http://localhost:8080/api/products?page=0&size=20

# Kategori filtreleme
curl http://localhost:8080/api/products?categoryId=1

# Fuzzy arama
curl http://localhost:8080/api/products/search?query=laptop

# Sıralama
curl "http://localhost:8080/api/products?sortBy=price"
```

### Cart

```bash
# Sepeti getir
curl http://localhost:8080/api/cart

# Ürün ekle
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'
```

### Orders

```bash
# Sipariş oluştur
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{...}'

# Siparişleri listele
curl http://localhost:8080/api/orders
```

### AI Service

```bash
# Ürün üret
curl -X POST http://localhost:8080/api/ai/generate-products \
  -H "Content-Type: application/json" \
  -d '{"category": "Elektronik", "count": 20}'
```

---

## 🔒 Security

- ✅ OAuth2 + Keycloak
- ✅ JWT Token-based auth
- ✅ CORS configured
- ✅ Role-based access control
- ✅ HTTPS ready (production)

---

## 📖 Detaylı Dokümantation

👉 **[Tam dokümantationı oku](./DOCUMENTATION.md)**

Detaylı bilgi için:
- System architecture
- Database schema
- API reference
- Setup & deployment
- Troubleshooting

---

## 🧪 Testing

```bash
# Backend testleri çalıştır
mvn test

# Frontend testleri (optional)
cd frontend && npm run test

# Coverage raporu
mvn clean test jacoco:report
```

---

## 📈 Performance

- ⚡ Redis caching (24h TTL)
- ⚡ Database indexes
- ⚡ Pagination (20 items/page)
- ⚡ Virtual threads (Spring Boot 3)
- ⚡ Async processing (RabbitMQ)

---

## 🐳 Docker

```bash
# Build
docker build -t eticaret/api-gateway:latest -f api-gateway/Dockerfile .

# Run
docker run -p 8080:8080 eticaret/api-gateway:latest

# Docker Compose
docker-compose up -d
docker-compose down
```

---

## 🚀 Deployment

### Production Checklist

- [ ] Environment variables ayarla
- [ ] SSL certificates konfigure et
- [ ] Database backup kur
- [ ] Monitoring (Prometheus/Grafana)
- [ ] Logging (ELK Stack)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Kubernetes deployment

---

## 🤝 Katılım

Katkılarınızı bekliyoruz! Lütfen:

1. Fork et
2. Feature branch oluştur (`git checkout -b feature/amazing-feature`)
3. Commitler (`git commit -m 'feat: amazing feature'`)
4. Push et (`git push origin feature/amazing-feature`)
5. Pull Request aç

---

## 📝 Commit Convention

```
feat:      New feature
fix:       Bug fix
docs:      Documentation
style:     Formatting changes
refactor:  Code restructuring
test:      Test additions
chore:     Build/dependency updates
ci:        CI/CD changes
```

---

## 📞 İletişim

- **Email:** team@example.com
- **Issues:** GitHub Issues
- **Discussions:** GitHub Discussions
- **Wiki:** Project Wiki

---

## 📄 License

MIT License - Kopyala, modifika et, dağıt. Ücretsiz!

```
MIT License

Copyright (c) 2026 E-Ticaret Microservice Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 🎓 Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [Tailwind CSS](https://tailwindcss.com)
- [Docker Documentation](https://docs.docker.com)
- [PostgreSQL Documentation](https://www.postgresql.org/docs)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

---

## 🙏 Teşekkürler

Bu proje için katkıda bulunanlara teşekkür ederiz!

---

## 🔔 Haberdar Kalın

⭐ Projeyi yıldızla  
🔔 Releases'ı takip et  
📧 Newsletter'a abone ol

---

**Made with ❤️ by E-Ticaret Team**

<div align="center">

[⬆ Başa Dön](#-e-ticaret-microservice-platform)

</div>

