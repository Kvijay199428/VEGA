-- Flyway Migration V11: Instrument Overlay Tables
-- Creates overlay tables for MIS, MTF, and Suspended instruments

-- ============================================
-- Suspended Instruments
-- Instruments blocked from trading
-- ============================================
CREATE TABLE IF NOT EXISTS instrument_suspension (
    instrument_key TEXT PRIMARY KEY,
    trading_date   DATE NOT NULL,
    reason         TEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_key) REFERENCES instrument_master(instrument_key)
);

CREATE INDEX IF NOT EXISTS idx_suspension_trading_date
ON instrument_suspension (trading_date);


-- ============================================
-- MIS Overlay (Margin Intraday Square-off)
-- Intraday trading rules
-- ============================================
CREATE TABLE IF NOT EXISTS instrument_mis (
    instrument_key      TEXT PRIMARY KEY,
    intraday_margin     REAL,
    intraday_leverage   REAL,
    qty_multiplier      REAL DEFAULT 1.0,
    trading_date        DATE NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_key) REFERENCES instrument_master(instrument_key)
);

CREATE INDEX IF NOT EXISTS idx_mis_trading_date
ON instrument_mis (trading_date);


-- ============================================
-- MTF Overlay (Margin Trading Facility)
-- Carry forward margin rules
-- ============================================
CREATE TABLE IF NOT EXISTS instrument_mtf (
    instrument_key TEXT PRIMARY KEY,
    mtf_enabled    INTEGER DEFAULT 0,
    mtf_bracket    REAL,
    trading_date   DATE NOT NULL,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instrument_key) REFERENCES instrument_master(instrument_key)
);

CREATE INDEX IF NOT EXISTS idx_mtf_trading_date
ON instrument_mtf (trading_date);

CREATE INDEX IF NOT EXISTS idx_mtf_enabled
ON instrument_mtf (mtf_enabled);
