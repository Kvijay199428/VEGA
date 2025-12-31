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
