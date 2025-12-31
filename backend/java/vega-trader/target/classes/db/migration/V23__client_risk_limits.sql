-- Flyway Migration V23: Client Risk Limits
-- Per-client exposure, order value, and loss limits

CREATE TABLE IF NOT EXISTS client_risk_limits (
    client_id              TEXT PRIMARY KEY,
    max_gross_exposure     REAL NOT NULL,
    max_net_exposure       REAL NOT NULL,
    max_order_value        REAL NOT NULL,
    max_intraday_turnover  REAL NOT NULL,
    max_open_positions     INTEGER NOT NULL,
    max_intraday_loss      REAL NOT NULL,
    trading_enabled        INTEGER NOT NULL DEFAULT 1,
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_client_trading_enabled 
ON client_risk_limits (trading_enabled);

-- Default client limits template
INSERT OR IGNORE INTO client_risk_limits VALUES (
    'DEFAULT',
    10000000.0,    -- max_gross_exposure: 1 Cr
    5000000.0,     -- max_net_exposure: 50 Lakh
    500000.0,      -- max_order_value: 5 Lakh
    50000000.0,    -- max_intraday_turnover: 5 Cr
    100,           -- max_open_positions
    100000.0,      -- max_intraday_loss: 1 Lakh
    1,             -- trading_enabled
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
