# Option Chain - SEBI Compliance Documentation

**Version:** 1.0  
**Last Updated:** 2025-12-30  
**Status:** SEBI SUBMISSION READY

---

## 1. System Overview

The Option Chain module provides real-time and batch access to derivatives data for NSE and BSE. All fetches are logged for regulatory compliance.

### Scope

| In Scope | Out of Scope |
|----------|--------------|
| Option chain data access | Order placement |
| Greeks calculation display | Trade execution |
| Audit logging | Position management |
| Rate limit enforcement | |

---

## 2. Data Sources

| Source | Purpose | Priority |
|--------|---------|----------|
| Upstox API (OPTIONCHAIN1, OPTIONCHAIN2) | Primary data | 1 |
| BSE API | Secondary (if enabled) | 2 |
| Cache | Fallback on API failure | 3 |

---

## 3. Compliance Controls

### 3.1 Zero-Delay Logging

Audit logs are committed **before** data is returned to the client.

```
Request → Audit Log → Cache/API → Response → Audit Complete
```

### 3.2 Token Tracking

Each request logs:
- Token ID used (OPTIONCHAIN1 / OPTIONCHAIN2)
- Request timestamp
- Response status code
- Full request/response payload

### 3.3 Rate Limit Enforcement

- Max 5 requests/second per token
- Token rotation on limit hit
- Backoff with exponential delay

### 3.4 Batch Pre-warm

Scheduler pre-fetches T-1 expiry option chains at **6:30 PM IST** to reduce latency and ensure data availability.

### 3.5 Fallback Strategy

If API fails, last cached value is served with an audit entry indicating `FALLBACK_USED`.

---

## 4. Audit Log Structure

All requests/responses are stored in an immutable audit database.

| Field | Description |
|-------|-------------|
| `id` | Unique log ID |
| `instrument_key` | Underlying symbol |
| `expiry_date` | Requested expiry |
| `token_used` | OPTIONCHAIN1/2 |
| `request_payload` | Full request JSON |
| `response_payload` | Full response JSON |
| `status_code` | HTTP status |
| `fetch_source` | CACHE / API / FALLBACK |
| `fetched_at` | Timestamp (IST) |

### Retention

| Log Type | Retention Period |
|----------|------------------|
| Audit logs | ≥1 year |
| Request/Response payloads | ≥1 year |
| Cache data | 60 minutes |

---

## 5. Governance

### Admin Controls

- Configure global settings (rate limits, cache TTL)
- Enable/disable BSE support
- Manage broker tokens (add/rotate)
- Export audit logs for review

### User Controls

- Override fetch priority (within bounds)
- Customize display preferences
- Export data for analysis

---

## 6. Evidence Attachments

### Annexure A - Sequence Diagrams

- Option chain fetch flow
- Token rotation flow
- T-1 pre-warm flow

### Annexure B - ER Diagrams

- `option_chain` table schema
- `option_chain_audit` table schema

### Annexure C - Settings Schema

- JSON schema for admin/user settings
- Default values and constraints

---

## 7. Test Evidence Templates

### TC001 - Cache Hit vs API Fetch

| Field | Value |
|-------|-------|
| Test ID | TC001 |
| Description | Verify cache hit returns cached data |
| Expected | Data from cache, audit log: CACHE_HIT |
| Actual | |
| Status | PASS/FAIL |

### TC002 - Rate Limit Enforcement

| Field | Value |
|-------|-------|
| Test ID | TC002 |
| Description | Exceed token rate limit |
| Expected | Requests throttled, token rotated |
| Actual | |
| Status | PASS/FAIL |

### TC003 - Audit Logging

| Field | Value |
|-------|-------|
| Test ID | TC003 |
| Description | Verify all requests logged |
| Expected | Request + Response in audit DB |
| Actual | |
| Status | PASS/FAIL |

---

## 8. Final Compliance Statement

> The Option Chain module provides **auditable, rate-limited, and compliant** access to derivatives market data. All requests are logged with full audit trails, tokens are tracked per request, and fallback mechanisms ensure data availability.

---

*Document Status: FINALIZED — SEBI SUBMISSION READY*
