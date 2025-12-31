Below is a testing-first guide you should follow before writing any documentation.
This ensures your Instruments module documentation is grounded in verified behavior, not assumptions.

This guide is written as an engineering validation checklist + execution manual.

Instruments Module – Testing Guide (Pre-Documentation)
Objective

Validate that the Instruments domain is functionally correct, deterministic, and production-safe before documenting flows, APIs, and guarantees.

Rule: If it is not tested, it must not be documented as guaranteed behavior.

1. Testing Strategy Overview
Layer	Purpose	Mandatory
Unit Tests	Entity logic, validators, resolvers	✅
Repository Tests	SQL correctness, indexing	✅
Loader Tests	Gzip + daily ingestion correctness	✅
Cache Tests	Eligibility & real-time mutation	✅
RMS Validation Tests	MIS / MTF / CNC enforcement	✅
WebSocket Tests	Live instrument updates	✅
Integration Tests	End-to-end instrument → order path	✅
Regression Tests	Series / PCA / IPO rule drift	✅
2. Test Environment Setup
2.1 Database Mode

Use SQLite in-memory for all tests.

spring.datasource.url=jdbc:sqlite::memory:
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=none

2.2 Migrations

Run Flyway migrations V10–V26 on test startup.

Test Assertion

No migration fails

All indexes exist

All NOT NULL constraints enforced

3. Entity & Schema Tests
3.1 Instrument Master Integrity

Test cases:

Unique (exchange, symbol)

Regex validation for:

instrument_key

symbol

expired_instrument_key

Fail conditions

Duplicate instrument insertion

Invalid regex accepted

3.2 Exchange Series Rules

Test:

NSE series (EQ, BE, BZ, SM, etc.)

BSE group (A, B, T, Z, X, XT)

Assertions:

Trade-for-Trade series flagged

SME / IPO mapped correctly

4. Loader & Ingestion Tests
4.1 GzipJsonLoader

Test:

.json.gz loading

Partial corruption recovery

Idempotency (re-run safe)

Assertions:

Duplicate rows not inserted

Old rows replaced or versioned

Loader fails fast on schema mismatch

4.2 Daily Lists Ingestion

Test ingestion for:

PCA

ASM / GSM

IPO Calendar

Trade-for-Trade symbols

Assertions:

Effective dates honored

Expired rules removed automatically

Eligibility cache refreshed

5. Eligibility Cache Tests
5.1 Cache Correctness

Test:

Cache hit/miss

TTL expiry

Manual invalidation

Assertions:

No DB hit after warm-up

Deterministic results under load

5.2 Dynamic Updates

Simulate:

PCA added intraday

IPO restriction toggled

Series change

Assertions:

Real-time updates reflected without restart

Orders blocked immediately

6. RMS Validation Tests
6.1 Product Type Enforcement

Test matrix:

Product	Expected
MIS	Intraday only
MTF	Leverage + carry
CNC	Delivery only

Assertions:

IPO day-0 blocks MIS

PCA blocks MTF

Trade-for-Trade blocks MIS

6.2 Intraday Margin Tests

Test:

Margin % by series

Symbol overrides

Assertions:

Rejections at RMS layer

Error reason preserved

6.3 Quantity Caps

Test:

Symbol-level caps

Client-level caps

Assertions:

Hard reject on breach

No partial fills allowed

7. Price Band & Volatility Tests

Test:

Upper/lower circuit enforcement

Dynamic price band updates

Assertions:

Order rejected outside band

Band updates applied mid-session

8. FO Contract Lifecycle Tests

Test:

Active → Near expiry → Expired

Rollover eligibility

Assertions:

Expired contracts blocked

Rollover allowed only when configured

9. WebSocket Subscription Tests
9.1 Instrument Update Feed

Simulate:

New symbol added

Series change

Instrument expired

Assertions:

InstrumentService updated

Cache invalidated

RMS rules re-evaluated

9.2 Fault Tolerance

Test:

WebSocket disconnect

Replay from snapshot

Assertions:

No missed updates

Idempotent replay

10. Integration Tests (Critical)
10.1 Order Path Validation

Flow:

Instrument → Eligibility → RMS → BrokerAdapter


Assertions:

Instrument rejection happens before broker call

Correct rejection reason propagated

10.2 Multi-Broker Resolution

Test:

Broker-specific symbol mapping

Instrument key translation

Assertions:

No ambiguity

Correct broker symbol used

11. CLI & REST Testing
11.1 CLI Tests

Commands:

Load instruments

Refresh eligibility

Validate symbol

Assertions:

Exit codes meaningful

Logs structured

11.2 REST API Tests

Test:

Autocomplete

Symbol lookup

Eligibility check

Assertions:

Low latency (<50ms cached)

Stable ordering

Prefix search correctness

12. Load & Stress Tests

Test:

1M+ instruments

10K queries/sec autocomplete

Assertions:

Cache hit rate > 99%

No GC pressure spikes

13. Logging & Observability Tests

Verify logs for:

Loader failures

RMS rejections

Cache invalidations

Assertions:

Correlation IDs present

No sensitive data leaked

14. Regression Test Checklist

Run after:

Exchange rule updates

NSE/BSE circular changes

Broker adapter upgrades

Mandatory re-tests:

IPO day-0

Trade-for-Trade

PCA enforcement

15. Documentation Readiness Gate

You may only start documentation after:

✅ All tests green

✅ Edge cases validated

✅ Rejection reasons standardized

✅ Performance benchmarks recorded