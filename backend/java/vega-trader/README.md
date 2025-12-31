# Upstox API Integration (VEGA Trader Backend)

## Overview

This module provides a production-ready Java integration for the Upstox API (v2 and v3) as part of the VEGA Trader Backend. It is designed for low-latency trading systems and emphasizes correctness, resilience, and maintainability.

The implementation follows enterprise patterns including:
- Strict DTO modeling
- Centralized rate limiting
- Defensive parsing
- Thread-safe caches
- Structured error handling
- WebSocket streaming support

## Key Features

### Trading & Orders
- Place, modify, cancel orders
- Support for market, limit, SL, and SLM orders
- GTT (Good Till Triggered) orders
- Multi-order workflows

### Portfolio & Risk
- Holdings and positions tracking
- Realized and unrealized P&L
- Margin-aware data handling

### Market Data
- Live quotes and OHLC
- Candles and historical data
- Option chains and Greeks
- WebSocket market data feeds

### Sectoral Analytics
- All 21 NSE sectoral indices
- Sector constituents and ranking utilities

### Infrastructure
- Auto-categorizing rate limiter
- Thread-safe singleton managers
- Clear error taxonomy
- Utility calculators (pricing, P&L, strategies)

## Package Structure

```
com.vegatrader.upstox.api
├── request        # Request DTOs
├── response       # Response DTOs
├── ratelimit      # Rate limiting framework
├── sectoral       # NSE sectoral indices & data
├── errors         # Exception hierarchy
├── utils          # Constants and helpers
├── websocket      # Market & portfolio streams
└── examples       # Usage samples
```

## Quick Start

### 1. Place an Order
```java
PlaceOrderRequest order = PlaceOrderRequest.builder()
        .instrumentKey("NSE_EQ|INE528G01035")
        .quantity(1)
        .product("D")
        .validity("DAY")
        .transactionType("BUY")
        .asMarketOrder()
        .build();
```

### 2. Rate Limit Enforcement
```java
RateLimitManager manager = RateLimitManager.getInstance();

if (manager.checkLimit(OrderEndpoints.PLACE_ORDER).isAllowed()) {
    // Execute API call
    manager.recordRequest(OrderEndpoints.PLACE_ORDER);
}
```

### 3. Sectoral Data
```java
SectorDataFetcher fetcher = new SectorDataFetcher();
List<SectorConstituent> niftyBank = fetcher.getTopConstituents(
        SectoralIndex.BANK, 10);
```

## Thread Safety Guarantees
- Rate limiters are fully thread-safe
- Caches use concurrent collections
- DTOs are immutable or request-scoped
- WebSocket handlers isolate state per stream

## Error Handling Philosophy
Errors are:
- Explicitly typed
- Mapped to API error codes
- Non-silent (no swallowed exceptions)
- Logged with structured context

## Java Version & Build
- **Java**: 21
- **Build Tool**: Maven

```bash
mvn clean install
```
