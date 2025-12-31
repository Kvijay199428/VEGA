# Rate Limiting

The application implements rate limiting to ensure compliance with Upstox API usage policies.

## Configuration
Rate limits are defined in `com.vegatrader.upstox.api.ratelimit.RateLimitConfig` and configured via `application.properties`.

### Standard API Limits
Used for most REST endpoints (Quotes, Funds, etc.)
- **Per Second**: 50 requests
- **Per Minute**: 500 requests
- **Per 30 Minutes**: 2000 requests

### Multi-Order API Limits
Used for order placement and modification.
- **Per Second**: 4 requests
- **Per Minute**: 40 requests
- **Per 30 Minutes**: 160 requests

## Implementation
- **Token Bucket**: Implemented using Guava `RateLimiter` for smooth bursting.
- **Properties**:
  - `api.ratelimiter.standard.rate`
  - `api.ratelimiter.multiorder.rate`

## Handling Rate Limits
When `429 Too Many Requests` is encountered:
1. The application throws a `UpstoxException`.
2. The `BaseErrorHandler` captures it.
3. The client effectively waits or retries with exponential backoff (if configured in the specific service logic).
