# RMS Control Plane v4.1 - Implementation Plan

## Source Documents
- `b1.md` - Equity security types, exchange series, instrument extensions
- `b2.md` - Series sync, regulatory watchlists, IPO restrictions, eligibility cache
- `b3.md` - Margin rules, quantity caps, price bands, T2T enforcement, FO lifecycle

---

## Summary

Extends the instrument module with institutional-grade RMS (Risk Management System) features:

1. **Equity Security Types** (NORMAL, SME, IPO, PCA, RELIST)
2. **Exchange Series** (NSE: EQ/BE/BZ/SM, BSE: A/B/T/Z/X/XT)
3. **Settlement Rules** (Rolling, T2T, Gross, Surveillance)
4. **Regulatory Watchlists** (Daily PCA/Surveillance sync)
5. **IPO Day-0 Restrictions**
6. **Series-Based Margin Rules**
7. **Symbol Quantity Caps**
8. **Dynamic Price Bands**
9. **FO Contract Lifecycle**
10. **Product Eligibility Cache** (Caffeine-backed)

---

## Database Schema (Flyway V13-V22)

| Migration | Table | Description |
|-----------|-------|-------------|
| V13 | `equity_security_type` | NORMAL, SME, IPO, PCA, RELIST |
| V14 | `exchange_series` | Series + settlement flags |
| V15 | Alter `instrument_master` | Add equity_security_type, exchange_series columns |
| V16 | `exchange_series_source` | Dynamic series sync |
| V17 | `regulatory_watchlist` | PCA/Surveillance tracking |
| V18 | `ipo_calendar` | Listing day restrictions |
| V19 | `intraday_margin_by_series` | Series-based margin % |
| V20 | `symbol_quantity_caps` | Max qty/value caps |
| V21 | `price_band` | Daily price limits |
| V22 | `fo_contract_lifecycle` | Expiry tracking |

---

## New Components

### Entities (6 new)
```
rms/entity/
├── EquitySecurityType.java (Enum)
├── ExchangeSeriesEntity.java
├── RegulatoryWatchlistEntity.java
├── IpoCalendarEntity.java
├── IntradayMarginEntity.java
├── QuantityCapEntity.java
├── PriceBandEntity.java
└── FoContractLifecycleEntity.java
```

### Repositories (7 new)
```
rms/repository/
├── ExchangeSeriesRepository.java
├── RegulatoryWatchlistRepository.java
├── IpoCalendarRepository.java
├── IntradayMarginRepository.java
├── QuantityCapRepository.java
├── PriceBandRepository.java
└── FoContractLifecycleRepository.java
```

### Scheduled Sync Jobs (5 new)
```
rms/sync/
├── ExchangeSeriesSyncJob.java     [6:30 AM]
├── RegulatoryWatchlistJob.java    [7:00 AM]
├── PriceBandIngestionJob.java     [6:45 AM]
├── IpoCalendarSyncJob.java        [5:30 AM]
└── FoExpiryDeactivationJob.java   [5:00 AM]
```

### Eligibility Cache
```
rms/eligibility/
├── ProductEligibility.java (record)
├── EligibilityResolver.java
└── EligibilityCache.java (Caffeine LoadingCache)
```

### Enhanced Risk Engine
```
rms/validation/
├── RmsValidationService.java (enhanced)
├── MarginProfile.java (record)
└── RmsException.java
```

---

## RMS Decision Matrix

| Condition | MIS | MTF | CNC |
|-----------|:---:|:---:|:---:|
| EQ Series EQ | ✅ | ✅ | ✅ |
| BE / T / Z / XT | ❌ | ❌ | ✅ |
| PCA / Surveillance | ❌ | ❌ | ✅ |
| IPO Day-0 | ❌ | ❌ | ✅ |
| SME | ❌ | ❌ | ✅ |
| Expired FO | ❌ | ❌ | ❌ |

---

## Implementation Order

1. **Phase A: Schema** - Flyway migrations V13-V22
2. **Phase B: Entities** - JPA entities for all new tables
3. **Phase C: Repositories** - Spring Data JPA repositories
4. **Phase D: Sync Jobs** - Scheduled ingestion jobs
5. **Phase E: Eligibility** - Cache + Resolver
6. **Phase F: Risk Engine** - Enhanced validation
7. **Phase G: Tests** - Integration tests

---

## Estimated Effort
- **Migrations**: 1 hour
- **Entities/Repos**: 2 hours
- **Sync Jobs**: 2 hours
- **Eligibility Cache**: 1 hour
- **Risk Engine**: 2 hours
- **Tests**: 1 hour

**Total: ~9 hours**

---

*Created: 2025-12-30*
