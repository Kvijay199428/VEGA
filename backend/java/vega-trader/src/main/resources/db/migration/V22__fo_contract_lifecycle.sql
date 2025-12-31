-- Flyway Migration V22: FO Contract Lifecycle
-- Track F&O contract expiry for rollover and deactivation

CREATE TABLE IF NOT EXISTS fo_contract_lifecycle (
    instrument_key  TEXT PRIMARY KEY,
    underlying_key  TEXT NOT NULL,
    underlying_symbol TEXT,
    instrument_type TEXT NOT NULL,  -- FUT, CE, PE
    expiry_date     DATE NOT NULL,
    is_active       INTEGER NOT NULL DEFAULT 1,
    strike_price    REAL,
    lot_size        INTEGER,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fo_underlying 
ON fo_contract_lifecycle (underlying_key);

CREATE INDEX IF NOT EXISTS idx_fo_expiry 
ON fo_contract_lifecycle (expiry_date);

CREATE INDEX IF NOT EXISTS idx_fo_active_expiry 
ON fo_contract_lifecycle (is_active, expiry_date);
