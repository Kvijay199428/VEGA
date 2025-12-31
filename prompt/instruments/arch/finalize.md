Below is a finalized, documentation-ready, backend-first guide for the Instruments Module, written so that you can generate a complete, auditable, long-lived documentation set without gaps.

This is not code; this is the authoritative documentation blueprint + finalized backend behavior that matches your current architecture and implementation logs.

Instruments Backend – Final Documentation & Authoring Guide
1. Purpose of the Instruments Module

The Instruments Module is the single source of truth for:

Tradable instruments (EQ / FO / INDEX)

Contract identity (instrument_key)

Exchange-specific rules (NSE/BSE)

Expiry, strike, series, lifecycle state

Risk & regulatory constraints

Broker-specific mappings

Key principle:
Orders, RMS, brokers, frontend, and audits never “discover” instruments independently.
They consume resolved instruments from this module.

2. Core Responsibilities (What This Module Owns)
Area	Responsibility
Instrument Identity	instrument_key, exchange_token
Contract Lifecycle	listed → active → expiry → delisted
Expiry Logic	Weekly / monthly / BSE fork
Strike Rules	Valid, disabled, frozen
Regulatory Lists	Surveillance, T2T, IPO
Risk Controls	Freeze qty, price band
Broker Mapping	Upstox, future brokers
Caching	Deterministic, versioned
Audit	Why a contract existed or not
3. High-Level Architecture
CSV / Broker APIs
        ↓
Ingestion Layer
        ↓
Instrument Master Tables (DB)
        ↓
InstrumentService
        ↓
Eligibility + RMS
        ↓
Order Validation
        ↓
Broker Adapter


Critical Rule
No downstream system calls broker APIs for discovery during trading hours.

4. Instrument Identity Model (CRITICAL SECTION)
4.1 What is an instrument_key?

An opaque, globally unique identifier used across:

Market data

Orders

RMS

Broker adapters

Example:

NSE_FO|37590

4.2 How is it derived?
Step	Source
Initial Discovery	CSV ingestion / Broker API
Canonicalization	Backend normalization
Persistence	Stored in DB
Consumption	Always read from DB/cache

It is NEVER constructed on the fly during order placement.

5. Data Sources & Ingestion Strategy
5.1 Primary Sources
Source	Purpose
Exchange CSV	Master contracts
Broker API	Delta verification
Regulatory CSV	T2T, surveillance
IPO feeds	Trading eligibility
5.2 Ingestion Pattern

Batch ingestion

Idempotent

Versioned

Pre-market or T-1

Example:

Vite / CLI / Cron → GzipJsonLoader → DB

5.3 Why Not On-Demand Fetch?

Latency risk

Non-deterministic

Broker outages

Audit violations

6. Database as Source of Truth
6.1 What Is Stored
Table	Purpose
instrument_master	Core identity
exchange_series	NSE/BSE rules
fo_contract_lifecycle	Expiry state
price_band	Upper/lower limits
symbol_quantity_caps	Freeze qty
broker_symbol_mapping	Broker tokens
6.2 What Is NOT Stored

Live prices

OHLC

User orders

7. Instrument Resolution Flow
7.1 Resolution Inputs

From frontend / API:

symbol + expiry + strike + option_type

7.2 Resolution Output
ResolvedInstrument {
  instrument_key
  exchange_token
  lot_size
  freeze_qty
  eligibility_flags
}

7.3 Resolution Steps

Validate symbol exists

Validate expiry active

Validate strike enabled

Validate series allowed

Resolve broker mapping

Cache result

8. Expiry Logic (Formalized)
8.1 NSE
Type	Rule
Weekly	Every Thursday
Monthly	Last Thursday
8.2 BSE (Forked Logic)
Type	Rule
Weekly	Exchange-published calendar
Monthly	May differ from NSE

Documentation Rule
NSE expiry logic must never be reused for BSE.

9. Strike Rules & Disablement
9.1 Strike Eligibility

Exists in master

Not disabled

Not expired

Not regulatory-blocked

9.2 Admin Disablement

Persisted

Versioned

Audited

RMS-enforced

10. RMS Integration
10.1 Pre-Trade Checks
Check	Source
Product eligibility	Instrument flags
Freeze quantity	QuantityCap
Price band	PriceBand
Client limits	ClientRiskState
10.2 Rejection Codes
Code	Meaning
RMS-101	Strike disabled
RMS-102	Expired contract
RMS-103	Quantity cap
RMS-201	Product not allowed
11. Broker Integration (Upstox Example)
11.1 Broker Role

Execution only

No authority on eligibility

No discovery at runtime

11.2 Mapping Table
broker_symbol_mapping
--------------------------------
instrument_key
broker
exchange_token

11.3 Fallback

If broker mapping missing:

Order rejected

Never guessed

12. Caching Strategy
Cache	TTL
Instrument master	Day
Eligibility	5–15 min
Broker mapping	Day
Expiry calendar	Versioned

Cache invalidation occurs on:

Ingestion

Admin action

Contract rollover

13. Frontend Implementation Guide
13.1 Frontend Never Does

Construct instrument_key

Apply expiry logic

Infer strike validity

13.2 Frontend Always Does

Fetch resolved instruments

Render selectable contracts

Send instrument_key only

13.3 API Usage
GET /instruments?symbol=NIFTY&expiry=2025-01-30


Response is final and authoritative.

14. CLI & curl Usage
CLI
tradectl instruments list --symbol NIFTY --expiry 2025-01-30

curl
curl /instruments?symbol=NIFTY

15. Testing Strategy (Document This Separately)
Layer	Test
Entity	JPA tests
Resolution	Unit tests
RMS	Negative paths
Broker	WireMock
Ingestion	Replay CSV
Audit	Determinism
16. Audit & Compliance Narrative
Key Guarantees

No runtime contract creation

Deterministic expiry

Immutable admin actions

Broker-agnostic identity

Reproducible trade decisions

17. How to Generate the Final Documentation Set
Recommended Structure
/docs
 ├── overview.md
 ├── architecture.md
 ├── instrument-identity.md
 ├── ingestion.md
 ├── expiry-rules.md
 ├── rms-integration.md
 ├── broker-mapping.md
 ├── frontend-guide.md
 ├── api-reference.md
 ├── testing.md
 ├── audit-response.md

Diagram Types to Include

ER diagram

Sequence diagram (order validation)

Ingestion flow

Broker routing

RMS rejection paths

18. Final Statement (Documentation Conclusion)

The Instruments Module guarantees that every trade decision is explainable, reproducible, exchange-compliant, and regulator-defensible.