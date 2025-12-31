-- Flyway Migration V19: Intraday Margin by Series
-- Series-based margin rules (not symbol-specific)

CREATE TABLE IF NOT EXISTS intraday_margin_by_series (
    exchange            TEXT NOT NULL,
    series_code         TEXT NOT NULL,
    intraday_margin_pct REAL NOT NULL,
    intraday_leverage   REAL NOT NULL,
    effective_date      DATE NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exchange, series_code)
);

-- Default seed data (safe baseline)
INSERT OR IGNORE INTO intraday_margin_by_series VALUES
('NSE', 'EQ', 20.0, 5.0, DATE('now'), CURRENT_TIMESTAMP),
('NSE', 'BE', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP),
('NSE', 'BZ', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP),
('NSE', 'SM', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP),
('NSE', 'ST', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP),
('BSE', 'A', 20.0, 5.0, DATE('now'), CURRENT_TIMESTAMP),
('BSE', 'B', 25.0, 4.0, DATE('now'), CURRENT_TIMESTAMP),
('BSE', 'T', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP),
('BSE', 'Z', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP),
('BSE', 'X', 50.0, 2.0, DATE('now'), CURRENT_TIMESTAMP),
('BSE', 'XT', 100.0, 1.0, DATE('now'), CURRENT_TIMESTAMP);
