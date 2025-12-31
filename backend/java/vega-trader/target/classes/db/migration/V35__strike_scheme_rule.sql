-- Flyway Migration V35: Strike Scheme Rules
-- Per a1.md section 2.3 - Strike scheme management

CREATE TABLE IF NOT EXISTS strike_scheme_rule (
    exchange            TEXT NOT NULL,
    underlying          TEXT NOT NULL,
    strike_interval     INTEGER NOT NULL,      -- Strike gap (e.g., 50, 100)
    min_strike          REAL,
    max_strike          REAL,
    review_frequency    TEXT NOT NULL,         -- DAILY / WEEKLY
    last_reviewed       DATETIME,
    active              INTEGER DEFAULT 1,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exchange, underlying)
);

-- Strike status tracking (disabled strikes)
CREATE TABLE IF NOT EXISTS strike_status (
    exchange            TEXT NOT NULL,
    underlying          TEXT NOT NULL,
    strike_price        REAL NOT NULL,
    option_type         TEXT NOT NULL,         -- CE / PE
    enabled             INTEGER DEFAULT 1,
    open_interest       INTEGER DEFAULT 0,
    disabled_reason     TEXT,
    disabled_at         DATETIME,
    PRIMARY KEY (exchange, underlying, strike_price, option_type)
);

CREATE INDEX IF NOT EXISTS idx_strike_status_enabled 
ON strike_status (exchange, underlying, enabled);

-- Seed data for major underlyings
INSERT OR IGNORE INTO strike_scheme_rule (exchange, underlying, strike_interval, review_frequency) VALUES
('NSE', 'NIFTY', 50, 'DAILY'),
('NSE', 'BANKNIFTY', 100, 'DAILY'),
('NSE', 'FINNIFTY', 50, 'DAILY'),
('NSE', 'MIDCPNIFTY', 25, 'DAILY'),
('BSE', 'SENSEX', 100, 'DAILY'),
('BSE', 'BANKEX', 100, 'DAILY');
