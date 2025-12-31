# Broker Module

The Broker module acts as the bridge between Vega Trader and external brokerage systems (e.g., Upstox). It implements an Adapter pattern to allow for multi-broker support.

## Core Components

### 1. Broker Adapter
**Interface**: `BrokerAdapter`
**Implementation**: `com.vegatrader.upstox.api.broker.adapter.UpstoxBrokerAdapter`

The adapter handles all direct communication with the broker's API.

**Key Responsibilities**:
- **Order Management**: `placeOrder`, `modifyOrder`, `cancelOrder`.
- **Data Retrieval**: `getPositions`, `getHoldings`, `getOrderStatus`.
- **Connection**: Manages WebSocket and REST session state (`isConnected`, `reconnect`).
- **Market Data**: Subscribes/Unsubscribes to real-time feeds.

### 2. Multi-Broker Resolver
**Service**: `com.vegatrader.upstox.api.broker.service.MultiBrokerResolver`

Allows the system to resolve instrument availability across multiple brokerage accounts.

**Features**:
- **Cross-Broker Resolution**: Finds if an option contract exists on a specific broker.
- **Resolver Registry**: Dynamic registration of broker-specific resolvers.
- **Fallback Logic**: Can query multiple brokers to find the best instrument match.

## Usage
The Order Management System (OMS) uses the `BrokerAdapter` interface, making it agnostic of the underlying broker implementation. The `BrokerPriorityService` (from Admin module) determines which adapter to use for a given order.
