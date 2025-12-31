# Proto & RPC Module

The `com.upstox.marketdatafeederv3udapi` package contains the generated Protobuf classes used for high-efficiency data transport.

## Key Classes

### 1. [MarketDataFeedV3.java](../src/main/java/com/upstox/marketdatafeederv3udapi/rpc/proto/MarketDataFeedV3.java)
**Generated Class**: `protoc`-generated Java wrapper for the Market Data Feed V3 Protocol Buffers.

This massive class (500KB+) defines the binary wire format for market data.
**Internal Messages**:
- `FeedResponse`: The top-level envelope for all messages from Upstox.
- `LTPC`: Compact struct for Last Traded Price/Close.
- `MarketFullFeed`: Detailed struct for full market depth.
- `OptionGreeks`: Struct for Greek values (Delta, Gamma, etc.).

**Usage**:
The `MarketDataStreamerV3` uses this class to parse incoming `ByteBuffer` payloads:
```java
FeedResponse response = FeedResponse.parseFrom(bytes);
```
It then converts these Proto objects into the friendly `MarketUpdateV3` POJOs.
