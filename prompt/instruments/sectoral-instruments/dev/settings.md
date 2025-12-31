Below is a production-grade, role-safe, cross-interface user settings design that you can directly implement in Vega Trader without allowing users to break instruments, risk, or data integrity.

This is not UI-specific. It is a centralized settings contract consumed uniformly by:

CLI

Vite + React frontend

REST (curl / automation)

Future tools (admin console, batch jobs)

1. Core Design Principle (Non-Negotiable)
Users NEVER directly change system behavior

They only change preferences, scopes, and feature toggles that are:

Validated

Permission-bound

Schema-enforced

Audited

System logic always reads settings → never trusts UI

2. Settings Architecture (Single Source of Truth)
┌──────────────┐
│  CLI / UI    │
│  curl / API  │
└──────┬───────┘
       │
       ▼
┌─────────────────────┐
│ UserSettingsService │  ← authoritative
└──────┬──────────────┘
       │
       ▼
┌───────────────────────────┐
│ user_settings (SQLite)    │
│ + schema + versioning     │
└───────────────────────────┘

3. Settings Storage Model (SQLite)
Table: user_settings
CREATE TABLE user_settings (
    user_id           TEXT NOT NULL,
    setting_key       TEXT NOT NULL,
    setting_value     TEXT NOT NULL,
    scope             TEXT NOT NULL, -- GLOBAL / SESSION / BROKER
    editable          BOOLEAN NOT NULL DEFAULT 1,
    role_min          TEXT NOT NULL, -- VIEWER / TRADER / ADMIN
    last_updated      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, setting_key)
);

4. What Users ARE Allowed to Control (Safe Settings)
A. Instrument Visibility & Filters (SAFE)
Key	Type	Description
instrument.exchange.enabled	list	NSE, BSE
instrument.segment.enabled	list	EQ, FNO, CDS
instrument.watchlist.indices	list	NIFTY50, BANKNIFTY
instrument.search.mode	enum	FUZZY, EXACT
instrument.refresh.interval	int	seconds

❌ Cannot add instruments
❌ Cannot delete master data

B. Trading Guardrails (User-Level Only)
Key	Type	Description
order.confirm.required	boolean	Order confirmation
order.max.qty.per.symbol	int	Soft cap
order.max.notional	decimal	Soft cap
order.price.deviation.pct	decimal	Allowed deviation

📌 These are soft guards
📌 RMS still enforces hard limits

C. Risk & Exposure View Controls (READ-ONLY EFFECT)
Key	Type	Description
risk.show.span_breakdown	boolean	UI only
risk.show.var_metrics	boolean	UI only
risk.refresh.interval	int	seconds

📌 Does NOT alter calculations

D. Broker Selection & Failover (Restricted)
Key	Type	Role
broker.primary	enum	TRADER+
broker.fallback.enabled	boolean	ADMIN
broker.auto.failover	boolean	ADMIN
E. Logging & Diagnostics (Non-Destructive)
Key	Type	Description
logging.level.ui	enum	INFO / DEBUG
logging.show.raw_payload	boolean	Masked
logging.order.trace	boolean	Per user

📌 Backend log levels remain immutable

5. Settings Resolution Order (Very Important)
SYSTEM DEFAULTS
   ↓
ROLE DEFAULTS
   ↓
USER GLOBAL SETTINGS
   ↓
BROKER-SCOPED SETTINGS
   ↓
SESSION OVERRIDES (non-persistent)

6. Unified Settings API (Used Everywhere)
Read Settings (CLI / UI / curl)
GET /api/settings
Authorization: Bearer <token>

Response
{
  "instrument.exchange.enabled": ["NSE"],
  "order.confirm.required": true,
  "order.max.qty.per.symbol": 1800,
  "logging.level.ui": "INFO"
}

Update Setting (Controlled)
PUT /api/settings/order.confirm.required

{
  "value": true
}

Server Enforces:

Role

Type

Range

Mutability

7. CLI Usage (Exact)
vega settings list
vega settings get order.max.qty.per.symbol
vega settings set order.confirm.required true


Internally → calls same REST API

8. Vite + React Integration
On Login
GET /api/settings


Store in:

SettingsContext


All components consume read-only settings

No component writes directly.

9. Curl Testing (Example)
curl -X PUT http://localhost:8080/api/settings/order.confirm.required \
 -H "Authorization: Bearer $TOKEN" \
 -H "Content-Type: application/json" \
 -d '{"value": false}'

10. What Users Can NEVER Touch (Hard Block)

🚫 Instrument master data
🚫 CSV loaders
🚫 RMS formulas
🚫 Margin engines
🚫 Exchange mappings
🚫 Surveillance flags
🚫 Trade-for-Trade lists
🚫 PCA / IPO logic

Those are system-owned only.

11. Audit & Safety

Every change logs:

user_id
setting_key
old_value
new_value
timestamp
ip_address
interface (CLI / UI / API)

12. Why This Design Is Correct

✔ One settings model
✔ Interface-agnostic
✔ Role-safe
✔ Cannot corrupt instruments
✔ Easy to document
✔ Easy to test
✔ Enterprise-compliant