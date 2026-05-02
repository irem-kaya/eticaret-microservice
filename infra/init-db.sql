-- Kullanıcı oluştur
CREATE USER eticaret WITH PASSWORD 'eticaret123';

-- Veritabanları oluştur
CREATE DATABASE user_db OWNER eticaret;
CREATE DATABASE product_db OWNER eticaret;
CREATE DATABASE cart_db OWNER eticaret;
CREATE DATABASE order_db OWNER eticaret;
CREATE DATABASE payment_db OWNER eticaret;
CREATE DATABASE keycloak_db OWNER eticaret;

-- Yetkiler
GRANT ALL PRIVILEGES ON DATABASE user_db TO eticaret;
GRANT ALL PRIVILEGES ON DATABASE product_db TO eticaret;
GRANT ALL PRIVILEGES ON DATABASE cart_db TO eticaret;
GRANT ALL PRIVILEGES ON DATABASE order_db TO eticaret;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO eticaret;
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO eticaret;