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
