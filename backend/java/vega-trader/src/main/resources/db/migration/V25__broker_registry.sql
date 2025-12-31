-- Flyway Migration V25: Broker Registry
-- Multi-broker configuration and status

CREATE TABLE IF NOT EXISTS brokers (
    broker_id       TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    api_type        TEXT NOT NULL,  -- REST, WebSocket, FIX
    enabled         INTEGER NOT NULL DEFAULT 1,
    priority        INTEGER NOT NULL DEFAULT 100,
    config_json     TEXT,           -- JSON configuration
    api_base_url    TEXT,
    websocket_url   TEXT,
    auth_type       TEXT DEFAULT 'OAUTH2',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_broker_enabled 
ON brokers (enabled);

-- Default broker entries
INSERT OR IGNORE INTO brokers (broker_id, name, api_type, enabled, priority, api_base_url, websocket_url) VALUES
('UPSTOX', 'Upstox', 'REST', 1, 1, 'https://api.upstox.com/v2', 'wss://api.upstox.com/v2/feed/market-data-feed'),
('FYERS', 'Fyers', 'REST', 0, 2, 'https://api-t1.fyers.in/api/v3', 'wss://api-t1.fyers.in/socket/v3'),
('ZERODHA', 'Zerodha Kite', 'REST', 0, 3, 'https://api.kite.trade', 'wss://ws.kite.trade');
