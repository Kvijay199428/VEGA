-- Flyway Migration V38: Broker Symbol Mapping Extensions
-- Per arch/a4.md section 3.2 - Contract versioning

-- Add versioning columns to broker_symbol_mapping
-- Note: If broker_symbol_mapping exists from V26, we ALTER; else this is the full schema

-- Contract versioning columns
ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS contract_version INTEGER DEFAULT 1;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS valid_from DATE DEFAULT CURRENT_DATE;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS valid_to DATE;

-- Add underlying_key and expiry for option lookups
ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS underlying_key TEXT;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS expiry DATE;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS strike REAL;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS option_type TEXT;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS lot_size INTEGER;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS tick_size REAL;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS freeze_quantity INTEGER;

ALTER TABLE broker_symbol_mapping 
ADD COLUMN IF NOT EXISTS weekly INTEGER DEFAULT 0;

-- Index for active contract lookups
CREATE INDEX IF NOT EXISTS idx_broker_mapping_active_version 
ON broker_symbol_mapping (broker, underlying_key, expiry, strike, option_type, active);

CREATE INDEX IF NOT EXISTS idx_broker_mapping_validity 
ON broker_symbol_mapping (valid_from, valid_to);
