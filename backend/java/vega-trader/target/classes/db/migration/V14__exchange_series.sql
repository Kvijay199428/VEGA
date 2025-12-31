-- Flyway Migration V14: Exchange Series Master
-- NSE: EQ, BE, BZ, SM, ST | BSE: A, B, T, Z, X, XT

CREATE TABLE IF NOT EXISTS exchange_series (
    exchange            TEXT NOT NULL,
    series_code         TEXT NOT NULL,
    security_class      TEXT NOT NULL DEFAULT 'EQUITY',
    rolling_settlement  INTEGER NOT NULL DEFAULT 1,
    trade_for_trade     INTEGER NOT NULL DEFAULT 0,
    gross_settlement    INTEGER NOT NULL DEFAULT 0,
    surveillance        INTEGER NOT NULL DEFAULT 0,
    mis_allowed         INTEGER NOT NULL DEFAULT 1,
    mtf_allowed         INTEGER NOT NULL DEFAULT 1,
    description         TEXT,
    PRIMARY KEY (exchange, series_code)
);

-- NSE Series
INSERT OR IGNORE INTO exchange_series VALUES
('NSE', 'EQ', 'EQUITY', 1, 0, 0, 0, 1, 1, 'Normal equity'),
('NSE', 'BE', 'EQUITY', 0, 1, 0, 1, 0, 0, 'Trade-for-Trade (Books Entry)'),
('NSE', 'BZ', 'EQUITY', 0, 1, 0, 1, 0, 0, 'Trade-for-Trade (Z series)'),
('NSE', 'SM', 'EQUITY', 1, 0, 0, 0, 0, 0, 'SME Platform'),
('NSE', 'ST', 'EQUITY', 1, 0, 0, 0, 0, 0, 'SME Trade-for-Trade'),
('NSE', 'N1', 'EQUITY', 1, 0, 0, 0, 1, 1, 'Normal 1'),
('NSE', 'N2', 'EQUITY', 1, 0, 0, 0, 1, 1, 'Normal 2'),
('NSE', 'IL', 'EQUITY', 1, 0, 0, 0, 0, 0, 'Illiquid'),
('NSE', 'W1', 'EQUITY', 1, 0, 0, 0, 1, 1, 'Weekly Options Series 1');

-- BSE Series (Groups)
INSERT OR IGNORE INTO exchange_series VALUES
('BSE', 'A',  'EQUITY', 1, 0, 0, 0, 1, 1, 'Group A - Large cap'),
('BSE', 'B',  'EQUITY', 1, 0, 0, 0, 1, 1, 'Group B - Mid cap'),
('BSE', 'T',  'EQUITY', 0, 1, 0, 1, 0, 0, 'Trade-for-Trade'),
('BSE', 'Z',  'EQUITY', 0, 1, 0, 1, 0, 0, 'Suspended/Non-compliant'),
('BSE', 'X',  'EQUITY', 1, 0, 0, 1, 0, 0, 'Surveillance'),
('BSE', 'XT', 'EQUITY', 0, 1, 0, 1, 0, 0, 'Surveillance T2T'),
('BSE', 'F',  'EQUITY', 1, 0, 0, 0, 1, 1, 'Fixed Income'),
('BSE', 'G',  'EQUITY', 1, 0, 0, 0, 1, 1, 'Government Securities');

-- Index for lookups
CREATE INDEX IF NOT EXISTS idx_exchange_series_t2t 
ON exchange_series (exchange, trade_for_trade);
