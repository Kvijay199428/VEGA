-- Flyway Migration V31: User Settings
-- Centralized user preference storage with role-based access control

CREATE TABLE IF NOT EXISTS user_settings (
    user_id         TEXT NOT NULL,
    setting_key     TEXT NOT NULL,
    setting_value   TEXT NOT NULL,
    scope           TEXT NOT NULL DEFAULT 'GLOBAL',  -- GLOBAL / SESSION / BROKER
    editable        INTEGER NOT NULL DEFAULT 1,
    role_min        TEXT NOT NULL DEFAULT 'TRADER', -- VIEWER / TRADER / ADMIN
    last_updated    DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, setting_key)
);

CREATE INDEX IF NOT EXISTS idx_settings_user 
ON user_settings (user_id);

CREATE INDEX IF NOT EXISTS idx_settings_scope 
ON user_settings (scope, editable);

-- Settings audit log
CREATE TABLE IF NOT EXISTS settings_audit_log (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         TEXT NOT NULL,
    setting_key     TEXT NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    changed_by      TEXT NOT NULL,
    interface       TEXT NOT NULL,  -- CLI / UI / API
    ip_address      TEXT,
    timestamp       DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_settings_audit 
ON settings_audit_log (user_id, setting_key, timestamp);
