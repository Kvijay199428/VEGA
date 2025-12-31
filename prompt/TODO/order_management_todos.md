# Order Management TODOs

This document details the pending tasks related to order routing, execution, and broker integration.

## 1. UpstoxBrokerAdapter.java

**Location**: `com.vegatrader.upstox.api.order.broker.UpstoxBrokerAdapter`

### Core Integration
-   **Line 14**: `// TODO: Integrate with actual Upstox SDK/API...`
    -   **Context**: Class header.
    -   **Task**: Finalize the connection to the official Upstox Java SDK or REST API endpoints.
-   **Line 49**: `// TODO: Call Upstox API`
    -   **Context**: `placeOrder` method.
    -   **Task**: Implement `upstoxClient.placeOrder(...)`. Map internal `OrderRequest` to Upstox specific request object.
-   **Line 125**: `// TODO: Call Upstox API`
    -   **Context**: `modifyOrder` method.
    -   **Task**: Implement `upstoxClient.modifyOrder(...)`.
-   **Line 147**: `// TODO: Call Upstox API`
    -   **Context**: `cancelOrder` method.
    -   **Task**: Implement `upstoxClient.cancelOrder(...)`.

### Data Retrieval requests
-   **Line 191**: `// TODO: Call Upstox API`
    -   **Context**: `getOrderStatus` method.
    -   **Task**: Fetch real-time status for a specific order ID.
-   **Line 199**: `// TODO: Call Upstox API - GET /v2/order/retrieve-all`
    -   **Context**: `getOrderBook` method.
    -   **Task**: Retrieve all orders for the day.
-   **Line 207**: `// TODO: Call Upstox API - GET /v2/order/trades/get-trades-for-day`
    -   **Context**: `getTradesForDay` method.
    -   **Task**: Retrieve trade book.
-   **Line 215**: `// TODO: Call Upstox API - GET /v2/order/trades?order_id=X`
    -   **Context**: `getOrderTrades` method.
    -   **Task**: Retrieve trades associated with a specific order.

### Position Management
-   **Line 226**: `// TODO: Call Upstox API - POST /v2/order/positions/exit`
    -   **Context**: `exitAllPositions` method.
    -   **Task**: Implement the "Exit All" functionality specifically for Upstox.

### System Health
-   **Line 234**: `// TODO: Health check to Upstox API`
    -   **Context**: `isAvailable` method.
    -   **Task**: Implement a lightweight ping or status check to verify connectivity.

## 2. BrokerRouter.java

**Location**: `com.vegatrader.upstox.api.order.broker.BrokerRouter`

### Routing Logic
-   **Line 59**: `// 2. User default broker (TODO: integrate with UserSettings)`
    -   **Context**: Determining which broker to use for an order.
    -   **Task**: Fetch user preference from `SettingsResolver` if no broker is specified in the request.
-   **Line 66**: `// 3. Strategy assigned broker (TODO: integrate with strategy config)`
    -   **Context**: Strategy-based routing.
    -   **Task**: Check if the strategy (e.g., "NIFTY_STRADDLE") dictates a specific broker.
-   **Line 137**: `// TODO: Integrate with UserSettingsService`
    -   **Context**: Helper method for default broker resolution.
    -   **Task**: Actual service call.
-   **Line 143**: `// TODO: Integrate with StrategyConfigService`
    -   **Context**: Helper method for strategy resolution.
    -   **Task**: Actual service call.

## 3. RiskEngine.java

**Location**: `com.vegatrader.upstox.api.order.risk.RiskEngine`

### Position Tracking
-   **Line 136**: `// TODO: Integrate with position tracking`
    -   **Context**: Pre-trade risk checks.
    -   **Task**: Verify if the new order exceeds position limits (e.g., max qty per instrument) by querying the current live positions.
