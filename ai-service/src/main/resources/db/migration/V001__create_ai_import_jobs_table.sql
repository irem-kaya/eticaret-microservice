-- Faz 2: AI İthalatı Takibi
-- V002__create_ai_import_jobs_table.sql

CREATE TABLE IF NOT EXISTS ai_import_jobs (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, RUNNING, COMPLETED, FAILED
    imported_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    total_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- İthalatı indeksle
CREATE INDEX IF NOT EXISTS idx_ai_import_jobs_category ON ai_import_jobs (category_id);
CREATE INDEX IF NOT EXISTS idx_ai_import_jobs_status ON ai_import_jobs (status);
CREATE INDEX IF NOT EXISTS idx_ai_import_jobs_created_at ON ai_import_jobs (created_at DESC);

