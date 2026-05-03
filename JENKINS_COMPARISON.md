# Jenkins vs GitHub Actions — Teknik Karşılaştırma Raporu

> **Proje:** E-Ticaret Microservice Sistemi  
> **Stack:** Java 21 · Spring Boot · Maven · Docker (Jib) · PostgreSQL · RabbitMQ · React (Vite) · Slack

---

## 1. Pipeline Tanım Yapısı

### GitHub Actions — YAML

Pipeline, repository içinde `.github/workflows/ci-cd.yml` dosyasında tanımlanır. Kod ile CI/CD konfigürasyonu aynı repo'da yaşar, versiyon kontrolüne tabi tutulur.

```yaml
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16         # bağımlı servis tek satırda
    steps:
      - uses: actions/checkout@v4
      - name: Maven Clean Install
        run: mvn clean install -DskipTests -q
```

**Özellikler:**
- Declarative YAML, öğrenmesi kolay
- `services:` bloğu ile PostgreSQL/RabbitMQ gibi bağımlılıklar tek satırda ayağa kalkar
- `matrix` stratejisi ile 8 microservice paralel build edilir (`max-parallel: 2`)
- Her değişiklik commit geçmişinde izlenebilir

---

### Jenkins — Groovy DSL (Jenkinsfile)

Pipeline, repository kökünde `Jenkinsfile` ile tanımlanır. Declarative veya Scripted Groovy sözdizimi kullanılır.

```groovy
pipeline {
    agent any
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9'
    }
    stages {
        stage('Maven Clean Install') {
            steps {
                sh 'mvn clean install -DskipTests -q'
            }
        }
        stage('Backend Tests') {
            parallel {
                stage('Product Service') {
                    steps {
                        withEnv(['SPRING_DATASOURCE_URL=jdbc:postgresql://...']) {
                            sh 'mvn -pl product-service-main test -q'
                        }
                    }
                }
                // diğer servisler...
            }
        }
    }
}
```

**Özellikler:**
- Groovy tabanlı, daha fazla programlama gücü sunar
- `parallel {}` bloğu ile GitHub Actions'daki matrix stratejisinin karşılığı sağlanır
- Paylaşılan fonksiyonlar (`def buildAndPushImage()`) ile kod tekrarı önlenir
- Jenkins server kurulumu ve plugin yönetimi gerektirir

---

## 2. Bu Projede Karşılıklı Eşleşmeler

| GitHub Actions Yapısı | Jenkins Karşılığı |
|---|---|
| `jobs:` | `stages:` |
| `steps:` | `steps {}` |
| `strategy.matrix` | `parallel {}` blokları |
| `services: postgres` | Jenkins agent üzerinde ayrı Docker container |
| `secrets.SLACK_WEBHOOK_URL` | Jenkins Credentials (Secret Text) |
| `secrets.CR_PAT` | Jenkins Credentials (Secret Text) |
| `if: success()` | `post { success {} }` |
| `if: failure()` | `post { failure {} }` |
| `continue-on-error: true` | `catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE')` |
| `upload-artifact@v4` | `junit` plugin + `archiveArtifacts` |
| `environment: production` (onay) | `input { message "Deploy edilsin mi?" }` |
| `actions/setup-java@v4` | `tools { jdk 'JDK-21' }` |
| `actions/setup-node@v4` | `tools { nodejs 'NodeJS-20' }` |

---

## 3. Servis Bağımlılıkları (PostgreSQL / RabbitMQ)

### GitHub Actions
```yaml
services:
  postgres:
    image: postgres:16
    env:
      POSTGRES_DB: product_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
    ports:
      - 5432:5432
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
```
GitHub Actions, `services:` bloğu ile bağımlı container'ları otomatik yönetir. Health check built-in gelir.

### Jenkins
```groovy
// Jenkins'de bağımlı servisler 2 yöntemle çözülür:

// Yöntem 1: Agent üzerinde Docker Compose ile
stage('Start Services') {
    steps {
        sh 'docker-compose -f docker-compose.test.yml up -d postgres rabbitmq'
        sh 'sleep 10'  // servisin hazır olmasını bekle
    }
}

// Yöntem 2: Jenkins Docker Plugin ile
agent {
    docker {
        image 'maven:3.9-eclipse-temurin-21'
        args '--network=host'
    }
}
```
Jenkins'de bu bağımlılıkları yönetmek için ek konfigürasyon gerekir; GitHub Actions kadar kolay değildir.

---

## 4. Slack Bildirimleri

### GitHub Actions
Her job içinde ayrı step olarak tanımlanır:
```yaml
- name: Slack — Build Başarılı
  if: success()
  uses: slackapi/slack-github-action@v1.24.0
  with:
    payload: |
      { "text": "✅ *Build Başarılı!*", ... }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
    SLACK_WEBHOOK_TYPE: INCOMING_WEBHOOK
```

### Jenkins
Global `post {}` bloğunda merkezi olarak yönetilir — her stage için tekrar tekrar yazmaya gerek yoktur:
```groovy
post {
    success {
        slackSend(channel: '#ci-cd', color: 'good',
                  message: "✅ Build Başarılı! Branch: ${env.GIT_BRANCH}")
    }
    failure {
        slackSend(channel: '#ci-cd', color: 'danger',
                  message: "❌ Build Başarısız! Log: ${env.BUILD_URL}")
    }
}
```
Jenkins'de Slack Plugin kurulduktan sonra Credentials üzerinden webhook URL yönetilir.

---

## 5. Docker Image Build (Jib)

### GitHub Actions
```yaml
strategy:
  matrix:
    service: [product-service-main, cart-service, order-service, ...]
  max-parallel: 2

steps:
  - name: Jib ile Docker Image Build
    run: |
      mvn -pl ${{ matrix.service }} compile jib:dockerBuild \
        -Djib.to.image=ghcr.io/irem-kaya/eticaret/${{ matrix.service }}:${{ github.sha }}
```
Matrix stratejisi ile 8 servis otomatik olarak ayrı job'larda paralel çalıştırılır.

### Jenkins
```groovy
stage('Docker Build Backend') {
    parallel {
        stage('product-service-main') {
            steps { script { buildAndPushImage('product-service-main') } }
        }
        stage('cart-service') {
            steps { script { buildAndPushImage('cart-service') } }
        }
        // ...
    }
}

def buildAndPushImage(String service) {
    sh "mvn -pl ${service} compile jib:dockerBuild -Djib.to.image=..."
}
```
Jenkins'de her servis manuel olarak `parallel {}` bloğuna eklenmek zorundadır; matrix otomasyonu yoktur. Ancak paylaşılan fonksiyon (`def`) ile kod tekrarı önlenir.

---

## 6. Production Deploy Onayı

### GitHub Actions
```yaml
environment:
  name: production
  url: http://13.50.239.68
```
GitHub Environment Protection Rules ile repo ayarlarından manual approval tanımlanır.

### Jenkins
```groovy
stage('Deploy Production') {
    input {
        message "Production'a deploy edilsin mi?"
        ok      "Evet, deploy et"
        submitter 'admin,team-lead'  // sadece belirli kullanıcılar onaylayabilir
    }
    steps {
        echo "Deploying to production..."
    }
}
```
Jenkins'de `input` step ile pipeline duraklatılır, yetkili kullanıcı UI'dan onay verir.

---

## 7. Test Sonuçları Raporlama

### GitHub Actions
```yaml
- name: Test Sonuçlarını Yükle
  uses: actions/upload-artifact@v4
  if: always()
  with:
    name: test-results
    path: '**/target/surefire-reports/'
    if-no-files-found: warn
```
Artifact olarak saklanır, Actions sekmesinden indirilebilir. Ancak görsel test raporu için ek action gerekir.

### Jenkins
```groovy
post {
    always {
        junit allowEmptyResults: true,
              testResults: '**/target/surefire-reports/*.xml'
        archiveArtifacts artifacts: '**/target/surefire-reports/**'
    }
}
```
Jenkins JUnit Plugin ile test sonuçları görsel grafik olarak Build sayfasında gösterilir: geçen/başarısız test sayıları, trend grafikleri, hata detayları.

---

## 8. Genel Karşılaştırma Tablosu

| Kriter | GitHub Actions | Jenkins |
|---|---|---|
| **Kurulum** | Sıfır kurulum, GitHub ile hazır | Sunucu kurulumu + plugin yönetimi gerekir |
| **Konfigürasyon dili** | YAML (declarative) | Groovy DSL (declarative veya scripted) |
| **Servis bağımlılıkları** | `services:` bloğu ile kolay | Docker Compose veya plugin ile manuel |
| **Paralel çalıştırma** | `matrix` + `strategy` ile otomatik | `parallel {}` ile manuel |
| **Ücret** | Public repo için ücretsiz (2.000 dk/ay) | Kendi sunucusu = altyapı maliyeti |
| **Plugin ekosistemi** | 10.000+ marketplace action | 1.800+ plugin (daha köklü ekosistem) |
| **Öğrenme eğrisi** | Düşük (YAML bilgisi yeterli) | Orta-Yüksek (Groovy + Jenkins bilgisi) |
| **Esneklik** | Orta | Yüksek (tam programlama gücü) |
| **Görsel UI** | GitHub Actions sekmesi | Jenkins Blue Ocean / klasik UI |
| **Test raporlama** | Artifact olarak (görsel sınırlı) | JUnit Plugin ile zengin görsel rapor |
| **Secrets yönetimi** | GitHub Secrets (repo/org seviyesi) | Jenkins Credentials (merkezi) |
| **Self-hosted runner** | Desteklenir | Zaten self-hosted |
| **On-premise destek** | Kısıtlı (GitHub Enterprise) | Tam destek |

---

## 9. Bu Proje İçin Değerlendirme

Bu e-ticaret microservice projesi için **GitHub Actions tercih edilmiştir**. Gerekçeler:

1. **Sıfır kurulum:** GitHub repo ile entegre, ek sunucu gerekmez
2. **Matrix build:** 8 microservice için `strategy.matrix` ile otomatik paralel build
3. **Services bloğu:** PostgreSQL ve RabbitMQ bağımlılıkları tek blokla yönetilir
4. **YAML sadeligi:** Ekip için düşük öğrenme eğrisi
5. **GHCR entegrasyonu:** GitHub Container Registry ile native uyum

**Jenkins ne zaman tercih edilir?**
- Kurumsal/on-premise ortamlarda (banka, kamu gibi GitHub'a erişimin kısıtlı olduğu yerler)
- Çok karmaşık pipeline mantığı gerektiren projelerde (koşullu dallanma, dinamik stage üretimi)
- Detaylı test trend raporlarının kritik olduğu ekiplerde
- Mevcut Jenkins altyapısının bulunduğu büyük organizasyonlarda

---

*Rapor tarihi: Mayıs 2025*