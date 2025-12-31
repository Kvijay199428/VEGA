-- V49: Trades and settlements tables
-- Per order-mgmt/a2.md sections 13.1-13.2

CREATE TABLE IF NOT EXISTS trades (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trade_id VARCHAR(64) UNIQUE NOT NULL,
    order_id VARCHAR(64) NOT NULL,
    
    -- Trade details
    quantity INT NOT NULL,
    price DECIMAL(12, 4) NOT NULL,
    exchange VARCHAR(16),
    
    -- Timestamps
    traded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_trades_order_id (order_id),
    INDEX idx_trades_traded_at (traded_at)
);

-- Settlements table
CREATE TABLE IF NOT EXISTS settlements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_id VARCHAR(64) UNIQUE NOT NULL,
    trade_id VARCHAR(64) NOT NULL,
    
    -- Settlement details
    settlement_date DATE NOT NULL,
    net_amount DECIMAL(14, 4),
    charges DECIMAL(12, 4),
    status VARCHAR(16) DEFAULT 'PENDING',  -- PENDING, COMPLETED, FAILED
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP,
    
    INDEX idx_settlements_trade_id (trade_id),
    INDEX idx_settlements_date (settlement_date),
    INDEX idx_settlements_status (status)
);

-- Comment
-- Trade → Settlement linkage for T+1 settlement tracking
