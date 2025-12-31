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
