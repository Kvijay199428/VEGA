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
# Enrollment Submodule

## Package: `com.vegatrader.upstox.api.instrument.enrollment`

The enrollment submodule handles subscription eligibility validation, ensuring that instrument subscription requests do not exceed Upstox API limits.

---

## Classes

### SubscriptionEligibilityValidator

**Path**: `instrument/enrollment/SubscriptionEligibilityValidator.java`  
**Since**: 3.1.0

A hard guardrail validator that prevents subscription requests from exceeding Upstox API limits.

#### Purpose

- Validates subscription counts against mode-specific limits
- Prevents WebSocket rejection at runtime due to limit violations
- Provides both exception-throwing and safe validation methods

#### Subscription Limits by Mode

| Mode | Limit | Description |
|------|-------|-------------|
| `LTPC` | 5,000 | Last Traded Price/Quantity/Time + Close Price + Volume |
| `OPTION_GREEKS` | 2,000 | LTPC + Option Greeks (Delta, Gamma, Vega, Theta, IV) |
| `FULL` | 2,000 | LTPC + 5-depth bid/ask + OHLC + metadata |
| `FULL_D30` | 1,000 | LTPC + 30-depth bid/ask + OHLC + metadata |

---

## API Reference

### validate(int count, Mode mode)

Validates that the subscription count does not exceed the mode limit.

```java
public void validate(int count, Mode mode)
```

**Parameters:**
- `count` - Number of instruments to subscribe
- `mode` - Subscription mode with individual limit

**Throws:**
- `IllegalStateException` - If count exceeds limit

**Example:**
```java
SubscriptionEligibilityValidator validator = new SubscriptionEligibilityValidator();

// This will pass
validator.validate(1000, Mode.LTPC);

// This will throw IllegalStateException
validator.validate(6000, Mode.LTPC); // Exceeds 5000 limit
```

---

### isValid(int count, Mode mode)

Checks if subscription is valid without throwing an exception.

```java
public boolean isValid(int count, Mode mode)
```

**Parameters:**
- `count` - Number of instruments
- `mode` - Subscription mode

**Returns:**
- `true` if valid, `false` if exceeds limit

**Example:**
```java
SubscriptionEligibilityValidator validator = new SubscriptionEligibilityValidator();

if (validator.isValid(instrumentKeys.size(), Mode.FULL)) {
    // Proceed with subscription
    streamer.subscribe(instrumentKeys, Mode.FULL);
} else {
    // Handle limit exceeded
    log.warn("Subscription limit exceeded, reducing set");
}
```

---

## Integration with InstrumentEnrollmentService

The `InstrumentEnrollmentService` uses `SubscriptionEligibilityValidator` internally:

```java
// In InstrumentEnrollmentService
public Set<String> enroll(Set<String> requestedKeys, Mode mode) {
    validator.validate(requestedKeys.size(), mode);
    logger.info("Enrolled {} instruments for mode {}", requestedKeys.size(), mode);
    return requestedKeys;
}
```

---

## Best Practices

1. **Always validate before subscribing** - Call `isValid()` or `validate()` before making subscription requests
2. **Handle limit exceeded gracefully** - Implement fallback logic to reduce subscription set
3. **Consider mode selection** - Choose appropriate mode based on required data and instrument count
4. **Batch subscriptions** - If you need more instruments than a mode allows, consider using multiple connections

---

## Error Handling

When validation fails, the error message includes:
- The mode that was exceeded
- The attempted count
- The maximum allowed limit

```
Subscription limit exceeded for mode FULL_D30: 1500 > 1000
```

---

*Part 2 of 8 - [Back to Overview](./01-overview.md)*
# Filter Submodule

## Package: `com.vegatrader.upstox.api.instrument.filter`

The filter submodule provides a flexible filtering system for instruments using the builder pattern.

---

## Classes

### InstrumentFilterCriteria

**Path**: `instrument/filter/InstrumentFilterCriteria.java`  
**Since**: 3.0.0

A criteria class for filtering instruments with support for multiple filter dimensions.

#### Filter Dimensions

| Filter | Type | Description | Example |
|--------|------|-------------|---------|
| `segments` | `List<String>` | Exchange segments | `NSE_EQ`, `NSE_FO`, `BSE_EQ` |
| `instrumentTypes` | `List<String>` | Instrument types | `EQ`, `OPTION`, `FUTURE`, `INDEX` |
| `exchanges` | `List<String>` | Exchanges | `NSE`, `BSE`, `MCX` |
| `tradingSymbolPattern` | `String` | Partial match on symbol | `RELIANCE`, `NIFTY` |
| `namePattern` | `String` | Partial match on name | `BANK` |
| `optionTypes` | `List<String>` | Option types | `CE`, `PE` |
| `expiryDate` | `String` | Exact expiry match | `2025-01-02` |
| `excludeExpired` | `boolean` | Exclude expired instruments | Default: `true` |
| `limit` | `Integer` | Max results | `100` |

---

## Builder Pattern

### Creating Filter Criteria

```java
InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
    .segment("NSE_EQ", "BSE_EQ")           // Multiple segments
    .instrumentType("EQ")                   // Equity only
    .exchange("NSE")                        // NSE exchange
    .tradingSymbolPattern("RELIANCE")       // Partial symbol match
    .excludeExpired(true)                   // Exclude expired
    .limit(10)                              // Max 10 results
    .build();
```

### Builder Methods

| Method | Parameters | Description |
|--------|------------|-------------|
| `segment(String...)` | Varargs segments | Filter by segments |
| `instrumentType(String...)` | Varargs types | Filter by instrument types |
| `exchange(String...)` | Varargs exchanges | Filter by exchanges |
| `tradingSymbolPattern(String)` | Pattern | Partial match on trading symbol |
| `namePattern(String)` | Pattern | Partial match on name |
| `optionType(String...)` | Varargs types | Filter by option types (CE/PE) |
| `expiryDate(String)` | Date string | Exact expiry date match |
| `excludeExpired(boolean)` | Flag | Exclude expired instruments |
| `limit(int)` | Count | Maximum results |
| `build()` | - | Build the criteria object |

---

## InstrumentFilterService

**Path**: `instrument/filter/InstrumentFilterService.java`  
**Since**: 3.0.0  
**Annotation**: `@Component`

A Spring-managed service for filtering instruments based on criteria.

### Methods

#### filter(List<InstrumentResponse>, InstrumentFilterCriteria)

Filters instruments based on criteria.

```java
public List<InstrumentResponse> filter(
    List<InstrumentResponse> instruments, 
    InstrumentFilterCriteria criteria
)
```

**Returns:** Filtered list of instruments

---

#### extractInstrumentKeys(List<InstrumentResponse>, InstrumentFilterCriteria)

Filters and extracts instrument keys.

```java
public List<String> extractInstrumentKeys(
    List<InstrumentResponse> instruments, 
    InstrumentFilterCriteria criteria
)
```

**Returns:** List of instrument keys (e.g., `"NSE_EQ|INE002A01018"`)

---

#### count(List<InstrumentResponse>, InstrumentFilterCriteria)

Counts instruments matching criteria.

```java
public long count(List<InstrumentResponse> instruments, InstrumentFilterCriteria criteria)
```

**Returns:** Count of matching instruments

---

### Pre-built Filter Methods

#### filterRelianceEquity

Quick filter for Reliance Industries equity.

```java
public List<InstrumentResponse> filterRelianceEquity(List<InstrumentResponse> instruments)
```

**Implementation:**
```java
InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
    .segment("NSE_EQ")
    .instrumentType("EQ")
    .tradingSymbolPattern("RELIANCE")
    .build();
```

---

#### filterNiftyOptions

Quick filter for Nifty options with specific expiry.

```java
public List<InstrumentResponse> filterNiftyOptions(
    List<InstrumentResponse> instruments, 
    String expiry
)
```

**Implementation:**
```java
InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
    .segment("NSE_FO")
    .instrumentType("OPTION")
    .tradingSymbolPattern("NIFTY")
    .expiryDate(expiry)
    .build();
```

---

## Usage Examples

### Example 1: Filter Bank Nifty Call Options

```java
@Autowired
private InstrumentFilterService filterService;

@Autowired
private InstrumentEnrollmentService enrollmentService;

public List<InstrumentResponse> getBankNiftyCalls(String expiry) {
    List<InstrumentResponse> allInstruments = enrollmentService.loadNSEInstruments();
    
    InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
        .segment("NSE_FO")
        .instrumentType("OPTION")
        .tradingSymbolPattern("BANKNIFTY")
        .optionType("CE")
        .expiryDate(expiry)
        .build();
    
    return filterService.filter(allInstruments, criteria);
}
```

### Example 2: Get Top 50 NSE Equities

```java
public List<String> getTop50NSEEquityKeys() {
    List<InstrumentResponse> instruments = enrollmentService.loadNSEInstruments();
    
    InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
        .segment("NSE_EQ")
        .instrumentType("EQ")
        .limit(50)
        .build();
    
    return filterService.extractInstrumentKeys(instruments, criteria);
}
```

### Example 3: Count Active Futures

```java
public long countActiveFutures() {
    List<InstrumentResponse> instruments = enrollmentService.loadNSEInstruments();
    
    InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
        .segment("NSE_FO")
        .instrumentType("FUTURE")
        .excludeExpired(true)
        .build();
    
    return filterService.count(instruments, criteria);
}
```

---

## Filter Matching Logic

The `matches()` method in `InstrumentFilterCriteria` uses AND logic:

1. If segments list is non-empty, instrument segment must match one
2. If instrumentTypes list is non-empty, instrument type must match one
3. If exchanges list is non-empty, instrument exchange must match one
4. If tradingSymbolPattern is set, symbol must contain pattern (case-insensitive)
5. If namePattern is set, name must contain pattern (case-insensitive)
6. If optionTypes list is non-empty, option type must match one
7. If expiryDate is set, expiry must match exactly
8. If excludeExpired is true, expired instruments are excluded

---

*Part 3 of 8 - [Back to Overview](./01-overview.md)*
# Provider Submodule

## Package: `com.vegatrader.upstox.api.instrument.provider`

The provider submodule provides an abstraction layer between instrument management and the MarketDataStreamerV3.

---

## Classes

### InstrumentKeyProvider (Interface)

**Path**: `instrument/provider/InstrumentKeyProvider.java`  
**Since**: 3.1.0

The mandatory contract that `MarketDataStreamerV3` depends on for obtaining subscription-ready instrument keys.

#### Design Philosophy

The interface enforces clean separation of concerns:

**MarketDataStreamerV3 SHOULD:**
- Depend only on `InstrumentKeyProvider`
- Focus on WebSocket streaming logic
- Trust that keys are already validated

**MarketDataStreamerV3 should NEVER:**
- Access instrument files directly
- Perform filtering logic
- Discover instruments
- Validate subscription limits

---

#### Interface Definition

```java
public interface InstrumentKeyProvider {
    
    /**
     * Returns subscription-ready instrument keys.
     * 
     * @return immutable set of instrument keys in format "EXCHANGE|SYMBOL"
     *         (e.g., "NSE_INDEX|Nifty 50", "NSE_FO|45450")
     */
    Set<String> getInstrumentKeys();
}
```

---

#### Contract Requirements

Implementations must ensure returned keys are:

| Requirement | Description |
|-------------|-------------|
| **Validated** | Keys conform to `EXCHANGE|IDENTIFIER` format |
| **De-duplicated** | No duplicate keys in the set |
| **Limit-safe** | Below subscription limits for the intended mode |
| **Ready** | Can be immediately used for WebSocket subscription |

---

### FileBackedInstrumentKeyProvider

**Path**: `instrument/provider/FileBackedInstrumentKeyProvider.java`  
**Since**: 3.1.0

File-backed implementation of `InstrumentKeyProvider` that delegates to `InstrumentEnrollmentService`.

#### Constructor

```java
public FileBackedInstrumentKeyProvider(InstrumentEnrollmentService enrollmentService)
```

**Parameters:**
- `enrollmentService` - The enrollment service that manages instrument data

---

#### Implementation

```java
@Override
public Set<String> getInstrumentKeys() {
    Set<String> keys = enrollmentService.enrollConfiguredInstruments();
    logger.debug("Providing {} instrument keys", keys.size());
    return keys;
}
```

---

## Usage with MarketDataStreamerV3

### Configuration Example

```java
@Configuration
public class StreamerConfig {
    
    @Bean
    public InstrumentKeyProvider instrumentKeyProvider(
            InstrumentEnrollmentService enrollmentService) {
        return new FileBackedInstrumentKeyProvider(enrollmentService);
    }
    
    @Bean
    public MarketDataStreamerV3 marketDataStreamer(
            InstrumentKeyProvider keyProvider,
            // ... other dependencies
    ) {
        MarketDataStreamerV3 streamer = new MarketDataStreamerV3();
        
        // Get keys from provider
        Set<String> instrumentKeys = keyProvider.getInstrumentKeys();
        
        // Subscribe
        streamer.subscribe(instrumentKeys, Mode.LTPC);
        
        return streamer;
    }
}
```

---

## Bounded Context Benefits

### Before (Tight Coupling)

```java
// BAD: MarketDataStreamerV3 directly accessing files and filtering
class MarketDataStreamerV3 {
    public void start() {
        // Direct file access - violation!
        List<Instrument> instruments = loadFromFile("NSE.json.gz");
        
        // Filtering logic - violation!
        List<String> keys = instruments.stream()
            .filter(i -> i.getSegment().equals("NSE_EQ"))
            .map(Instrument::getKey)
            .collect(toList());
        
        subscribe(keys);
    }
}
```

### After (Clean Separation)

```java
// GOOD: MarketDataStreamerV3 depends only on provider interface
class MarketDataStreamerV3 {
    private final InstrumentKeyProvider keyProvider;
    
    public void start() {
        // Clean delegation
        Set<String> keys = keyProvider.getInstrumentKeys();
        subscribe(keys);
    }
}
```

---

## Creating Custom Providers

You can implement custom providers for different use cases:

### Database-Backed Provider

```java
public class DatabaseInstrumentKeyProvider implements InstrumentKeyProvider {
    
    private final InstrumentRepository repository;
    
    @Override
    public Set<String> getInstrumentKeys() {
        return repository.findActiveInstrumentKeys()
            .stream()
            .collect(Collectors.toSet());
    }
}
```

### Static Configuration Provider

```java
public class StaticInstrumentKeyProvider implements InstrumentKeyProvider {
    
    private final Set<String> keys = Set.of(
        "NSE_INDEX|Nifty 50",
        "NSE_INDEX|Nifty Bank",
        "NSE_EQ|INE002A01018"  // Reliance
    );
    
    @Override
    public Set<String> getInstrumentKeys() {
        return keys;
    }
}
```

### User-Selection Provider

```java
public class UserSelectionKeyProvider implements InstrumentKeyProvider {
    
    private final WatchlistService watchlistService;
    private final String userId;
    
    @Override
    public Set<String> getInstrumentKeys() {
        return watchlistService.getUserWatchlist(userId)
            .stream()
            .map(WatchlistItem::getInstrumentKey)
            .collect(Collectors.toSet());
    }
}
```

---

*Part 4 of 8 - [Back to Overview](./01-overview.md)*
# Service Submodule

## Package: `com.vegatrader.upstox.api.instrument.service`

The service submodule contains the core `InstrumentEnrollmentService` for loading, caching, and enrolling instruments.

---

## Classes

### InstrumentEnrollmentService

**Path**: `instrument/service/InstrumentEnrollmentService.java`  
**Since**: 3.0.0  
**Annotation**: `@Service`

The central service for managing instrument master data with intelligent caching and enrollment capabilities.

---

## Data Sources

### Default URLs

| Constant | URL | Description |
|----------|-----|-------------|
| `DEFAULT_NSE_URL` | `https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz` | NSE instruments |
| `DEFAULT_BSE_URL` | `https://assets.upstox.com/market-quote/instruments/exchange/BSE.json.gz` | BSE instruments |

---

## TTL (Time-To-Live) Configuration

The service implements multi-tier caching based on instrument volatility:

| Tier | TTL | Applies To |
|------|-----|------------|
| **Stable** | 24 hours | Equity, Index |
| **Volatile** | 2 hours | F&O (Futures & Options) |

```java
private static final long TTL_STABLE_MS = 24 * 60 * 60 * 1000;   // 24 hours
private static final long TTL_VOLATILE_MS = 2 * 60 * 60 * 1000;  // 2 hours
```

---

## API Reference

### Loading Methods

#### loadNSEInstruments()

Loads instruments from the default NSE source.

```java
public List<InstrumentResponse> loadNSEInstruments()
```

**Returns:** List of NSE instruments

---

#### loadBSEInstruments()

Loads instruments from the default BSE source.

```java
public List<InstrumentResponse> loadBSEInstruments()
```

**Returns:** List of BSE instruments

---

#### loadInstruments(String url, String exchange)

Loads instruments from a custom URL with TTL logic and background refresh.

```java
public List<InstrumentResponse> loadInstruments(String url, String exchange)
```

**Parameters:**
- `url` - The URL to load from
- `exchange` - Exchange name for caching key

**Returns:** List of instruments

**Behavior:**
1. If cache entry exists and is valid → return cached data
2. If cache entry exists but expired → trigger background refresh, return stale data
3. If no cache entry (cold start) → block and load synchronously

---

### Enrollment Methods

#### enrollInstruments(InstrumentFilterCriteria criteria)

Enrolls instruments from multiple exchanges based on filter criteria.

```java
public List<String> enrollInstruments(InstrumentFilterCriteria criteria)
```

**Returns:** List of instrument keys matching criteria

---

#### enrollBySegmentAndType(String segment, String instrumentType)

Enrolls instruments for a specific segment and type.

```java
public List<String> enrollBySegmentAndType(String segment, String instrumentType)
```

**Example:**
```java
List<String> nseEquities = enrollmentService.enrollBySegmentAndType("NSE_EQ", "EQ");
```

---

#### enrollByPattern(String segment, String instrumentType, String symbolPattern, int limit)

Enrolls instruments by trading symbol pattern with limit.

```java
public List<String> enrollByPattern(
    String segment, 
    String instrumentType,
    String symbolPattern, 
    int limit
)
```

**Example:**
```java
List<String> tataScrips = enrollmentService.enrollByPattern(
    "NSE_EQ", "EQ", "TATA", 10
);
```

---

### Quick Enrollment Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `enrollRelianceEquity()` | Reliance equity instrument | 1 key |
| `enrollNifty50()` | Nifty 50 index | 1 key |
| `enrollBankNifty()` | Bank Nifty index | 1 key |
| `enrollConfiguredInstruments()` | Default set (Nifty 50 + Bank Nifty + Reliance) | 3 keys |

---

#### enroll(Set<String> requestedKeys, Mode mode)

Enrolls requested instruments with mode validation.

```java
public Set<String> enroll(Set<String> requestedKeys, Mode mode)
```

**Parameters:**
- `requestedKeys` - Instrument keys to enroll
- `mode` - Subscription mode with limit

**Throws:** `IllegalStateException` if count exceeds mode limit

---

### Lookup Methods

#### getInstrumentDetails(String instrumentKey)

Gets instrument details by key.

```java
public InstrumentResponse getInstrumentDetails(String instrumentKey)
```

**Returns:** Instrument details or `null` if not found

---

### Cache Management

#### clearCache()

Clears all cached instruments.

```java
public void clearCache()
```

---

#### getCacheStats()

Gets cache statistics.

```java
public Map<String, Object> getCacheStats()
```

**Returns:** Map containing:
- `cached_exchanges` - Set of cached exchange keys
- `total_cached_instruments` - Total count across all exchanges
- `expiry_status` - Map of exchange → expired status

---

## Caching Architecture

### CacheEntry (Internal Class)

```java
private static class CacheEntry {
    private final List<InstrumentResponse> instruments;
    private final Instant expiresAt;
    
    public CacheEntry(List<InstrumentResponse> instruments, long ttlMs) {
        this.instruments = Collections.unmodifiableList(instruments);
        this.expiresAt = Instant.now().plusMillis(ttlMs);
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
```

### Background Refresh

When cache expires, the service:
1. Returns stale data immediately (non-blocking)
2. Triggers asynchronous refresh in background
3. Updates cache when refresh completes

```java
private void triggerBackgroundRefresh(String url, String exchange) {
    refreshScheduler.submit(() -> {
        List<InstrumentResponse> instruments = downloadAndParseInstruments(url);
        long ttl = getTTLForExchange(exchange);
        cache.put(exchange, new CacheEntry(instruments, ttl));
    });
}
```

---

## Usage Examples

### Example 1: Load and Filter

```java
@Autowired
private InstrumentEnrollmentService enrollmentService;

public void processNiftyOptions() {
    // Load all NSE instruments
    List<InstrumentResponse> instruments = enrollmentService.loadNSEInstruments();
    
    // Enroll Nifty options
    InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
        .segment("NSE_FO")
        .instrumentType("OPTION")
        .tradingSymbolPattern("NIFTY")
        .expiryDate("2025-01-02")
        .build();
    
    List<String> keys = enrollmentService.enrollInstruments(criteria);
    
    // Subscribe with validation
    Set<String> validatedKeys = enrollmentService.enroll(
        new HashSet<>(keys), Mode.OPTION_GREEKS
    );
}
```

### Example 2: Check Cache Status

```java
public void logCacheStatus() {
    Map<String, Object> stats = enrollmentService.getCacheStats();
    
    logger.info("Cached exchanges: {}", stats.get("cached_exchanges"));
    logger.info("Total instruments: {}", stats.get("total_cached_instruments"));
    
    @SuppressWarnings("unchecked")
    Map<String, Boolean> expiry = (Map<String, Boolean>) stats.get("expiry_status");
    expiry.forEach((exchange, expired) -> 
        logger.info("{} cache expired: {}", exchange, expired)
    );
}
```

---

## Scheduler Submodule

### InstrumentStagingScheduler

**Path**: `instrument/scheduler/InstrumentStagingScheduler.java`

> **Note**: This class is currently a placeholder for future scheduled instrument staging tasks.

**Planned Features:**
- Scheduled instrument master refresh
- Pre-market instrument staging
- Expiry date monitoring
- New listing detection

---

*Part 5 of 8 - [Back to Overview](./01-overview.md)*
# DTOs and Models

## Response DTOs

### InstrumentResponse

**Path**: `response/instrument/InstrumentResponse.java`  
**Since**: 2.0.0

The primary DTO for instrument data, used for parsing instrument master CSV/JSON files.

#### Fields

| Field | Type | JSON Key | Description |
|-------|------|----------|-------------|
| `instrumentKey` | `String` | `instrument_key` | Unique identifier (e.g., `NSE_EQ|INE002A01018`) |
| `exchangeToken` | `String` | `exchange_token` | Exchange-specific token |
| `tradingSymbol` | `String` | `trading_symbol` | Trading symbol (e.g., `RELIANCE`) |
| `name` | `String` | `name` | Full name of the instrument |
| `segment` | `String` | `segment` | Market segment (e.g., `NSE_EQ`, `NSE_FO`) |
| `exchange` | `String` | `exchange` | Exchange name (e.g., `NSE`, `BSE`) |
| `isin` | `String` | `isin` | ISIN code |
| `expiry` | `String` | `expiry` | Expiry date for derivatives |
| `strike` | `Double` | `strike` | Strike price for options |
| `lotSize` | `Integer` | `lot_size` | Contract lot size |
| `instrumentType` | `String` | `instrument_type` | Type (e.g., `EQ`, `OPTION`, `FUTURE`) |
| `optionType` | `String` | `option_type` | Option type (`CE` or `PE`) |
| `tickSize` | `Double` | `tick_size` | Minimum price movement |
| `lastPrice` | `Double` | `last_price` | Last traded price |

#### Utility Methods

```java
// Check if instrument is an option
public boolean isOption() {
    return "OPTION".equalsIgnoreCase(instrumentType);
}

// Check if instrument is a future
public boolean isFuture() {
    return "FUTURE".equalsIgnoreCase(instrumentType);
}

// Check if instrument is equity
public boolean isEquity() {
    return "EQUITY".equalsIgnoreCase(instrumentType);
}

// Check if instrument has expired
public boolean isExpired() {
    // Simple non-blocking check (returns false by default)
    return false;
}
```

#### Example Usage

```java
InstrumentResponse instrument = enrollmentService.getInstrumentDetails("NSE_EQ|INE002A01018");

if (instrument != null) {
    System.out.println("Symbol: " + instrument.getTradingSymbol());
    System.out.println("Type: " + instrument.getInstrumentType());
    System.out.println("Is Option: " + instrument.isOption());
}
```

---

### ExpiredInstrumentResponse

**Path**: `response/instrument/ExpiredInstrumentResponse.java`  
**Since**: 2.0.0

DTO for expired instruments data, containing expiry dates and contract details.

#### Fields

| Field | Type | JSON Key | Description |
|-------|------|----------|-------------|
| `expiries` | `List<String>` | `expiries` | List of expiry dates |
| `contracts` | `List<ExpiredContract>` | `contracts` | List of expired contracts |

#### Utility Methods

```java
public int getExpiryCount() {
    return expiries != null ? expiries.size() : 0;
}

public int getContractCount() {
    return contracts != null ? contracts.size() : 0;
}
```

---

### ExpiredContract (Nested Class)

Represents an individual expired contract.

| Field | Type | JSON Key | Description |
|-------|------|----------|-------------|
| `instrumentKey` | `String` | `instrument_key` | Unique identifier |
| `strikePrice` | `Double` | `strike_price` | Strike price |
| `optionType` | `String` | `option_type` | `CE` or `PE` |
| `expiryDate` | `String` | `expiry_date` | Expiry date |
| `lotSize` | `Integer` | `lot_size` | Contract lot size |
| `tradingSymbol` | `String` | `tradingsymbol` | Trading symbol |

#### Utility Methods

```java
public boolean isCallOption() {
    return "CE".equalsIgnoreCase(optionType);
}

public boolean isPutOption() {
    return "PE".equalsIgnoreCase(optionType);
}
```

---

## Request DTOs

### ExpiredInstrumentRequest

**Path**: `request/instrument/ExpiredInstrumentRequest.java`  
**Since**: 2.0.0

Request DTO for querying expired instruments.

#### Fields

| Field | Type | JSON Key | Description |
|-------|------|----------|-------------|
| `underlyingKey` | `String` | `underlying_key` | Underlying instrument key |
| `instrumentType` | `String` | `instrument_type` | `OPTION` or `FUTURE` |
| `expiryDate` | `String` | `expiry_date` | Target expiry date |

#### Builder Pattern

```java
ExpiredInstrumentRequest request = ExpiredInstrumentRequest.builder()
    .underlyingKey("NSE_INDEX|Nifty 50")
    .option()                    // Sets instrumentType to "OPTION"
    .expiryDate("2025-01-02")
    .build();
```

#### Validation

```java
public void validate() {
    if (underlyingKey == null || underlyingKey.isEmpty()) {
        throw new IllegalArgumentException("Underlying key is required");
    }
    if (instrumentType == null || instrumentType.isEmpty()) {
        throw new IllegalArgumentException("Instrument type is required");
    }
    if (!("OPTION".equalsIgnoreCase(instrumentType) || 
          "FUTURE".equalsIgnoreCase(instrumentType))) {
        throw new IllegalArgumentException("Instrument type must be OPTION or FUTURE");
    }
}
```

---

## Segment Values

| Segment | Description | Example Instruments |
|---------|-------------|---------------------|
| `NSE_EQ` | NSE Equity | RELIANCE, TCS, INFY |
| `NSE_FO` | NSE Futures & Options | NIFTY Options, Futures |
| `NSE_INDEX` | NSE Indices | Nifty 50, Bank Nifty |
| `BSE_EQ` | BSE Equity | All BSE listed stocks |
| `BSE_FO` | BSE Futures & Options | BSE derivatives |
| `MCX_FO` | MCX Commodity F&O | Gold, Silver, Crude |

---

## Instrument Type Values

| Type | Description |
|------|-------------|
| `EQ` | Equity shares |
| `OPTION` | Options contracts |
| `FUTURE` | Futures contracts |
| `INDEX` | Index instruments |
| `ETF` | Exchange Traded Funds |
| `COMMODITY` | Commodity instruments |

---

## Option Type Values

| Type | Description |
|------|-------------|
| `CE` | Call European |
| `PE` | Put European |

---

*Part 6 of 8 - [Back to Overview](./01-overview.md)*
# Utility Classes

## Package: `com.vegatrader.upstox.api.utils`

Utility classes for instrument key validation and master file downloading.

---

## InstrumentKeyValidator

**Path**: `utils/InstrumentKeyValidator.java`  
**Since**: 2.0.0

A utility class for validating and manipulating instrument keys.

### Instrument Key Format

```
EXCHANGE|IDENTIFIER
```

**Examples:**
- `NSE_EQ|INE528G01035` - NSE Equity (Reliance)
- `NSE_INDEX|Nifty 50` - Nifty 50 Index
- `NSE_FO|45450` - NSE F&O contract
- `BSE_EQ|500325` - BSE Equity

---

### Validation Methods

#### isValid(String instrumentKey)

Validates instrument key format.

```java
public static boolean isValid(String instrumentKey)
```

**Validation Rules:**
1. Key must not be null or empty
2. Key must contain pipe (`|`) separator
3. Key must split into exactly 2 parts
4. Both exchange and identifier parts must be non-empty

**Returns:** `true` if valid, `false` otherwise

---

#### validate(String instrumentKey)

Validates and throws exception if invalid.

```java
public static void validate(String instrumentKey)
```

**Throws:** `IllegalArgumentException` with message:
```
Invalid instrument key format. Expected: EXCHANGE|IDENTIFIER (e.g., NSE_EQ|INE528G01035)
```

---

### Extraction Methods

#### getExchange(String instrumentKey)

Extracts exchange part from instrument key.

```java
public static String getExchange(String instrumentKey)
```

**Example:**
```java
String exchange = InstrumentKeyValidator.getExchange("NSE_EQ|INE528G01035");
// exchange = "NSE_EQ"
```

---

#### getIdentifier(String instrumentKey)

Extracts identifier part from instrument key.

```java
public static String getIdentifier(String instrumentKey)
```

**Example:**
```java
String identifier = InstrumentKeyValidator.getIdentifier("NSE_EQ|INE528G01035");
// identifier = "INE528G01035"
```

---

### Type Check Methods

| Method | Returns True If |
|--------|-----------------|
| `isNSEEquity(key)` | Key starts with `NSE_EQ|` |
| `isNSEFO(key)` | Key starts with `NSE_FO|` |
| `isIndex(key)` | Key starts with `NSE_INDEX|` |

---

### Build Method

#### build(String exchange, String identifier)

Builds an instrument key from parts.

```java
public static String build(String exchange, String identifier)
```

**Example:**
```java
String key = InstrumentKeyValidator.build("NSE_EQ", "INE528G01035");
// key = "NSE_EQ|INE528G01035"
```

**Throws:** `IllegalArgumentException` if exchange or identifier is null/empty

---

## InstrumentMasterDownloader

**Path**: `utils/InstrumentMasterDownloader.java`  
**Since**: 2.0.0

A utility class for downloading and parsing instrument master files from Upstox.

---

### Constants

```java
public static final String BASE_URL = 
    "https://assets.upstox.com/market-quote/instruments/exchange/";
```

---

### Exchange Enum

Available exchanges for instrument download:

| Exchange | Filename | Description |
|----------|----------|-------------|
| `COMPLETE` | `complete.csv.gz` | All instruments (CSV) |
| `NSE` | `NSE.json.gz` | NSE instruments (JSON) |
| `BSE` | `BSE.json.gz` | BSE instruments (JSON) |
| `MCX` | `MCX.json.gz` | MCX commodities (JSON) |
| `SUSPENDED` | `suspended-instrument.json.gz` | Suspended instruments |
| `MTF` | `MTF.json.gz` | Margin Trading Facility |
| `NSE_MIS` | `NSE_MIS.json.gz` | NSE MIS eligible |
| `BSE_MIS` | `BSE_MIS.json.gz` | BSE MIS eligible |

#### Exchange Methods

```java
// Get download URL
String url = Exchange.NSE.getUrl();
// "https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz"

// Get filename
String filename = Exchange.NSE.getFilename();
// "NSE.json.gz"
```

---

### Download Methods

#### download(Exchange exchange, String outputPath)

Downloads instrument master file (compressed).

```java
public static void download(Exchange exchange, String outputPath) throws IOException
```

**Example:**
```java
InstrumentMasterDownloader.download(
    Exchange.NSE, 
    "data/instruments/NSE.json.gz"
);
```

---

#### downloadAndDecompress(Exchange exchange, String outputPath)

Downloads and decompresses instrument master file.

```java
public static void downloadAndDecompress(Exchange exchange, String outputPath) 
    throws IOException
```

**Example:**
```java
InstrumentMasterDownloader.downloadAndDecompress(
    Exchange.NSE, 
    "data/instruments/NSE.json"
);
```

---

### Parse Method

#### parseCSV(String filePath)

Parses CSV instrument master file.

```java
public static List<InstrumentResponse> parseCSV(String filePath) throws IOException
```

**CSV Format Expected:**
```
exchange_token,trading_symbol,name,segment,expiry,strike,lot_size,instrument_type,...
```

**Note:** This method is for the `complete.csv` file format.

---

### Utility Method

#### getRecommendedPath(Exchange exchange, String baseDir)

Gets recommended file path for saving instrument master.

```java
public static String getRecommendedPath(Exchange exchange, String baseDir)
```

**Example:**
```java
String path = InstrumentMasterDownloader.getRecommendedPath(
    Exchange.NSE, 
    "data/instruments"
);
// path = "data/instruments/NSE.json"
```

---

## Usage Examples

### Example 1: Download and Parse NSE Instruments

```java
// Download NSE instruments
String outputPath = InstrumentMasterDownloader.getRecommendedPath(
    Exchange.NSE, "data/instruments"
);

InstrumentMasterDownloader.downloadAndDecompress(Exchange.NSE, outputPath);

// Parse if CSV format
List<InstrumentResponse> instruments = 
    InstrumentMasterDownloader.parseCSV(outputPath);
```

### Example 2: Validate User Input

```java
public void subscribeToInstrument(String instrumentKey) {
    // Validate format
    if (!InstrumentKeyValidator.isValid(instrumentKey)) {
        throw new IllegalArgumentException("Invalid instrument key: " + instrumentKey);
    }
    
    // Check segment
    if (InstrumentKeyValidator.isNSEFO(instrumentKey)) {
        logger.info("Subscribing to F&O instrument");
    } else if (InstrumentKeyValidator.isNSEEquity(instrumentKey)) {
        logger.info("Subscribing to equity instrument");
    }
    
    // Proceed with subscription
    streamer.subscribe(Set.of(instrumentKey), Mode.LTPC);
}
```

### Example 3: Build Keys Dynamically

```java
public Set<String> buildInstrumentKeys(List<String> identifiers, String exchange) {
    return identifiers.stream()
        .map(id -> InstrumentKeyValidator.build(exchange, id))
        .collect(Collectors.toSet());
}

// Usage
Set<String> keys = buildInstrumentKeys(
    List.of("INE002A01018", "INE009A01021", "INE040A01034"),
    "NSE_EQ"
);
// ["NSE_EQ|INE002A01018", "NSE_EQ|INE009A01021", "NSE_EQ|INE040A01034"]
```

---

*Part 7 of 8 - [Back to Overview](./01-overview.md)*
# Subscription Modes

## Package: `com.vegatrader.upstox.api.websocket`

The subscription modes define data fidelity levels and corresponding limits for Upstox Market Data Feed V3 WebSocket connections.

---

## Mode Enum

**Path**: `websocket/Mode.java`  
**Since**: 3.1.0

Defines subscription modes for the Upstox Market Data Feed V3.

---

## Mode Definitions

### LTPC

**Wire Value**: `ltpc`  
**Subscription Limit**: 5,000 instruments

**Data Included:**
- Last Traded Price (LTP)
- Last Traded Quantity (LTQ)
- Last Traded Time (LTT)
- Close Price
- Total Volume

**Use Case:** High-volume monitoring where only price/volume data is needed.

---

### OPTION_GREEKS

**Wire Value**: `option_greeks`  
**Subscription Limit**: 2,000 instruments

**Data Included:**
- All LTPC data
- Delta
- Gamma
- Vega
- Theta
- Implied Volatility (IV)

**Use Case:** Options trading strategies requiring Greeks calculations.

> **Note:** Only applicable to option instruments.

---

### FULL

**Wire Value**: `full`  
**Subscription Limit**: 2,000 instruments

**Data Included:**
- All LTPC data
- 5-depth bid/ask order book
- OHLC (Open, High, Low, Close)
- Metadata
- Option Greeks (for options)

**Use Case:** Active trading requiring order book depth and full price data.

---

### FULL_D30

**Wire Value**: `full_d30`  
**Subscription Limit**: 1,000 instruments

**Data Included:**
- All LTPC data
- 30-depth bid/ask order book
- OHLC
- Metadata
- Option Greeks (for options)

**Use Case:** High-frequency trading and market making requiring deep order book visibility.

---

## Mode API

### getWireValue()

Gets the wire protocol value for WebSocket messages.

```java
public String getWireValue()
```

**Example:**
```java
Mode mode = Mode.FULL;
String wireValue = mode.getWireValue();
// wireValue = "full"
```

---

### getIndividualLimit()

Gets the maximum subscription limit for this mode.

```java
public int getIndividualLimit()
```

**Example:**
```java
Mode mode = Mode.LTPC;
int limit = mode.getIndividualLimit();
// limit = 5000
```

---

### fromWireValue(String wireValue)

Parses mode from wire value.

```java
public static Mode fromWireValue(String wireValue)
```

**Throws:** `IllegalArgumentException` if invalid wire value

**Example:**
```java
Mode mode = Mode.fromWireValue("option_greeks");
// mode = Mode.OPTION_GREEKS
```

---

## Mode Selection Guide

```
┌──────────────────────────────────────────────────────────────────┐
│                     MODE SELECTION FLOWCHART                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Need Option Greeks?                                              │
│       │                                                           │
│       ├── YES ──► OPTION_GREEKS (2,000 limit)                    │
│       │                                                           │
│       └── NO ──► Need Order Book Depth?                          │
│                       │                                           │
│                       ├── YES ──► Need 30-depth?                 │
│                       │               │                           │
│                       │               ├── YES ──► FULL_D30       │
│                       │               │           (1,000 limit)   │
│                       │               │                           │
│                       │               └── NO ──► FULL            │
│                       │                          (2,000 limit)    │
│                       │                                           │
│                       └── NO ──► LTPC (5,000 limit)              │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Limit Comparison

| Mode | Limit | Data Fidelity | Bandwidth | Use Case |
|------|-------|---------------|-----------|----------|
| `LTPC` | 5,000 | Basic | Low | Watchlists, alerts |
| `OPTION_GREEKS` | 2,000 | Medium | Medium | Options strategies |
| `FULL` | 2,000 | High | High | Active trading |
| `FULL_D30` | 1,000 | Highest | Highest | HFT, market making |

---

## Integration Example

### Selecting Mode Based on Requirements

```java
public Mode selectOptimalMode(Set<String> instrumentKeys, boolean needGreeks, boolean needDepth) {
    int count = instrumentKeys.size();
    
    if (needGreeks) {
        if (count <= Mode.OPTION_GREEKS.getIndividualLimit()) {
            return Mode.OPTION_GREEKS;
        }
        throw new IllegalStateException("Too many instruments for OPTION_GREEKS mode");
    }
    
    if (needDepth) {
        if (count <= Mode.FULL_D30.getIndividualLimit()) {
            return Mode.FULL_D30;
        } else if (count <= Mode.FULL.getIndividualLimit()) {
            return Mode.FULL;
        }
        throw new IllegalStateException("Too many instruments for depth modes");
    }
    
    // Default to LTPC for basic data
    if (count <= Mode.LTPC.getIndividualLimit()) {
        return Mode.LTPC;
    }
    
    throw new IllegalStateException("Exceeds maximum subscription limit");
}
```

### Subscription with Mode

```java
@Autowired
private InstrumentEnrollmentService enrollmentService;

@Autowired
private MarketDataStreamerV3 streamer;

public void subscribeWithMode(InstrumentFilterCriteria criteria, Mode mode) {
    // Enroll instruments
    List<String> keys = enrollmentService.enrollInstruments(criteria);
    Set<String> keySet = new HashSet<>(keys);
    
    // Validate against mode limit
    Set<String> validated = enrollmentService.enroll(keySet, mode);
    
    // Subscribe
    streamer.subscribe(validated, mode);
    
    logger.info("Subscribed {} instruments in {} mode", validated.size(), mode);
}
```

---

## Error Handling

When subscription limit is exceeded:

```java
try {
    enrollmentService.enroll(instruments, Mode.FULL_D30);
} catch (IllegalStateException e) {
    // e.getMessage() = "Subscription limit exceeded for mode FULL_D30: 1500 > 1000"
    
    // Fallback to lower fidelity mode
    Mode fallbackMode = Mode.FULL;  // Try 2000 limit
    enrollmentService.enroll(instruments, fallbackMode);
}
```

---

## Wire Protocol Usage

When sending subscription messages via WebSocket:

```json
{
    "guid": "someguid",
    "method": "sub",
    "data": {
        "mode": "full",
        "instrumentKeys": ["NSE_INDEX|Nifty 50", "NSE_EQ|INE002A01018"]
    }
}
```

The `mode` field uses the wire value from `Mode.getWireValue()`.

---

*Part 8 of 9 - [Back to Overview](./01-overview.md)* 
# Sectoral Module

## Package: `com.vegatrader.upstox.api.sectoral`

The sectoral module provides functionality for fetching, caching, and managing NSE sectoral index data and their constituent stocks.

---

## Overview

This module fetches constituent data for all 21 NSE Sectoral Indices from NSE's public CSV endpoints. It provides:
- **21 predefined sector indices** (Bank, IT, Pharma, Auto, etc.)
- **Thread-safe caching** with configurable TTL
- **CSV parsing** from NSE public endpoints
- **Filtering capabilities** by weight and top-N constituents

---

## Classes

| Class | Description |
|-------|-------------|
| `SectoralIndex` | Enum defining all 21 NSE sectoral indices with CSV URLs |
| `SectorConstituent` | DTO representing a stock in a sectoral index |
| `SectorDataFetcher` | Fetches and parses sector CSV data from NSE |
| `SectorCache` | Thread-safe cache with TTL for sectoral data |

---

## SectoralIndex Enum

**Path**: `sectoral/SectoralIndex.java`  
**Since**: 2.0.0

Defines all 21 NSE Sectoral Indices with their CSV file URLs.

### Sector Groups

#### Banking & Finance
| Enum | Display Name | CSV File |
|------|--------------|----------|
| `BANK` | Nifty Bank Index | `ind_niftybanklist.csv` |
| `FINANCIAL_SERVICES` | Nifty Financial Services | `ind_niftyfinancelist.csv` |
| `FIN_SERVICES_25_50` | Nifty Fin Services 25/50 | `ind_niftyfinancialservices25-50list.csv` |
| `PRIVATE_BANK` | Nifty Private Bank | `ind_nifty_privatebanklist.csv` |
| `PSU_BANK` | Nifty PSU Bank | `ind_niftypsubanklist.csv` |
| `MIDSMALL_FINANCIAL` | Nifty MidSmall Financial | `ind_niftymidsmallfinancailservice_list.csv` |

#### Technology & IT
| Enum | Display Name | CSV File |
|------|--------------|----------|
| `IT` | Nifty IT | `ind_niftyitlist.csv` |
| `MIDSMALL_IT_TELECOM` | Nifty MidSmall IT & Telecom | `ind_niftymidsmallitAndtelecom_list.csv` |

#### Healthcare & Pharma
| Enum | Display Name | CSV File |
|------|--------------|----------|
| `HEALTHCARE` | Nifty Healthcare | `ind_niftyhealthcarelist.csv` |
| `PHARMA` | Nifty Pharma | `ind_niftypharmalist.csv` |
| `NIFTY500_HEALTHCARE` | Nifty500 Healthcare | `ind_nifty500Healthcare_list.csv` |
| `MIDSMALL_HEALTHCARE` | Nifty MidSmall Healthcare | `ind_niftymidsmallhealthcare_list.csv` |

#### Consumer & FMCG
| Enum | Display Name | CSV File |
|------|--------------|----------|
| `FMCG` | Nifty FMCG | `ind_niftyfmcglist.csv` |
| `CONSUMER_DURABLES` | Nifty Consumer Durables | `ind_niftyconsumerdurableslist.csv` |

#### Cyclicals & Commodities
| Enum | Display Name | CSV File |
|------|--------------|----------|
| `AUTO` | Nifty Auto | `ind_niftyautolist.csv` |
| `METAL` | Nifty Metal | `ind_niftymetallist.csv` |
| `OIL_GAS` | Nifty Oil & Gas | `ind_niftyoilgaslist.csv` |
| `REALTY` | Nifty Realty | `ind_niftyrealtylist.csv` |

#### Others
| Enum | Display Name | CSV File |
|------|--------------|----------|
| `CHEMICALS` | Nifty Chemicals | `ind_niftyChemicals_list.csv` |
| `MEDIA` | Nifty Media | `ind_niftymedialist.csv` |
| `ENERGY` | Nifty Energy | `ind_niftyenergylist.csv` |

### API Methods

```java
// Get full download URL
String url = SectoralIndex.BANK.getFullUrl();
// "https://www.niftyindices.com/IndexConstituent/ind_niftybanklist.csv"

// Get display name
String name = SectoralIndex.IT.getDisplayName();
// "Nifty IT"

// Get sector key
String key = SectoralIndex.PHARMA.getSectorKey();
// "nifty_pharma"

// Find by key
SectoralIndex sector = SectoralIndex.fromSectorKey("nifty_bank");

// Get sectors by group
SectoralIndex[] bankSectors = SectoralIndex.getSectorsByGroup("BANKING");
```

---

## SectorConstituent

**Path**: `sectoral/SectorConstituent.java`  
**Since**: 2.0.0

DTO representing a constituent stock in a sectoral index.

### Fields

| Field | Type | JSON Key | Description |
|-------|------|----------|-------------|
| `symbol` | `String` | `symbol` | Stock symbol (e.g., `RELIANCE`) |
| `companyName` | `String` | `company_name` | Full company name |
| `industry` | `String` | `industry` | Industry classification |
| `series` | `String` | `series` | Trading series (e.g., `EQ`) |
| `isinCode` | `String` | `isin_code` | ISIN code |
| `weight` | `Double` | `weight` | Weight in index (percentage) |
| `marketCap` | `Long` | `market_cap` | Market capitalization |
| `instrumentKey` | `String` | `instrument_key` | Upstox instrument key |

### Builder Pattern

```java
SectorConstituent constituent = SectorConstituent.builder()
    .symbol("RELIANCE")
    .companyName("Reliance Industries Limited")
    .industry("Refineries")
    .isinCode("INE002A01018")
    .weight(9.82)
    .build();

// Auto-generates instrument key: "NSE_EQ|INE002A01018"
String key = constituent.getInstrumentKey();
```

### Instrument Key Generation

```java
// Generates Upstox-compatible instrument key
public String generateInstrumentKey() {
    if (isinCode != null && !isinCode.isEmpty()) {
        this.instrumentKey = "NSE_EQ|" + isinCode;
    }
    return instrumentKey;
}
```

---

## SectorDataFetcher

**Path**: `sectoral/SectorDataFetcher.java`  
**Since**: 2.0.0

Service for fetching and parsing NSE sectoral index CSV data.

### Configuration

| Constant | Value | Description |
|----------|-------|-------------|
| `CONNECT_TIMEOUT_SECONDS` | 10 | HTTP connection timeout |
| `READ_TIMEOUT_SECONDS` | 30 | HTTP read timeout |

### Constructors

```java
// Default HTTP client
SectorDataFetcher fetcher = new SectorDataFetcher();

// Custom HTTP client
HttpClient customClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build();
SectorDataFetcher fetcher = new SectorDataFetcher(customClient);
```

### API Methods

#### fetchSectorData(SectoralIndex sector)

Fetches all constituents for a sector.

```java
List<SectorConstituent> bankStocks = fetcher.fetchSectorData(SectoralIndex.BANK);
```

**Throws:** `SectorDataException` if fetching or parsing fails

---

#### getTopConstituents(SectoralIndex sector, int limit)

Gets top N constituents by weight.

```java
List<SectorConstituent> top5Banks = fetcher.getTopConstituents(SectoralIndex.BANK, 5);
```

---

#### getConstituentsByMinWeight(SectoralIndex sector, double minWeight)

Gets constituents with weight >= minWeight.

```java
// Get all constituents with at least 5% weight
List<SectorConstituent> majorStocks = fetcher.getConstituentsByMinWeight(
    SectoralIndex.IT, 5.0
);
```

---

## SectorCache

**Path**: `sectoral/SectorCache.java`  
**Since**: 2.0.0

Thread-safe cache for sectoral index data with TTL (Time-To-Live).

### Configuration

| Constant | Value | Description |
|----------|-------|-------------|
| `DEFAULT_TTL` | 24 hours | Default cache entry lifetime |

### Constructors

```java
// Default 24-hour TTL
SectorCache cache = new SectorCache();

// Custom 2-hour TTL
SectorCache cache = new SectorCache(Duration.ofHours(2));
```

### API Methods

#### get(SectoralIndex sector)

Gets cached data if available and not expired.

```java
List<SectorConstituent> cached = cache.get(SectoralIndex.BANK);
if (cached != null) {
    // Use cached data
}
```

---

#### put(SectoralIndex sector, List<SectorConstituent> data)

Stores data in cache.

```java
List<SectorConstituent> stocks = fetcher.fetchSectorData(SectoralIndex.IT);
cache.put(SectoralIndex.IT, stocks);
```

---

#### getOrFetch(SectoralIndex sector, SectorFetcher fetcher)

Cache-aside pattern: returns cached or fetches if miss.

```java
List<SectorConstituent> data = cache.getOrFetch(
    SectoralIndex.BANK,
    () -> fetcher.fetchSectorData(SectoralIndex.BANK)
);
```

---

#### Cache Management

```java
// Invalidate specific sector
cache.invalidate(SectoralIndex.BANK);

// Clear all
cache.clearAll();

// Get cache size
int size = cache.size();

// Remove expired entries
int removed = cache.cleanupExpired();

// Get statistics
String stats = cache.getStatistics();
// "SectorCache{total=5, expired=1, active=4, ttl=PT24H}"
```

---

## Usage Examples

### Example 1: Fetch Bank Sector Stocks

```java
SectorDataFetcher fetcher = new SectorDataFetcher();
SectorCache cache = new SectorCache();

// Get bank stocks with caching
List<SectorConstituent> bankStocks = cache.getOrFetch(
    SectoralIndex.BANK,
    () -> fetcher.fetchSectorData(SectoralIndex.BANK)
);

// Print top 5 by weight
bankStocks.stream()
    .sorted((a, b) -> Double.compare(b.getWeight(), a.getWeight()))
    .limit(5)
    .forEach(stock -> System.out.printf("%s: %.2f%%\n", 
        stock.getSymbol(), stock.getWeight()));
```

### Example 2: Get Instrument Keys for Sector

```java
SectorDataFetcher fetcher = new SectorDataFetcher();

// Get IT sector stocks
List<SectorConstituent> itStocks = fetcher.fetchSectorData(SectoralIndex.IT);

// Extract instrument keys for Upstox subscription
Set<String> instrumentKeys = itStocks.stream()
    .map(SectorConstituent::generateInstrumentKey)
    .filter(key -> key != null)
    .collect(Collectors.toSet());

// Subscribe to market data
enrollmentService.enroll(instrumentKeys, Mode.LTPC);
```

### Example 3: Scan All Banking Sectors

```java
SectorDataFetcher fetcher = new SectorDataFetcher();

// Get all banking-related sectors
SectoralIndex[] bankSectors = SectoralIndex.getSectorsByGroup("BANKING");

for (SectoralIndex sector : bankSectors) {
    try {
        List<SectorConstituent> stocks = fetcher.fetchSectorData(sector);
        System.out.printf("%s: %d stocks\n", sector.getDisplayName(), stocks.size());
    } catch (SectorDataException e) {
        System.err.println("Failed to fetch: " + sector.getDisplayName());
    }
}
```

### Example 4: Heavy-Weight Stock Screener

```java
SectorDataFetcher fetcher = new SectorDataFetcher();

// Find stocks with >5% weight across all sectors
for (SectoralIndex sector : SectoralIndex.values()) {
    try {
        List<SectorConstituent> heavyWeights = 
            fetcher.getConstituentsByMinWeight(sector, 5.0);
        
        if (!heavyWeights.isEmpty()) {
            System.out.println("\n" + sector.getDisplayName() + ":");
            heavyWeights.forEach(s -> 
                System.out.printf("  %s: %.2f%%\n", s.getSymbol(), s.getWeight()));
        }
    } catch (SectorDataException e) {
        // Skip failed sectors
    }
}
```

---

## Integration with Instrument Module

The sectoral module integrates with the instrument module through instrument keys:

```java
// Fetch sector data
List<SectorConstituent> pharmaStocks = fetcher.fetchSectorData(SectoralIndex.PHARMA);

// Build instrument keys
List<String> keys = pharmaStocks.stream()
    .map(SectorConstituent::generateInstrumentKey)
    .filter(Objects::nonNull)
    .collect(Collectors.toList());

// Validate keys
keys.forEach(InstrumentKeyValidator::validate);

// Enroll for subscription
InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
    .segment("NSE_EQ")
    .instrumentType("EQ")
    .build();

// Filter and subscribe
List<String> validKeys = enrollmentService.enrollInstruments(criteria);
```

---

## Error Handling

### SectorDataException

Custom exception for sector data fetching failures.

```java
try {
    List<SectorConstituent> data = fetcher.fetchSectorData(SectoralIndex.BANK);
} catch (SectorDataFetcher.SectorDataException e) {
    logger.error("Failed to fetch sector data: {}", e.getMessage());
    
    // Try cache fallback
    List<SectorConstituent> cached = cache.get(SectoralIndex.BANK);
    if (cached != null) {
        return cached;  // Return stale data
    }
    
    throw e;  // No fallback available
}
```

---

*Part 9 of 9 - [Back to Overview](./01-overview.md)*
