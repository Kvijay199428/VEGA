-- Flyway Migration V16: Exchange Series Source (Dynamic Sync)
-- Ground truth from exchanges, refreshed daily

CREATE TABLE IF NOT EXISTS exchange_series_source (
    exchange        TEXT NOT NULL,
    series_code     TEXT NOT NULL,
    description     TEXT,
    last_seen_date  DATE NOT NULL,
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exchange, series_code)
);

CREATE INDEX IF NOT EXISTS idx_series_source_date 
ON exchange_series_source (last_seen_date);
