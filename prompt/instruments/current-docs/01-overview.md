# Instrument Module - Complete Documentation

## Overview

The Instrument Module is a comprehensive subsystem within the VEGA TRADER backend that handles all aspects of instrument (securities) management for the Upstox API integration. It provides functionality for loading, filtering, caching, validating, and subscribing to market data for various financial instruments.

---

## Module Architecture

```
instrument/
├── enrollment/
│   └── SubscriptionEligibilityValidator.java
├── filter/
│   ├── InstrumentFilterCriteria.java
│   └── InstrumentFilterService.java
├── provider/
│   ├── InstrumentKeyProvider.java (interface)
│   └── FileBackedInstrumentKeyProvider.java
├── scheduler/
│   └── InstrumentStagingScheduler.java (placeholder)
└── service/
    └── InstrumentEnrollmentService.java
```

### Related Components

```
response/instrument/
├── InstrumentResponse.java
└── ExpiredInstrumentResponse.java

request/instrument/
└── ExpiredInstrumentRequest.java

utils/
├── InstrumentKeyValidator.java
└── InstrumentMasterDownloader.java

websocket/
└── Mode.java (subscription limits)

sectoral/
├── SectoralIndex.java (21 NSE sector indices)
├── SectorConstituent.java
├── SectorDataFetcher.java
└── SectorCache.java
```

---

## Package Summary

| Package | Purpose | Key Classes |
|---------|---------|-------------|
| `instrument.enrollment` | Subscription limit validation | `SubscriptionEligibilityValidator` |
| `instrument.filter` | Instrument filtering with criteria builder | `InstrumentFilterCriteria`, `InstrumentFilterService` |
| `instrument.provider` | Instrument key providers for streaming | `InstrumentKeyProvider`, `FileBackedInstrumentKeyProvider` |
| `instrument.scheduler` | Scheduled instrument staging tasks | `InstrumentStagingScheduler` |
| `instrument.service` | Core enrollment and caching service | `InstrumentEnrollmentService` |
| `response.instrument` | Response DTOs | `InstrumentResponse`, `ExpiredInstrumentResponse` |
| `request.instrument` | Request DTOs | `ExpiredInstrumentRequest` |
| `utils` | Utility classes | `InstrumentKeyValidator`, `InstrumentMasterDownloader` |
| `sectoral` | NSE sectoral index data | `SectoralIndex`, `SectorConstituent`, `SectorDataFetcher`, `SectorCache` |

---

## Key Features

### 1. **Instrument Master Loading**
- Downloads instrument data from Upstox JSON/CSV endpoints
- Supports NSE, BSE, MCX, and other exchanges
- GZIP decompression for efficient data transfer

### 2. **Smart Caching with TTL**
- Multi-tier TTL based on instrument volatility:
  - **Stable (Equity/Index)**: 24-hour TTL
  - **Volatile (F&O)**: 2-hour TTL
- Background refresh to avoid blocking reads
- Stale-while-revalidate pattern for high availability

### 3. **Flexible Filtering**
- Builder pattern for complex filter criteria
- Supports: Segment, Instrument Type, Exchange, Symbol Pattern, Expiry, Option Type

### 4. **Subscription Limit Enforcement**
- Hard guardrails to prevent exceeding Upstox API limits
- Mode-specific limits:
  - LTPC: 5,000 instruments
  - OPTION_GREEKS: 2,000 instruments
  - FULL: 2,000 instruments
  - FULL_D30: 1,000 instruments

### 5. **Provider Abstraction**
- Clean interface for MarketDataStreamerV3 integration
- Decoupled instrument discovery from streaming logic

### 6. **Sectoral Index Data**
- All 21 NSE sectoral indices (Bank, IT, Pharma, Auto, etc.)
- CSV parsing from NSE public endpoints
- Thread-safe caching with 24-hour TTL
- Integration with instrument enrollment for sector-based subscriptions

---

## Quick Start

### Loading Instruments

```java
@Autowired
private InstrumentEnrollmentService enrollmentService;

// Load NSE instruments
List<InstrumentResponse> nseInstruments = enrollmentService.loadNSEInstruments();

// Load BSE instruments
List<InstrumentResponse> bseInstruments = enrollmentService.loadBSEInstruments();
```

### Filtering Instruments

```java
@Autowired
private InstrumentFilterService filterService;

// Build filter criteria
InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
    .segment("NSE_FO")
    .instrumentType("OPTION")
    .tradingSymbolPattern("NIFTY")
    .expiryDate("2025-01-02")
    .limit(100)
    .build();

// Apply filter
List<InstrumentResponse> filtered = filterService.filter(instruments, criteria);
```

### Enrolling for Subscription

```java
// Enroll configured default instruments (Nifty 50, Bank Nifty, Reliance)
Set<String> keys = enrollmentService.enrollConfiguredInstruments();

// Enroll specific instruments with validation
Set<String> requestedKeys = Set.of("NSE_EQ|INE528G01035", "NSE_INDEX|Nifty 50");
Set<String> validatedKeys = enrollmentService.enroll(requestedKeys, Mode.LTPC);
```

---

## Documentation Index

| Part | Document | Description |
|------|----------|-------------|
| 1 | [01-overview.md](./01-overview.md) | This document - Architecture overview |
| 2 | [02-enrollment-submodule.md](./02-enrollment-submodule.md) | Enrollment & subscription validation |
| 3 | [03-filter-submodule.md](./03-filter-submodule.md) | Filtering service & criteria builder |
| 4 | [04-provider-submodule.md](./04-provider-submodule.md) | Provider interface & implementations |
| 5 | [05-service-submodule.md](./05-service-submodule.md) | Core enrollment service |
| 6 | [06-dtos-and-models.md](./06-dtos-and-models.md) | Request/Response DTOs |
| 7 | [07-utilities.md](./07-utilities.md) | Utility classes |
| 8 | [08-subscription-modes.md](./08-subscription-modes.md) | WebSocket subscription modes |
| 9 | [09-sectoral-module.md](./09-sectoral-module.md) | NSE sectoral indices & data |

---

## Version History

| Version | Since | Changes |
|---------|-------|---------|
| 2.0.0 | - | Initial DTOs and utilities |
| 3.0.0 | - | Filter service and criteria builder |
| 3.1.0 | - | Enrollment service, provider abstraction, subscription validation |

---

*Last Updated: 2025-12-29*
