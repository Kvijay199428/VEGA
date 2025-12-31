-- Flyway Migration V17: Regulatory Watchlist
-- PCA (Prompt Corrective Action) and Surveillance tracking

CREATE TABLE IF NOT EXISTS regulatory_watchlist (
    exchange        TEXT NOT NULL,
    symbol          TEXT NOT NULL,
    watch_type      TEXT NOT NULL,  -- PCA, SURVEILLANCE, ASM, GSM
    stage           TEXT,           -- Stage 1, 2, 3, etc.
    effective_date  DATE NOT NULL,
    expiry_date     DATE,
    reason          TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exchange, symbol, watch_type)
);

CREATE INDEX IF NOT EXISTS idx_watchlist_type 
ON regulatory_watchlist (watch_type);

CREATE INDEX IF NOT EXISTS idx_watchlist_symbol 
ON regulatory_watchlist (symbol);

CREATE INDEX IF NOT EXISTS idx_watchlist_effective 
ON regulatory_watchlist (effective_date, expiry_date);
