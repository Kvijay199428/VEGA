-- Flyway Migration V26: Broker Symbol Mapping
-- Maps instrument_key to broker-specific symbols

CREATE TABLE IF NOT EXISTS broker_symbol_mapping (
    broker_id       TEXT NOT NULL,
    instrument_key  TEXT NOT NULL,
    broker_symbol   TEXT NOT NULL,
    broker_token    TEXT,           -- Exchange token for broker
    tradeable       INTEGER NOT NULL DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (broker_id, instrument_key)
);

CREATE INDEX IF NOT EXISTS idx_broker_symbol 
ON broker_symbol_mapping (broker_id, broker_symbol);

CREATE INDEX IF NOT EXISTS idx_instrument_key 
ON broker_symbol_mapping (instrument_key);
