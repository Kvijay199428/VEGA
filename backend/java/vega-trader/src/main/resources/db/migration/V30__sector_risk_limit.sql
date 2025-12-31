-- Flyway Migration V30: Sector Risk Limits
-- Optional sector-level exposure caps for RMS

CREATE TABLE IF NOT EXISTS sector_risk_limit (
    sector_code         TEXT PRIMARY KEY,
    max_exposure        REAL,                   -- Max exposure in value
    max_exposure_pct    REAL DEFAULT 100.0,     -- Max % of portfolio
    max_open_positions  INTEGER DEFAULT 100,
    trading_blocked     INTEGER NOT NULL DEFAULT 0,
    block_reason        TEXT,
    effective_date      DATE,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sector_code) REFERENCES sector_master(sector_code)
);

-- Default limits (can be overridden)
INSERT OR IGNORE INTO sector_risk_limit (sector_code, max_exposure_pct, max_open_positions) VALUES
('IT', 40.0, 50),
('BANKING', 40.0, 50),
('FINANCE', 30.0, 40),
('PHARMA', 25.0, 30),
('AUTO', 25.0, 30),
('FMCG', 25.0, 30),
('METAL', 20.0, 25),
('ENERGY', 30.0, 35),
('REALTY', 15.0, 20),
('INFRA', 20.0, 25);
