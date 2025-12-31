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
