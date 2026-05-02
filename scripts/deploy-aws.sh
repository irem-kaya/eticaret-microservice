#!/bin/bash
# ────────────────────────────────────────────────────────────────────────────
# AWS Deployment Script - E-Ticaret Microservice
# GitHub Actions tarafından çalıştırılır
# ────────────────────────────────────────────────────────────────────────────

set -e  # Hata durumunda çık

# ── Konfigürasyon ─────────────────────────────────────────────────────────
AWS_REGION=${AWS_REGION:-"eu-west-1"}
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
EC2_INSTANCE_ID=${EC2_INSTANCE_ID}
INSTANCE_IP=${INSTANCE_IP:-"13.50.239.68"}

DOCKER_IMAGE_API_GATEWAY="${ECR_REGISTRY}/eticaret/api-gateway:${CI_COMMIT_SHA}"
DOCKER_IMAGE_PRODUCT="${ECR_REGISTRY}/eticaret/product-service:${CI_COMMIT_SHA}"
DOCKER_IMAGE_FRONTEND="${ECR_REGISTRY}/eticaret/frontend:${CI_COMMIT_SHA}"

# ── Renkli Çıktı ──────────────────────────────────────────────────────────
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

# ── 1. ECR Giriş ───────────────────────────────────────────────────────────
log_info "ECR'a giriş yapılıyor..."
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin ${ECR_REGISTRY}

# ── 2. ECR Repository Oluştur (varsa skip) ────────────────────────────────
create_ecr_repo() {
  local repo_name=$1
  log_info "ECR Repository kontrol ediliyor: $repo_name"

  if ! aws ecr describe-repositories \
       --repository-names $repo_name \
       --region ${AWS_REGION} 2>/dev/null; then
    log_info "Repository oluşturuluyor: $repo_name"
    aws ecr create-repository \
      --repository-name $repo_name \
      --region ${AWS_REGION} \
      --encryption-configuration encryptionType=AES \
      --image-scanning-configuration scanOnPush=true
  fi
}

create_ecr_repo "eticaret/api-gateway"
create_ecr_repo "eticaret/product-service"
create_ecr_repo "eticaret/frontend"

# ── 3. Docker Images Push ──────────────────────────────────────────────────
log_info "Docker images ECR'a push ediliyor..."

docker tag $(docker images -q -f "reference=*api-gateway*" | head -1) ${DOCKER_IMAGE_API_GATEWAY}
docker push ${DOCKER_IMAGE_API_GATEWAY}

docker tag $(docker images -q -f "reference=*product-service*" | head -1) ${DOCKER_IMAGE_PRODUCT}
docker push ${DOCKER_IMAGE_PRODUCT}

docker tag $(docker images -q -f "reference=*frontend*" | head -1) ${DOCKER_IMAGE_FRONTEND}
docker push ${DOCKER_IMAGE_FRONTEND}

# ── 4. EC2'ye Deploy Script Gönder ─────────────────────────────────────────
log_info "EC2 instance'a deploy scripti gönderiliyor..."

# Deploy Script Template
cat > /tmp/deploy.sh << 'EOF'
#!/bin/bash

set -e

ECR_REGISTRY=$1
DOCKER_IMAGE_FRONTEND=$2
DOCKER_IMAGE_PRODUCT=$3

# ECR Login
aws ecr get-login-password --region eu-west-1 | \
  docker login --username AWS --password-stdin ${ECR_REGISTRY}

# Frontend Deploy
log_info "Frontend deploy edilmektedir..."
docker pull ${DOCKER_IMAGE_FRONTEND}
docker stop eticaret-frontend || true
docker rm eticaret-frontend || true
docker run -d \
  --name eticaret-frontend \
  -p 80:80 \
  -p 443:443 \
  --restart unless-stopped \
  ${DOCKER_IMAGE_FRONTEND}

# Product Service Deploy
log_info "Product Service deploy edilmektedir..."
docker pull ${DOCKER_IMAGE_PRODUCT}
docker stop eticaret-product || true
docker rm eticaret-product || true
docker run -d \
  --name eticaret-product \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=${DB_URL} \
  -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
  -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
  -e SPRING_RABBITMQ_HOST=${RABBITMQ_HOST} \
  -e SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20 \
  --restart unless-stopped \
  ${DOCKER_IMAGE_PRODUCT}

# Healthcheck
log_info "Healthcheck yapılmaktedir..."
sleep 10
curl -f http://localhost/health || exit 1
curl -f http://localhost:8081/actuator/health/live || exit 1

log_info "✅ Deploy Başarılı!"
EOF

# EC2'ye Script Gönder
aws ssm send-command \
  --document-name "AWS-RunShellScript" \
  --instance-ids "${EC2_INSTANCE_ID}" \
  --parameters "commands=$(cat /tmp/deploy.sh)" \
  --region ${AWS_REGION}

log_info "Deploy komutu EC2'ye gönderildi."

# ── 5. RDS Backup (Opsiyonel) ──────────────────────────────────────────────
log_info "RDS database backup yapılmaktedir..."

aws rds create-db-snapshot \
  --db-instance-identifier eticaret-db \
  --db-snapshot-identifier eticaret-db-$(date +%s) \
  --region ${AWS_REGION} || log_warn "RDS snapshot başarısız oldu"

# ── 6. Deployment Verify ───────────────────────────────────────────────────
log_info "Deployment doğrulanmaktedir..."

for i in {1..30}; do
  if curl -sf http://${INSTANCE_IP}/health > /dev/null; then
    log_info "✅ Frontend sağlık durumu: OK"
    break
  fi
  if [ $i -eq 30 ]; then
    log_error "❌ Frontend healthcheck başarısız"
    exit 1
  fi
  log_warn "⏳ Deneme $i/30..."
  sleep 10
done

log_info "✅ Deployment Başarılı!"
log_info "Frontend URL: http://${INSTANCE_IP}"
log_info "API Gateway: http://${INSTANCE_IP}/api"

