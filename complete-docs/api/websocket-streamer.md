# WebSocket Streamer API

The Vega Trader backend provides robust WebSocket integration for real-time data streaming.

## Overview
Defined in `com.vegatrader.upstox.api.endpoints.WebSocketEndpoints` and implemented by `MarketDataStreamerV3`.

## Authentication
WebSocket connections use OAuth2 access tokens for authentication.
- **Authorization**: The access token is typically passed via the URL query parameter `?access_token=<YOUR_TOKEN>` or Bearer header depending on the specific endpoint implementation guidelines.

## Endpoints

### 1. Market Stream
**Definition**: `MARKET_STREAM`
- **Url**: `wss://api.upstox.com/v2/market/stream`
- **Description**: Provides real-time market data updates (LTP, Depth, Greeks).
- **Format**: Data is streamed in Protocol Buffers (ProtoBuf) format.
- **Buffer**: Tuned with `marketdata.buffer-capacity` (default 50,000 events).
- **Reconnect**: Automatic reconnection with exponential backoff (`marketdata.reconnect-delay-ms`).

### 2. Portfolio Stream
**Definition**: `PORTFOLIO_STREAM`
- **Url**: `wss://api.upstox.com/v2/portfolio/stream`
- **Description**: Real-time updates for orders and positions.
- **Format**: JSON.
- **Events**:
  - `order_update`: Status changes (PENDING -> OPEN -> FILLED).
  - `position_update`: Changes in open positions.
  - `holding_update`: Changes in Demat holdings.

## Implementation Details
The backend uses `MarketDataStreamerV3` for high-performance handling:
- **Disruptor Pattern**: Used for low-latency event processing.
- **Event Bus**: Internal event bus handles `MarketUpdateEvent` distribution.
- **Metrics**: Published via Micrometer/Prometheus (`market_data_events_count`).
- **Logging**: Dedicated log file at `log/ws.log` (configured in `application.properties`).
