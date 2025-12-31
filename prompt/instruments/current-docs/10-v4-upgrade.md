# Instrument Module - v4.0 Upgrade Documentation

## Overview

The Instrument Module v4.0 upgrade introduces database-backed persistence, structured validation, overlay-aware search, and risk engine integration based on the Upstox API specifications.

---

## New Architecture (v4.0)

```
instrument/
├── controller/
│   └── InstrumentController.java          [NEW - REST API]
├── entity/
│   ├── InstrumentMasterEntity.java        [NEW - JPA]
│   ├── InstrumentMisEntity.java           [NEW]
│   ├── InstrumentMtfEntity.java           [NEW]
│   ├── InstrumentSuspensionEntity.java    [NEW]
│   └── ProductRiskProfileEntity.java      [NEW]
├── loader/
│   ├── InstrumentFileSource.java          [NEW - URLs]
│   ├── InstrumentLoaderService.java       [NEW - Streaming]
│   └── DailyRefreshScheduler.java         [NEW - 6 AM IST]
├── provider/
│   ├── InstrumentKeyProvider.java         [Interface]
│   ├── FileBackedInstrumentKeyProvider.java
│   └── DatabaseBackedInstrumentKeyProvider.java  [NEW]
├── repository/
│   ├── InstrumentMasterRepository.java    [NEW - JPA]
│   ├── InstrumentMisRepository.java       [NEW]
│   ├── InstrumentMtfRepository.java       [NEW]
│   ├── InstrumentSuspensionRepository.java [NEW]
│   └── ProductRiskProfileRepository.java  [NEW]
├── risk/
│   ├── ProductType.java                   [NEW - CNC/MIS/MTF]
│   └── RiskValidationService.java         [NEW]
├── search/
│   ├── InstrumentSearchService.java       [NEW]
│   └── InstrumentAutocompleteService.java [NEW - Cached]
└── validation/
    ├── InstrumentKeyPattern.java          [NEW - Regex]
    ├── ValidInstrumentKey.java            [NEW - Bean Validation]
    └── ValidExchange.java                 [NEW - Bean Validation]
```

---

## Database Schema

### Flyway Migrations

| Migration | Tables |
|-----------|--------|
| V10 | `instrument_master` with indexes |
| V11 | `instrument_mis`, `instrument_mtf`, `instrument_suspension` |
| V12 | `product_risk_profile`, `sectoral_constituent` |

---

## Key Features

### 1. Streaming JSON Loader

```java
// Batch inserts with Jackson streaming
loaderService.loadBodInstruments(InstrumentFileSource.NSE);
loaderService.loadMisOverlay(InstrumentFileSource.NSE_MIS);
loaderService.loadMtfOverlay(InstrumentFileSource.MTF);
loaderService.loadSuspensionOverlay(InstrumentFileSource.SUSPENDED);
```

### 2. Strict Key Validation

```java
// Single key
InstrumentKeyPattern.isValidSingleKey("NSE_EQ|INE002A01018");

// Multiple keys
InstrumentKeyPattern.isValidMultiKey("NSE_EQ|REL,NSE_EQ|TCS");

// Expired key
InstrumentKeyPattern.isValidExpiredKey("NSE_FO|RELIANCE|27-06-2024");
```

### 3. Risk Validation

```java
// Validate order against risk rules
RiskValidationResult result = riskService.validate(
    "NSE_EQ|INE002A01018",
    ProductType.MIS,
    100,
    2500.0
);

if (result.isApproved()) {
    System.out.println("Margin required: " + result.getRequiredMargin());
} else {
    System.out.println("Rejected: " + result.getReason());
}
```

### 4. Autocomplete with Caching

```java
// Cached autocomplete (Caffeine/Redis)
List<AutocompleteResult> results = autocompleteService.autocomplete("REL");
```

---

## REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/instruments/resolve` | Symbol → instrument_key |
| GET | `/api/v1/instruments/autocomplete` | Cached autocomplete |
| GET | `/api/v1/instruments/search` | Full search with overlays |
| GET | `/api/v1/instruments/{key}` | Get by key with flags |
| GET | `/api/v1/instruments/expiries` | Expiry dates for underlying |
| GET | `/api/v1/instruments/options-chain` | Options chain |
| POST | `/api/v1/instruments/load` | Manual load trigger |
| POST | `/api/v1/instruments/refresh` | Daily refresh trigger |

---

## Configuration

```properties
# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Instrument Refresh
instrument.refresh.enabled=true
instrument.refresh.auto-start=false
```

---

## Testing

Integration tests available in:
`src/test/java/com/vegatrader/upstox/api/instrument/InstrumentModuleIntegrationTest.java`

**Test Coverage:**
- Pattern validation (single, multi, expired keys)
- Repository operations
- Overlay checks (MIS, MTF, Suspension)
- Search and autocomplete
- Risk validation

---

*Version: 4.0.0 | Updated: 2025-12-30*
