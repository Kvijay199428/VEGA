-- V42: User profile snapshot table
-- Per profile/a1.md section 4.3
-- Retention: 90 days minimum (SEBI audit safety)

CREATE TABLE IF NOT EXISTS user_profile_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(32) NOT NULL,
    broker VARCHAR(16) NOT NULL DEFAULT 'UPSTOX',
    
    -- Profile data
    email VARCHAR(128),
    mobile VARCHAR(20),
    name VARCHAR(100),
    pan VARCHAR(20),
    
    -- Capabilities
    exchanges TEXT,              -- JSON array: ["NSE","BSE","NFO"]
    products TEXT,               -- JSON array: ["I","D","CO"]
    order_types TEXT,            -- JSON array: ["MARKET","LIMIT","SL","SL-M"]
    
    -- Permissions
    poa BOOLEAN DEFAULT FALSE,
    ddpi BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Metadata
    payload LONGTEXT,            -- Full raw JSON response
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_user_profile_user_id (user_id),
    INDEX idx_user_profile_fetched (fetched_at)
);

-- Comment
-- This table stores profile snapshots for audit replay
-- Retention policy: 90 days (managed by cleanup job)
