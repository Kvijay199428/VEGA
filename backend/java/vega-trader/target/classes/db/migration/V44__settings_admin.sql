-- V44: Admin settings table
-- Per IMPLEMENTATION_ROADMAP.md section 3.1
-- Admin-only settings with audit tracking

CREATE TABLE IF NOT EXISTS settings_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(64) NOT NULL,
    setting_value TEXT,
    tenant_id VARCHAR(32) NOT NULL DEFAULT 'GLOBAL',
    
    -- Effective date (for scheduled changes)
    effective_from TIMESTAMP,
    effective_until TIMESTAMP NULL,
    
    -- Audit fields
    updated_by VARCHAR(64) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason_code VARCHAR(32),
    reason_comment TEXT,
    
    -- Indexes
    UNIQUE KEY uk_admin_setting (setting_key, tenant_id),
    INDEX idx_admin_effective (effective_from),
    INDEX idx_admin_updated_by (updated_by)
);

-- Insert default admin settings
INSERT INTO settings_admin (setting_key, setting_value, tenant_id, updated_by, reason_code) VALUES
    -- Trading controls
    ('trading.maxOrderQty', '1800', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('trading.maxNotionalValue', '10000000', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('trading.allowedExchanges', '["NSE","BSE","NFO"]', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('trading.allowedProducts', '["I","D","CO"]', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('trading.allowedOrderTypes', '["MARKET","LIMIT","SL","SL-M"]', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('trading.allowAfterMarket', 'true', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    
    -- Option chain controls
    ('options.maxStrikesPerSide', '20', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('options.expiryFetchWindowDays', '90', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('options.disableIlliquidStrikes', 'true', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('options.bseExpiryForkEnabled', 'false', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    
    -- RMS controls
    ('rms.marginBufferPct', '5', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('rms.rejectOnMarginDrop', 'true', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('rms.maxOrdersPerSecond', '10', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('rms.killSwitch.enabled', 'false', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('rms.killSwitch.reason', '', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    
    -- Latency & streaming
    ('latency.mode', 'NORMAL', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('ws.deltaOnly', 'true', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('ws.binaryEncoding', 'true', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    
    -- Maintenance
    ('maintenance.fundsWindowStart', '00:00', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP'),
    ('maintenance.fundsWindowEnd', '05:30', 'GLOBAL', 'SYSTEM', 'INITIAL_SETUP');

-- Comment
-- Admin settings are controlled by Compliance/Ops
-- All changes require reason_code and are audited
