# Base Architecture & Token Management

This section documents the foundational services that manage Upstox API connectivity and Token distribution.

## 1. Upstox Token Provider
**Class**: `com.vegatrader.service.UpstoxTokenProvider`

An intelligent provider that routes token requests based on the required **Capability**.

### Capabilities
- **CORE_REST**: Standard API calls (Orders, Holdings). Uses `PRIMARY` token.
- **MARKET_DATA_WS**: Real-time feeds. Uses `WEBSOCKET1`, `WEBSOCKET2`, `WEBSOCKET3` in round-robin to spread load.
- **OPTION_CHAIN**: Uses `OPTIONCHAIN1`, `OPTIONCHAIN2`.

### Health Checking
Optionally integrates with `UpstoxTokenHealthChecker` to verify a token is actually live (via a profile call) before returning it, preventing 401 errors in critical paths.

## 2. Token Lease Manager
**Class**: `com.vegatrader.service.TokenLeaseManager`

 Prevents connection stomping by "leasing" tokens.
- **Mechanism**: A client acquires a lease on a token (identified by SHA-256 hash).
- **Exclusivity**: While leased, the token cannot be handed out to another high-sensitivity consumer (configurable).
- **TTL**: Leases auto-expire (default 5 minutes) to preventing locking if a thread crashes.

## 3. Rate Limit Management (Internal)
**Class**: `com.vegatrader.controller.RateLimiterController`

Provides an internal API to query the state of rate limiters (e.g., number of orders placed per second), useful for the Admin Dashboard.
