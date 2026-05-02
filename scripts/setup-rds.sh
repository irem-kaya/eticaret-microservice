#!/bin/bash
# ────────────────────────────────────────────────────────────────────────────
# AWS RDS Database Setup Script
# PostgreSQL 16 Multi-Database Setup
# ────────────────────────────────────────────────────────────────────────────

set -e

# ── Konfigürasyon ─────────────────────────────────────────────────────────
AWS_REGION=${AWS_REGION:-"eu-west-1"}
DB_INSTANCE_IDENTIFIER=${DB_INSTANCE_IDENTIFIER:-"eticaret-db"}
DB_ENGINE_VERSION=${DB_ENGINE_VERSION:-"16.2"}
DB_INSTANCE_CLASS=${DB_INSTANCE_CLASS:-"db.t3.micro"}
DB_ALLOCATED_STORAGE=${DB_ALLOCATED_STORAGE:-"100"}
DB_MASTER_USERNAME=${DB_MASTER_USERNAME:-"postgres"}
DB_MASTER_PASSWORD=${DB_MASTER_PASSWORD:-"$(openssl rand -base64 32)"}
SECURITY_GROUP_ID=${SECURITY_GROUP_ID}
DB_SUBNET_GROUP=${DB_SUBNET_GROUP}

# ── Renkli Çıktı ──────────────────────────────────────────────────────────
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ── 1. RDS DB Instance Oluştur ─────────────────────────────────────────────
log_info "RDS DB Instance kontrol ediliyor..."

if ! aws rds describe-db-instances \
     --db-instance-identifier ${DB_INSTANCE_IDENTIFIER} \
     --region ${AWS_REGION} 2>/dev/null; then

  log_info "RDS DB Instance oluşturuluyor: ${DB_INSTANCE_IDENTIFIER}"

  aws rds create-db-instance \
    --db-instance-identifier ${DB_INSTANCE_IDENTIFIER} \
    --db-engine postgres \
    --db-engine-version ${DB_ENGINE_VERSION} \
    --db-instance-class ${DB_INSTANCE_CLASS} \
    --allocated-storage ${DB_ALLOCATED_STORAGE} \
    --storage-type gp3 \
    --storage-iops 3000 \
    --storage-throughput 125 \
    --master-username ${DB_MASTER_USERNAME} \
    --master-user-password "${DB_MASTER_PASSWORD}" \
    --vpc-security-group-ids ${SECURITY_GROUP_ID} \
    --db-subnet-group-name ${DB_SUBNET_GROUP} \
    --publicly-accessible false \
    --enable-iam-database-authentication \
    --copy-tags-to-snapshot \
    --backup-retention-period 30 \
    --preferred-backup-window "03:00-04:00" \
    --preferred-maintenance-window "Mon:04:00-Mon:05:00" \
    --enable-cloudwatch-logs-exports "postgresql" \
    --deletion-protection \
    --region ${AWS_REGION}

  log_info "RDS Instance oluşturuluyor. Lütfen bekleyin (yaklaşık 5-10 dakika)..."

  # Instance'ın kullanılabilir hale gelmesini bekle
  aws rds wait db-instance-available \
    --db-instance-identifier ${DB_INSTANCE_IDENTIFIER} \
    --region ${AWS_REGION}

  log_info "✅ RDS Instance hazır!"
else
  log_warn "RDS Instance zaten mevcut: ${DB_INSTANCE_IDENTIFIER}"
fi

# ── 2. RDS Endpoint'i Al ───────────────────────────────────────────────────
log_info "RDS Endpoint alınmaktedir..."

DB_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier ${DB_INSTANCE_IDENTIFIER} \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text \
  --region ${AWS_REGION})

log_info "RDS Endpoint: ${DB_ENDPOINT}"

# ── 3. PostgreSQL Bağlantısı Kur ──────────────────────────────────────────
log_info "PostgreSQL'e bağlanılmaktedir..."

# PGPASSWORD environment variable'i ayarla
export PGPASSWORD="${DB_MASTER_PASSWORD}"

# Bağlantı test et
psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -c "SELECT version();" || {
  log_error "PostgreSQL bağlantısı başarısız. Lütfen Security Group'u kontrol edin."
  exit 1
}

# ── 4. Databases Oluştur ──────────────────────────────────────────────────
log_info "Veritabanları oluşturuluyor..."

DATABASES=(
  "product_db"
  "cart_db"
  "order_db"
  "payment_db"
  "user_db"
  "notification_db"
  "ai_db"
  "keycloak_db"
)

for db in "${DATABASES[@]}"; do
  psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -tc "SELECT 1 FROM pg_database WHERE datname = '${db}'" | grep -q 1 && {
    log_warn "Veritabanı zaten mevcut: ${db}"
  } || {
    psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -c "CREATE DATABASE ${db} WITH ENCODING 'UTF8';"
    log_info "✅ Veritabanı oluşturuldu: ${db}"
  }
done

# ── 5. Roles (Users) Oluştur ──────────────────────────────────────────────
log_info "Database roles oluşturuluyor..."

ROLES=(
  "product_user:$(openssl rand -base64 16)"
  "cart_user:$(openssl rand -base64 16)"
  "order_user:$(openssl rand -base64 16)"
  "payment_user:$(openssl rand -base64 16)"
  "user_user:$(openssl rand -base64 16)"
  "notification_user:$(openssl rand -base64 16)"
  "ai_user:$(openssl rand -base64 16)"
)

for role_pass in "${ROLES[@]}"; do
  IFS=':' read -r role pass <<< "$role_pass"

  psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -tc "SELECT 1 FROM pg_roles WHERE rolname = '${role}'" | grep -q 1 && {
    log_warn "Role zaten mevcut: ${role}"
  } || {
    psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -c "CREATE ROLE ${role} WITH LOGIN PASSWORD '${pass}';"
    psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -c "ALTER ROLE ${role} CREATEDB;"
    log_info "✅ Role oluşturuldu: ${role}"
    echo "  Password: ${pass}" >> /tmp/db-credentials.txt
  }
done

# ── 6. Permissions Ata ────────────────────────────────────────────────────
log_info "Permissions atanmaktedir..."

psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -c "
  GRANT ALL PRIVILEGES ON DATABASE product_db TO product_user;
  GRANT ALL PRIVILEGES ON DATABASE cart_db TO cart_user;
  GRANT ALL PRIVILEGES ON DATABASE order_db TO order_user;
  GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_user;
  GRANT ALL PRIVILEGES ON DATABASE user_db TO user_user;
  GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification_user;
  GRANT ALL PRIVILEGES ON DATABASE ai_db TO ai_user;
"

# ── 7. Extensions Etkinleştir ─────────────────────────────────────────────
log_info "PostgreSQL extensions etkinleştiriliyor..."

for db in "${DATABASES[@]}"; do
  psql -h ${DB_ENDPOINT} -U ${DB_MASTER_USERNAME} -d ${db} -c "
    CREATE EXTENSION IF NOT EXISTS uuid-ossp;
    CREATE EXTENSION IF NOT EXISTS pg_trgm;
    CREATE EXTENSION IF NOT EXISTS citext;
  " || log_warn "Extension etkinleştirme başarısız: ${db}"
done

# ── 8. Backup Oluştur ─────────────────────────────────────────────────────
log_info "Database backup oluşturuluyor..."

aws rds create-db-snapshot \
  --db-instance-identifier ${DB_INSTANCE_IDENTIFIER} \
  --db-snapshot-identifier eticaret-db-initial-$(date +%s) \
  --region ${AWS_REGION}

# ── 9. Parameter Group Oluştur (Performance Tuning) ──────────────────────
log_info "DB Parameter Group oluşturuluyor..."

aws rds create-db-parameter-group \
  --db-parameter-group-name eticaret-postgres16 \
  --db-parameter-group-family postgres16 \
  --description "E-Ticaret Microservice Parameter Group" \
  --region ${AWS_REGION} || log_warn "Parameter Group zaten mevcut"

# Performance Parameters
aws rds modify-db-parameter-group \
  --db-parameter-group-name eticaret-postgres16 \
  --parameters \
    "ParameterName=shared_buffers,ParameterValue=262144,ApplyMethod=pending-reboot" \
    "ParameterName=effective_cache_size,ParameterValue=786432,ApplyMethod=immediate" \
    "ParameterName=maintenance_work_mem,ParameterValue=65536,ApplyMethod=immediate" \
    "ParameterName=checkpoint_completion_target,ParameterValue=0.9,ApplyMethod=immediate" \
    "ParameterName=wal_buffers,ParameterValue=16384,ApplyMethod=pending-reboot" \
    "ParameterName=default_statistics_target,ParameterValue=100,ApplyMethod=immediate" \
    "ParameterName=random_page_cost,ParameterValue=1.1,ApplyMethod=immediate" \
    "ParameterName=effective_io_concurrency,ParameterValue=200,ApplyMethod=immediate" \
    "ParameterName=work_mem,ParameterValue=16384,ApplyMethod=immediate" \
    "ParameterName=min_wal_size,ParameterValue=1024,ApplyMethod=immediate" \
    "ParameterName=max_wal_size,ParameterValue=4096,ApplyMethod=immediate" \
  --region ${AWS_REGION} || log_warn "Parameter modifikasyonu başarısız"

# ── 10. Konfigürasyon Dosyası Oluştur ─────────────────────────────────────
log_info "Konfigürasyon dosyası oluşturuluyor..."

cat > /tmp/rds-config.env << EOF
# ──────────────────────────────────────────────────────────
# AWS RDS Configuration
# ──────────────────────────────────────────────────────────

# Master User
DB_MASTER_USERNAME=${DB_MASTER_USERNAME}
DB_MASTER_PASSWORD=${DB_MASTER_PASSWORD}

# Endpoint
DB_ENDPOINT=${DB_ENDPOINT}
DB_PORT=5432

# Application Users (copy credentials from /tmp/db-credentials.txt)
PRODUCT_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/product_db
PRODUCT_DB_USER=product_user
PRODUCT_DB_PASSWORD=<from-credentials-file>

CART_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/cart_db
CART_DB_USER=cart_user
CART_DB_PASSWORD=<from-credentials-file>

ORDER_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/order_db
ORDER_DB_USER=order_user
ORDER_DB_PASSWORD=<from-credentials-file>

PAYMENT_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/payment_db
PAYMENT_DB_USER=payment_user
PAYMENT_DB_PASSWORD=<from-credentials-file>

USER_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/user_db
USER_DB_USER=user_user
USER_DB_PASSWORD=<from-credentials-file>

NOTIFICATION_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/notification_db
NOTIFICATION_DB_USER=notification_user
NOTIFICATION_DB_PASSWORD=<from-credentials-file>

AI_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/ai_db
AI_DB_USER=ai_user
AI_DB_PASSWORD=<from-credentials-file>

KEYCLOAK_DB_URL=jdbc:postgresql://${DB_ENDPOINT}:5432/keycloak_db
KEYCLOAK_DB_USER=postgres
KEYCLOAK_DB_PASSWORD=${DB_MASTER_PASSWORD}
EOF

log_info "✅ RDS Setup Tamamlandı!"
log_info "Konfigürasyon dosyası: /tmp/rds-config.env"
log_info "DB Credentials: /tmp/db-credentials.txt"
log_info "DB Endpoint: ${DB_ENDPOINT}"

# ── Credentials Kaydet ─────────────────────────────────────────────────
cat > /tmp/db-credentials.txt << EOF
# ──────────────────────────────────────────────────────────
# Database Credentials
# ──────────────────────────────────────────────────────────
Master: ${DB_MASTER_USERNAME} / ${DB_MASTER_PASSWORD}
Endpoint: ${DB_ENDPOINT}

$(cat /tmp/db-credentials.txt || echo "Credentials will be added during role creation")
EOF

log_info "📝 Tüm kimlik bilgilerini güvenli bir yerde saklayın!"

