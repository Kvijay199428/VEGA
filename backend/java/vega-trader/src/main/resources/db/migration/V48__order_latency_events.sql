-- V48: Order latency and events tables
-- Per order-mgmt/a1.md section 3.3-3.4 and a2.md section 12.2

CREATE TABLE IF NOT EXISTS order_latency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(64) NOT NULL,
    
    -- Latency breakdown
    broker_latency_ms INT,
    system_latency_ms INT,
    network_latency_ms INT,
    total_latency_ms INT,
    
    -- Metadata
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_latency_order_id (order_id)
);

-- Order audit events (compliance)
CREATE TABLE IF NOT EXISTS order_audit_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,  -- PLACE, MODIFY, CANCEL, FILL, REJECT
    payload LONGTEXT,                  -- JSON payload
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_audit_order_id (order_id),
    INDEX idx_audit_event_type (event_type)
);

-- Order events (lifecycle tracking)
CREATE TABLE IF NOT EXISTS order_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL,  -- PLACE, MODIFY, CANCEL
    
    -- Before/after state
    old_quantity INT,
    new_quantity INT,
    old_price DECIMAL(12, 4),
    new_price DECIMAL(12, 4),
    
    -- Charges delta
    charges_delta DECIMAL(12, 4) DEFAULT 0,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_events_order_id (order_id)
);

-- Comment
-- Latency metrics for SLA monitoring
-- Events for lifecycle accounting (modification = new financial event)
