-- Flyway Migration V24: Client Risk State
-- Real-time tracking of client exposure and MTM

CREATE TABLE IF NOT EXISTS client_risk_state (
    client_id          TEXT PRIMARY KEY,
    gross_exposure     REAL NOT NULL DEFAULT 0,
    net_exposure       REAL NOT NULL DEFAULT 0,
    intraday_turnover  REAL NOT NULL DEFAULT 0,
    current_mtm        REAL NOT NULL DEFAULT 0,
    open_positions     INTEGER NOT NULL DEFAULT 0,
    last_updated       DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_client_mtm 
ON client_risk_state (current_mtm);
