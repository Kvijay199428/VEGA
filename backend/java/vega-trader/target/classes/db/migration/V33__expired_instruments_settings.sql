-- Flyway Migration V33: User Settings for Expired Instruments
-- Settings specific to historical data and expired instrument features

-- Add new settings metadata for expired instruments
INSERT OR IGNORE INTO settings_metadata (setting_key, display_name, description, data_type, category, default_value, min_role, display_order) VALUES
-- Expired instruments settings
('expired.default_expiry_fetch', 'Default Expiry Fetch', 'Fetch latest or all expiries', 'ENUM', 'INSTRUMENT', 'latest', 'TRADER', 40),
('expired.default_instrument_type', 'Default Instrument Type', 'Options, futures, or both', 'ENUM', 'INSTRUMENT', 'both', 'TRADER', 41),
('expired.default_interval', 'Default Candle Interval', 'Historical candle interval', 'ENUM', 'INSTRUMENT', 'day', 'TRADER', 42),
('expired.auto_cache_expiries', 'Cache Expiry Lists', 'Enable expiry caching', 'BOOLEAN', 'INSTRUMENT', 'true', 'TRADER', 43),
('expired.auto_cache_contracts', 'Cache Contract Lists', 'Enable contract caching', 'BOOLEAN', 'INSTRUMENT', 'true', 'TRADER', 44),
('expired.max_historical_days', 'Max Historical Days', 'Maximum days to fetch', 'INTEGER', 'INSTRUMENT', '365', 'TRADER', 45),
('expired.show_weekly_options', 'Show Weekly Options', 'Include weekly expiries', 'BOOLEAN', 'INSTRUMENT', 'true', 'TRADER', 46);

-- Set allowed values for ENUM types
UPDATE settings_metadata SET allowed_values = '["latest","all"]' WHERE setting_key = 'expired.default_expiry_fetch';
UPDATE settings_metadata SET allowed_values = '["options","futures","both"]' WHERE setting_key = 'expired.default_instrument_type';
UPDATE settings_metadata SET allowed_values = '["1minute","3minute","5minute","15minute","30minute","day"]' WHERE setting_key = 'expired.default_interval';

-- Set ranges for numeric types
UPDATE settings_metadata SET min_value = '30', max_value = '730' WHERE setting_key = 'expired.max_historical_days';
