# RMS API Reference

## ProductEligibility

```java
public record ProductEligibility(
    boolean misAllowed,
    boolean mtfAllowed,
    boolean cncAllowed,
    String reason
)
```

### Factory Methods

| Method | Returns |
|--------|---------|
| `normal()` | All products allowed |
| `cncOnly(reason)` | CNC only, MIS/MTF blocked |
| `blocked(reason)` | All products blocked |

---

## EligibilityResolver

### resolve(instrumentKey)

Returns `ProductEligibility` for instrument.

**Decision Flow:**
1. Instrument exists? → No → blocked("NOT_FOUND")
2. On PCA watchlist? → Yes → cncOnly("PCA")
3. T2T series? → Yes → cncOnly("T2T")
4. IPO day-0? → Yes → cncOnly("IPO_DAY0")
5. SME security type? → Yes → cncOnly("SME")
6. Otherwise → normal()

---

## RmsValidationService

### validate(instrumentKey, product, qty, price)

Returns `RmsValidationResult`.

**Checks:**
1. Product eligibility
2. Price within band
3. Quantity within cap
4. Value within cap
5. F&O not expired

---

## ClientRiskEvaluator

### validate(limit, state, orderValue, projectedGross, projectedNet, projectedPositions)

Throws `RiskRejectException` on breach.

**Rejection Codes:**

| Code | Reason |
|------|--------|
| CLIENT_DISABLED | Kill-switch active |
| ORDER_VALUE_LIMIT | Single order too large |
| GROSS_EXPOSURE_LIMIT | Total exposure exceeded |
| NET_EXPOSURE_LIMIT | Directional exposure exceeded |
| TURNOVER_LIMIT | Intraday turnover exceeded |
| POSITION_COUNT_LIMIT | Too many positions |
| MAX_LOSS_HIT | Intraday loss limit hit |

---

## MultiBrokerEngine

### routeOrder(brokerId, request)

Routes order to specified broker adapter.

### getAggregatedPositions()

Returns combined positions from all brokers.

### getAggregatedPnl()

Returns total P&L across all brokers.

---

## OrderRequest

```java
public record OrderRequest(
    String instrumentKey,
    String brokerSymbol,
    String product,      // CNC, MIS, MTF
    String orderType,    // MARKET, LIMIT, SL
    String transactionType, // BUY, SELL
    int qty,
    double price,
    double triggerPrice,
    String validity,     // DAY, IOC
    int disclosedQty
)
```

### Factory Methods

```java
OrderRequest.marketBuy(instrumentKey, symbol, product, qty)
OrderRequest.marketSell(instrumentKey, symbol, product, qty)
OrderRequest.limit(instrumentKey, symbol, product, side, qty, price)
```
