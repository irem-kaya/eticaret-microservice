# 📚 E-Ticaret Microservice - Dokumentasyon İndeksi

**Tüm Dokümantasyon Dosyaları Rehberi**

---

## 📑 Dokümantasyon Yapısı

```
📚 Documentation Collection
├── 📄 README.md                      ← START HERE! (5 min read)
├── 📄 SETUP.md                       ← Kurulum (30-40 min)
├── 📄 DOCUMENTATION.md               ← Teknik (45-60 min)
├── 📄 AI_DEVELOPMENT_GUIDE.md        ← AI/Geliştirme (40-50 min)
└── 📄 INDEX.md                       ← Bu dosya
```

---

## 🎯 Hangi Dosyayı Hangi Durum İçin Oku?

### 🆕 Proje Hakkında Bilgi Almak İstiyorum

**👉 README.md** (5 dakika)

İçeriği:
- Proje özeti
- Temel özellikleri
- Hızlı başlangıç (5 adım)
- Sistem mimarisi
- Teknoloji stack
- Microservices listesi
- Key features (Fuzzy, AI, MCP)

```bash
# Tavsiye: İlk olarak bunu oku
cat README.md
```

---

### 💻 Projeyi Kurgulamak Istiyorum

**👉 SETUP.md** (30-40 dakika)

İçeriği:
- Sistem gereksinimleri
- Windows/macOS/Linux setup
- Docker services başlatma
- Backend services çalıştırma
- Frontend kurulumu
- Keycloak konfigürasyonu
- Health checks & verification
- Troubleshooting

```bash
# Adım adım takip et
# Terminal'de bu komutu çalıştır
nano SETUP.md  # veya less SETUP.md
```

---

### 🏗️ Sistem Mimarisini Anlamak İstiyorum

**👉 DOCUMENTATION.md** (45-60 dakika)

İçeriği:
- Proje file structure (detaylı)
- 9 microservice'in detaylı açıklaması
- Frontend architecture
- Database schema & design
- Complete API reference
- Authentication & Security
- CI/CD pipeline
- Docker & Kubernetes
- Deployment guide

```bash
# Bölüm bölüm oku
# Search: Ctrl+F (VS Code)
code DOCUMENTATION.md
```

---

### 🤖 AI Araçları Entegre Etmek İstiyorum

**👉 AI_DEVELOPMENT_GUIDE.md** (40-50 dakika)

İçeriği:
- **Proje dosya yapısı (detaylı)**
- **Ürün çekme modülleri analizi**
- **AI Service detaylı kod**
- **Product Service detaylı kod**
- **Fuzzy search algoritması**
- **ChatGPT/Claude/Ollama entegrasyonu**
- **Geliştirme rehberi**
- **Testing & monitoring**
- **Performance tuning**

```bash
# AI development için bunu oku
code AI_DEVELOPMENT_GUIDE.md
```

---

## 📖 Her Dosyanın Detaylı İçeriği

### 1️⃣ README.md

| Bölüm | Sayfa | Açıklama |
|-------|-------|----------|
| Project Overview | 1-2 | Proje özeti, özellikler |
| Quick Start | 3-4 | 5 adımda başlangıç |
| System Architecture | 5 | ASCII diagram |
| Technology Stack | 6-7 | Tüm teknolojiler |
| Microservices | 8-9 | 9 service temel bilgi |
| Frontend Features | 10 | React bileşenleri |
| API Examples | 11-12 | curl örnekleri |
| Security | 13 | OAuth2, JWT |
| Testing | 14 | Unit, integration tests |
| Deployment | 15 | Docker, K8s |

**Hedef Kitle:** Yöneticiler, PM'ler, yeni team members  
**Okunma Süresi:** 5 dakika

---

### 2️⃣ SETUP.md

| Bölüm | Sayfalar | Açıklama |
|-------|---------|----------|
| System Requirements | 1-2 | Minimum specs, ports |
| Windows Setup | 3-6 | Step-by-step Windows |
| macOS Setup | 7-9 | Step-by-step macOS |
| Linux Setup | 10-12 | Step-by-step Ubuntu/Debian |
| Docker & Services | 13-16 | Docker compose, start |
| Backend Services | 17-30 | 9 terminal'de start |
| Frontend Setup | 31-35 | npm install, npm run dev |
| Keycloak Config | 36-42 | Realm, client, user setup |
| Verification | 43-48 | Health checks, API tests |
| Troubleshooting | 49-60 | 10+ sık sorunlar |

**Hedef Kitle:** Backend/DevOps engineers  
**Okunma Süresi:** 30-40 dakika  
**Uyarı:** Adım adım takip etmelisin!

---

### 3️⃣ DOCUMENTATION.md

| Bölüm | Sayfalar | Açıklama |
|-------|---------|----------|
| System Architecture | 1-5 | Detailed diagrams |
| Technology Stack | 6-15 | Versions, links |
| Microservices (9 s.) | 16-80 | Her birinin detaylı analizi |
| Frontend | 81-100 | React, Tailwind, hooks, context |
| Database | 101-110 | Schema, migrations, indexes |
| API Reference | 111-120 | Complete endpoints |
| Installation | 121-135 | Full setup guide |
| CI/CD | 136-145 | GitHub Actions workflow |
| Docker & K8s | 146-160 | Containerization, orchestration |
| Security | 161-175 | OAuth2, JWT, CORS |
| Monitoring | 176-185 | Actuator, logging |
| Troubleshooting | 186-200 | Advanced debugging |

**Hedef Kitle:** Developers, architects, DevOps  
**Okunma Süresi:** 45-60 dakika  
**Tavsiye:** Ctrl+F ile arama yap

---

### 4️⃣ AI_DEVELOPMENT_GUIDE.md

| Bölüm | Sayfalar | Açıklama |
|-------|---------|----------|
| Project File Structure | 1-30 | Tüm dosya yapısı detaylı |
| Ürün Çekme Modülleri | 31-40 | Modül haritası |
| AI Service Analysis | 41-85 | 6 component detaylı |
| Product Service Analysis | 86-110 | 3 component detaylı |
| AI Tools Integration | 111-130 | ChatGPT, Claude, Ollama |
| Development Guide | 131-150 | Yeni tool ekleme, testing |
| Monitoring & Logging | 151-160 | AOP, debugging |
| Performance Tuning | 161-170 | Configuration, optimization |
| Checklist & Debugging | 171-180 | Kontrol listesi, hata çözme |

**Hedef Kitle:** AI developers, backend engineers  
**Okunma Süresi:** 40-50 dakika  
**İçerik:** Kod örnekleriyle dolu!

---

## 🗺️ Geliştirme Sırasında Dosya Referansları

### Senaryo 1: Backend Feature Ekleme

```
1. README.md → Proje hakkında bilgi al
   ↓
2. DOCUMENTATION.md → Sistem mimarisini anla
   ↓
3. AI_DEVELOPMENT_GUIDE.md → Module structure oku
   ↓
4. Kod yaz ve test et
   ↓
5. SETUP.md → Eğer sorun varsa troubleshooting
```

### Senaryo 2: AI Tool Entegre Etme

```
1. AI_DEVELOPMENT_GUIDE.md → "AI Tools Integration" bölümü
   ↓
2. Product generation code oku ve anla
   ↓
3. SETUP.md → API keys ayarla
   ↓
4. Backend services başlat
   ↓
5. Test et (curl commands kullan)
```

### Senaryo 3: Sistem Deployment

```
1. DOCUMENTATION.md → Deployment bölümü
   ↓
2. SETUP.md → Docker setup
   ↓
3. CI/CD pipeline yapılandır
   ↓
4. Production tests çalıştır
   ↓
5. Go live!
```

---

## 🔑 Anahtar Modüller & Dosya Konumları

### AI Service (Ürün Çekme)

```
📁 ai-service/
├── controller/
│   └── AiController.java           ← REST endpoints
├── service/
│   ├── ProductGenerationService.java  ← Ürün üretim logic
│   ├── GeminiApiService.java        ← Gemini API client
│   └── ImageService.java            ← Unsplash images
├── scheduler/
│   └── ProductSyncScheduler.java    ← Günlük sync (02:00 AM)
└── dto/
    └── GenerateProductsRequest.java ← Request format

📖 AI_DEVELOPMENT_GUIDE.md
   ├── Section 3: AI Service Detaylı Analiz
   ├── AiController açıklaması
   ├── ProductGenerationService açıklaması
   ├── GeminiApiService açıklaması
   └── ProductSyncScheduler açıklaması
```

### Product Service (Ürün Yönetimi)

```
📁 product-service-main/
├── domain/
│   ├── ProductService.java          ← Business logic
│   ├── ProductController.java       ← REST API
│   └── ProductRepository.java       ← Database access
├── features/
│   ├── search/
│   │   └── FuzzySearchService.java  ← %75 search algorithm
│   └── authorization/
│       └── StockAuthorizationService.java ← Role-based visibility
└── dto/
    ├── BulkImportRequest.java
    └── BulkImportResponse.java

📖 AI_DEVELOPMENT_GUIDE.md
   ├── Section 4: Product Service Detaylı Analiz
   ├── ProductController açıklaması
   ├── ProductService açıklaması
   └── FuzzySearchService açıklaması
```

### Frontend

```
📁 frontend/
├── services/
│   ├── productService.js   ← API calls
│   ├── fuzzySearch.js      ← ⭐ Fuzzy search algorithm
│   └── api.js              ← Axios config
├── components/
│   ├── Navbar.jsx          ← Arama müşteri
│   ├── Sidebar.jsx         ← Filtreler
│   └── Pagination.jsx      ← Sayfalama
└── pages/
    ├── ProductListPage.jsx
    └── ProductDetailPage.jsx

📖 DOCUMENTATION.md
   ├── Section 5: Frontend Architecture
   ├── React components yapısı
   └── API integration
```

---

## ❓ Sık Sorulan Sorular

### Q: Hangi dosya uzun? Başında ne okusam?

**A:** README.md başla (5 min), sonra ihtiyacına göre diğerlerini oku.

---

### Q: Hangi dosyada Fuzzy search algoritması var?

**A:**
- Frontend: `AI_DEVELOPMENT_GUIDE.md` → Section 4 → FuzzySearchService
- Backend: `AI_DEVELOPMENT_GUIDE.md` → Section 4 veya `DOCUMENTATION.md` → Product Service

---

### Q: Gemini API nasıl entegre ederim?

**A:** `AI_DEVELOPMENT_GUIDE.md` → Section 3 → GeminiApiService.java

---

### Q: RabbitMQ event publishing nasıl çalışır?

**A:** `DOCUMENTATION.md` → Advanced Topics → Message Queue Section

---

### Q: Ürün listeleme API'si nasıl çalışır?

**A:** `AI_DEVELOPMENT_GUIDE.md` → Section 4 → ProductController.java veya `DOCUMENTATION.md` → API Reference

---

## 🔗 Başlıklar ve Linkler

### README.md
- `#-proje-özeti` → Proje hakkında
- `#-hızlı-başlangıç` → Quick start
- `#-sistem-mimarisi` → Architecture diagram

### SETUP.md
- `#system-requirements` → Specs
- `#windows-setup` → Windows
- `#docker--services` → Docker start
- `#troubleshooting` → Hata çözme

### DOCUMENTATION.md
- `#-sistem-mimarisi` → Architecture
- `#-microservisler` → Services
- `#-frontend` → Frontend
- `#-api-endpoints` → API Reference

### AI_DEVELOPMENT_GUIDE.md
- `#-proje-dosya-yapısı` → File structure
- `#-ürün-çekme-modülleri` → Modules
- `#ai-service---detaylı-analiz` → AI Service code
- `#-ai-araçları-entegrasyonu` → ChatGPT, Claude

---

## 📊 Dosya Boyut & İçerik Özeti

| Dosya | Boyut | Satır | Okunma | Kod |
|-------|-------|-------|--------|-----|
| README.md | ~10 KB | 250 | 5 min | - |
| SETUP.md | ~30 KB | 800 | 30 min | Çok |
| DOCUMENTATION.md | ~150 KB | 3500 | 60 min | Orta |
| AI_DEVELOPMENT_GUIDE.md | ~120 KB | 2800 | 50 min | Çok |

**Toplam:** ~310 KB, ~7350 satır

---

## 🎓 Öğrenme Yolu

### 1. Hafta

```
Pazartesi:   README.md oku (5 min)
Salı:        SETUP.md → System Requirements (10 min)
Çarşamba:    SETUP.md → Kurulumu tamamla (60 min)
Perşembe:    DOCUMENTATION.md → Architecture (30 min)
Cuma:        Proje dosyalarını keşfet (60 min)
Cumartesi:   AI_DEVELOPMENT_GUIDE.md oku (60 min)
Pazar:       Hafta özeti ve Q&A
```

### 2. Hafta

```
Bugünün planlı: Bu hafta projede bir feature implement et
- README.md referans al
- DOCUMENTATION.md'de modülü bul
- AI_DEVELOPMENT_GUIDE.md'de örnek kodu oku
- Kod yaz ve test et
- SETUP.md'de troubleshoot
```

---

## ✅ Kontrol Listesi

Dokümantasyon okuması tamamlandığında:

- [ ] README.md tamamen okundu
- [ ] SETUP.md'deki "System Requirements" okundu
- [ ] DOCUMENTATION.md'deki system architecture anlaşıldı
- [ ] 9 microservice'in görevleri öğrenildi
- [ ] API endpoints listesi incelendi
- [ ] AI_DEVELOPMENT_GUIDE.md ürün çekme modülleri okundu
- [ ] En az 1 file yapısı tam olarak incelendi
- [ ] Fuzzy search algoritması anlaşıldı
- [ ] PostgreSQL, RabbitMQ, Redis rolleri öğrenildi
- [ ] CI/CD pipeline yapısı öğrenildi

---

## 🛠️ Tools & Resources

### Dokümantasyon Okuma Tools

```bash
# VS Code
code README.md AI_DEVELOPMENT_GUIDE.md

# Terminal viewers
less DOCUMENTATION.md
cat README.md | head -50

# Grep search
grep -n "AI Service" AI_DEVELOPMENT_GUIDE.md

# Word count
wc -l *.md
```

### Diagram Tools (eğer modifiye etmek isterseniz)

- **PlantUML:** ASCII diagram'ları UML'ye dönüştür
- **Draw.io:** Visual diagram oluştur
- **Figma:** UI mockups

---

## 📧 Support & Questions

Sorularınız varsa:

1. **İnternal**: GitHub Issues
2. **Documentation**: İlgili markdown file'ında section aç
3. **Code Examples**: AI_DEVELOPMENT_GUIDE.md'de bak
4. **Setup Issues**: SETUP.md → Troubleshooting

---

## 📝 Dokümantasyon Güncellemeleri

- **README.md**: Proje özelliklerine göre güncelle
- **SETUP.md**: Yeni tools/services eklenirse güncelle
- **DOCUMENTATION.md**: Mimarı değişirse güncelle
- **AI_DEVELOPMENT_GUIDE.md**: Yeni AI tools eklenirse güncelle
- **INDEX.md** (bu dosya): Aylık güncelleme

**Son Güncelleme:** 1 Mayıs 2026

---

## 🎯 Next Steps

1. README.md oku (5 dakika) ✅
2. SETUP.md'yi takip eden kurulumu yap (60 dakika)
3. DOCUMENTATION.md'deki ilgi alanını oku (30-60 dakika)
4. AI_DEVELOPMENT_GUIDE.md ürün çekme kısmını oku (30-40 dakika)
5. Proje dosyalarında keşfet ve ilişkiler kur (60+ dakika)

---

**Happy Learning! 🚀**

Made with ❤️ for E-Ticaret Platform

---

**Version:** 1.0.0  
**Created:** 1 May 2026  
**Last Updated:** 1 May 2026

