-- Flyway Migration: V2__create_audit_tables.sql
-- Creates SEBI-compliant audit tables (append-only, immutable)
-- Per a3.md DDL Schemas

-- Token Audit Trail
CREATE TABLE IF NOT EXISTS token_audit (
    audit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    api_name VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL, -- ISSUED, REFRESHED, REVOKED, EXPIRED, HEALTH_CHECK_FAIL
    token_hash VARCHAR(64), -- SHA-256 hash
    actor_id VARCHAR(64),
    actor_type VARCHAR(16), -- USER, SYSTEM, SCHEDULER
    ip_address VARCHAR(45),
    user_agent VARCHAR(256),
    event_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(1024),
    integrity_hash VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_token_audit_api ON token_audit(api_name);
CREATE INDEX IF NOT EXISTS idx_token_audit_ts ON token_audit(event_ts);

-- Option Chain Audit Trail
CREATE TABLE IF NOT EXISTS option_chain_audit (
    audit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    instrument_key VARCHAR(64) NOT NULL,
    expiry_date DATE,
    fetch_source VARCHAR(16), -- API, CACHE, FALLBACK
    status_code INTEGER,
    strike_count INTEGER,
    spot_price REAL,
    fetch_latency_ms INTEGER,
    user_id VARCHAR(64),
    ip_address VARCHAR(45),
    user_agent VARCHAR(256),
    fetch_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message VARCHAR(512),
    integrity_hash VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_oc_audit_instrument ON option_chain_audit(instrument_key);
CREATE INDEX IF NOT EXISTS idx_oc_audit_ts ON option_chain_audit(fetch_ts);

-- Admin Actions Audit Trail
CREATE TABLE IF NOT EXISTS admin_actions_audit (
    audit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    action_type VARCHAR(64) NOT NULL, -- STRIKE_DISABLE, STRIKE_ENABLE, BROKER_PRIORITY, CONTRACT_ROLLBACK
    target_entity VARCHAR(64),
    target_id VARCHAR(128),
    old_value VARCHAR(1024),
    new_value VARCHAR(1024),
    reason VARCHAR(512),
    performed_by VARCHAR(64) NOT NULL,
    performer_role VARCHAR(32),
    ip_address VARCHAR(45),
    user_agent VARCHAR(256),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN,
    error_message VARCHAR(512),
    integrity_hash VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_type ON admin_actions_audit(action_type);
CREATE INDEX IF NOT EXISTS idx_admin_audit_performer ON admin_actions_audit(performed_by);
CREATE INDEX IF NOT EXISTS idx_admin_audit_ts ON admin_actions_audit(performed_at);

-- Order Audit Trail (SEBI Critical)
CREATE TABLE IF NOT EXISTS order_audit (
    audit_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(32) NOT NULL, -- PLACE, MODIFY, CANCEL, REJECT, FILL
    old_snapshot TEXT, -- JSON
    new_snapshot TEXT, -- JSON
    actor_type VARCHAR(16), -- USER, SYSTEM, ADMIN
    actor_id VARCHAR(64),
    ip_address VARCHAR(45),
    user_agent VARCHAR(256),
    event_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    broker_code VARCHAR(16),
    latency_ms INTEGER,
    integrity_hash VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_order_audit_oid ON order_audit(order_id);
CREATE INDEX IF NOT EXISTS idx_order_audit_event ON order_audit(event_type);
CREATE INDEX IF NOT EXISTS idx_order_audit_ts ON order_audit(event_ts);

-- Audit Export History
CREATE TABLE IF NOT EXISTS audit_export_history (
    export_id INTEGER PRIMARY KEY AUTOINCREMENT,
    export_type VARCHAR(32) NOT NULL, -- TOKEN, ORDER, ADMIN, OPTIONCHAIN
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    record_count INTEGER,
    file_path VARCHAR(512),
    file_format VARCHAR(16), -- CSV, PDF, JSON
    requested_by VARCHAR(64) NOT NULL,
    exported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(16), -- SUCCESS, FAILED, IN_PROGRESS
    error_message VARCHAR(512)
);

CREATE INDEX IF NOT EXISTS idx_export_type ON audit_export_history(export_type);
CREATE INDEX IF NOT EXISTS idx_export_ts ON audit_export_history(exported_at);
