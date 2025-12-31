-- Flyway Migration V27: Sector Master
-- High-level sector classification (IT, Banking, Healthcare, etc.)

CREATE TABLE IF NOT EXISTS sector_master (
    sector_code     TEXT PRIMARY KEY,
    sector_name     TEXT NOT NULL,
    category        TEXT NOT NULL DEFAULT 'SECTORAL',  -- SECTORAL / THEMATIC / BROAD
    description     TEXT,
    active          INTEGER NOT NULL DEFAULT 1,
    display_order   INTEGER DEFAULT 100,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sector_category 
ON sector_master (category, active);

-- Seed sectors
INSERT OR IGNORE INTO sector_master (sector_code, sector_name, category, display_order) VALUES
('IT', 'Information Technology', 'SECTORAL', 1),
('BANKING', 'Bank', 'SECTORAL', 2),
('FINANCE', 'Financial Services', 'SECTORAL', 3),
('PHARMA', 'Pharma', 'SECTORAL', 4),
('HEALTHCARE', 'Healthcare', 'SECTORAL', 5),
('AUTO', 'Auto', 'SECTORAL', 6),
('FMCG', 'FMCG', 'SECTORAL', 7),
('METAL', 'Metal', 'SECTORAL', 8),
('ENERGY', 'Energy', 'SECTORAL', 9),
('REALTY', 'Realty', 'SECTORAL', 10),
('INFRA', 'Infrastructure', 'SECTORAL', 11),
('MEDIA', 'Media', 'SECTORAL', 12),
('PSU', 'Public Sector Undertakings', 'THEMATIC', 20),
('MNC', 'Multinational Companies', 'THEMATIC', 21),
('CONSUMPTION', 'India Consumption', 'THEMATIC', 22),
('COMMODITIES', 'Commodities', 'THEMATIC', 23),
('CPSE', 'Central Public Sector Enterprises', 'THEMATIC', 24),
('NIFTY50', 'Nifty 50', 'BROAD', 50),
('NIFTY100', 'Nifty 100', 'BROAD', 51),
('NIFTY500', 'Nifty 500', 'BROAD', 52),
('MIDCAP', 'Mid Cap', 'BROAD', 53),
('SMALLCAP', 'Small Cap', 'BROAD', 54);
