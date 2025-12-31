-- Flyway Migration V40: Admin Actions Audit
-- Per arch/a6.md section 5.2 - Regulatory audit tables

CREATE TABLE IF NOT EXISTS admin_actions_audit (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    action_type         TEXT NOT NULL,       -- STRIKE_DISABLE, BROKER_PRIORITY, CONTRACT_ROLLBACK
    action_target       TEXT NOT NULL,       -- What was affected
    action_payload      TEXT,                -- JSON of the action parameters
    performed_by        TEXT NOT NULL,
    performed_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address          TEXT,
    reason              TEXT
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_type 
ON admin_actions_audit (action_type, performed_at);

CREATE INDEX IF NOT EXISTS idx_admin_audit_user 
ON admin_actions_audit (performed_by, performed_at);

-- Contract version history for audit
CREATE TABLE IF NOT EXISTS contract_version_history (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    broker              TEXT NOT NULL,
    underlying_key      TEXT NOT NULL,
    expiry              DATE NOT NULL,
    strike              REAL NOT NULL,
    option_type         TEXT NOT NULL,
    old_version         INTEGER,
    new_version         INTEGER NOT NULL,
    change_reason       TEXT NOT NULL,
    changed_at          DATETIME DEFAULT CURRENT_TIMESTAMP
);
