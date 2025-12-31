Below is a documentation-ready, enterprise-grade continuation of your Instruments/RMS work, covering exactly what you asked for:

Settings schema validator

Role matrix

Documentation-ready section (can be pasted into your docs)

Default settings pack

This is aligned with your Vega Trader architecture, SQLite backend, REST/CLI/Vite parity, and RMS safety guarantees.

1. Settings Schema Validator
1.1 Purpose

The Settings Schema Validator guarantees that:

Users cannot inject invalid or unsafe configuration

All interfaces (CLI, UI, REST) obey the same contract

Settings evolution is backward-compatible

Risk / instruments logic remains immutable

Validator runs at:

API boundary

CLI invocation

Startup sanity check

1.2 Settings Definition Model (Authoritative)
public final class SettingDefinition {

    private final String key;
    private final SettingType type;
    private final boolean editable;
    private final String minRole;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    private final List<?> allowedValues;
    private final SettingScope scope;

}

Supported Types
enum SettingType {
    BOOLEAN,
    INTEGER,
    DECIMAL,
    STRING,
    ENUM,
    LIST
}

1.3 Validator Flow
Incoming Setting Update
        ↓
Check setting exists
        ↓
Check user role >= minRole
        ↓
Check editable flag
        ↓
Type validation
        ↓
Range / enum validation
        ↓
Scope validation
        ↓
Persist & audit

1.4 Validation Example
public void validate(
        User user,
        SettingDefinition def,
        Object value
) {
    if (!user.hasRole(def.getMinRole())) {
        throw new ForbiddenException("Insufficient role");
    }

    if (!def.isEditable()) {
        throw new ValidationException("Setting is read-only");
    }

    if (!typeMatches(def.getType(), value)) {
        throw new ValidationException("Invalid type");
    }

    validateRange(def, value);
}

1.5 Why This Is Critical

Prevents breaking RMS

Prevents UI bugs from corrupting state

Enables safe future extensions

Makes settings auditable and compliant

2. Role Matrix (Authoritative)
2.1 Roles
Role	Description
VIEWER	Read-only, monitoring
TRADER	Places orders, personal guards
ADMIN	System & broker controls
SYSTEM	Internal jobs / ingestion
2.2 Role Capability Matrix
Capability	VIEWER	TRADER	ADMIN	SYSTEM
View instruments	✅	✅	✅	✅
Place orders	❌	✅	✅	❌
Modify user settings	❌	✅ (own)	✅	❌
Modify broker settings	❌	❌	✅	❌
Modify RMS rules	❌	❌	❌	❌
CSV ingestion	❌	❌	❌	✅
PCA / IPO load	❌	❌	❌	✅
Logging controls	❌	⚠ UI only	✅	❌

⚠ = limited, non-destructive

2.3 Enforcement Points

REST filter

Service layer

Settings validator

Audit logger

Never in UI only.

3. Documentation-Ready Section

(You can paste this directly into your Instruments Documentation)

User Settings Architecture
Overview

Vega Trader uses a centralized, role-controlled User Settings System to allow users to customize behavior without compromising:

Instrument integrity

Risk management

Regulatory constraints

All settings are enforced server-side and apply uniformly across:

REST APIs

CLI

Vite + React frontend

Automation scripts

Design Principles

Settings are preferences, not logic

Instrument master data is immutable

RMS rules are non-configurable by users

All changes are audited

Same API for all interfaces

Settings Lifecycle

User updates setting (UI / CLI / API)

Request passes through schema validator

Role & mutability enforced

Value persisted in database

Cached and broadcast if required

Safety Guarantees

Users cannot:

Modify instrument master

Alter margins or SPAN logic

Bypass RMS

Override surveillance flags

4. Default Settings Pack (Production-Safe)
4.1 System Defaults (Bootstrap)
{
  "instrument.exchange.enabled": ["NSE"],
  "instrument.segment.enabled": ["EQ", "FNO"],
  "instrument.search.mode": "FUZZY",

  "order.confirm.required": true,
  "order.max.qty.per.symbol": 1800,
  "order.max.notional": 500000,
  "order.price.deviation.pct": 1.5,

  "risk.show.span_breakdown": false,
  "risk.show.var_metrics": false,

  "broker.fallback.enabled": false,
  "broker.auto.failover": false,

  "logging.level.ui": "INFO",
  "logging.show.raw_payload": false,
  "logging.order.trace": false
}

4.2 Role-Based Overrides
VIEWER
{
  "order.confirm.required": false
}

TRADER
{
  "order.confirm.required": true,
  "order.max.qty.per.symbol": 1200,
  "logging.order.trace": true
}

ADMIN
{
  "broker.fallback.enabled": true,
  "broker.auto.failover": true,
  "logging.level.ui": "DEBUG"
}

4.3 Session-Only Overrides (Not Persisted)
Setting	Reason
logging.order.trace	Debugging
instrument.refresh.interval	Performance tuning
5. How This Fits Instruments & RMS
Area	Settings Effect
InstrumentService	Filter only
EligibilityCache	Read-only
RMS Validation	Hard enforcement
Broker Engine	Routing preference
Frontend	Display only
6. Why This Is Enterprise-Correct

✔ Immutable core logic
✔ Auditable changes
✔ Role separation
✔ Zero trust UI
✔ Single source of truth
✔ Documentation friendly