-- Flyway Migration V15: Extend instrument_master with RMS columns
-- Adds equity_security_type and exchange_series columns

-- Note: SQLite does not support adding FK constraints via ALTER TABLE
-- We add columns and enforce FK logic in application layer

ALTER TABLE instrument_master ADD COLUMN equity_security_type TEXT DEFAULT 'NORMAL';

ALTER TABLE instrument_master ADD COLUMN exchange_series TEXT DEFAULT 'EQ';

-- Create index for RMS lookups
CREATE INDEX IF NOT EXISTS idx_instrument_equity_type 
ON instrument_master (equity_security_type);

CREATE INDEX IF NOT EXISTS idx_instrument_series 
ON instrument_master (exchange, exchange_series);
