# Option Chain Module

The Option Chain module provides both REST-based snapshots and WebSocket-based streaming of option chains with Greeks and Valuation.

## 1. Option Chain Service (REST)
**Class**: `com.vegatrader.upstox.api.optionchain.service.OptionChainService`

Responsible for fetching snapshots and managing caching.

- **Caching**: Implements an in-memory `ConcurrentHashMap` cache with TTL (1 hour).
- **Audit**: Logs every fetch request (CACHE_HIT vs API_FETCH).
- **Fallback**: Returns stale cache data if the Upstox API is down.
- **Endpoint**: Fetches from `https://api.upstox.com/v2/option/chain`.

## 2. Option Chain Streamer (WebSocket)
**Class**: `com.vegatrader.upstox.api.optionchain.stream.OptionChainStreamManager`

Manages real-time "Authoritative Streams" for option chains.

- **Stream LifeCycle**: Creates streams on demand (`getOrCreateStream`) based on Underlying + Expiry.
- **Valuation Enrichment**: Integrates with `OptionChainValuationService` to calculate Greeks (Delta, Theta, Gamma, Vega) and theoretical prices before publishing updates.
- **Cleanup**: Automatically removes streams for expired dates.
- **Transport**: Supports both Text and Binary (Protobuf) WebSocket transport modes.

## Data Model
**DTO**: `OptionChainResponse`
Contains the full chain structure:
- `expiry`
- `spotPrice`
- List of `OptionChainStrike` (Call/Put data, OI, LTP, Greeks).
