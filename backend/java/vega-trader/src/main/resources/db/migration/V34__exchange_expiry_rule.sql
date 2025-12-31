-- Flyway Migration V34: Exchange Expiry Rules
-- Deterministic expiry calculation per exchange and instrument type

CREATE TABLE IF NOT EXISTS exchange_expiry_rule (
    exchange            TEXT NOT NULL,
    instrument_type     TEXT NOT NULL,     -- FUTIDX / OPTIDX / FUTSTK / OPTSTK
    cycle_type          TEXT NOT NULL,     -- WEEKLY / MONTHLY / QUARTERLY / HALF_YEARLY
    expiry_day          TEXT NOT NULL,     -- MONDAY / TUESDAY / THURSDAY / FRIDAY
    fallback_strategy   TEXT NOT NULL,     -- PREVIOUS_TRADING_DAY / NEXT_TRADING_DAY
    active              INTEGER DEFAULT 1,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exchange, instrument_type, cycle_type)
);

CREATE INDEX IF NOT EXISTS idx_expiry_rule_exchange 
ON exchange_expiry_rule (exchange, active);

-- Seed data for NSE and BSE expiry rules
INSERT OR IGNORE INTO exchange_expiry_rule (exchange, instrument_type, cycle_type, expiry_day, fallback_strategy) VALUES
-- NSE Index Options (Weekly + Monthly)
('NSE', 'OPTIDX', 'WEEKLY', 'TUESDAY', 'PREVIOUS_TRADING_DAY'),
('NSE', 'OPTIDX', 'MONTHLY', 'TUESDAY', 'PREVIOUS_TRADING_DAY'),
('NSE', 'OPTIDX', 'QUARTERLY', 'TUESDAY', 'PREVIOUS_TRADING_DAY'),

-- NSE Index Futures
('NSE', 'FUTIDX', 'MONTHLY', 'TUESDAY', 'PREVIOUS_TRADING_DAY'),
('NSE', 'FUTIDX', 'QUARTERLY', 'TUESDAY', 'PREVIOUS_TRADING_DAY'),

-- NSE Stock Options
('NSE', 'OPTSTK', 'WEEKLY', 'THURSDAY', 'PREVIOUS_TRADING_DAY'),
('NSE', 'OPTSTK', 'MONTHLY', 'THURSDAY', 'PREVIOUS_TRADING_DAY'),

-- NSE Stock Futures
('NSE', 'FUTSTK', 'MONTHLY', 'THURSDAY', 'PREVIOUS_TRADING_DAY'),

-- BSE Index Options (Monthly)
('BSE', 'OPTIDX', 'MONTHLY', 'THURSDAY', 'PREVIOUS_TRADING_DAY'),
('BSE', 'OPTIDX', 'WEEKLY', 'FRIDAY', 'PREVIOUS_TRADING_DAY'),

-- BSE Index Futures
('BSE', 'FUTIDX', 'MONTHLY', 'THURSDAY', 'PREVIOUS_TRADING_DAY');
