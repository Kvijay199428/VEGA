# Upstox API Endpoints Generator

**Version:** 1.0.0  
**Package:** `com.vegatrader.upstox.api`

A comprehensive, type-safe Java library for generating Upstox API URLs with full support for all 60+ Upstox API v2 and HFT v3 endpoints.

---

## ✨ Features

- ✅ **Type-safe endpoint definitions** - Compile-time safety for all API endpoints
- ✅ **Environment switching** - Seamless production ↔ sandbox switching
- ✅ **Multiple API versions** - Support for both v2 Standard and v3 HFT APIs
- ✅ **WebSocket URLs** - Easy generation of WebSocket stream URLs
- ✅ **Option Chain support** - Dedicated builders for option chain queries
- ✅ **Comprehensive error codes** - 60+ error codes with descriptions and resolutions
- ✅ **Fluent API** - Clean, readable URL building syntax
- ✅ **Thread-safe** - Immutable configurations with singleton pattern
- ✅ **Auto URL encoding** - Automatic handling of special characters

---

## 📦 Installation

Add the package to your project. The classes are located in:
```
src/main/java/com/vegatrader/upstox/api/
```

---

## 🚀 Quick Start

### 1. Basic URL Generation

```java
import com.vegatrader.upstox.api.generator.UpstoxEndpointGenerator;
import com.vegatrader.upstox.api.endpoints.UserProfileEndpoints;

// Create generator for production
UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

// Generate URL
String profileUrl = generator.generateUrl(UserProfileEndpoints.USER_PROFILE);
// Result: https://api.upstox.com/v2/user/profile
```

### 2. URL with Query Parameters

```java
import com.vegatrader.upstox.api.endpoints.MarketDataEndpoints;
import java.util.Map;

// Generate quote URL with parameters
Map<String, String> params = Map.of(
    "instrument_key", "NSE_EQ|INE848E01016"
);
String quoteUrl = generator.generateUrl(MarketDataEndpoints.FULL_QUOTE, params);
// Result: https://api.upstox.com/v2/market-quote/quotes?instrument_key=NSE_EQ%7CINE848E01016
```

### 3. URL with Path Parameters

```java
import com.vegatrader.upstox.api.endpoints.OrderEndpoints;

// Generate order details URL
Map<String, String> pathParams = Map.of("order_id", "240127000123456");
String orderUrl = generator.generateUrl(OrderEndpoints.GET_ORDER_DETAILS, pathParams, null);
// Result: https://api.upstox.com/v2/order/240127000123456
```

### 4. Option Chain URL

```java
// Generate option chain URL
String optionChainUrl = generator.generateOptionChainUrl(
    "NSE_INDEX|Nifty 50",
    "2025-02-27"
);
// Result: https://api.upstox.com/v2/option/chain?instrument_key=NSE_INDEX%7CNifty+50&expiry_date=2025-02-27
```

### 5. WebSocket URLs

```java
// Get WebSocket market stream URL
String marketWsUrl = generator.generateMarketStreamUrl();
// Result: wss://api.upstox.com/v2/market/stream

// Get WebSocket portfolio stream URL
String portfolioWsUrl = generator.generatePortfolioStreamUrl();
// Result: wss://api.upstox.com/v2/portfolio/stream
```

---

## 🏗️ Architecture

### Package Structure

```
com.vegatrader.upstox.api/
├── config/                  # Configuration and base URLs
│   ├── UpstoxEnvironment.java
│   ├── UpstoxApiVersion.java
│   ├── UpstoxBaseUrlConfig.java
│   ├── UpstoxBaseUrlFactory.java
│   ├── UpstoxWebSocketConfig.java
│   └── UpstoxOptionChainConfig.java
│
├── endpoints/               # Endpoint definitions
│   ├── UpstoxEndpoint.java (interface)
│   ├── AuthenticationEndpoints.java
│   ├── UserProfileEndpoints.java
│   ├── OrderEndpoints.java
│   ├── PortfolioEndpoints.java
│   ├── MarketDataEndpoints.java
│   ├── OptionChainEndpoints.java
│   ├── WebSocketEndpoints.java
│   └── UpstoxEndpointRegistry.java
│
├── errors/                  # Error handling
│   ├── UpstoxHttpStatus.java
│   └── UpstoxErrorCode.java
│
└── generator/               # URL generation
    ├── UpstoxUrlBuilder.java
    └── UpstoxEndpointGenerator.java
```

### Component Overview

#### 1. **Configuration Layer** (`config/`)
- `UpstoxEnvironment` - PRODUCTION vs SANDBOX
- `UpstoxApiVersion` - V2_STANDARD vs V3_HFT
- `UpstoxBaseUrlConfig` - Immutable configuration holder
- `UpstoxBaseUrlFactory` - Factory with thread-safe caching
- `UpstoxWebSocketConfig` - WebSocket URL utilities
- `UpstoxOptionChainConfig` - Option chain URL builders

#### 2. **Endpoint Definitions** (`endpoints/`)
- `UpstoxEndpoint` - Base interface for all endpoints
- Category-specific enums (7 total) with 49+ endpoints
- `UpstoxEndpointRegistry` - Central registry with filtering and search

#### 3. **Error Management** (`errors/`)
- `UpstoxHttpStatus` - 13 HTTP status codes with descriptions
- `UpstoxErrorCode` - 60+ error codes with resolutions

#### 4. **URL Generation** (`generator/`)
- `UpstoxUrlBuilder` - Fluent API for URL construction
- `UpstoxEndpointGenerator` - Main generator facade

---

## 📚 Usage Examples

### Environment Switching

```java
// Production v2 (default)
UpstoxEndpointGenerator prodGen = UpstoxEndpointGenerator.forProduction();

// Sandbox v2 (for testing)
UpstoxEndpointGenerator sandboxGen = UpstoxEndpointGenerator.forSandbox();

// Production HFT v3
UpstoxEndpointGenerator hftGen = UpstoxEndpointGenerator.forProductionHft();

// Custom configuration
UpstoxEndpointGenerator customGen = UpstoxEndpointGenerator.forEnvironment(
    UpstoxEnvironment.SANDBOX,
    UpstoxApiVersion.V3_HFT
);
```

### Order Management

```java
import com.vegatrader.upstox.api.endpoints.OrderEndpoints;

// Place order
String placeOrderUrl = generator.generateUrl(OrderEndpoints.PLACE_ORDER);

// Get all orders
String allOrdersUrl = generator.generateUrl(OrderEndpoints.GET_ALL_ORDERS);

// Modify order
String modifyUrl = generator.generateUrl(OrderEndpoints.MODIFY_ORDER);

// Cancel order
String cancelUrl = generator.generateUrl(OrderEndpoints.CANCEL_ORDER);

// Get GTT orders
String gttOrdersUrl = generator.generateUrl(OrderEndpoints.GET_GTT_ORDERS);
```

### Market Data

```java
import com.vegatrader.upstox.api.endpoints.MarketDataEndpoints;

// Get full quote
Map<String, String> quoteParams = Map.of(
    "instrument_key", "NSE_EQ|INE848E01016"
);
String quoteUrl = generator.generateUrl(MarketDataEndpoints.FULL_QUOTE, quoteParams);

// Get Option Greeks
Map<String, String> greeksParams = Map.of(
    "instrument_key", "NSE_FO|51059"
);
String greeksUrl = generator.generateUrl(MarketDataEndpoints.OPTION_GREEKS, greeksParams);

// Get historical candlestick data
Map<String, String> histParams = Map.of(
    "instrument_key", "NSE_EQ|INE848E01016",
    "interval", "1day",
    "from_date", "2025-01-01",
    "to_date", "2025 -01-31"
);
String candleUrl = generator.generateUrl(MarketDataEndpoints.CANDLESTICK_DATA, histParams);
```

### Portfolio

```java
import com.vegatrader.upstox.api.endpoints.PortfolioEndpoints;

// Get holdings
String holdingsUrl = generator.generateUrl(PortfolioEndpoints.GET_HOLDINGS);

// Get positions
String positionsUrl = generator.generateUrl(PortfolioEndpoints.GET_POSITIONS);

// Get net positions
String netPositionsUrl = generator.generateUrl(PortfolioEndpoints.GET_NET_POSITIONS);

// Convert position
String convertUrl = generator.generateUrl(PortfolioEndpoints.CONVERT_POSITION);
```

### Using UpstoxUrlBuilder Directly

```java
import com.vegatrader.upstox.api.generator.UpstoxUrlBuilder;

// Build custom URL
String customUrl = UpstoxUrlBuilder.create()
    .baseUrl("https://api.upstox.com/v2")
    .path("/order/place")
    .queryParam("segment", "NSE")
    .queryParam("product", "INTRADAY")
    .build();
```

---

## 🗂️ All Available Endpoints

### Authentication (4 endpoints)
- LOGIN_DIALOG, GET_TOKEN, LOGOUT, RENEW_TOKEN

### User Profile (2 endpoints)
- USER_PROFILE, USER_FUNDS

### Orders (22 endpoints)
- PLACE_ORDER, MODIFY_ORDER, CANCEL_ORDER, GET_ORDER_DETAILS, GET_ALL_ORDERS, GET_TRADES, GET_ORDER_BOOK, GET_TRADE_BOOK
- PLACE_AMO, MODIFY_AMO, CANCEL_AMO, GET_AMO_BOOK, GET_AMO_TRADES
- PLACE_OSL, CANCEL_OSL
- CREATE_GTT, GET_GTT_ORDERS, MODIFY_GTT, CANCEL_GTT
- GET_TRADE_PNL, GET_PNL_BY_SYMBOL, GET_PNL_METADATA

### Portfolio (4 endpoints)
- GET_HOLDINGS, GET_POSITIONS, GET_NET_POSITIONS, CONVERT_POSITION

### Market Data (13 endpoints)
- FULL_QUOTE, OHLC_QUOTE, LTP_QUOTE, OPTION_GREEKS
- CANDLESTICK_DATA, HISTORICAL_OHLC, INDEX_HISTORICAL
- GET_BROKERS, MARKET_STATUS
- GET_INSTRUMENTS, GET_EXPIRED_INSTRUMENTS
- GET_CHARGES, GET_MARGINS

### Option Chain (2 endpoints)
- GET_OPTION_CHAIN, GET_PUT_CALL_CHAIN

### WebSocket (2 endpoints)
- MARKET_STREAM, PORTFOLIO_STREAM

---

## ⚠️ Error Handling

### Accessing Error Codes

```java
import com.vegatrader.upstox.api.errors.UpstoxErrorCode;
import com.vegatrader.upstox.api.errors.UpstoxHttpStatus;

// Get error information
UpstoxErrorCode error = UpstoxErrorCode.INSUFFICIENT_FUNDS;
System.out.println(error.getDescription());  // "Not enough margin/funds"
System.out.println(error.getResolution());   // "Check account balance or reduce order quantity"
System.out.println(error.getHttpStatus());   // UNPROCESSABLE_ENTITY (422)

// Find error by code string
UpstoxErrorCode foundError = UpstoxErrorCode.fromCode("invalid_instrument_key");
```

### Common Errors

- `INSUFFICIENT_FUNDS` - Not enough margin
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `UNAUTHORIZED` - Token expired
- `ORDER_NOT_FOUND` - Order doesn't exist
- `INVALID_INSTRUMENT_KEY` - Invalid instrument format
- `EXCHANGE_NOT_OPEN` - Market closed

---

## 📊 Registry Statistics

```java
import com.vegatrader.upstox.api.endpoints.UpstoxEndpointRegistry;

// Print statistics
UpstoxEndpointRegistry.printStatistics();

// Get specific counts
int totalEndpoints = UpstoxEndpointRegistry.getTotalEndpointCount();
Map<String, Integer> byCategory = UpstoxEndpointRegistry.getEndpointCountByCategory();

// Find endpoints
Optional<UpstoxEndpoint> endpoint = UpstoxEndpointRegistry.findEndpoint(
    "/order/place",
    UpstoxEndpoint.HttpMethod.POST
);
```

---

## 🔒 Thread Safety

All configuration classes use:
- Immutable design patterns
- Thread-safe caching with `ConcurrentHashMap`
- Singleton factory pattern

Safe for concurrent use in multi-threaded environments.

---

## 📝 Best Practices

1. **Reuse Generator Instances**
   ```java
   // Create once, use many times
   private static final UpstoxEndpointGenerator GENERATOR = 
       UpstoxEndpointGenerator.forProduction();
   ```

2. **Use Type-Safe Endpoints**
   ```java
   // Good - compile-time safety
   generator.generateUrl(OrderEndpoints.PLACE_ORDER);
   
   // Avoid - error-prone
   "https://api.upstox.com/v2/order/place"
   ```

3. **Handle URL Encoding Automatically**
   ```java
   // Special characters are automatically encoded
   Map<String, String> params = Map.of(
       "instrument_key", "NSE_INDEX|Nifty 50"  // Pipe and space encoded
   );
   ```

4. **Environment Configuration**
   ```java
   // Use environment variables or configuration files
   String env = System.getenv("UPSTOX_ENV");
   UpstoxEndpointGenerator generator = env.equals("production")
       ? UpstoxEndpointGenerator.forProduction()
       : UpstoxEndpointGenerator.forSandbox();
   ```

---

## 🔗 Links

- **Upstox API Documentation**: https://upstox.com/developer/api-documentation/
- **Authentication Guide**: https://upstox.com/developer/api-documentation/authentication/
- **Error Codes Reference**: https://upstox.com/developer/api-documentation/appendix/

---

## 📄 License

Part of the VEGA TRADER platform.

---

## 🤝 Contributing

For issues or enhancements, please contact the VEGA TRADER development team.
