-- Flyway Migration V12: Product Risk Profile Table
-- Lookup table for product type risk parameters

CREATE TABLE IF NOT EXISTS product_risk_profile (
    product_type    TEXT PRIMARY KEY,
    leverage        REAL NOT NULL,
    intraday        INTEGER NOT NULL,
    carry_forward   INTEGER NOT NULL,
    squareoff_time  TEXT,
    margin_pct      REAL,
    description     TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert default product types
INSERT OR IGNORE INTO product_risk_profile (product_type, leverage, intraday, carry_forward, squareoff_time, margin_pct, description)
VALUES 
    ('CNC', 1.0, 0, 1, NULL, 100.0, 'Cash and Carry - Delivery trades with full margin'),
    ('MIS', 5.0, 1, 0, '15:20', 20.0, 'Margin Intraday Square-off - Auto square-off at 3:20 PM'),
    ('MTF', 3.0, 0, 1, NULL, 33.33, 'Margin Trading Facility - Carry forward with margin');


-- ============================================
-- Sectoral Index Constituents Table
-- For storing sectoral index constituent data
-- ============================================
CREATE TABLE IF NOT EXISTS sectoral_constituent (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    sector_key          TEXT NOT NULL,
    sector_name         TEXT NOT NULL,
    symbol              TEXT NOT NULL,
    company_name        TEXT,
    industry            TEXT,
    series              TEXT,
    isin_code           TEXT,
    instrument_key      TEXT,
    weight              REAL,
    market_cap          INTEGER,
    trading_date        DATE NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sector_key, symbol, trading_date)
);

CREATE INDEX IF NOT EXISTS idx_sectoral_sector_key
ON sectoral_constituent (sector_key);

CREATE INDEX IF NOT EXISTS idx_sectoral_symbol
ON sectoral_constituent (symbol);

CREATE INDEX IF NOT EXISTS idx_sectoral_trading_date
ON sectoral_constituent (trading_date);
