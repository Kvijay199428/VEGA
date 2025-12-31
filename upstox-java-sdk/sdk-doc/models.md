# Data Models (DTOs)

The `com.upstox.api` package contains the Request and Response models used by the API Clients.

## Order Models
- **[PlaceOrderRequest](../src/main/java/com/upstox/api/PlaceOrderRequest.java)**: Parameters for placing an order (qty, product, price, etc.).
- **[OrderData](../src/main/java/com/upstox/api/OrderData.java)**: Comprehensive order details (id, status, timestamps).
- **[OrderBookData](../src/main/java/com/upstox/api/OrderBookData.java)**: Collection of orders for the order book.
- **[TradeData](../src/main/java/com/upstox/api/TradeData.java)**: Execution details (fill price, qty).

## Market Data Models
- **[MarketQuoteOHLC](../src/main/java/com/upstox/api/MarketQuoteOHLC.java)**: Open, High, Low, Close data.
- **[InstrumentData](../src/main/java/com/upstox/api/InstrumentData.java)**: Details about a scrip (lot size, tick size).
- **[Depth](../src/main/java/com/upstox/api/Depth.java)**: Market depth (bids/asks).

## User & Portfolio Models
- **[ProfileData](../src/main/java/com/upstox/api/ProfileData.java)**: User details (email, phone, exchanges).
- **[HoldingsData](../src/main/java/com/upstox/api/HoldingsData.java)**: Long-term holding details.
- **[PositionData](../src/main/java/com/upstox/api/PositionData.java)**: Net and Day positions.
- **[UserFundMarginData](../src/main/java/com/upstox/api/UserFundMarginData.java)**: available funds and margin usage.

## Auth Models
- **[TokenResponse](../src/main/java/com/upstox/api/TokenResponse.java)**: Contains Access Token and User Profile.
- **[TokenRequest](../src/main/java/com/upstox/api/TokenRequest.java)**: Payload for token exchange.

## Other Common Models
- **[Problem](../src/main/java/com/upstox/api/Problem.java)**: Standard error response format.
- **[ApiGatewayErrorResponse](../src/main/java/com/upstox/api/ApiGatewayErrorResponse.java)**: Wrapper for API errors.

> **Note**: This package contains over 120+ generated model classes. Each class serves as a strongly-typed definition for a JSON object in the Upstox API.
