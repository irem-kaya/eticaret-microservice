-- Faz 1: Arama ve Stok Yönetimi İçin İndeksler
-- V001__add_search_indexes.sql

-- Ürün adı arama için indeks (tam metin araması)
CREATE INDEX IF NOT EXISTS idx_product_name_search ON products (name);
CREATE INDEX IF NOT EXISTS idx_product_description_search ON products (description);

-- Kategori + aktif durum indeksi (sık sorgu)
CREATE INDEX IF NOT EXISTS idx_product_category_active ON products (category_id, active);

-- Fiyat aralığı sorguları için indeks
CREATE INDEX IF NOT EXISTS idx_product_price ON products (price);
CREATE INDEX IF NOT EXISTS idx_product_price_active ON products (price, active);

-- Stok durumu (tükenme takibi)
CREATE INDEX IF NOT EXISTS idx_product_stock ON products (stock);

-- Tarih bazlı sorgular
CREATE INDEX IF NOT EXISTS idx_product_created_at ON products (created_at DESC);

