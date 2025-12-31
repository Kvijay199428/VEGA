-- V46: Orders core table
-- Per order-mgmt/a1.md section 3.1
-- Core order persistence after broker ACK

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(64) UNIQUE NOT NULL,
    broker_order_id VARCHAR(64),
    user_id VARCHAR(64) NOT NULL,
    broker VARCHAR(32) NOT NULL DEFAULT 'UPSTOX',
    
    -- Instrument
    exchange VARCHAR(16),
    symbol VARCHAR(64),
    instrument_key VARCHAR(64),
    
    -- Order details
    side VARCHAR(4),              -- BUY, SELL
    order_type VARCHAR(16),       -- MARKET, LIMIT, SL, SL-M
    product VARCHAR(16),          -- I, D, CO, MTF
    quantity INT,
    price DECIMAL(12, 4),
    trigger_price DECIMAL(12, 4),
    
    -- Status
    status VARCHAR(24),           -- PENDING, ACKNOWLEDGED, FILLED, CANCELLED
    filled_quantity INT DEFAULT 0,
    average_price DECIMAL(12, 4),
    
    -- Timestamps
    placed_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    final_status_at TIMESTAMP,
    
    -- RMS snapshot
    rms_snapshot_id BIGINT,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_placed (placed_at),
    INDEX idx_orders_instrument (instrument_key)
);

-- Comment
-- Orders are persisted ONLY after broker ACK
-- This table is audit-critical and immutable post-settlement
