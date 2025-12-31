-- Flyway Migration V13: Equity Security Type Master
-- Classification: NORMAL, SME, IPO, PCA, RELIST

CREATE TABLE IF NOT EXISTS equity_security_type (
    code        TEXT PRIMARY KEY,
    description TEXT NOT NULL,
    mis_allowed INTEGER NOT NULL DEFAULT 1,
    mtf_allowed INTEGER NOT NULL DEFAULT 1,
    cnc_allowed INTEGER NOT NULL DEFAULT 1
);

-- Seed default security types
INSERT OR IGNORE INTO equity_security_type (code, description, mis_allowed, mtf_allowed, cnc_allowed) VALUES
('NORMAL', 'Standard equity', 1, 1, 1),
('SME',    'Small and Medium Enterprise equity', 0, 0, 1),
('IPO',    'Initial Public Offering', 0, 0, 1),
('PCA',    'Under regulatory watch (Prompt Corrective Action)', 0, 0, 1),
('RELIST', 'Relisted equity', 0, 0, 1);
