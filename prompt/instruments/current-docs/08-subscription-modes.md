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

*Part 8 of 8 - [Back to Overview](./01-overview.md)*
