-- V45: Settings definition (schema registry)
-- Per IMPLEMENTATION_ROADMAP.md section 3.1
-- Defines scope, data type, and constraints for all settings

CREATE TABLE IF NOT EXISTS settings_definition (
    setting_key VARCHAR(64) PRIMARY KEY,
    
    -- Scope: who can modify
    scope ENUM('SYSTEM', 'ADMIN', 'USER') NOT NULL,
    
    -- Data type and constraints
    data_type VARCHAR(32) NOT NULL,
    schema_json TEXT,               -- JSON schema for validation
    default_value TEXT,
    
    -- Constraints
    min_value DOUBLE NULL,
    max_value DOUBLE NULL,
    allowed_values TEXT NULL,       -- JSON array for enums
    
    -- Status
    locked BOOLEAN DEFAULT FALSE,   -- System-locked (immutable)
    deprecated BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    schema_version VARCHAR(16) DEFAULT '1.0',
    description TEXT,
    category VARCHAR(32),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_definition_scope (scope),
    INDEX idx_definition_category (category)
);

-- Insert setting definitions
INSERT INTO settings_definition (setting_key, scope, data_type, default_value, min_value, max_value, category, description) VALUES
    -- Trading controls (ADMIN only)
    ('trading.maxOrderQty', 'ADMIN', 'integer', '1800', 1, 5000, 'TRADING', 'Maximum order quantity'),
    ('trading.maxNotionalValue', 'ADMIN', 'decimal', '10000000', 100000, 100000000, 'TRADING', 'Maximum notional value per order'),
    ('trading.allowedExchanges', 'ADMIN', 'json_array', '["NSE","BSE","NFO"]', NULL, NULL, 'TRADING', 'Allowed exchanges'),
    ('trading.allowedProducts', 'ADMIN', 'json_array', '["I","D","CO"]', NULL, NULL, 'TRADING', 'Allowed product types'),
    ('trading.allowedOrderTypes', 'ADMIN', 'json_array', '["MARKET","LIMIT","SL","SL-M"]', NULL, NULL, 'TRADING', 'Allowed order types'),
    ('trading.allowAfterMarket', 'ADMIN', 'boolean', 'true', NULL, NULL, 'TRADING', 'Allow after-market orders'),
    
    -- Option chain controls (ADMIN only)
    ('options.maxStrikesPerSide', 'ADMIN', 'integer', '20', 5, 50, 'OPTION_CHAIN', 'Max strikes per side'),
    ('options.expiryFetchWindowDays', 'ADMIN', 'integer', '90', 30, 180, 'OPTION_CHAIN', 'Expiry fetch window'),
    ('options.disableIlliquidStrikes', 'ADMIN', 'boolean', 'true', NULL, NULL, 'OPTION_CHAIN', 'Disable illiquid strikes'),
    ('options.bseExpiryForkEnabled', 'ADMIN', 'boolean', 'false', NULL, NULL, 'OPTION_CHAIN', 'Enable BSE expiry fork'),
    
    -- RMS controls (ADMIN only, some SYSTEM-locked)
    ('rms.marginBufferPct', 'ADMIN', 'decimal', '5', 0, 50, 'RMS', 'Margin buffer percentage'),
    ('rms.rejectOnMarginDrop', 'ADMIN', 'boolean', 'true', NULL, NULL, 'RMS', 'Reject on margin drop'),
    ('rms.maxOrdersPerSecond', 'ADMIN', 'integer', '10', 1, 100, 'RMS', 'Rate limit per second'),
    ('rms.killSwitch.enabled', 'ADMIN', 'boolean', 'false', NULL, NULL, 'RMS', 'Global kill switch'),
    ('rms.killSwitch.reason', 'ADMIN', 'string', '', NULL, NULL, 'RMS', 'Kill switch reason'),
    
    -- User-editable settings
    ('execution.defaultOrderType', 'USER', 'enum', 'LIMIT', NULL, NULL, 'EXECUTION', 'Default order type'),
    ('execution.defaultProduct', 'USER', 'enum', 'I', NULL, NULL, 'EXECUTION', 'Default product'),
    ('execution.priceProtectionTicks', 'USER', 'integer', '10', 0, 100, 'EXECUTION', 'Price protection ticks'),
    
    ('ui.optionChain.strikeStep', 'USER', 'integer', '100', 50, 500, 'UI', 'Strike step'),
    ('ui.optionChain.highlightOI', 'USER', 'boolean', 'true', NULL, NULL, 'UI', 'Highlight OI'),
    ('ui.optionChain.autoRefreshMs', 'USER', 'integer', '5000', 1000, 60000, 'UI', 'Auto refresh interval'),
    ('ui.optionChain.greeksView', 'USER', 'boolean', 'true', NULL, NULL, 'UI', 'Show greeks'),
    
    -- Latency controls (ADMIN only)
    ('latency.mode', 'ADMIN', 'enum', 'NORMAL', NULL, NULL, 'LATENCY', 'Latency mode'),
    ('ws.deltaOnly', 'ADMIN', 'boolean', 'true', NULL, NULL, 'WEBSOCKET', 'Delta-only updates'),
    ('ws.binaryEncoding', 'ADMIN', 'boolean', 'true', NULL, NULL, 'WEBSOCKET', 'Binary encoding');

-- Mark system-locked settings
UPDATE settings_definition SET locked = TRUE WHERE setting_key IN (
    'rms.killSwitch.enabled',
    'rms.killSwitch.reason'
);

-- Set allowed values for enums
UPDATE settings_definition SET allowed_values = '["MARKET","LIMIT"]' WHERE setting_key = 'execution.defaultOrderType';
UPDATE settings_definition SET allowed_values = '["I","D","CO","MTF"]' WHERE setting_key = 'execution.defaultProduct';
UPDATE settings_definition SET allowed_values = '["NORMAL","LOW_LATENCY","DEBUG"]' WHERE setting_key = 'latency.mode';

-- Comment
-- This table defines the schema for all settings
-- Scope determines who can modify (USER < ADMIN < SYSTEM)
-- Locked settings cannot be modified at all
