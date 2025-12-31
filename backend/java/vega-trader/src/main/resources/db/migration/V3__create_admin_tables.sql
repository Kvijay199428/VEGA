-- Flyway Migration: V3__create_admin_tables.sql
-- Creates admin governance tables for strike management and broker registry
-- Per a1.md Section 9

-- Disabled Strikes Table
CREATE TABLE IF NOT EXISTS disabled_strikes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    underlying_key VARCHAR(64) NOT NULL,
    expiry_date DATE,
    strike_price REAL NOT NULL,
    option_type VARCHAR(2) NOT NULL, -- CE, PE
    disabled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disabled_by VARCHAR(64) NOT NULL,
    reason VARCHAR(512),
    active BOOLEAN NOT NULL DEFAULT true,
    enabled_at TIMESTAMP,
    enabled_by VARCHAR(64),
    enable_reason VARCHAR(512)
);

CREATE INDEX IF NOT EXISTS idx_disabled_strikes_underlying ON disabled_strikes(underlying_key);
CREATE INDEX IF NOT EXISTS idx_disabled_strikes_expiry ON disabled_strikes(expiry_date);
CREATE INDEX IF NOT EXISTS idx_disabled_strikes_active ON disabled_strikes(active);
CREATE UNIQUE INDEX IF NOT EXISTS idx_disabled_strikes_unique 
    ON disabled_strikes(underlying_key, expiry_date, strike_price, option_type) 
    WHERE active = true;

-- Broker Registry Table
CREATE TABLE IF NOT EXISTS broker_registry (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    broker_code VARCHAR(32) NOT NULL UNIQUE,
    broker_name VARCHAR(128) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    instrument_type VARCHAR(16),
    priority INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    max_order_value INTEGER,
    rate_limit_per_minute INTEGER,
    supports_multi_order BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_broker_registry_exchange ON broker_registry(exchange);
CREATE INDEX IF NOT EXISTS idx_broker_registry_priority ON broker_registry(priority);
CREATE INDEX IF NOT EXISTS idx_broker_registry_active ON broker_registry(is_active);

-- Insert default Upstox broker
INSERT OR IGNORE INTO broker_registry (broker_code, broker_name, exchange, instrument_type, priority, rate_limit_per_minute)
VALUES ('UPSTOX', 'Upstox', 'NSE', 'FO', 1, 300);

INSERT OR IGNORE INTO broker_registry (broker_code, broker_name, exchange, instrument_type, priority, rate_limit_per_minute)
VALUES ('UPSTOX', 'Upstox', 'NSE', 'EQ', 1, 300);

-- Contract Version History (for rollback)
CREATE TABLE IF NOT EXISTS contract_version_history (
    version_id INTEGER PRIMARY KEY AUTOINCREMENT,
    broker_code VARCHAR(32) NOT NULL,
    version_number INTEGER NOT NULL,
    contract_data TEXT, -- JSON
    effective_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    is_active BOOLEAN NOT NULL DEFAULT true,
    rollback_count INTEGER DEFAULT 0,
    last_rollback_at TIMESTAMP,
    last_rollback_by VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_contract_version_broker ON contract_version_history(broker_code);
CREATE INDEX IF NOT EXISTS idx_contract_version_active ON contract_version_history(is_active);
