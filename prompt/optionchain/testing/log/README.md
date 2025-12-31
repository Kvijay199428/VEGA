# Option Chain Live Testing - README

## Log Directory: `prompt/optionchain/testing/log/`

This directory contains live test execution logs for SEBI compliance evidence.

## Log Files

| File | Description |
|------|-------------|
| `api-fetch.log` | All API fetch requests with latency |
| `cache-hit.log` | Cache hit events |
| `fallback.log` | Fallback activations |
| `rate-limit.log` | Rate limit events |
| `token-rotation.log` | Token rotation events |

## Running Live Tests

```bash
# From backend/java/vega-trader directory
mvn test -Dtest="OptionChainLiveTest" -DLIVE_TEST=true
```

## Test Cases Covered

- TC-OC-001: Basic Live Fetch (NIFTY, BANKNIFTY)
- TC-OC-010: Cache Hit Validation
- TC-OC-020: Fallback Test
- TC-OC-040: RMS Integration (disabled strikes)
- TC-OC-050: Expiries Endpoint
- TC-OC-070: Invalid Expiry (negative test)

## Log Format

```
[TIMESTAMP] ACTION: symbol=X, expiry=Y, status=200, latency=50ms
Response: {...}
---
```

## Compliance Requirements

- All logs timestamped in IST
- Response payloads truncated to 500 chars
- Logs append-only (immutable)

---
*Generated: 2025-12-30*
