-- V47: Order charges table
-- Per order-mgmt/a1.md section 3.2
-- All broker + exchange + statutory levies

CREATE TABLE IF NOT EXISTS order_charges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(64) NOT NULL,
    
    -- Brokerage
    brokerage DECIMAL(12, 4) DEFAULT 0,
    
    -- Exchange charges
    exchange_txn_charge DECIMAL(12, 4) DEFAULT 0,
    
    -- Statutory charges
    sebi_charge DECIMAL(12, 4) DEFAULT 0,
    stt DECIMAL(12, 4) DEFAULT 0,
    stamp_duty DECIMAL(12, 4) DEFAULT 0,
    gst DECIMAL(12, 4) DEFAULT 0,
    ipf DECIMAL(12, 4) DEFAULT 0,
    
    -- Total
    total_charges DECIMAL(12, 4) DEFAULT 0,
    currency VARCHAR(8) DEFAULT 'INR',
    
    -- Metadata
    computed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_charges_order_id (order_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- Comment
-- This table is APPEND-ONLY (never updated post-finalization)
-- Charges are deterministic and reproducible
