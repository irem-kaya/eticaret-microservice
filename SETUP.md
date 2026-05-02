# 🔧 E-Ticaret Platform - Setup & Installation Guide

**Master Setup Guide - Baştan Sona Kurulum Talimatları**

---

## 📋 Table of Contents

1. [System Requirements](#system-requirements)
2. [Windows Setup](#windows-setup)
3. [macOS Setup](#macos-setup)
4. [Linux Setup](#linux-setup)
5. [Docker & Services](#docker--services)
6. [Backend Services](#backend-services)
7. [Frontend Setup](#frontend-setup)
8. [Keycloak Configuration](#keycloak-configuration)
9. [Verification](#verification)
10. [Troubleshooting](#troubleshooting)

---

## System Requirements

### Minimum Requirements

| Component | Version | Min Space |
|-----------|---------|-----------|
| **Java** | 17 or higher | 200MB |
| **Node.js** | 18+ | 300MB |
| **Docker** | 20.10+ | 20GB |
| **Docker Compose** | 2.0+ | - |
| **Maven** | 3.8+ | 500MB |
| **Git** | 2.40+ | 100MB |
| **RAM** | - | 8GB (minimum), 16GB (recommended) |

### Ports Required

```
Port 5433   - PostgreSQL
Port 5672   - RabbitMQ (AMQP)
Port 15672  - RabbitMQ (Management UI)
Port 6380   - Redis
Port 8180   - Keycloak
Port 5050   - PgAdmin
Port 8080   - API Gateway
Port 8081   - Cart Service
Port 8082   - Product Service
Port 8083   - Order Service
Port 8084   - Payment Service
Port 8085   - Notification Service
Port 8086   - User Service
Port 8087   - AI Service
Port 8088   - Recommendation Service
Port 5173   - React Frontend (Vite)
```

---

## Windows Setup

### Step 1: Install Prerequisites

#### 1.1 Install Java 17+

```powershell
# Kullanında Chocolatey varsa
choco install openjdk17

# Yoksa manual indir: https://adoptium.net/
# Kurulumdan sonra kontrol et:
java -version
# Output: openjdk 17.x.x ...

# JAVA_HOME ayarla
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.x.x", "User")
```

#### 1.2 Install Node.js 18+

```powershell
# Chocolatey ile
choco install nodejs

# Yoksa manuel: https://nodejs.org/
# Kontrol et:
node --version   # v18.x.x
npm --version    # 9.x.x
```

#### 1.3 Install Maven 3.8+

```powershell
# Chocolatey ile
choco install maven

# Manuel: https://maven.apache.org/download.cgi
# environment PATH'e ekle

# Kontrol et:
mvn --version
# Output: Apache Maven 3.8.x
```

#### 1.4 Install Docker Desktop

- Download: https://www.docker.com/products/docker-desktop
- Install and restart
- Terminal'de kontrol et:

```powershell
docker --version
docker-compose --version
```

#### 1.5 Install Git

```powershell
# Chocolatey ile
choco install git

# Manuel: https://git-scm.com/
# Kontrol et:
git --version
```

### Step 2: Clone Repository

```powershell
# Repository klonla
cd "C:\Users\YourUsername\Desktop"
git clone https://github.com/your-repo/eticaret-microservice.git
cd eticaret-microservice

# Tüm submodules'ı çek (varsa)
git submodule update --init --recursive
```

### Step 3: Create Environment File

```powershell
# .env dosyası oluştur (root directory)
$env:USERPROFILE + "\Desktop\final ödevi n11\eticaret-microservice"

@"
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
RABBITMQ_USER=eticaret
RABBITMQ_PASSWORD=eticaret123
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
PGADMIN_EMAIL=admin@example.com
PGADMIN_PASSWORD=admin123
SPRING_PROFILES_ACTIVE=local
"@ | Out-File -FilePath ".env" -Encoding UTF8
```

---

## macOS Setup

### Step 1: Install Prerequisites Using Homebrew

```bash
# Homebrew install (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install all tools
brew install java17 nodejs maven docker git

# Kontrol et
java -version
node --version
npm --version
mvn --version
```

### Step 2: Install Docker Desktop

```bash
# Homebrew ile
brew install docker

# Veya manual download dari: https://www.docker.com/products/docker-desktop
```

### Step 3: Clone & Setup

```bash
cd ~/Desktop
git clone https://github.com/your-repo/eticaret-microservice.git
cd eticaret-microservice

# .env dosyası oluştur
cat > .env << EOF
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
RABBITMQ_USER=eticaret
RABBITMQ_PASSWORD=eticaret123
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
PGADMIN_EMAIL=admin@example.com
PGADMIN_PASSWORD=admin123
SPRING_PROFILES_ACTIVE=local
EOF
```

---

## Linux Setup

### Step 1: Install Dependencies (Ubuntu/Debian)

```bash
# Update package manager
sudo apt-get update
sudo apt-get upgrade -y

# Install Java 17
sudo apt-get install -y openjdk-17-jdk

# Install Node.js 18+
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install Maven
sudo apt-get install -y maven

# Install Docker
sudo apt-get install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker

# Add current user to docker group
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt-get install -y docker-compose

# Install Git
sudo apt-get install -y git

# Logout & login for docker permissions
exit
```

### Step 2: Clone & Setup

```bash
git clone https://github.com/your-repo/eticaret-microservice.git
cd eticaret-microservice

# .env dosyası oluştur
cat > .env << 'EOF'
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
RABBITMQ_USER=eticaret
RABBITMQ_PASSWORD=eticaret123
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
PGADMIN_EMAIL=admin@example.com
PGADMIN_PASSWORD=admin123
SPRING_PROFILES_ACTIVE=local
EOF
```

---

## Docker & Services

### Step 1: Start Infrastructure

```bash
# infra directory'ye git
cd infra

# Docker Compose up
docker-compose up -d

# Services'ı kontrol et
docker-compose ps

# Output görünümü:
# NAME                IMAGE                    STATUS
# eticaret-postgres   postgres:16              Up 2 minutes (healthy)
# eticaret-rabbitmq   rabbitmq:3.13-management Up 2 minutes (healthy)
# eticaret-redis      redis:7.2-alpine         Up 2 minutes (healthy)
# eticaret-keycloak   quay.io/keycloak/...     Up 2 minutes
# eticaret-pgadmin    dpage/pgadmin4:8         Up 2 minutes
```

### Step 2: Verify Services

```bash
# PostgreSQL
docker exec eticaret-postgres pg_isready -U postgres
# Output: accepting connections

# RabbitMQ
curl -u eticaret:eticaret123 http://localhost:15672/api/overview
# Output: {"management_version":"3.13.x","...}

# Redis
redis-cli -p 6380 ping
# Output: PONG

# Keycloak
curl -s http://localhost:8180 | head -20
# Output: HTML content
```

### Step 3: View Logs

```bash
# PostgreSQL logs
docker-compose logs -f postgres

# RabbitMQ logs
docker-compose logs -f rabbitmq

# Redis logs
docker-compose logs -f redis

# Keycloak logs
docker-compose logs -f keycloak

# All logs
docker-compose logs -f
```

---

## Backend Services

### Step 1: Build All Services

```bash
# Root directory'ye dön
cd .. # from infra
cd .. # to project root

# Tüm servisleri build et
mvn clean package -DskipTests

# Build time: ~5-10 dakika
# Output: BUILD SUCCESS
```

### Step 2: Start Each Service

**Terminal'lerin açık tutacağız. Her terminal'de birer service çalıştırılacak.**

#### Terminal 1: API Gateway (Port 8080)

```bash
cd api-gateway
mvn spring-boot:run

# Output (başarılı):
# 2026-05-01 10:00:00 Started GatewayApplication in 4.5s on port 8080
```

#### Terminal 2: User Service (Port 8086)

```bash
cd user-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:05 Started UserServiceApplication in 4.2s on port 8086
```

#### Terminal 3: Product Service (Port 8082)

```bash
cd product-service-main
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:10 Started ProductServiceApplication in 5.1s on port 8082
```

#### Terminal 4: Cart Service (Port 8081)

```bash
cd cart-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:15 Started CartServiceApplication in 3.8s on port 8081
```

#### Terminal 5: Order Service (Port 8083)

```bash
cd order-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:20 Started OrderServiceApplication in 4.5s on port 8083
```

#### Terminal 6: Payment Service (Port 8084)

```bash
cd payment-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:25 Started PaymentServiceApplication in 4.0s on port 8084
```

#### Terminal 7: AI Service (Port 8087)

```bash
cd ai-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:30 Started AiServiceApplication in 4.3s on port 8087
```

#### Terminal 8: Recommendation Service (Port 8088)

```bash
cd recommendation-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:35 Started RecommendationServiceApplication in 4.2s on port 8088
```

#### Terminal 9: Notification Service (Port 8085)

```bash
cd notification-service
mvn spring-boot:run

# Output:
# 2026-05-01 10:00:40 Started NotificationServiceApplication in 3.9s on port 8085
```

### Step 3: Verify All Services Are Running

**Yeni bir terminal açarak test et:**

```bash
# API Gateway health ✅
curl http://localhost:8080/actuator/health
# Output: {"status":"UP"}

# User Service health
curl http://localhost:8086/actuator/health
# Output: {"status":"UP"}

# Product Service health
curl http://localhost:8082/actuator/health
# Output: {"status":"UP"}

# All services in loop
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088; do
  echo "Port $port: $(curl -s http://localhost:$port/actuator/health | grep -o '\"status\":\"[^\"]*\"')"
done
```

---

## Frontend Setup

### Step 1: Install Dependencies

```bash
# Frontend directory'ye git
cd frontend

# Node modules yükle
npm install

# Çıktı:
# added 400+ packages in 15s
```

### Step 2: Start Development Server

```bash
# Development server başlat
npm run dev

# Output:
# VITE v5.0.0 ready in 234 ms
# ➜  Local:   http://localhost:5173/
# ➜  press h to show help
```

### Step 3: Test Frontend

- Tarayıcıda açı: **http://localhost:5173**
- Sayfanın yüklendiğini gözle
- Ürünleri görmek için scroll yap

### Step 4: Build for Production (Optional)

```bash
# Production build
npm run build

# Output:
# vite v5.0.0 building for production...
# ✓ 1234 modules transformed.
# dist/index.html    15.23 kB │ gzip: 5.23 kB
```

---

## Keycloak Configuration

### Step 1: Access Keycloak

1. Browser'da açı: **http://localhost:8180**
2. Username: `admin`
3. Password: `admin123`

### Step 2: Create Realm

1. **Realms** → **Create Realm**
2. Name: `eticaret`
3. **Create**

### Step 3: Create Client

1. **Clients** → **Create Client**
2. Client ID: `eticaret-app`
3. **Next**
4. Enable: `Client authentication` → OFF
5. **Next** → **Save**

### Step 4: Configure Client

1. **Settings** tab'ına git
2. **Valid redirect URIs**:
   ```
   http://localhost:5173/*
   http://localhost:8080/*
   ```
3. **Valid post logout redirect URIs**:
   ```
   http://localhost:5173
   ```
4. **Web origins**:
   ```
   http://localhost:5173
   http://localhost:8080
   ```
5. **Save**

### Step 5: Get Client Credentials

1. **Credentials** tab'ına git
2. Client Secret'i kopyala (varsa)
3. Frontend'de kullan

### Step 6: Create Test User (Optional)

1. **Users** → **Add User**
   - Username: `testuser`
   - Email: `test@example.com`
   - First Name: Test
   - Last Name: User

2. **Credentials** tab
3. Set Password: `test123`
4. Temporary OFF
5. Set Password

---

## Verification

### Check All Services

```bash
# Create verification script
cat > verify.sh << 'EOF'
#!/bin/bash
echo "=== E-Ticaret Platform - Health Check ==="
echo ""

services=(
  "API Gateway:8080"
  "User Service:8086"
  "Product Service:8082"
  "Cart Service:8081"
  "Order Service:8083"
  "Payment Service:8084"
  "AI Service:8087"
  "Recommendation:8088"
  "Notification:8085"
)

for service in "${services[@]}"; do
  IFS=':' read -r name port <<< "$service"
  status=$(curl -s http://localhost:$port/actuator/health | grep -o '"status":"[^"]*"')
  echo "✓ $name ($port): $status"
done

echo ""
echo "=== Docker Containers ==="
docker-compose -f infra/docker-compose.yml ps

echo ""
echo "=== Frontend ==="
echo "✓ Frontend (5173): http://localhost:5173"

echo ""
echo "=== Admin Panels ==="
echo "✓ Keycloak (8180): http://localhost:8180"
echo "✓ PgAdmin (5050): http://localhost:5050"
echo "✓ RabbitMQ (15672): http://localhost:15672"
EOF

chmod +x verify.sh
./verify.sh
```

### Test API Endpoints

```bash
# Get products
curl http://localhost:8080/api/products?page=0&size=10 | jq .

# Get categories
curl http://localhost:8080/api/categories | jq .

# Search products
curl "http://localhost:8080/api/products/search?query=laptop" | jq .

# Get recommendations
curl "http://localhost:8080/api/recommendations/category/1" | jq .
```

---

## Troubleshooting

### Port Already in Use

```bash
# Windows
netstat -ano | findstr ":8080"
taskkill /PID <ProcessID> /F

# macOS/Linux
lsof -i :8080
kill -9 <PID>
```

### Docker Issues

```bash
# Containers temizle
docker-compose down
docker system prune -a

# Fresh start
docker-compose up -d
```

### Maven Build Failed

```bash
# Cache temizle
mvn clean install -DskipTests

# Offline mode devre dışı
mvn clean install -o false
```

### Database Connection Error

```bash
# PostgreSQL kontrol et
docker-compose logs postgres

# Yeniden başlat
docker-compose restart postgres

# Wait 30 seconds for recovery
sleep 30
```

### Node Modules Error

```bash
# Windows
rmdir /s /q node_modules
del package-lock.json
npm install

# macOS/Linux
rm -rf node_modules package-lock.json
npm install
```

### Keycloak Not Available

```bash
# Logs check
docker-compose logs keycloak

# Restart
docker-compose restart keycloak

# Wait for initialization
sleep 60
```

### RabbitMQ Connection Failed

```bash
# Check container
docker-compose logs rabbitmq

# Recreate container
docker-compose down
docker volume rm infra_rabbitmq_data
docker-compose up -d rabbitmq
```

---

## Performance Tips

### Increase Docker Resources

```yaml
# docker-compose.yml
services:
  postgres:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### Spring Boot Optimization

```bash
# Add JVM options
export JAVA_OPTS="-Xmx2048m -Xms1024m -XX:+UseG1GC"
mvn spring-boot:run
```

### Frontend Optimization

```bash
# Production build with gzip
npm run build
# Serve with gzip compression
npx serve -s dist -Z
```

---

## Next Steps

1. ✅ [Dokümantasyonu Oku](./DOCUMENTATION.md)
2. ✅ [API Reference](./DOCUMENTATION.md#-api-endpoints)
3. ✅ [Security Guidelines](./DOCUMENTATION.md#-security)
4. ✅ [Deployment Guide](./DOCUMENTATION.md#-deployment)

---

## Quick Commands Reference

```bash
# Start all services (one-liner - Linux/macOS only)
docker-compose -f infra/docker-compose.yml up -d && \
mvn clean package -DskipTests && \
(cd frontend && npm install && npm run dev &) && \
for service in api-gateway user-service product-service-main cart-service order-service payment-service ai-service recommendation-service notification-service; do
  (cd $service && mvn spring-boot:run &)
done

# Stop all services
docker-compose -f infra/docker-compose.yml down
killall java
killall node

# View all logs
docker-compose -f infra/docker-compose.yml logs -f

# Reset everything
docker-compose -f infra/docker-compose.yml down -v
mvn clean
rm -rf frontend/node_modules frontend/dist
```

---

## Support

- 📖 [Dokümantasyon](./DOCUMENTATION.md)
- ⚠️ Sorunlar: GitHub Issues
- 💬 Tartışmalar: GitHub Discussions
- 📧 Email: team@example.com

---

**Version:** 1.0.0  
**Last Updated:** May 1, 2026

Happy Coding! 🚀

