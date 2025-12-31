-- Flyway Migration V20: Symbol Quantity Caps
-- Per-symbol RMS limits (max quantity, max value)

CREATE TABLE IF NOT EXISTS symbol_quantity_caps (
    instrument_key  TEXT PRIMARY KEY,
    max_qty         INTEGER NOT NULL,
    max_value       REAL,
    reason          TEXT,
    effective_date  DATE NOT NULL,
    expiry_date     DATE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_qty_caps_effective 
ON symbol_quantity_caps (effective_date, expiry_date);
