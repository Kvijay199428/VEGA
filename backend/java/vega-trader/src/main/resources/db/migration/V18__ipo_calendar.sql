-- Flyway Migration V18: IPO Calendar
-- Track listing dates for Day-0 restrictions

CREATE TABLE IF NOT EXISTS ipo_calendar (
    symbol          TEXT NOT NULL,
    exchange        TEXT NOT NULL,
    company_name    TEXT,
    listing_date    DATE NOT NULL,
    issue_price     REAL,
    lot_size        INTEGER,
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (symbol, exchange)
);

CREATE INDEX IF NOT EXISTS idx_ipo_listing_date 
ON ipo_calendar (listing_date);

CREATE INDEX IF NOT EXISTS idx_ipo_active 
ON ipo_calendar (is_active, listing_date);
