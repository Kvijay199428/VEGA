# API Clients

The `io.swagger.client.api` package contains thread-safe clients for accessing Upstox REST Endpoints. Each client corresponds to a functional domain of the Upstox API.

## Core Clients

### 1. [OrderApi.java](../src/main/java/io/swagger/client/api/OrderApi.java)
Manages the lifecycle of orders.
- **Methods**:
  - `placeOrder`: Submit new orders.
  - `cancelOrder`: Cancel open orders.
  - `modifyOrder`: Update open orders.
  - `getOrderBook`: Retrieve all orders for the day.
  - `getTradeHistory`: Retrieve executed trades.
  - `exitPositions`: Emergency exit for positions.

### 2. [LoginApi.java](../src/main/java/io/swagger/client/api/LoginApi.java)
Handles the OAuth2 login flow.
- **Methods**:
  - `authorize`: Initiates the authorization dialog.
  - `token`: Exchanges Auth Code for Access Token.
  - `logout`: Invalidates the current session.

### 3. [PortfolioApi.java](../src/main/java/io/swagger/client/api/PortfolioApi.java)
Access user's holdings and positions.
- **Methods**:
  - `getHoldings`: Long-term investment holdings.
  - `getPositions`: Intraday and F&O positions.
  - `convertPosition`: Convert between Intraday and Delivery.

### 4. [MarketQuoteApi.java](../src/main/java/io/swagger/client/api/MarketQuoteApi.java)
Retrieves snapshot market data (Quotes, OHLC).
- **Methods**:
  - `getLtp`: Last Traded Price.
  - `getMarketQuoteOHLC`: Daily/Intraday OHLC.
  - `getOptionChain`: Full option chain for an instrument.

### 5. [WebsocketApi.java](../src/main/java/io/swagger/client/api/WebsocketApi.java)
Manages WebSocket authentication.
- **Methods**:
  - `getMarketDataFeedAuthorizeV3`: Generates the authorized URL for V3 Feed.
  - `getPortfolioStreamFeedAuthorize`: Generates the authorized URL for Portfolio Stream.

## Other Clients
- `ChargeApi`: Brokerage and tax calculations.
- `HistoryApi`: Historical candle data.
- `UserApi`: User profile and funds.
