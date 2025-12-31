# Database Schema

## 1. Database Location

```
backend/java/vega-trader/database/vega_trade.db
```

SQLite 3.x compatible.

---

## 2. Tables

### 2.1 upstox_tokens

Primary table for storing access tokens.

```sql
CREATE TABLE IF NOT EXISTS upstox_tokens (
    api_name TEXT PRIMARY KEY,
    api_index INTEGER NOT NULL,
    access_token TEXT NOT NULL,
    user_id TEXT NOT NULL,
    user_name TEXT,
    is_active INTEGER DEFAULT 1,
    validity_at TEXT NOT NULL,
    generated_at TEXT NOT NULL
);
```

**Column Details:**

| Column | Type | Description |
|--------|------|-------------|
| `api_name` | TEXT | Primary key (PRIMARY, WEBSOCKET1, etc.) |
| `api_index` | INTEGER | Index 0-5 |
| `access_token` | TEXT | OAuth access token |
| `user_id` | TEXT | Upstox user ID (e.g., 7EAHBJ) |
| `user_name` | TEXT | User display name |
| `is_active` | INTEGER | 1=active, 0=inactive |
| `validity_at` | TEXT | Expiry timestamp (yyyy-MM-dd HH:mm:ss) |
| `generated_at` | TEXT | Generation timestamp |

**Sample Data:**
```sql
INSERT INTO upstox_tokens VALUES (
    'PRIMARY', 0, 'eyJ0eXAi...', '7EAHBJ', 'vijay kumar sharma',
    1, '2025-12-30 03:30:00', '2025-12-29 21:10:15'
);
```

---

### 2.2 token_execution_state

Resume-from-failure state persistence.

```sql
CREATE TABLE IF NOT EXISTS token_execution_state (
    execution_id TEXT PRIMARY KEY,
    last_success_api TEXT,
    next_api TEXT,
    last_failure_epoch INTEGER,
    status TEXT,
    created_at INTEGER DEFAULT (strftime('%s','now')),
    updated_at INTEGER DEFAULT (strftime('%s','now'))
);
```

**Column Details:**

| Column | Type | Description |
|--------|------|-------------|
| `execution_id` | TEXT | UUID execution identifier |
| `last_success_api` | TEXT | Last successfully generated API |
| `next_api` | TEXT | Next API to generate |
| `last_failure_epoch` | INTEGER | Unix timestamp of failure |
| `status` | TEXT | RUNNING, COOLDOWN, RESUMING, COMPLETED |
| `created_at` | INTEGER | Unix timestamp |
| `updated_at` | INTEGER | Unix timestamp |

**Status Values:**

| Status | Description |
|--------|-------------|
| `RUNNING` | Execution in progress |
| `COOLDOWN` | Waiting for 11-minute cooldown |
| `RESUMING` | Resuming after cooldown |
| `COMPLETED` | All tokens generated |

---

### 2.3 upstox_api_configs (Optional)

Database-based API configuration (alternative to .env).

```sql
CREATE TABLE IF NOT EXISTS upstox_api_configs (
    api_index INTEGER PRIMARY KEY,
    api_name TEXT NOT NULL,
    client_id TEXT NOT NULL,
    client_secret TEXT NOT NULL,
    is_primary INTEGER DEFAULT 0,
    enabled INTEGER DEFAULT 1,
    created_at INTEGER,
    updated_at INTEGER
);
```

---

## 3. Queries

### 3.1 Get All Active Tokens
```sql
SELECT * FROM upstox_tokens 
WHERE is_active = 1 
ORDER BY api_index;
```

### 3.2 Get Token by API Name
```sql
SELECT * FROM upstox_tokens 
WHERE api_name = ? AND is_active = 1;
```

### 3.3 Upsert Token (INSERT OR REPLACE)
```sql
INSERT OR REPLACE INTO upstox_tokens 
(api_name, api_index, access_token, user_id, user_name, is_active, validity_at, generated_at)
VALUES (?, ?, ?, ?, ?, 1, ?, ?);
```

### 3.4 Deactivate Token
```sql
UPDATE upstox_tokens SET is_active = 0 WHERE api_name = ?;
```

### 3.5 Get Resumable State
```sql
SELECT * FROM token_execution_state 
WHERE status IN ('COOLDOWN', 'RUNNING', 'RESUMING')
ORDER BY updated_at DESC LIMIT 1;
```

---

## 4. Indexes

```sql
CREATE INDEX IF NOT EXISTS idx_tokens_active 
ON upstox_tokens(is_active);

CREATE INDEX IF NOT EXISTS idx_tokens_validity 
ON upstox_tokens(validity_at);

CREATE INDEX IF NOT EXISTS idx_state_status 
ON token_execution_state(status);
```

---

## 5. Backup & Recovery

### 5.1 Backup Command
```bash
sqlite3 database/vega_trade.db ".backup 'database/backup_$(date +%Y%m%d).db'"
```

### 5.2 Export to CSV
```bash
sqlite3 -header -csv database/vega_trade.db \
  "SELECT api_name, user_id, is_active, validity_at FROM upstox_tokens" \
  > tokens_export.csv
```

---

## 6. Security

- File permissions should be **600** (owner read/write only)
- Never log access_token values
- Consider encrypting client_secret at rest

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
