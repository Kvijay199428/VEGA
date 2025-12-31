# Auth Database Schema

The Auth module uses a dedicated SQLite database to store tokens and audit logs.

## Entity: UpstoxTokenEntity
**Table**: `upstox_tokens`
**Class**: `com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity`

Stores the OAuth access tokens.

| Field | Description |
|---|---|
| `api_name` | Unique Identifier (e.g., `PRIMARY`, `ORDER_CONN`, `MARKET_DATA`) |
| `access_token` | The active Bearer token |
| `refresh_token` | Token used to refresh access_token (not used in current flow) |
| `is_active` | 1 = Active, 0 = Disabled |
| `validity_at` | Timestamp string of last successful validation |
| `generated_at`| Timestamp string of generation |

## Entity: TokenAuditEntity
**Table**: `token_audit_log`

Logs every generation attempt and validation check for compliance and debugging.
