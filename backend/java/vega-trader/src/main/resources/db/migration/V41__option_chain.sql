-- Flyway Migration V41: Option Chain Tables
-- Per optionchain/implementations/a1.md section 3.2

CREATE TABLE IF NOT EXISTS option_chain (
    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
    instrument_key          TEXT NOT NULL,
    expiry_date             DATE NOT NULL,
    strike_price            REAL NOT NULL,
    underlying_spot_price   REAL,
    pcr                     REAL,
    call_ltp                REAL,
    call_oi                 INTEGER,
    call_iv                 REAL,
    call_delta              REAL,
    call_gamma              REAL,
    call_theta              REAL,
    call_vega               REAL,
    put_ltp                 REAL,
    put_oi                  INTEGER,
    put_iv                  REAL,
    put_delta               REAL,
    put_gamma               REAL,
    put_theta               REAL,
    put_vega                REAL,
    fetched_at              DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_option_chain_lookup 
ON option_chain (instrument_key, expiry_date, strike_price);

CREATE INDEX IF NOT EXISTS idx_option_chain_expiry 
ON option_chain (expiry_date);

-- Option Chain Audit Log for SEBI compliance
CREATE TABLE IF NOT EXISTS option_chain_audit (
    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
    instrument_key          TEXT NOT NULL,
    expiry_date             DATE NOT NULL,
    token_used              TEXT NOT NULL,
    request_payload         TEXT,
    response_payload        TEXT,
    status_code             INTEGER,
    fetch_source            TEXT,           -- CACHE, API, FALLBACK
    fetched_at              DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_oc_audit_lookup 
ON option_chain_audit (instrument_key, expiry_date, fetched_at);

-- Option Chain Cache Metadata
CREATE TABLE IF NOT EXISTS option_chain_cache_meta (
    cache_key               TEXT PRIMARY KEY,
    instrument_key          TEXT NOT NULL,
    expiry_date             DATE NOT NULL,
    cached_at               DATETIME NOT NULL,
    expires_at              DATETIME NOT NULL,
    hit_count               INTEGER DEFAULT 0
);
