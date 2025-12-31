-- Flyway Migration V37: RMS Rejection Codes
-- Per a1.md section 4.2 - Standardized rejection codes

CREATE TABLE IF NOT EXISTS rms_rejection_code (
    code                TEXT PRIMARY KEY,
    category            TEXT NOT NULL,
    severity            TEXT NOT NULL,          -- ERROR / WARNING / INFO
    description         TEXT NOT NULL,
    user_message        TEXT,                   -- User-friendly message
    retry_allowed       INTEGER DEFAULT 0,
    active              INTEGER DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_rejection_category 
ON rms_rejection_code (category, severity);

-- Seed rejection codes per a1.md
INSERT OR IGNORE INTO rms_rejection_code (code, category, severity, description, user_message, retry_allowed) VALUES
-- Expiry Related
('RMS_EXPIRY_INVALID', 'EXPIRY', 'ERROR', 'Contract has expired or invalid expiry date', 'This contract has expired', 0),
('RMS_EXPIRY_NOT_FOUND', 'EXPIRY', 'ERROR', 'Expiry date not found for instrument', 'Cannot find expiry for this instrument', 0),
('RMS_EXPIRY_TOO_FAR', 'EXPIRY', 'ERROR', 'Expiry exceeds allowed range', 'Expiry date too far in the future', 0),

-- Strike Related
('RMS_STRIKE_DISABLED', 'STRIKE', 'ERROR', 'Strike is disabled due to OI rules', 'This strike is not available for trading', 0),
('RMS_STRIKE_INVALID', 'STRIKE', 'ERROR', 'Invalid strike price for instrument', 'Invalid strike price', 0),
('RMS_STRIKE_OUT_OF_RANGE', 'STRIKE', 'ERROR', 'Strike outside allowed range', 'Strike price out of valid range', 0),

-- Price Related
('RMS_PRICE_BAND', 'PRICE', 'ERROR', 'Price outside circuit limits', 'Price exceeds circuit limits', 1),
('RMS_PRICE_INVALID', 'PRICE', 'ERROR', 'Invalid price format or value', 'Invalid price entered', 1),
('RMS_PRICE_TICK_SIZE', 'PRICE', 'ERROR', 'Price not in tick size', 'Price must be in valid tick increments', 1),

-- Quantity Related
('RMS_QTY_CAP', 'QUANTITY', 'ERROR', 'Quantity exceeds symbol cap', 'Order quantity exceeds maximum allowed', 1),
('RMS_QTY_LOT_SIZE', 'QUANTITY', 'ERROR', 'Quantity not in lot size', 'Quantity must be in lot multiples', 1),
('RMS_QTY_FREEZE', 'QUANTITY', 'ERROR', 'Quantity exceeds freeze limit', 'Order exceeds freeze quantity limit', 1),

-- BSE Specific
('RMS_BSE_T2T', 'BSE', 'ERROR', 'Trade-for-trade violation', 'Intraday not allowed for this stock', 0),
('RMS_BSE_CNC_ONLY', 'BSE', 'ERROR', 'CNC/Delivery only allowed', 'Only delivery orders allowed for this stock', 0),
('RMS_BSE_GROUP_BLOCKED', 'BSE', 'ERROR', 'BSE group blocked for trading', 'Trading suspended for this stock', 0),

-- Client Risk
('RMS_CLIENT_LIMIT', 'CLIENT', 'ERROR', 'Client limit breached', 'Your trading limit has been reached', 0),
('RMS_CLIENT_MARGIN', 'CLIENT', 'ERROR', 'Insufficient margin', 'Insufficient margin for this order', 0),
('RMS_CLIENT_BLOCKED', 'CLIENT', 'ERROR', 'Client blocked for trading', 'Your account is restricted', 0),

-- Sector Related
('RMS_SECTOR_BLOCKED', 'SECTOR', 'ERROR', 'Sector blocked for trading', 'Trading blocked in this sector', 0),
('RMS_SECTOR_EXPOSURE', 'SECTOR', 'ERROR', 'Sector exposure limit exceeded', 'Sector exposure limit reached', 0),

-- General
('RMS_INSTRUMENT_INACTIVE', 'GENERAL', 'ERROR', 'Instrument not active', 'This instrument is not available', 0),
('RMS_SEGMENT_BLOCKED', 'GENERAL', 'ERROR', 'Segment blocked for trading', 'This segment is currently blocked', 0),
('RMS_SYSTEM_ERROR', 'GENERAL', 'ERROR', 'Internal RMS error', 'System error, please try again', 1);
