-- V43: Funds margin snapshot table
-- Per profile/a1.md section 5.4
-- Retention: 180 days (margin disputes)

CREATE TABLE IF NOT EXISTS funds_margin_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(32) NOT NULL,
    broker VARCHAR(16) NOT NULL DEFAULT 'UPSTOX',
    
    -- Equity segment (combined after July 2025)
    equity_available DOUBLE,
    equity_used DOUBLE,
    equity_balance DOUBLE,
    
    -- Commodity segment (zeroed after July 2025)
    commodity_available DOUBLE DEFAULT 0,
    commodity_used DOUBLE DEFAULT 0,
    commodity_balance DOUBLE DEFAULT 0,
    
    -- Margin details
    span_margin DOUBLE,
    exposure_margin DOUBLE,
    payin_amount DOUBLE,
    notional_cash DOUBLE,
    
    -- Active orders
    active_orders_count INT DEFAULT 0,
    
    -- Metadata
    payload LONGTEXT,            -- Full raw JSON response
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- July 2025 flag
    combined_margin BOOLEAN DEFAULT FALSE,
    
    -- Indexes
    INDEX idx_funds_user_id (user_id),
    INDEX idx_funds_fetched (fetched_at)
);

-- Comment
-- This table stores funds/margin snapshots for audit and dispute resolution
-- After July 19, 2025: combined_margin=TRUE, commodity values=0
-- Retention policy: 180 days (managed by cleanup job)
