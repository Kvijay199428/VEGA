-- Flyway Migration V21: Dynamic Price Bands
-- Daily price limits (ingested from exchange)

CREATE TABLE IF NOT EXISTS price_band (
    instrument_key  TEXT PRIMARY KEY,
    lower_price     REAL NOT NULL,
    upper_price     REAL NOT NULL,
    lower_pct       REAL,
    upper_pct       REAL,
    effective_date  DATE NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_price_band_date 
ON price_band (effective_date);
