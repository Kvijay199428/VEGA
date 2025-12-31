-- Flyway Migration V32: Settings Metadata (Schema)
-- Defines allowed settings with validation rules

CREATE TABLE IF NOT EXISTS settings_metadata (
    setting_key     TEXT PRIMARY KEY,
    display_name    TEXT NOT NULL,
    description     TEXT,
    data_type       TEXT NOT NULL,     -- BOOLEAN / INTEGER / DECIMAL / STRING / ENUM / LIST
    category        TEXT NOT NULL,     -- INSTRUMENT / ORDER / RISK / BROKER / LOGGING
    default_value   TEXT,
    min_value       TEXT,
    max_value       TEXT,
    allowed_values  TEXT,              -- JSON array for ENUM/LIST types
    min_role        TEXT NOT NULL DEFAULT 'TRADER',
    editable        INTEGER NOT NULL DEFAULT 1,
    scope           TEXT NOT NULL DEFAULT 'GLOBAL',
    display_order   INTEGER DEFAULT 100
);

-- Seed settings definitions
INSERT OR IGNORE INTO settings_metadata (setting_key, display_name, description, data_type, category, default_value, min_role) VALUES
-- Instrument Settings
('instrument.exchange.enabled', 'Enabled Exchanges', 'Exchanges to display', 'LIST', 'INSTRUMENT', '["NSE","BSE"]', 'TRADER'),
('instrument.segment.enabled', 'Enabled Segments', 'Segments to trade', 'LIST', 'INSTRUMENT', '["EQ","FNO"]', 'TRADER'),
('instrument.search.mode', 'Search Mode', 'Symbol search strategy', 'ENUM', 'INSTRUMENT', 'FUZZY', 'TRADER'),
('instrument.refresh.interval', 'Refresh Interval', 'Auto-refresh seconds', 'INTEGER', 'INSTRUMENT', '30', 'TRADER'),

-- Order Settings
('order.confirm.required', 'Require Confirmation', 'Show confirmation dialog', 'BOOLEAN', 'ORDER', 'true', 'TRADER'),
('order.max.qty.per.symbol', 'Max Qty Per Symbol', 'Soft cap on quantity', 'INTEGER', 'ORDER', '1800', 'TRADER'),
('order.max.notional', 'Max Order Value', 'Soft cap on value', 'DECIMAL', 'ORDER', '500000', 'TRADER'),
('order.price.deviation.pct', 'Price Deviation %', 'Allowed deviation from LTP', 'DECIMAL', 'ORDER', '1.5', 'TRADER'),

-- Risk Settings
('risk.show.span_breakdown', 'Show SPAN Breakdown', 'Display margin details', 'BOOLEAN', 'RISK', 'false', 'VIEWER'),
('risk.show.var_metrics', 'Show VaR Metrics', 'Display VaR analysis', 'BOOLEAN', 'RISK', 'false', 'VIEWER'),
('risk.refresh.interval', 'Risk Refresh Interval', 'Auto-refresh seconds', 'INTEGER', 'RISK', '10', 'TRADER'),

-- Broker Settings
('broker.primary', 'Primary Broker', 'Default broker for orders', 'ENUM', 'BROKER', 'UPSTOX', 'TRADER'),
('broker.fallback.enabled', 'Enable Fallback', 'Use fallback on failure', 'BOOLEAN', 'BROKER', 'false', 'ADMIN'),
('broker.auto.failover', 'Auto Failover', 'Automatic broker switch', 'BOOLEAN', 'BROKER', 'false', 'ADMIN'),

-- Logging Settings
('logging.level.ui', 'UI Log Level', 'Frontend log level', 'ENUM', 'LOGGING', 'INFO', 'TRADER'),
('logging.show.raw_payload', 'Show Raw Payload', 'Display API payloads', 'BOOLEAN', 'LOGGING', 'false', 'ADMIN'),
('logging.order.trace', 'Order Trace', 'Enable order tracing', 'BOOLEAN', 'LOGGING', 'false', 'TRADER');

-- Set allowed values for ENUM types
UPDATE settings_metadata SET allowed_values = '["FUZZY","EXACT","PREFIX"]' WHERE setting_key = 'instrument.search.mode';
UPDATE settings_metadata SET allowed_values = '["UPSTOX","FYERS","ZERODHA"]' WHERE setting_key = 'broker.primary';
UPDATE settings_metadata SET allowed_values = '["DEBUG","INFO","WARN","ERROR"]' WHERE setting_key = 'logging.level.ui';

-- Set ranges for numeric types
UPDATE settings_metadata SET min_value = '5', max_value = '300' WHERE setting_key = 'instrument.refresh.interval';
UPDATE settings_metadata SET min_value = '1', max_value = '10000' WHERE setting_key = 'order.max.qty.per.symbol';
UPDATE settings_metadata SET min_value = '1000', max_value = '10000000' WHERE setting_key = 'order.max.notional';
UPDATE settings_metadata SET min_value = '0.1', max_value = '10' WHERE setting_key = 'order.price.deviation.pct';
UPDATE settings_metadata SET min_value = '5', max_value = '60' WHERE setting_key = 'risk.refresh.interval';
