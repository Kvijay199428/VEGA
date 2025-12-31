-- Flyway Migration V10: Instrument Master Table
-- Creates the core instrument reference data table for Upstox BOD data

CREATE TABLE IF NOT EXISTS instrument_master (
    instrument_key        TEXT PRIMARY KEY,
    segment               TEXT NOT NULL,
    exchange              TEXT NOT NULL,
    instrument_type       TEXT NOT NULL,
    
    trading_symbol        TEXT NOT NULL,
    name                  TEXT,
    short_name            TEXT,
    
    isin                  TEXT,
    underlying_key        TEXT,
    underlying_symbol     TEXT,
    underlying_type       TEXT,
    
    expiry                DATE,
    strike_price          REAL,
    
    lot_size              INTEGER NOT NULL DEFAULT 1,
    minimum_lot           INTEGER,
    freeze_quantity       INTEGER,
    
    tick_size             REAL,
    exchange_token        TEXT,
    
    weekly                INTEGER DEFAULT 0,
    security_type         TEXT,
    
    trading_date          DATE NOT NULL,
    is_active             INTEGER DEFAULT 1,
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Critical indexes for search and autocomplete
CREATE INDEX IF NOT EXISTS idx_instrument_symbol_search 
ON instrument_master (trading_symbol, segment, instrument_type);

CREATE INDEX IF NOT EXISTS idx_instrument_underlying 
ON instrument_master (underlying_key);

CREATE INDEX IF NOT EXISTS idx_instrument_expiry 
ON instrument_master (expiry);

CREATE INDEX IF NOT EXISTS idx_instrument_exchange_segment
ON instrument_master (exchange, segment);

CREATE INDEX IF NOT EXISTS idx_instrument_trading_date
ON instrument_master (trading_date);

CREATE INDEX IF NOT EXISTS idx_instrument_isin
ON instrument_master (isin);
