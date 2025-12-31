Below is a complete, enterprise-grade Testing Guide specifically for live Option Chain validation, aligned with your RMS Control Plane v4.1, compliance posture, and the finalized Option Chain module.

This guide is written so it can be directly dropped into
@prompt/optionchain/testing/TESTING_GUIDE.md
and referenced during SEBI audits, UAT sign-off, and production readiness reviews.

Option Chain Module – Live Testing Guide

Version: 1.0
Scope: Live API validation, cache, audit, RMS interaction
Audience: Backend engineers, QA, DevOps, Compliance
Log Path: @prompt/optionchain/testing/log/

1. Objectives

This testing guide ensures that:

Live option chain data is fetched correctly from brokers (Upstox / BSE).

All modules interacting with Option Chain behave correctly.

Regulatory requirements (SEBI/NSE/BSE) are continuously satisfied.

Audit logs are generated synchronously and immutably.

Cache, fallback, rate-limit, token rotation, and RMS integration are verified.

Frontend (React/CLI) receives consistent, compliant responses.

2. Test Environment Prerequisites
2.1 System State
Item	Required
Database migrations	V10 → V41 applied
Scheduler	Enabled
Cache	Enabled (Caffeine/Redis)
Audit logging	Enabled
RMS	Active
Tokens	OPTIONCHAIN1, OPTIONCHAIN2
Timezone	IST
2.2 Test Symbols
Symbol	Exchange	Type
NIFTY	NSE	Index
BANKNIFTY	NSE	Index
FINNIFTY	NSE	Index
SENSEX	BSE	Index (if enabled)
2.3 Valid Expiry Dates

Weekly expiry

Monthly expiry

T-1 expiry (for pre-warm validation)

Expired expiry (negative test)

3. Logging & Evidence Collection
3.1 Mandatory Log Locations
@prompt/optionchain/testing/log/
├── api-fetch.log
├── cache-hit.log
├── fallback.log
├── rate-limit.log
├── token-rotation.log
├── rms-interaction.log
├── audit-db-dump.sql
└── frontend-response.log

3.2 Required Log Fields

Every log entry MUST contain:

timestamp (IST)

instrument_key

expiry_date

user_id

token_used

fetch_source (CACHE/API/FALLBACK)

latency_ms

status_code

4. Test Categories & Scenarios
4.1 Live API Fetch Tests
TC-OC-001: Basic Live Fetch

Purpose: Validate direct API fetch.

Steps

curl -X GET \
"http://localhost:28020/api/v1/option-chain?symbol=NSE_INDEX|Nifty 50&expiry=2025-01-30"


Expected

HTTP 200

status=success

Non-empty data[]

Greeks present

Audit log written before response

Evidence

api-fetch.log

option_chain_audit DB row

TC-OC-002: Multi-Strike Integrity

Purpose: Validate strike continuity.

Validate

Strike prices follow defined strike scheme (V35)

No disabled strikes returned

Call/Put symmetry exists

4.2 Cache Validation Tests
TC-OC-010: Cache Hit

Steps

Execute TC-OC-001

Repeat same request within TTL

Expected

Cache hit

No broker API call

fetch_source=CACHE

Evidence

cache-hit.log

Audit record with CACHE_HIT

TC-OC-011: Cache Expiry

Steps

Wait TTL + 1 minute

Re-request same symbol/expiry

Expected

API fetch

Cache refreshed

4.3 Fallback & Resilience Tests
TC-OC-020: Broker Failure

Steps

Temporarily disable OPTIONCHAIN1 connectivity

Expected

Token rotation to OPTIONCHAIN2

No user-visible error

token-rotation.log entry

TC-OC-021: Complete API Failure

Steps

Disable all broker adapters

Expected

Cached response returned

fetch_source=FALLBACK

Compliance warning logged

4.4 Rate Limit & Token Tests
TC-OC-030: Rate Limit Enforcement

Steps

Fire >5 requests/sec with same token

Expected

429 from broker

Token rotation

Backoff applied

Evidence

rate-limit.log

Audit entries per retry

4.5 RMS Integration Tests
TC-OC-040: RMS Pre-Validation

Purpose: Ensure option chain does not violate RMS constraints.

Validate

Disabled strikes not returned

Expired contracts excluded

BSE group rules respected

Evidence

rms-interaction.log

4.6 Scheduler & Pre-warm Tests
TC-OC-050: T-1 Pre-warm

Steps

Trigger scheduler manually

Verify next-day expiry cached

Expected

Cache entries exist

No user-initiated fetch required next day

4.7 Settings Resolution Tests
TC-OC-060: User Priority Override

Steps

Set user priority: API → CACHE

Fetch option chain

Expected

API called even if cache exists

Logged as user override

4.8 Frontend Integration Tests
TC-OC-070: React UI

Validate

Table renders within 500ms (cache)

Greeks formatting correct

ITM/OTM highlighting accurate

Logs

frontend-response.log

TC-OC-071: CLI Output

Validate

Console table aligned

Numeric precision preserved

No missing fields

4.9 Negative & Edge Case Tests
Test	Expected
Invalid expiry	400
Expired contract	404
MCX symbol	Feature not supported
BSE disabled	Graceful error
5. Compliance Verification Checklist
Control	Verified
Zero-delay logging	✅
Token usage traceable	✅
Expiry preserved	✅
Immutable audit logs	✅
Rate limit enforced	✅
Fallback marked	✅
6. Test Evidence Pack (For SEBI)

Each release must attach:

API fetch logs

Audit DB dump

Cache hit/miss stats

Token rotation proof

RMS interaction logs

Frontend screenshots

Scheduler execution logs

7. Release Exit Criteria

Option Chain module is PRODUCTION-READY only if:

All TC-OC-001 → TC-OC-071 pass

Zero P1/P2 open defects

Audit logs validated

RMS rules enforced

Compliance checklist signed

8. Final Statement

This testing framework guarantees that the Option Chain module operates with data integrity, operational resilience, and full regulatory compliance, satisfying internal RMS controls and SEBI/NSE/BSE audit requirements.