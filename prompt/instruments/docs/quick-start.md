# Instruments Module - Quick Start Guide

## Prerequisites

- Java 21+
- Maven 3.9+
- SQLite 3

## Build & Run

```bash
cd backend/java/vega-trader

# Compile
mvn compile

# Run tests
mvn test

# Start application
mvn spring-boot:run
```

## Database Setup

Flyway migrations run automatically on startup. Database location:
```
backend/java/vega-trader/database/vega_trade.db
```

## Key API Endpoints

### Instruments

```bash
# Search
curl http://localhost:28020/api/v1/instruments/search?query=RELIANCE

# Details
curl http://localhost:28020/api/v1/instruments/NSE_EQ%7CINE002A01018
```

### Orders

```bash
# Place order
curl -X POST http://localhost:28020/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "instrumentKey": "NSE_EQ|INE002A01018",
    "qty": 100,
    "price": 2500.0,
    "product": "CNC",
    "side": "BUY"
  }'
```

### Portfolio

```bash
# Get positions
curl http://localhost:28020/api/v1/portfolio/positions

# Get holdings
curl http://localhost:28020/api/v1/portfolio/holdings
```

## RMS Validation Flow

```
Order → Eligibility Check → RMS Validation → Client Risk → Broker
```

## Key Classes

| Class | Purpose |
|-------|---------|
| `EligibilityResolver` | Product eligibility |
| `RmsValidationService` | Pre-trade validation |
| `ClientRiskEvaluator` | Client exposure limits |
| `MultiBrokerEngine` | Order routing |

## Tests

```bash
# Run all RMS tests
mvn test -Dtest="RmsEntityTest,RmsValidationTest,MultiBrokerTest"
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| DB locked | Restart app, check connections |
| Eligibility cache miss | Check TTL, verify instrument exists |
| Order rejected | Check RMS logs for reason code |
