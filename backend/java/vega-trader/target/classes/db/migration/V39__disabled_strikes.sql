-- Flyway Migration V39: Disabled Strikes Table
-- Per arch/a4.md section 4.3 - Strike disablement

CREATE TABLE IF NOT EXISTS disabled_strikes (
    underlying_key      TEXT NOT NULL,
    expiry              DATE NOT NULL,
    strike              REAL NOT NULL,
    option_type         TEXT NOT NULL,      -- CE / PE
    disabled_reason     TEXT NOT NULL,
    disabled_from       DATE NOT NULL,
    disabled_by         TEXT,               -- SYSTEM / ADMIN / EXCHANGE
    notes               TEXT,
    active              INTEGER DEFAULT 1,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (underlying_key, expiry, strike, option_type)
);

CREATE INDEX IF NOT EXISTS idx_disabled_strikes_active 
ON disabled_strikes (active, underlying_key, expiry);

-- Audit log for strike disablement actions
CREATE TABLE IF NOT EXISTS strike_disablement_audit (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    underlying_key      TEXT NOT NULL,
    expiry              DATE NOT NULL,
    strike              REAL NOT NULL,
    option_type         TEXT NOT NULL,
    action              TEXT NOT NULL,       -- DISABLED / ENABLED
    reason              TEXT NOT NULL,
    performed_by        TEXT NOT NULL,
    performed_at        DATETIME DEFAULT CURRENT_TIMESTAMP
);
