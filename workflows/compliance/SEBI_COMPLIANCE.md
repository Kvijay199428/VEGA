# SEBI Compliance Documentation

**System:** Instrument Master & Risk Management System (IM-RMS)  
**Version:** 4.6.0  
**Status:** SEBI SUBMISSION READY

---

## Part 1 — System Overview

The IM-RMS is a **deterministic, auditable backend system** responsible for:

- Instrument master ingestion
- Contract lifecycle governance
- Risk Management System (RMS) enforcement
- Broker-agnostic order admissibility validation

### Regulatory Scope

| In Scope | Out of Scope |
|----------|--------------|
| Instrument discovery | Price discovery |
| Contract eligibility | Market making |
| Risk checks | Proprietary trading |
| Broker routing | |

---

## Part 2 — Compliance Statement

### Non-Negotiable Controls

1. **No instrument is created dynamically at runtime**
2. **All tradable instruments are sourced from official exchange contract masters**
3. **User settings cannot override exchange rules, regulatory constraints, or RMS checks**
4. **Every order decision is reproducible, auditable, and timestamped**

### Regulatory Alignment

| Requirement | Status |
|-------------|--------|
| SEBI RMS Guidelines | ✅ Compliant |
| Exchange F&O Specifications | ✅ Enforced |
| Broker API Constraints | ✅ Integrated |
| Audit Traceability | ✅ Full replay capability |

---

## Part 3 — Instrument Governance

### 3.1 Instrument Identity

- Each instrument uses a **persistent `instrument_key`**
- Keys are **immutable and broker-agnostic**
- Metadata is stored **prior to trading hours (T-1)**

### 3.2 Contract Lifecycle

- Expiry dates from **exchange calendars**
- NSE and BSE rules are **physically separated**
- Strike disablement follows **exchange circulars**

### 3.3 Source of Truth

| Layer | Role |
|-------|------|
| Database | Single source of truth |
| Broker APIs | Verification only |
| Frontend | Read-only consumer |

---

## Part 4 — RMS Enforcement

### Validation Chain

```
1. Instrument Eligibility
2. Contract Status
3. Quantity/Freeze Limits
4. Price Band Validation
5. Client Risk Checks
6. Broker Routing
```

**Failure at any stage → Deterministic rejection with traceable codes**

### Rejection Traceability

Every rejection contains:
- RMS code
- Human-readable reason
- Instrument snapshot
- Timestamp
- Configuration version

---

## Part 5 — User Settings Framework

### Design Principle

> User settings affect **ORDER** of operations, not **AUTHORITY** of operations.

### Settings Hierarchy (Immutable)

```
1. Regulatory Rules     (highest)
2. Exchange Rules
3. System Defaults
4. User Preferences
5. Session Overrides    (lowest)
```

Lower-priority settings **cannot override** higher-priority constraints.

### User-Configurable Settings

| Setting | Purpose | Editable By |
|---------|---------|-------------|
| Instrument Load Priority | Search/display order | User |
| Preferred Sectors | Cache pre-warm, grouping | User |
| Validation Order | Within allowed bounds | User |
| Broker Routing Priority | Multi-broker routing | User |

### Non-Configurable (Regulatory)

- Expiry rules
- T2T restrictions
- Surveillance flags
- Lot sizes

---

## Part 6 — Audit & Reproducibility

For any historical order, the system can reconstruct:

- Instrument state at order time
- RMS ruleset version
- User preference snapshot
- System defaults
- Exchange and regulatory context

**This ensures full SEBI-compliant explainability.**

---

## Part 7 — Explicit SEBI Assurances

| Guarantee | Status |
|-----------|--------|
| No runtime contract creation | ✅ |
| No broker-driven eligibility | ✅ |
| No user override of regulations | ✅ |
| Deterministic order outcomes | ✅ |
| Replay-safe architecture | ✅ |

---

## Final Compliance Statement

> The IM-RMS system enforces **deterministic, regulator-aligned, and auditable control** over instrument eligibility and order admission, while allowing **bounded user preferences** that do not compromise market integrity.

---

*Document Status: FINALIZED — SEBI SUBMISSION READY*  
*Generated: 2025-12-30*
