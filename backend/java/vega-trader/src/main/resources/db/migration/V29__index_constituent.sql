-- Flyway Migration V29: Index Constituents
-- Maps instruments to their index memberships

CREATE TABLE IF NOT EXISTS index_constituent (
    index_code      TEXT NOT NULL,
    instrument_key  TEXT NOT NULL,
    symbol          TEXT NOT NULL,
    company_name    TEXT,
    series          TEXT DEFAULT 'EQ',
    isin            TEXT,
    industry        TEXT,
    weight          REAL,
    free_float_mcap REAL,
    effective_date  DATE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (index_code, instrument_key),
    FOREIGN KEY (index_code) REFERENCES index_master(index_code)
);

CREATE INDEX IF NOT EXISTS idx_constituent_symbol 
ON index_constituent (symbol);

CREATE INDEX IF NOT EXISTS idx_constituent_instrument 
ON index_constituent (instrument_key);

CREATE INDEX IF NOT EXISTS idx_constituent_industry 
ON index_constituent (industry);

-- View: Instrument to Sector mapping (derived)
CREATE VIEW IF NOT EXISTS instrument_sector_view AS
SELECT DISTINCT
    ic.instrument_key,
    ic.symbol,
    ic.company_name,
    ic.industry,
    im.sector_code,
    sm.sector_name,
    sm.category AS sector_category
FROM index_constituent ic
JOIN index_master im ON ic.index_code = im.index_code
JOIN sector_master sm ON im.sector_code = sm.sector_code
WHERE im.active = 1 AND sm.active = 1;
