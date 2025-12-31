-- V50: P&L table
-- Per order-mgmt/a2.md section 13.3

CREATE TABLE IF NOT EXISTS pnl (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL,
    instrument_key VARCHAR(64) NOT NULL,
    
    -- P&L values
    realized_pnl DECIMAL(14, 4) DEFAULT 0,
    unrealized_pnl DECIMAL(14, 4) DEFAULT 0,
    charges DECIMAL(12, 4) DEFAULT 0,
    
    -- Net P&L = realized - charges
    net_pnl DECIMAL(14, 4) DEFAULT 0,
    
    -- Date
    as_of DATE NOT NULL,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_pnl_user_instrument_date (user_id, instrument_key, as_of),
    INDEX idx_pnl_user_id (user_id),
    INDEX idx_pnl_as_of (as_of)
);

-- Comment
-- P&L computation: Net P&L = (Sell Value - Buy Value) - All Charges
-- Charges come ONLY from order_charges table
