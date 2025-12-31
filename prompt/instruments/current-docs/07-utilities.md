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
