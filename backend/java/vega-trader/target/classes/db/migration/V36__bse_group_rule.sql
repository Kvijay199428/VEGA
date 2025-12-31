-- Flyway Migration V36: BSE Group Rules
-- Per a1.md section 3.2 - BSE special cases

CREATE TABLE IF NOT EXISTS bse_group_rule (
    group_code          TEXT PRIMARY KEY,
    group_name          TEXT NOT NULL,
    cnc_only            INTEGER DEFAULT 0,      -- Delivery only
    trade_for_trade     INTEGER DEFAULT 0,      -- T2T segment
    quantity_cap        INTEGER,
    margin_multiplier   REAL DEFAULT 1.0,
    active              INTEGER DEFAULT 1,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bse_group_active 
ON bse_group_rule (active, trade_for_trade);

-- Seed data for BSE groups
INSERT OR IGNORE INTO bse_group_rule (group_code, group_name, cnc_only, trade_for_trade, quantity_cap, margin_multiplier) VALUES
('A', 'Group A - Large Cap', 0, 0, 50000, 1.0),
('B', 'Group B - Mid Cap', 0, 0, 20000, 1.0),
('T', 'T Group - Trade-for-Trade', 1, 1, 1000, 1.0),
('TS', 'Trade-to-Trade Surveillance', 1, 1, 500, 1.5),
('Z', 'Z Group - Suspended', 1, 0, 500, 2.0),
('X', 'Dropped Companies', 0, 0, 2000, 1.0),
('XT', 'XT Group - T2T Transition', 1, 1, 1000, 1.5),
('XD', 'Ex-Dividend', 0, 0, 10000, 1.0),
('IF', 'Infrastructure', 0, 0, 15000, 1.0),
('M', 'Medium Enterprises', 0, 0, 10000, 1.0);
