# Option Chain Module - Architecture Overview

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## 1. Purpose

The Option Chain module provides real-time and batch access to Put/Call option chain data for NSE and BSE indices and stocks. It ensures:

- **SEBI/NSE/BSE Compliance** - All requests logged with audit trails
- **Rate-limit Safety** - Bounded concurrency per token
- **Zero-delay Extraction** - Synchronous logging within request lifecycle
- **Caching** - T-1 expiry pre-fetch and fallback support
- **Multi-interface** - REST API, CLI, and React/Vite frontend

---

## 2. API Integration

### Upstox Endpoint

```
GET https://api.upstox.com/v2/option/chain
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `instrument_key` | string | e.g., `NSE_INDEX|Nifty 50` |
| `expiry_date` | string | Format: `YYYY-MM-DD` |

### Response Structure

```json
{
  "status": "success",
  "data": [
    {
      "expiry": "2025-01-30",
      "pcr": 0.85,
      "strike_price": 24000,
      "underlying_key": "NSE_INDEX|Nifty 50",
      "underlying_spot_price": 24150.25,
      "call_options": {
        "instrument_key": "NSE_FO|...",
        "market_data": { "ltp": 150.5, "oi": 5000000, ... },
        "option_greeks": { "iv": 15.2, "delta": 0.55, ... }
      },
      "put_options": { ... }
    }
  ]
}
```

---

## 3. Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│  OptionChain     │────▶│   Upstox API    │
│  (React/CLI)    │     │    Service       │     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │                        
                               ▼                        
                        ┌──────────────────┐            
                        │  Cache Layer     │            
                        │  (Redis/Memory)  │            
                        └──────────────────┘            
                               │                        
                               ▼                        
                        ┌──────────────────┐            
                        │   Audit Log DB   │            
                        │   (SQLite/PG)    │            
                        └──────────────────┘            
```

### Flow Sequence

1. User requests option chain via API
2. Service checks cache for `(instrument_key, expiry)`
3. **Cache Hit** → Return cached data, log CACHE_HIT
4. **Cache Miss** → Fetch from Upstox, cache response, log API_FETCH
5. Return data to frontend

---

## 4. Token Management

### Dedicated Tokens

| Token | Purpose |
|-------|---------|
| `OPTIONCHAIN1` | Primary option chain fetches |
| `OPTIONCHAIN2` | Fallback/rotation token |

### Rate Limiting

- **Max 5 requests/second** per token
- Token rotation on rate limit exceeded
- Backoff strategy with exponential delay

---

## 5. Caching Strategy

| Data Type | TTL | Invalidation |
|-----------|-----|--------------|
| Option Chain | 60 minutes | On expiry change |
| Expiry List | 24 hours | Daily refresh |
| Historical | Permanent | Never |

### T-1 Pre-warm

Scheduler runs at **6:30 PM IST** to pre-fetch next-day expiry option chains.

---

## 6. Compliance

### SEBI/NSE/BSE Requirements

- ✅ All API requests logged with timestamp
- ✅ Expiry headers preserved in audit
- ✅ Token usage tracked per request
- ✅ Zero-delay logging (synchronous commit)
- ✅ Immutable audit storage (≥1 year retention)

### Audit Log Fields

| Field | Description |
|-------|-------------|
| `instrument_key` | Underlying symbol |
| `expiry_date` | Requested expiry |
| `token_used` | OPTIONCHAIN1/2 |
| `request_payload` | Full request JSON |
| `response_payload` | Full response JSON |
| `status_code` | HTTP status |
| `fetched_at` | Timestamp |

---

## 7. Database Schema

### option_chain

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | PK, auto-increment |
| `instrument_key` | VARCHAR | Upstox key |
| `expiry_date` | DATE | Option expiry |
| `strike_price` | DOUBLE | Strike price |
| `underlying_spot_price` | DOUBLE | Spot price |
| `pcr` | DOUBLE | Put/Call Ratio |
| `call_ltp` | DOUBLE | Call LTP |
| `call_oi` | BIGINT | Call OI |
| `call_iv` | DOUBLE | Call IV |
| `put_ltp` | DOUBLE | Put LTP |
| `put_oi` | BIGINT | Put OI |
| `put_iv` | DOUBLE | Put IV |
| `fetched_at` | TIMESTAMP | Fetch time |

### option_chain_audit

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `instrument_key` | VARCHAR | Underlying |
| `expiry_date` | DATE | Expiry |
| `token_used` | VARCHAR | Token ID |
| `request_payload` | TEXT | JSON |
| `response_payload` | TEXT | JSON |
| `status_code` | INT | HTTP status |
| `fetched_at` | TIMESTAMP | Time |

---

## 8. API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/option-chain` | GET | Fetch option chain |
| `/api/v1/option-chain/expiries` | GET | Get available expiries |
| `/api/v1/option-chain/cache/clear` | POST | Clear cache (admin) |

---

*Document Status: FINALIZED*
