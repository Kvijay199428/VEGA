# Upstox Java SDK Documentation

Welcome to the detailed documentation for the Upstox Java SDK. This SDK provides a comprehensive interface to interact with the Upstox API, including Order Management, Market Data Streaming, and Authentication.

## Documentation Modules

### 1. [Authentication Module](auth-module.md)
   - Handles OAuth2 flow and API Key authentication.
   - **Key Classes**: `OAuth`, `OAuthFlow`, `ApiKeyAuth`.

### 2. [Feeder Module (Market Data)](feeder-module.md)
   - Real-time WebSocket streaming for Market Data (V3).
   - **Key Classes**: `MarketDataFeederV3`, `MarketDataStreamerV3`, `MarketUpdateV3`.

### 3. [API Clients](api-clients.md)
   - REST API wrappers for all Upstox endpoints.
   - **Key Classes**: `OrderApi`, `LoginApi`, `HistoryApi`, `PortfolioApi`.

### 4. [Proto & RPC](proto-rpc.md)
   - Protobuf definitions for efficient data transport.
   - **Key Classes**: `MarketDataFeedV3` (Protobuf wrapper).

### 5. [Data Models](models.md)
   - Request and Response Data Transfer Objects (DTOs).
   - Covers all 120+ models used in the API.

### 6. [Manifest](manifest.md)
   - Minimal Android Manifest for build compatibility.

## Project Structure
- `com.upstox.api`: request/response models.
- `com.upstox.auth`: authentication schemes.
- `com.upstox.feeder`: websocket streaming logic.
- `io.swagger.client.api`: REST API clients.
