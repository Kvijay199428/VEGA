-- Flyway Migration V28: Index Master
-- NSE/BSE indices with source URLs for CSV ingestion

CREATE TABLE IF NOT EXISTS index_master (
    index_code      TEXT PRIMARY KEY,
    index_name      TEXT NOT NULL,
    sector_code     TEXT,
    exchange        TEXT NOT NULL DEFAULT 'NSE',
    source_url      TEXT NOT NULL,
    csv_format      TEXT DEFAULT 'NIFTY_INDICES',  -- NIFTY_INDICES / BSE_INDICES
    active          INTEGER NOT NULL DEFAULT 1,
    last_updated    DATE,
    constituent_count INTEGER DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sector_code) REFERENCES sector_master(sector_code)
);

CREATE INDEX IF NOT EXISTS idx_index_sector 
ON index_master (sector_code, active);

-- Seed indices with source URLs
INSERT OR IGNORE INTO index_master (index_code, index_name, sector_code, source_url) VALUES
('NIFTY_IT', 'Nifty IT', 'IT', 'https://www.niftyindices.com/IndexConstituent/ind_niftyitlist.csv'),
('NIFTY_BANK', 'Nifty Bank', 'BANKING', 'https://www.niftyindices.com/IndexConstituent/ind_niftybanklist.csv'),
('NIFTY_FIN_SERVICE', 'Nifty Financial Services', 'FINANCE', 'https://www.niftyindices.com/IndexConstituent/ind_niftyfinancelist.csv'),
('NIFTY_PHARMA', 'Nifty Pharma', 'PHARMA', 'https://www.niftyindices.com/IndexConstituent/ind_niftypharmalist.csv'),
('NIFTY_HEALTHCARE', 'Nifty Healthcare', 'HEALTHCARE', 'https://www.niftyindices.com/IndexConstituent/ind_niftyhealthcarelist.csv'),
('NIFTY_AUTO', 'Nifty Auto', 'AUTO', 'https://www.niftyindices.com/IndexConstituent/ind_niftyautolist.csv'),
('NIFTY_FMCG', 'Nifty FMCG', 'FMCG', 'https://www.niftyindices.com/IndexConstituent/ind_niftyfmcglist.csv'),
('NIFTY_METAL', 'Nifty Metal', 'METAL', 'https://www.niftyindices.com/IndexConstituent/ind_niftymetallist.csv'),
('NIFTY_ENERGY', 'Nifty Energy', 'ENERGY', 'https://www.niftyindices.com/IndexConstituent/ind_niftyenergylist.csv'),
('NIFTY_REALTY', 'Nifty Realty', 'REALTY', 'https://www.niftyindices.com/IndexConstituent/ind_niftyrealtylist.csv'),
('NIFTY_INFRA', 'Nifty Infrastructure', 'INFRA', 'https://www.niftyindices.com/IndexConstituent/ind_niftyinfralist.csv'),
('NIFTY_MEDIA', 'Nifty Media', 'MEDIA', 'https://www.niftyindices.com/IndexConstituent/ind_niftymedialist.csv'),
('NIFTY_PSU_BANK', 'Nifty PSU Bank', 'PSU', 'https://www.niftyindices.com/IndexConstituent/ind_niftypsubanklist.csv'),
('NIFTY_MNC', 'Nifty MNC', 'MNC', 'https://www.niftyindices.com/IndexConstituent/ind_niftymnclist.csv'),
('NIFTY_CONSUMPTION', 'Nifty India Consumption', 'CONSUMPTION', 'https://www.niftyindices.com/IndexConstituent/ind_niftyconsumptionlist.csv'),
('NIFTY_COMMODITIES', 'Nifty Commodities', 'COMMODITIES', 'https://www.niftyindices.com/IndexConstituent/ind_niftycommoditieslist.csv'),
('NIFTY_CPSE', 'Nifty CPSE', 'CPSE', 'https://www.niftyindices.com/IndexConstituent/ind_niftycpselist.csv'),
('NIFTY_50', 'Nifty 50', 'NIFTY50', 'https://www.niftyindices.com/IndexConstituent/ind_nifty50list.csv'),
('NIFTY_100', 'Nifty 100', 'NIFTY100', 'https://www.niftyindices.com/IndexConstituent/ind_nifty100list.csv'),
('NIFTY_500', 'Nifty 500', 'NIFTY500', 'https://www.niftyindices.com/IndexConstituent/ind_nifty500list.csv'),
('NIFTY_MIDCAP_100', 'Nifty Midcap 100', 'MIDCAP', 'https://www.niftyindices.com/IndexConstituent/ind_niftymidcap100list.csv'),
('NIFTY_SMLCAP_100', 'Nifty Smallcap 100', 'SMALLCAP', 'https://www.niftyindices.com/IndexConstituent/ind_niftysmallcap100list.csv'),
('NIFTY_NEXT_50', 'Nifty Next 50', 'NIFTY100', 'https://www.niftyindices.com/IndexConstituent/ind_niftynext50list.csv'),
('NIFTY_MIDCAP_50', 'Nifty Midcap 50', 'MIDCAP', 'https://www.niftyindices.com/IndexConstituent/ind_niftymidcap50list.csv'),
('NIFTY_PVT_BANK', 'Nifty Private Bank', 'BANKING', 'https://www.niftyindices.com/IndexConstituent/ind_niftypvtbanklist.csv');
