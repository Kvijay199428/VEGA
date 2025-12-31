# 🔗 UPSTOX API BASE URLS & ENDPOINTS

## COMPLETE UPSTOX API INTEGRATION GUIDE

This document lists all Upstox API base URLs and endpoints that your application uses.

---

## UPSTOX API BASE URLS

### **1. OAuth Base URL**
```
https://api.upstox.com
```

**Used For**: Authentication and OAuth token management

---

### **2. REST API Base URL**
```
https://api.upstox.com
```

**Used For**: All REST API calls (market data, orders, portfolio, etc.)

**Prefix**: `/v2/` (Version 2 of Upstox API)

**Full Path Pattern**: `https://api.upstox.com/v2/[endpoint]`

---

### **3. WebSocket Base URL**
```
wss://ws.upstox.com
```

**Used For**: Real-time market data streaming

**Protocol**: WebSocket Secure (WSS)

**No version prefix** (native WebSocket)

---

## DETAILED UPSTOX API ENDPOINTS USED

### **SECTION A: OAUTH & AUTHENTICATION**

#### 1. OAuth Authorization Endpoint
```
GET https://api.upstox.com/authorize
```
**Purpose**: Redirect user to Upstox login page

**Your Endpoint**: `POST /api/v1/auth/login`

**Parameters**:
- `client_id`: Your app's client ID
- `redirect_uri`: https://yourapp.com/auth/callback
- `scope`: profile orders
- `state`: CSRF token
- `response_type`: code

**Example**:
```
https://api.upstox.com/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=https://yourapp.com/auth/callback&scope=profile%20orders&state=RANDOM_STATE&response_type=code
```

---

#### 2. OAuth Token Exchange Endpoint
```
POST https://api.upstox.com/oauth/token
```
**Purpose**: Exchange authorization code for access token

**Your Endpoints**: 
- `GET /api/v1/auth/callback`
- `POST /api/v1/auth/refresh-token`

**Request Body**:
```json
{
  "code": "AUTH_CODE_FROM_REDIRECT",
  "client_id": "YOUR_CLIENT_ID",
  "client_secret": "YOUR_CLIENT_SECRET",
  "redirect_uri": "https://yourapp.com/auth/callback",
  "grant_type": "authorization_code"
}
```

**Response**:
```json
{
  "access_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 82800,
  "refresh_token": "refresh_token_string"
}
```

---

### **SECTION B: USER & ACCOUNT ENDPOINTS**

#### 3. Get User Profile
```
GET https://api.upstox.com/v2/user/profile
```
**Your Endpoint**: `GET /api/v1/user/profile`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "user_id": "USER_ID",
    "name": "User Name",
    "email": "user@example.com",
    "phone": "9876543210",
    "broker": "UPSTOX",
    "exchanges": ["NSE", "BSE", "MCX", "NCDEX"],
    "products": ["MIS", "CNC", "NRML"],
    "order_types": ["REGULAR", "BRACKET", "COVER"],
    "status": "active"
  }
}
```

---

#### 4. Get User Margins
```
GET https://api.upstox.com/v2/user/get-margin
```
**Your Endpoint**: `GET /api/v1/user/margins`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "equity": {
      "used": 150000.00,
      "available": 450000.00,
      "opening_balance": 600000.00,
      "payin": 0.00,
      "span": 50000.00,
      "adhoc": 0.00,
      "notional": 450000.00,
      "fpop": 0.00,
      "cncBuy": 0.00,
      "multiplier": 4.0,
      "exposure": 0.00,
      "optionPremium": 0.00
    },
    "commodity": {
      "used": 0.00,
      "available": 0.00,
      "opening_balance": 0.00
    }
  }
}
```

---

#### 5. Get User Charges
```
GET https://api.upstox.com/v2/user/get-charges
```
**Your Endpoint**: `GET /api/v1/user/charges`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "equity": {
      "brokerage": "0.05%",
      "exchange": "varies",
      "clearinghouse": "varies",
      "stt": "0.1%",
      "gst": "18%",
      "stampDuty": "0.003%"
    },
    "derivatives": {
      "brokerage": "varies",
      "exchange": "varies",
      "clearinghouse": "varies",
      "gst": "18%"
    }
  }
}
```

---

### **SECTION C: MARKET DATA ENDPOINTS**

#### 6. Get All Instruments
```
GET https://api.upstox.com/v2/market/instruments/
```
**Your Endpoint**: `GET /api/v1/instruments/all`

**Query Parameters**:
```
exchange=NSE,BSE,MCX,NCDEX
```

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**: Array of 5000+ instruments
```json
{
  "status": "success",
  "data": [
    {
      "instrument_token": "3045",
      "exchange_token": "999900000",
      "tradingsymbol": "SBIN",
      "name": "State Bank of India",
      "last_price": 542.50,
      "expiry": null,
      "strike": null,
      "lot_size": 1,
      "instrument_type": "EQ",
      "segment": "NSE",
      "exchange": "NSE"
    }
  ]
}
```

---

#### 7. Get Live Quote (Single Instrument)
```
GET https://api.upstox.com/v2/market/quote/
```
**Your Endpoint**: `GET /api/v1/market/quote/{instrument_token}`

**Query Parameters**:
```
mode=FULL
instrumentToken=3045
```

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "instrument_token": "3045",
    "symbol": "SBIN",
    "mode": "FULL",
    "last_price": 542.50,
    "ohlc": {
      "open": 540.00,
      "high": 545.00,
      "low": 539.50,
      "close": 542.50
    },
    "depth": {
      "buy": [...],
      "sell": [...]
    },
    "bid": 542.40,
    "ask": 542.60,
    "bid_quantity": 1000,
    "ask_quantity": 1500,
    "volume": 5000000,
    "iv": 18.5,
    "oi": 0,
    "change": 2.50,
    "change_percent": 0.46,
    "last_trade_time": "2025-12-12T15:30:00Z",
    "timestamp": 1702393800
  }
}
```

---

#### 8. Get Multiple Quotes (Bulk)
```
GET https://api.upstox.com/v2/market/quote/
```
**Your Endpoint**: `POST /api/v1/market/quotes`

**Query Parameters**:
```
mode=FULL
instrumentToken=3045,3059,3062
```

**Note**: Pass multiple tokens separated by commas

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

---

#### 9. Get Historical OHLCV Data
```
GET https://api.upstox.com/v2/market/historical-data/intraday
```
**Your Endpoint**: `GET /api/v1/market/ohlc/{instrument_token}`

**Query Parameters**:
```
instrumentToken=3045
interval=15minute
to_date=2025-12-12
from_date=2025-12-01
```

**Supported Intervals**:
- `1minute`
- `5minute`
- `15minute`
- `30minute`
- `60minute`
- `day`
- `week`
- `month`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "timestamp": 1702393800,
      "open": 540.00,
      "high": 545.00,
      "low": 539.50,
      "close": 542.50,
      "volume": 5000000
    }
  ]
}
```

---

### **SECTION D: ORDERS ENDPOINTS**

#### 10. Place Order
```
POST https://api.upstox.com/v2/order/place
```
**Your Endpoint**: `POST /api/v1/orders/place`

**Headers**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "quantity": 1,
  "price": 542.50,
  "product": "MIS",
  "order_type": "REGULAR",
  "transaction_type": "BUY",
  "instrument_token": "3045",
  "disclosed_quantity": 0,
  "trigger_price": 0,
  "validity": "DAY",
  "validity_days": 1,
  "order_side": "ENTER",
  "user_order_id": "user_12345"
}
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "order_id": "250110000123456",
    "parent_order_id": null,
    "status": "PENDING",
    "order_timestamp": "2025-12-12T15:30:00Z"
  }
}
```

---

#### 11. Retrieve All Orders
```
GET https://api.upstox.com/v2/order/retrieve-all
```
**Your Endpoint**: `GET /api/v1/orders`

**Query Parameters**:
```
status=COMPLETE
```

**Status Values**: PENDING, COMPLETE, CANCELLED, REJECTED, EXPIRED

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "order_id": "250110000123456",
      "parent_order_id": null,
      "user_order_id": "user_12345",
      "status": "COMPLETE",
      "instrument_token": "3045",
      "trading_symbol": "SBIN",
      "exchange": "NSE",
      "transaction_type": "BUY",
      "order_type": "REGULAR",
      "quantity": 1,
      "price": 542.50,
      "trigger_price": 0.00,
      "filled_quantity": 1,
      "pending_quantity": 0,
      "average_price": 542.60,
      "product": "MIS",
      "validity": "DAY",
      "validity_days": 1,
      "disclosed_quantity": 0,
      "order_timestamp": "2025-12-12T15:30:00Z",
      "exchange_timestamp": "2025-12-12T15:30:05Z"
    }
  ]
}
```

---

#### 12. Update Order
```
PUT https://api.upstox.com/v2/order/update
```
**Your Endpoint**: `PUT /api/v1/orders/{order_id}`

**Headers**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "order_id": "250110000123456",
  "quantity": 2,
  "price": 545.00,
  "trigger_price": 540.00,
  "disclosed_quantity": 0,
  "validity": "DAY"
}
```

---

#### 13. Cancel Order
```
DELETE https://api.upstox.com/v2/order/cancel
```
**Your Endpoint**: `DELETE /api/v1/orders/{order_id}`

**Headers**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "order_id": "250110000123456"
}
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "order_id": "250110000123456",
    "parent_order_id": null,
    "status": "CANCELLED",
    "order_timestamp": "2025-12-12T15:35:00Z"
  }
}
```

---

#### 14. Create GTT Order
```
POST https://api.upstox.com/v2/order/place-gtt
```
**Your Endpoint**: `POST /api/v1/gtt/create`

**Headers**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "instrument_token": "3045",
  "trigger_price": 540.00,
  "last_price": 542.50,
  "orders": [
    {
      "quantity": 1,
      "price": 535.00,
      "product": "MIS",
      "order_type": "LIMIT",
      "transaction_type": "SELL"
    }
  ]
}
```

**Response**:
```json
{
  "status": "success",
  "data": {
    "gtt_id": "gtt_123456",
    "status": "ACTIVE"
  }
}
```

---

#### 15. Retrieve All GTT Orders
```
GET https://api.upstox.com/v2/order/retrieve-all-gtt
```
**Your Endpoint**: `GET /api/v1/gtt`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "gtt_id": "gtt_123456",
      "instrument_token": "3045",
      "trigger_price": 540.00,
      "status": "ACTIVE",
      "orders": [...]
    }
  ]
}
```

---

### **SECTION E: PORTFOLIO ENDPOINTS**

#### 16. Get Holdings (Long Stock Positions)
```
GET https://api.upstox.com/v2/portfolio/long-stock
```
**Your Endpoint**: `GET /api/v1/portfolio/holdings`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "instrument_token": "3045",
      "trading_symbol": "SBIN",
      "exchange": "NSE",
      "isin": "INE062A01020",
      "quantity": 100,
      "t1_quantity": 0,
      "realised_gain": 1000.00,
      "unrealised_gain": 250.00,
      "average_price": 540.00,
      "last_price": 542.50,
      "collateral_quantity": 100,
      "collateral_type": "CASH"
    }
  ]
}
```

---

#### 17. Get Net Positions
```
GET https://api.upstox.com/v2/portfolio/net-positions
```
**Your Endpoint**: `GET /api/v1/portfolio/positions`

**Headers**:
```
Authorization: Bearer {access_token}
Accept: application/json
```

**Response**:
```json
{
  "status": "success",
  "data": [
    {
      "instrument_token": "3045",
      "trading_symbol": "SBIN",
      "exchange": "NSE",
      "product": "MIS",
      "net_quantity": 10,
      "multiplier": 1,
      "buy_quantity": 10,
      "buy_price": 542.50,
      "buy_value": 5425.00,
      "sell_quantity": 0,
      "sell_price": 0.00,
      "sell_value": 0.00,
      "net_weight": 5425.00,
      "realised_gain": 0.00,
      "unrealised_gain": 25.00,
      "day_change": 25.00,
      "day_change_percentage": 0.46,
      "value_multiplier": 1
    }
  ]
}
```

---

### **SECTION F: WEBSOCKET ENDPOINTS**

#### 18. WebSocket Real-time Data Stream
```
wss://ws.upstox.com/feed
```
**Your Endpoints**: WebSocket Quote, Order, Portfolio, Option Chain channels

**Protocol**: WebSocket Secure (WSS)

**Connection Headers**:
```
Authorization: Bearer {access_token}
```

**Subscribe Message Format**:
```json
{
  "guid": "unique_client_id_123",
  "method": "sub",
  "data": {
    "mode": "quote",
    "instrumentTokens": ["3045", "3059"]
  }
}
```

**Supported Modes**:
- `quote` - Live price quotes
- `ohlc` - OHLCV data
- `ltpc` - LTP with change

**Data Received Example**:
```json
{
  "type": "quote",
  "data": {
    "mode": "quote",
    "instrument_token": "3045",
    "ltp": 542.50,
    "bid": 542.40,
    "ask": 542.60,
    "bid_qty": 1000,
    "ask_qty": 1500,
    "total_buy_qty": 500000,
    "total_sell_qty": 450000,
    "volume": 5000000,
    "oi": 0,
    "iv": 18.5,
    "change": 2.50,
    "change_percent": 0.46,
    "last_trade_time": 1702393800,
    "timestamp": 1702393800
  }
}
```

**Unsubscribe Message**:
```json
{
  "guid": "unique_client_id_123",
  "method": "unsub",
  "data": {
    "mode": "quote",
    "instrumentTokens": ["3045"]
  }
}
```

---

## COMPLETE API MAPPING TABLE

| Your Endpoint | Method | Upstox Base URL | Upstox Endpoint | Full Upstox Path |
|---|---|---|---|---|
| `/auth/login` | POST | https://api.upstox.com | authorize | https://api.upstox.com/authorize |
| `/auth/callback` | GET | https://api.upstox.com | oauth/token | https://api.upstox.com/oauth/token |
| `/auth/refresh-token` | POST | https://api.upstox.com | oauth/token | https://api.upstox.com/oauth/token |
| `/user/profile` | GET | https://api.upstox.com | v2/user/profile | https://api.upstox.com/v2/user/profile |
| `/user/margins` | GET | https://api.upstox.com | v2/user/get-margin | https://api.upstox.com/v2/user/get-margin |
| `/user/charges` | GET | https://api.upstox.com | v2/user/get-charges | https://api.upstox.com/v2/user/get-charges |
| `/instruments/all` | GET | https://api.upstox.com | v2/market/instruments/ | https://api.upstox.com/v2/market/instruments/ |
| `/market/quote/{token}` | GET | https://api.upstox.com | v2/market/quote/ | https://api.upstox.com/v2/market/quote/ |
| `/market/quotes` | POST | https://api.upstox.com | v2/market/quote/ | https://api.upstox.com/v2/market/quote/ |
| `/market/ohlc/{token}` | GET | https://api.upstox.com | v2/market/historical-data/intraday | https://api.upstox.com/v2/market/historical-data/intraday |
| `/orders/place` | POST | https://api.upstox.com | v2/order/place | https://api.upstox.com/v2/order/place |
| `/orders` | GET | https://api.upstox.com | v2/order/retrieve-all | https://api.upstox.com/v2/order/retrieve-all |
| `/orders/{id}` | PUT | https://api.upstox.com | v2/order/update | https://api.upstox.com/v2/order/update |
| `/orders/{id}` | DELETE | https://api.upstox.com | v2/order/cancel | https://api.upstox.com/v2/order/cancel |
| `/gtt/create` | POST | https://api.upstox.com | v2/order/place-gtt | https://api.upstox.com/v2/order/place-gtt |
| `/gtt` | GET | https://api.upstox.com | v2/order/retrieve-all-gtt | https://api.upstox.com/v2/order/retrieve-all-gtt |
| `/portfolio/holdings` | GET | https://api.upstox.com | v2/portfolio/long-stock | https://api.upstox.com/v2/portfolio/long-stock |
| `/portfolio/positions` | GET | https://api.upstox.com | v2/portfolio/net-positions | https://api.upstox.com/v2/portfolio/net-positions |
| WebSocket | WSS | wss://ws.upstox.com | /feed | wss://ws.upstox.com/feed |

---

## HTTP HEADERS REQUIRED FOR ALL UPSTOX API CALLS

```
Authorization: Bearer {access_token}
Accept: application/json
Content-Type: application/json (for POST/PUT requests)
User-Agent: YourApp/1.0
```

---

## RATE LIMITS (Upstox API)

| Endpoint Category | Limit | Window |
|---|---|---|
| Authentication | 10 requests/min | Per IP |
| Market Data (Quotes, OHLC) | 100 requests/min | Per token |
| Orders (Place, Cancel, Modify) | 50 requests/min | Per token |
| Portfolio (Holdings, Positions) | 50 requests/min | Per token |
| WebSocket | Unlimited | Bandwidth: ~1 MB/s |

---

## ERROR RESPONSES FROM UPSTOX API

All error responses follow this format:

```json
{
  "status": "error",
  "code": "error_code",
  "message": "Human readable message",
  "errors": [
    {
      "field": "field_name",
      "message": "Field specific error"
    }
  ]
}
```

**Common Error Codes**:
- `INVALID_TOKEN` - Access token expired or invalid
- `INSUFFICIENT_MARGIN` - Not enough margin for order
- `INSTRUMENT_NOT_FOUND` - Invalid instrument token
- `ORDER_NOT_FOUND` - Order ID doesn't exist
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `INVALID_REQUEST` - Malformed request

---

## EXAMPLE: COMPLETE ORDER PLACEMENT FLOW WITH UPSTOX APIs

### Step 1: Get User Margins
```
GET https://api.upstox.com/v2/user/get-margin
Headers: Authorization: Bearer {access_token}
Response: Available margin = 450000
```

### Step 2: Get Instrument Quote
```
GET https://api.upstox.com/v2/market/quote/?mode=FULL&instrumentToken=3045
Headers: Authorization: Bearer {access_token}
Response: LTP = 542.50, Ask Price = 542.60
```

### Step 3: Place Order
```
POST https://api.upstox.com/v2/order/place
Headers: Authorization: Bearer {access_token}
Body: {
  "quantity": 1,
  "price": 542.60,
  "product": "MIS",
  "order_type": "REGULAR",
  "transaction_type": "BUY",
  "instrument_token": "3045"
}
Response: order_id = 250110000123456
```

### Step 4: Get Order Status (Subscribe to WebSocket)
```
wss://ws.upstox.com/feed
Subscribe to order updates
Receive: Order status = COMPLETE, filled_quantity = 1
```

---

## PRODUCTION DEPLOYMENT NOTES

### Base URLs by Environment

**Development/Sandbox**:
```
Rest API: https://api.upstox.com (still uses live data, no sandbox)
WebSocket: wss://ws.upstox.com
```

**Production**:
```
Rest API: https://api.upstox.com
WebSocket: wss://ws.upstox.com
```

**Note**: Upstox doesn't have a separate sandbox environment. Always test carefully.

---

## CONFIGURATION FOR YOUR APPLICATION

### Backend FastAPI Configuration
```python
# config.py
UPSTOX_API_BASE_URL = "https://api.upstox.com"
UPSTOX_API_VERSION = "v2"
UPSTOX_OAUTH_URL = "https://api.upstox.com/authorize"
UPSTOX_TOKEN_URL = "https://api.upstox.com/oauth/token"
UPSTOX_WEBSOCKET_URL = "wss://ws.upstox.com/feed"

UPSTOX_CLIENT_ID = os.getenv("UPSTOX_CLIENT_ID")
UPSTOX_CLIENT_SECRET = os.getenv("UPSTOX_CLIENT_SECRET")
UPSTOX_REDIRECT_URI = "https://yourapp.com/api/v1/auth/callback"
```

### Python Requests Example
```python
import requests

# Get Quote
response = requests.get(
    "https://api.upstox.com/v2/market/quote/",
    params={
        "mode": "FULL",
        "instrumentToken": "3045"
    },
    headers={
        "Authorization": f"Bearer {access_token}",
        "Accept": "application/json"
    }
)
quote = response.json()
```

### JavaScript Fetch Example
```javascript
// Get Quote
const response = await fetch(
  'https://api.upstox.com/v2/market/quote/?mode=FULL&instrumentToken=3045',
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Accept': 'application/json'
    }
  }
);
const quote = await response.json();

// WebSocket Connection
const ws = new WebSocket('wss://ws.upstox.com/feed');
ws.onopen = () => {
  ws.send(JSON.stringify({
    guid: 'client-123',
    method: 'sub',
    data: {
      mode: 'quote',
      instrumentTokens: ['3045']
    }
  }));
};
```

---

## SUMMARY: ALL UPSTOX BASE URLS

| Purpose | Base URL | Protocol | Used For |
|---------|----------|----------|----------|
| OAuth & Authentication | https://api.upstox.com | HTTPS | Login, token exchange |
| REST API v2 | https://api.upstox.com/v2 | HTTPS | Market data, orders, portfolio |
| WebSocket Feed | wss://ws.upstox.com | WSS | Real-time data streaming |

---

**Last Updated**: December 12, 2025
**Version**: 1.0
**Status**: Complete & Production Ready

