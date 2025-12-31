# Feeder Module (Market Data Streaming)

The `com.upstox.feeder` package provides a robust WebSocket implementation for consuming real-time market data from Upstox (v3).

## Core Classes

### 1. [MarketDataStreamerV3.java](../src/main/java/com/upstox/feeder/MarketDataStreamerV3.java)
**Extends**: `Streamer`
The high-level client for streaming market data. It handles:
- Connection management (via `MarketDataFeederV3`).
- Protobuf decoding (`FeedResponse` -> `MarketUpdateV3`).
- Listener callbacks.

**Key Methods**:
- `connect()`: Establishes the WebSocket connection.
- `subscribe(Set<String> keys, Mode mode)`: Subscribes to instruments.
- `changeMode(...)`: Switches mode (e.g., LTPC -> FULL).
- `setOnMarketUpdateListener(...)`: Registers callback for incoming data.

### 2. [MarketDataFeederV3.java](../src/main/java/com/upstox/feeder/MarketDataFeederV3.java)
**Extends**: `Feeder`
The low-level socket handler.
- **Responsibilities**:
  - Fetching authorized WebSocket URI.
  - initializing `WebSocketClient`.
  - Sending Subscribe/Unsubscribe frames.
  - Handling Reconnection (via `Streamer` logic).

### 3. [MarketUpdateV3.java](../src/main/java/com/upstox/feeder/MarketUpdateV3.java)
The Data Model for market updates. Converted from Protobuf.
**Structure**:
- `feeds`: Map of Instrument Key -> `Feed` object.
- **Inner Classes**:
  - `LTPC`: Last Traded Price, Time, Quantity, Close.
  - `Quote`: Bid/Ask depth (Price and Qty).
  - `OptionGreeks`: Delta, Theta, Gamma, Vega, Rho, IV.
  - `MarketFullFeed`: comprehensive view including OI, VTT, ATP.

## Listeners (`com.upstox.feeder.listener`)
Event callbacks for the streamer.
- `OnOpenListener`: Connection established.
- `OnMessageListener`: Raw message received (bytes/string).
- `OnErrorListener`: Exception occurred.
- `OnCloseListener`: Connection closed.
- `OnMarketUpdateV3Listener`: Parsed `MarketUpdateV3` received.

## Constants (`com.upstox.feeder.constants`)
- **Mode**: Subscription modes (`LTPC`, `FULL`, `OPTION_GREEKS`).
- **Method**: WebSocket operations (`SUBSCRIBE`, `UNSUBSCRIBE`, `CHANGE_METHOD`).
