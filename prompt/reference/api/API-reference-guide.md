# 📋 COMPLETE API REFERENCE GUIDE

## TABLE OF CONTENTS
1. Authentication APIs
2. User & Account APIs
3. Instruments & Market Data APIs
4. Orders & Trading APIs
5. Portfolio APIs
6. Strategy & AI APIs
7. Webhooks & WebSocket APIs
8. Settings & Configuration APIs
9. Technical Indicators APIs
10. Error Handling & Rate Limits

---

## 1. AUTHENTICATION APIs

### 1.1 POST /api/v1/auth/login
**OAuth Login Initiation**

```
Method: POST
Path: /api/v1/auth/login
Authentication: None (public endpoint)

Request:
{
  "client_id": "upstox_client_id_here",
  "redirect_uri": "https://yourapp.com/auth/callback",
  "state": "random_csrf_token_here"
}

Response (302 Redirect):
Location: https://api.upstox.com/authorize?client_id=...&redirect_uri=...&state=...

Usage: 
- Initiate OAuth flow to Upstox
- User will be redirected to Upstox login
```

### 1.2 GET /api/v1/auth/callback
**OAuth Callback Handler**

```
Method: GET
Path: /api/v1/auth/callback?code=AUTH_CODE&state=STATE

Request Parameters:
- code (string): Authorization code from Upstox
- state (string): CSRF token for verification

Response (200 OK):
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 82800,
  "refresh_token": "refresh_token_string_here",
  "session_id": "sess_uuid_string",
  "valid_until": "2025-12-13T02:30:00Z",
  "user": {
    "user_id": "user_uuid",
    "email": "user@example.com",
    "name": "User Name",
    "broker": "UPSTOX",
    "account_status": "active"
  }
}

Cookie Set: 
Set-Cookie: access_token=<token>; HttpOnly; Secure; SameSite=Strict; Max-Age=82800

Usage:
- Exchanges auth code for access token
- Creates user session
- Sets httpOnly cookie for token persistence
- Token valid from 3:15 AM to 2:30 AM IST (23 hours)
```

### 1.3 POST /api/v1/auth/refresh-token
**Token Refresh Before Expiration**

```
Method: POST
Path: /api/v1/auth/refresh-token
Authentication: Bearer token

Request:
{
  "refresh_token": "refresh_token_string_here",
  "timestamp": "2025-12-12T10:44:00Z"
}

Response (200 OK):
{
  "access_token": "new_access_token_jwt",
  "token_type": "Bearer",
  "expires_in": 82800,
  "refresh_token": "new_refresh_token",
  "valid_until": "2025-12-13T02:30:00Z"
}

Error Cases (401 Unauthorized):
{
  "error": "token_expired",
  "message": "Refresh token has expired, please login again"
}

Usage:
- Call this 15 minutes before token expiration
- Maintains continuous session without re-login
- Only works during valid window (3:15 AM - 2:30 AM IST)
```

### 1.4 POST /api/v1/auth/manual-token-generation
**Manual Token Generation Outside Validity Window**

```
Method: POST
Path: /api/v1/auth/manual-token-generation
Authentication: None

Request:
{
  "user_id": "upstox_user_id",
  "refresh_token": "refresh_token_string",
  "reason": "emergency_access_needed"
}

Response (200 OK):
{
  "access_token": "new_access_token",
  "valid_until": "2025-12-13T02:30:00Z",
  "warning": "Outside normal trading hours window"
}

Usage:
- Generate token outside 3:15 AM - 2:30 AM window
- For manual token generation by user
- Requires valid refresh token
```

### 1.5 GET /api/v1/auth/session-status
**Check Current Session Validity**

```
Method: GET
Path: /api/v1/auth/session-status
Authentication: Bearer token

Response (200 OK):
{
  "is_valid": true,
  "expires_at": "2025-12-13T02:30:00Z",
  "time_remaining_minutes": 1450,
  "token_type": "access",
  "session_id": "sess_uuid",
  "needs_refresh": false,
  "valid_window": {
    "start": "03:15:00",
    "end": "02:30:00",
    "timezone": "IST"
  }
}

Usage:
- Called every 30 seconds on dashboard
- Frontend determines if refresh needed
- Shows remaining time to user
```

### 1.6 POST /api/v1/auth/logout
**Terminate Session**

```
Method: POST
Path: /api/v1/auth/logout
Authentication: Bearer token

Request: {} (empty body)

Response (200 OK):
{
  "status": "success",
  "message": "Successfully logged out"
}

Side Effects:
- Session marked as inactive in database
- Token revoked
- Cookies cleared on client
- All WebSocket connections closed

Usage:
- User logout
- Session cleanup
- Security: Closes all connections
```

---

## 2. USER & ACCOUNT APIs

### 2.1 GET /api/v1/user/profile
**Fetch User Profile & Account Details**

```
Method: GET
Path: /api/v1/user/profile
Authentication: Bearer token

Response (200 OK):
{
  "user_id": "user_uuid",
  "name": "John Trader",
  "email": "john@example.com",
  "phone": "+919876543210",
  "broker": "UPSTOX",
  "account_type": "individual",
  "account_status": "active",
  "kra_status": "approved",
  "pan": "XXXXX5678X (masked)",
  "aadhaar_verified": true,
  "kyc_status": "complete",
  "created_at": "2025-01-01T10:00:00Z",
  "last_login": "2025-12-12T09:30:00Z",
  "trading_segments": ["EQUITY", "DERIVATIVES", "COMMODITY"],
  "max_holdings": 20,
  "margin_multiplier": 4
}

Usage:
- Display user info on dashboard
- Verify account status
- Check trading segment eligibility
```

### 2.2 GET /api/v1/user/margins
**Fetch Current Margin Details**

```
Method: GET
Path: /api/v1/user/margins
Authentication: Bearer token

Response (200 OK):
{
  "margin": {
    "available": 450000.50,
    "used": 150000.00,
    "total": 600000.50,
    "margin_type": "cash"
  },
  "segment_wise": {
    "EQUITY": {
      "available": 250000,
      "used": 100000,
      "total": 350000
    },
    "DERIVATIVES": {
      "available": 200000,
      "used": 50000,
      "total": 250000
    }
  },
  "commodities_margin": {
    "available": 50000,
    "used": 0,
    "total": 50000
  },
  "margin_multiplier": 4,
  "utilization_percent": 25,
  "last_updated": "2025-12-12T10:44:00Z"
}

Usage:
- Display available capital
- Validate order quantity before placement
- Show margin utilization percentage
- Warn if margin < 25%
```

### 2.3 GET /api/v1/user/charges
**Fetch Brokerage Charges & Fees**

```
Method: GET
Path: /api/v1/user/charges
Authentication: Bearer token

Response (200 OK):
{
  "equity": {
    "intraday_percent": 0.05,
    "delivery_percent": 0.05,
    "short_selling_percent": 0.10,
    "min_charge": 20,
    "max_charge": 500
  },
  "derivatives": {
    "options_percent": 0.15,
    "options_min": 20,
    "futures_percent": 0.02,
    "futures_min": 10
  },
  "charges_per_lot": {
    "options": 20,
    "futures": 10
  },
  "taxes": {
    "stt_applicable": true,
    "gst_applicable": true,
    "stamp_duty": 0.003
  }
}

Usage:
- Calculate real P&L after charges
- Display charges in order preview
- Warn about high-cost strategies
```

---

## 3. INSTRUMENTS & MARKET DATA APIs

### 3.1 GET /api/v1/instruments/all
**Fetch All Available Instruments**

```
Method: GET
Path: /api/v1/instruments/all
Authentication: Bearer token

Query Parameters:
- segment (string): "EQUITY", "DERIVATIVES", "COMMODITY", "CURRENCY"
- type (string): "STOCKS", "INDEX", "OPTIONS", "FUTURES"
- search (string): Symbol or name to search (optional)
- limit (int): Default 100, max 1000
- offset (int): For pagination

Request Example:
/api/v1/instruments/all?segment=DERIVATIVES&type=OPTIONS&limit=100&offset=0

Response (200 OK):
{
  "total": 5000,
  "data": [
    {
      "instrument_token": "12345",
      "trading_symbol": "SBIN",
      "name": "State Bank of India",
      "segment": "EQUITY",
      "exchange": "NSE",
      "instrument_type": "EQUITY",
      "isin": "INE062A01020",
      "minimum_lot": 1,
      "tick_size": 0.05,
      "multiplier": 1,
      "expiry_date": null
    },
    {
      "instrument_token": "67890",
      "trading_symbol": "SBIN-SEP-550-CE",
      "name": "SBIN SEP 2025 550 CALL",
      "segment": "DERIVATIVES",
      "exchange": "NSE",
      "instrument_type": "OPTIONS",
      "isin": null,
      "minimum_lot": 1,
      "tick_size": 0.05,
      "multiplier": 1,
      "expiry_date": "2025-09-30"
    }
  ],
  "cached": true,
  "cache_expires": "2025-12-13T10:44:00Z"
}

Usage:
- Populate search autocomplete
- Build instrument filters
- Load on app initialization
- Cache for 1 hour
```

### 3.2 GET /api/v1/instruments/search
**Real-time Search for Instruments**

```
Method: GET
Path: /api/v1/instruments/search
Authentication: Bearer token

Query Parameters:
- query (string): Search term (symbol or name) - min 2 chars
- type (string): Filter by type (optional)
- limit (int): Default 10, max 50

Request Example:
/api/v1/instruments/search?query=sbin&type=STOCKS&limit=20

Response (200 OK):
{
  "results": [
    {
      "instrument_token": "12345",
      "trading_symbol": "SBIN",
      "name": "State Bank of India",
      "type": "EQUITY",
      "current_price": 542.50,
      "change_percent": 2.5,
      "exchange": "NSE"
    },
    {
      "instrument_token": "12346",
      "trading_symbol": "SBINETSEC",
      "name": "SBI ETF Sensex",
      "type": "EQUITY",
      "current_price": 1250.00,
      "change_percent": 1.2,
      "exchange": "NSE"
    }
  ],
  "query_time": 45
}

Usage:
- Real-time search box dropdown
- Debounce requests (min 50ms)
- Cache results for 5 minutes
```

### 3.3 GET /api/v1/instruments/options-chain
**Fetch Option Chain for Underlying**

```
Method: GET
Path: /api/v1/instruments/options-chain
Authentication: Bearer token

Query Parameters:
- instrument_token (string): Underlying stock/index token
- expiry_date (YYYY-MM-DD): Specific expiry (optional)
- strike_range (int): Number of strikes ±ATM (optional, default all)

Request Example:
/api/v1/instruments/options-chain?instrument_token=12345&expiry_date=2025-12-18&strike_range=5

Response (200 OK):
{
  "underlying": {
    "symbol": "NIFTY50",
    "instrument_token": "12345",
    "current_price": 23500.00,
    "change_percent": 1.5
  },
  "expiry_dates": ["2025-12-18", "2025-12-25", "2026-01-30"],
  "options": [
    {
      "instrument_token": "67890",
      "strike_price": 228020,
      "expiry_date": "2025-12-18",
      "option_type": "CE",
      "trading_symbol": "NIFTY50-DEC-228020-CE",
      "bid_price": 520.50,
      "ask_price": 525.00,
      "last_price": 523.00,
      "volume": 1500,
      "open_interest": 50000,
      "implied_volatility": 18.5,
      "greeks": {
        "delta": 0.75,
        "gamma": 0.002,
        "theta": -0.05,
        "vega": 2.3,
        "rho": 0.015
      },
      "time_decay_per_day": -35.50
    }
  ],
  "max_pain": 23500,
  "put_call_ratio": 1.2,
  "total_volume": 250000,
  "total_oi": 5000000,
  "chain_date": "2025-12-12T15:30:00Z"
}

Usage:
- Display option chain on derivatives page
- Filter by strike range
- Show Greeks for analysis
- Calculate max pain level
```

### 3.4 GET /api/v1/market/quote/{instrument_token}
**Fetch Live Market Quote**

```
Method: GET
Path: /api/v1/market/quote/{instrument_token}
Authentication: Bearer token

Path Parameters:
- instrument_token (string): Unique instrument identifier

Response (200 OK):
{
  "instrument_token": "12345",
  "trading_symbol": "SBIN",
  "name": "State Bank of India",
  "ltp": 542.50,
  "open": 540.00,
  "high": 545.00,
  "low": 539.50,
  "close": 542.50,
  "volume": 5000000,
  "oi": 0,
  "bid": 542.40,
  "bid_qty": 1000,
  "ask": 542.60,
  "ask_qty": 1500,
  "52_week_high": 650.00,
  "52_week_low": 420.00,
  "change": 2.50,
  "change_percent": 0.46,
  "previous_close": 540.00,
  "vwap": 541.85,
  "timestamp": "2025-12-12T15:30:00Z",
  "source": "cache"
}

Usage:
- Display current price
- Show bid/ask spread
- Display change percentage
- Cache for 1 minute
```

### 3.5 POST /api/v1/market/quotes
**Fetch Multiple Quotes at Once**

```
Method: POST
Path: /api/v1/market/quotes
Authentication: Bearer token

Request:
{
  "instrument_tokens": ["12345", "67890", "11111"],
  "mode": "FULL"
}

Response (200 OK):
{
  "quotes": [
    {
      "instrument_token": "12345",
      "trading_symbol": "SBIN",
      "ltp": 542.50,
      "bid": 542.40,
      "ask": 542.60,
      ...
    },
    {
      "instrument_token": "67890",
      "trading_symbol": "INFY",
      "ltp": 2250.00,
      ...
    }
  ],
  "timestamp": "2025-12-12T15:30:00Z"
}

Usage:
- Bulk update dashboard prices
- Update watchlist quotes
- Called every 5-10 seconds
```

### 3.6 GET /api/v1/market/ohlc/{instrument_token}
**Fetch OHLC Historical Data**

```
Method: GET
Path: /api/v1/market/ohlc/{instrument_token}
Authentication: Bearer token

Query Parameters:
- interval (string): "1m", "5m", "15m", "30m", "1h", "1d", "1w", "1mo"
- from_date (YYYY-MM-DD): Start date
- to_date (YYYY-MM-DD): End date
- count (int): Number of candles (default 100)

Request Example:
/api/v1/market/ohlc/12345?interval=15m&from_date=2025-12-01&to_date=2025-12-12&count=100

Response (200 OK):
{
  "instrument_token": "12345",
  "interval": "15m",
  "count": 100,
  "data": [
    {
      "timestamp": "2025-12-12T09:15:00Z",
      "open": 540.00,
      "high": 545.00,
      "low": 539.50,
      "close": 542.50,
      "volume": 5000000,
      "oi": 0
    },
    {
      "timestamp": "2025-12-12T09:30:00Z",
      "open": 542.50,
      "high": 547.00,
      "low": 541.00,
      "close": 545.50,
      "volume": 4500000,
      "oi": 0
    }
  ]
}

Usage:
- Chart data for TradingView
- Technical indicator calculation
- Strategy backtesting
```

### 3.7 GET /api/v1/market/historical
**Fetch Detailed Historical Data with Indicators**

```
Method: GET
Path: /api/v1/market/historical
Authentication: Bearer token

Query Parameters:
- instrument_token (string): Required
- interval (string): "1m", "5m", "15m", "30m", "1h", "1d"
- from_date (YYYY-MM-DD)
- to_date (YYYY-MM-DD)
- include_indicators (boolean): Calculate technical indicators

Request Example:
/api/v1/market/historical?instrument_token=12345&interval=1d&from_date=2025-10-01&to_date=2025-12-12&include_indicators=true

Response (200 OK):
{
  "data": [
    {
      "timestamp": "2025-12-12T15:30:00Z",
      "open": 540.00,
      "high": 545.00,
      "low": 539.50,
      "close": 542.50,
      "volume": 5000000,
      "indicators": {
        "rsi": 65.5,
        "macd": {"macd": 0.85, "signal": 0.72, "histogram": 0.13},
        "bollinger_bands": {"upper": 550.00, "middle": 540.00, "lower": 530.00},
        "ema_20": 539.50,
        "sma_50": 535.00
      }
    }
  ],
  "summary_stats": {
    "highest": 550.00,
    "lowest": 530.00,
    "average": 539.50,
    "volatility": 0.85
  }
}

Usage:
- Derivatives strategy development page
- Historical analysis with indicators
- Backtest strategy development
```

---

## 4. ORDERS & TRADING APIs

### 4.1 POST /api/v1/orders/place
**Place New Order**

```
Method: POST
Path: /api/v1/orders/place
Authentication: Bearer token

Request:
{
  "instrument_token": "12345",
  "order_type": "REGULAR",
  "transaction_type": "BUY",
  "quantity": 1,
  "price": 542.50,
  "product": "MIS",
  "order_side": "BUY",
  "disclosed_quantity": 0,
  "trigger_price": 0,
  "validity": "DAY",
  "tag": "strategy_001"
}

Order Types:
- REGULAR: Standard order
- BRACKET: Bracket order with SL & TP
- COVER: Cover order (intraday)

Products:
- MIS: Margin Intraday Square Off
- CNC: Cash n Carry (delivery)
- NRML: Normal (derivatives)

Validity:
- DAY: Valid for the trading day
- IOC: Immediate or Cancel
- GTT: Good Till Triggered

Response (200 OK):
{
  "order_id": "ord_12345",
  "status": "PENDING",
  "timestamp": "2025-12-12T15:30:00Z",
  "message": "Order placed successfully"
}

Response (400 Bad Request):
{
  "error": "insufficient_margin",
  "message": "Available margin insufficient for this order"
}

Response (403 Forbidden):
{
  "error": "order_not_allowed",
  "message": "Options trading not allowed on this segment"
}

Usage:
- User-initiated buy/sell orders
- Single leg orders
- Called from Order Form component
```

### 4.2 GET /api/v1/orders
**Fetch User Orders**

```
Method: GET
Path: /api/v1/orders
Authentication: Bearer token

Query Parameters:
- status (string): "PENDING", "COMPLETE", "CANCELLED", "REJECTED"
- from_date (YYYY-MM-DD): Filter orders from date
- to_date (YYYY-MM-DD): Filter orders to date
- segment (string): "EQUITY", "DERIVATIVES"
- limit (int): Default 50
- offset (int): For pagination

Request Example:
/api/v1/orders?status=COMPLETE&from_date=2025-12-01&to_date=2025-12-12&limit=50

Response (200 OK):
{
  "total": 150,
  "data": [
    {
      "order_id": "ord_12345",
      "instrument_token": "12345",
      "trading_symbol": "SBIN",
      "transaction_type": "BUY",
      "quantity": 1,
      "price": 542.50,
      "executed_price": 542.60,
      "status": "COMPLETE",
      "filled_quantity": 1,
      "pending_quantity": 0,
      "created_at": "2025-12-12T15:30:00Z",
      "executed_at": "2025-12-12T15:30:05Z",
      "product": "MIS",
      "charges": 25.50,
      "pnl": 150.00
    }
  ]
}

Usage:
- Display order history
- Filter by status/date
- Show order execution details
```

### 4.3 POST /api/v1/orders/bulk
**Place Multiple Orders (for Strategies)**

```
Method: POST
Path: /api/v1/orders/bulk
Authentication: Bearer token

Request:
{
  "orders": [
    {
      "instrument_token": "67890",
      "order_type": "REGULAR",
      "transaction_type": "BUY",
      "quantity": 1,
      "price": 520.00,
      "product": "NRML",
      "tag": "iron_condor_leg_1"
    },
    {
      "instrument_token": "67891",
      "order_type": "REGULAR",
      "transaction_type": "SELL",
      "quantity": 1,
      "price": 510.00,
      "product": "NRML",
      "tag": "iron_condor_leg_2"
    }
  ],
  "strategy_id": "strat_001"
}

Response (200 OK):
{
  "strategy_id": "strat_001",
  "orders": [
    {
      "order_id": "ord_12346",
      "tag": "iron_condor_leg_1",
      "status": "PENDING"
    },
    {
      "order_id": "ord_12347",
      "tag": "iron_condor_leg_2",
      "status": "PENDING"
    }
  ],
  "total_margin_required": 50000,
  "timestamp": "2025-12-12T15:30:00Z"
}

Usage:
- Place multi-leg strategies
- AI strategy order placement
- User-created strategy orders
```

### 4.4 PUT /api/v1/orders/{order_id}
**Modify Open Order**

```
Method: PUT
Path: /api/v1/orders/{order_id}
Authentication: Bearer token

Request:
{
  "quantity": 2,
  "price": 545.00,
  "trigger_price": 540.00
}

Response (200 OK):
{
  "order_id": "ord_12345",
  "status": "PENDING",
  "updated_at": "2025-12-12T15:35:00Z"
}

Usage:
- Modify quantity before execution
- Adjust price
- Add trigger for conditional orders
```

### 4.5 DELETE /api/v1/orders/{order_id}
**Cancel Open Order**

```
Method: DELETE
Path: /api/v1/orders/{order_id}
Authentication: Bearer token

Response (200 OK):
{
  "order_id": "ord_12345",
  "status": "CANCELLED",
  "cancelled_at": "2025-12-12T15:35:00Z"
}

Usage:
- Cancel pending orders
- Close out positions
```

---

## 5. PORTFOLIO APIs

### 5.1 GET /api/v1/portfolio/holdings
**Fetch Delivery Holdings**

```
Method: GET
Path: /api/v1/portfolio/holdings
Authentication: Bearer token

Response (200 OK):
{
  "holdings": [
    {
      "instrument_token": "12345",
      "trading_symbol": "SBIN",
      "name": "State Bank of India",
      "segment": "EQUITY",
      "quantity": 100,
      "average_price": 540.00,
      "current_price": 542.50,
      "value": 54250.00,
      "pnl": 250.00,
      "pnl_percent": 0.46,
      "collateral_type": "CASH",
      "collateral_value": 27000.00,
      "haircut": 0.25
    }
  ],
  "total_value": 500000.00,
  "total_pnl": 15000.00,
  "total_pnl_percent": 3.33
}

Usage:
- Display portfolio holdings
- Show holding-wise P&L
- Calculate collateral value
```

### 5.2 GET /api/v1/portfolio/positions
**Fetch Open Intraday/Derivatives Positions**

```
Method: GET
Path: /api/v1/portfolio/positions
Authentication: Bearer token

Query Parameters:
- segment (string): "EQUITY", "DERIVATIVES" (optional)

Response (200 OK):
{
  "positions": [
    {
      "instrument_token": "12345",
      "trading_symbol": "SBIN",
      "segment": "EQUITY",
      "quantity": 10,
      "buy_quantity": 10,
      "sell_quantity": 0,
      "entry_price": 542.50,
      "current_price": 545.00,
      "pnl": 250.00,
      "pnl_percent": 0.46,
      "product": "MIS",
      "value": 5450.00,
      "side": "LONG"
    },
    {
      "instrument_token": "67890",
      "trading_symbol": "NIFTY50-DEC-228020-CE",
      "segment": "DERIVATIVES",
      "quantity": 1,
      "entry_price": 520.00,
      "current_price": 523.00,
      "pnl": 300.00,
      "pnl_percent": 0.58,
      "greeks": {
        "delta": 0.75,
        "gamma": 0.002
      }
    }
  ],
  "total_pnl": 550.00
}

Usage:
- Display open positions
- Track intraday P&L
- Show position-wise Greeks
```

### 5.3 GET /api/v1/portfolio/net-worth
**Calculate Total Net Worth & Metrics**

```
Method: GET
Path: /api/v1/portfolio/net-worth
Authentication: Bearer token

Response (200 OK):
{
  "total_value": 500000.00,
  "invested_value": 450000.00,
  "cash": 50000.00,
  "margin": {
    "available": 450000.00,
    "used": 150000.00,
    "total": 600000.00
  },
  "total_pnl": 15000.00,
  "total_pnl_percent": 3.33,
  "today_pnl": 2500.00,
  "today_pnl_percent": 0.55,
  "month_pnl": 12000.00,
  "year_pnl": 45000.00,
  "highest_value": 510000.00,
  "lowest_value": 485000.00
}

Usage:
- Dashboard summary cards
- Portfolio performance tracking
```

### 5.4 GET /api/v1/portfolio/pnl
**Fetch Detailed P&L Breakdown**

```
Method: GET
Path: /api/v1/portfolio/pnl
Authentication: Bearer token

Query Parameters:
- from_date (YYYY-MM-DD)
- to_date (YYYY-MM-DD)
- segment (string): Filter by segment

Response (200 OK):
{
  "realized_pnl": 10000.00,
  "unrealized_pnl": 5000.00,
  "total_pnl": 15000.00,
  "by_segment": {
    "EQUITY": {
      "realized": 28021.00,
      "unrealized": 28020.00,
      "total": 11000.00
    },
    "DERIVATIVES": {
      "realized": 2000.00,
      "unrealized": 2000.00,
      "total": 4000.00
    }
  },
  "by_date": [
    {
      "date": "2025-12-12",
      "pnl": 2500.00,
      "trades": 5
    },
    {
      "date": "2025-12-11",
      "pnl": -1500.00,
      "trades": 3
    }
  ],
  "best_trade": 5000.00,
  "worst_trade": -2000.00,
  "win_rate": 0.72
}

Usage:
- P&L analytics dashboard
- Performance tracking by segment
- Daily P&L chart
```

---

## 6. STRATEGY & AI APIs

### 6.1 POST /api/v1/ai/analyze-contract
**AI Technical Analysis**

```
Method: POST
Path: /api/v1/ai/analyze-contract
Authentication: Bearer token

Request:
{
  "instrument_token": "12345",
  "timeframe": "15m",
  "indicators": ["RSI", "MACD", "Bollinger Bands", "EMA"],
  "include_sentiment": true
}

Response (200 OK):
{
  "symbol": "SBIN",
  "current_price": 542.50,
  "analysis": {
    "trend": "BULLISH",
    "strength": 0.72,
    "signals": [
      "RSI Oversold Recovery (59 > 30)",
      "MACD Bullish Cross (positive histogram)",
      "Price above EMA20"
    ],
    "support_levels": [540.00, 535.00, 530.00],
    "resistance_levels": [545.00, 550.00, 555.00],
    "recommendation": "BUY",
    "confidence": 0.85,
    "next_target": 550.00,
    "stop_loss": 535.00
  },
  "indicators": {
    "rsi": 59.5,
    "macd": {"value": 0.85, "signal": 0.72},
    "bb": {"position": "middle_to_upper"},
    "ema": {"20": 541.50, "50": 540.00}
  },
  "analysis_time_ms": 245
}

Usage:
- Display on contract analysis cards
- AI recommendation signal
- Support/resistance levels
```

### 6.2 POST /api/v1/ai/generate-strategy
**AI Strategy Generation**

```
Method: POST
Path: /api/v1/ai/generate-strategy
Authentication: Bearer token

Request:
{
  "instrument_token": "12345",
  "strategy_type": "OPTIONS",
  "timeframe": "intraday",
  "risk_tolerance": "moderate",
  "capital": 100000,
  "use_live_websocket": true
}

Response (200 OK):
{
  "strategy_id": "ai_strat_12345",
  "type": "IRON_CONDOR",
  "underlying": {
    "symbol": "NIFTY50",
    "current_price": 23500.00
  },
  "legs": [
    {
      "leg_id": 1,
      "action": "SELL",
      "instrument_token": "67890",
      "strike": 228020,
      "option_type": "CE",
      "quantity": 1,
      "entry_price": 520.00,
      "recommended_price": 515.00
    },
    {
      "leg_id": 2,
      "action": "BUY",
      "instrument_token": "67891",
      "strike": 23100,
      "option_type": "CE",
      "quantity": 1,
      "entry_price": 420.00,
      "recommended_price": 415.00
    }
  ],
  "entry_signal": {
    "conditions": [
      "RSI between 40-60 (neutral)",
      "MACD histogram positive",
      "IV Rank > 50th percentile"
    ],
    "timing": "Within next 15 minutes"
  },
  "exit_strategy": {
    "profit_target": 0.50,
    "stop_loss": -0.02,
    "time_decay": "Collect at 50% profit or T+1 day"
  },
  "statistics": {
    "expected_profit": 5000.00,
    "max_loss": 10000.00,
    "profit_factor": 0.50,
    "win_rate": 0.72,
    "avg_hold_time_hours": 4,
    "historical_backtest": {
      "trades": 25,
      "wins": 18,
      "losses": 7,
      "avg_profit_trade": 278,
      "avg_loss_trade": -357
    }
  },
  "confidence": 0.88,
  "reasoning": "Based on RSI recovery from oversold (25->59) combined with MACD bullish cross. Historical data shows 72% win rate on this setup in similar market conditions. Iron Condor optimal for sideways market with falling IV.",
  "data_sources": [
    "Live WebSocket: NIFTY50 30-sec updates",
    "Historical: Last 60 days OHLCV",
    "Market Profile: Current IV rank"
  ],
  "timestamp": "2025-12-12T15:30:00Z"
}

Usage:
- One-click AI strategy generation
- Display on derivatives page
- Show reasoning to user
- Place order directly from this response
```

### 6.3 POST /api/v1/ai/place-order-from-strategy
**Execute AI Strategy Orders**

```
Method: POST
Path: /api/v1/ai/place-order-from-strategy
Authentication: Bearer token

Request:
{
  "strategy_id": "ai_strat_12345",
  "confirm": true
}

Response (200 OK):
{
  "strategy_execution_id": "exec_12345",
  "strategy_id": "ai_strat_12345",
  "status": "EXECUTING",
  "orders": [
    {
      "leg_id": 1,
      "order_id": "ord_12346",
      "status": "PENDING"
    },
    {
      "leg_id": 2,
      "order_id": "ord_12347",
      "status": "PENDING"
    }
  ],
  "timestamp": "2025-12-12T15:30:00Z",
  "message": "Strategy orders placed. Monitor via order list."
}

Usage:
- Execute AI-generated strategy
- Creates multi-leg orders
- Track execution via order IDs
```

### 6.4 POST /api/v1/strategies/create
**Save User-Created Strategy**

```
Method: POST
Path: /api/v1/strategies/create
Authentication: Bearer token

Request:
{
  "name": "My Iron Condor",
  "description": "Sell iron condor on NIFTY50 when RSI < 30",
  "strategy_type": "OPTIONS",
  "legs": [
    {
      "action": "SELL",
      "instrument_token": "67890",
      "strike": 228020,
      "option_type": "CE",
      "quantity": 1
    },
    {
      "action": "BUY",
      "instrument_token": "67891",
      "strike": 23100,
      "option_type": "CE",
      "quantity": 1
    }
  ],
  "entry_condition": "RSI < 30 AND MACD Positive",
  "exit_condition": "Profit 50% OR Loss 2%",
  "is_predefined": false,
  "tags": ["options", "income", "neutral"]
}

Response (200 OK):
{
  "strategy_id": "strat_uuid",
  "name": "My Iron Condor",
  "created_at": "2025-12-12T15:30:00Z",
  "status": "active"
}

Usage:
- Save custom strategies
- Reuse in future trades
- Build strategy library
```

### 6.5 GET /api/v1/strategies
**Fetch Strategies**

```
Method: GET
Path: /api/v1/strategies
Authentication: Bearer token

Query Parameters:
- type (string): "PREDEFINED", "USER_CREATED", "AI_GENERATED"
- strategy_type (string): "OPTIONS", "STOCKS", "FUTURES"
- limit (int): Default 50

Response (200 OK):
{
  "strategies": [
    {
      "strategy_id": "strat_001",
      "name": "Iron Condor - Weekly",
      "type": "USER_CREATED",
      "strategy_type": "OPTIONS",
      "description": "Sell OTM calls and puts",
      "created_at": "2025-12-01T10:00:00Z",
      "last_used": "2025-12-12T10:00:00Z",
      "usage_count": 5,
      "avg_profit": 1500.00,
      "win_rate": 0.80
    }
  ]
}

Usage:
- Display strategy library
- Filter by type/strategy
```

### 6.6 POST /api/v1/strategies/{strategy_id}/backtest
**Backtest Strategy**

```
Method: POST
Path: /api/v1/strategies/{strategy_id}/backtest
Authentication: Bearer token

Request:
{
  "from_date": "2025-10-01",
  "to_date": "2025-12-12",
  "initial_capital": 100000,
  "position_size": 50000
}

Response (200 OK):
{
  "strategy_id": "strat_001",
  "backtest_period": "2025-10-01 to 2025-12-12",
  "results": {
    "total_trades": 25,
    "winning_trades": 18,
    "losing_trades": 7,
    "win_rate": 0.72,
    "profit_factor": 1.5,
    "total_return": 15.5,
    "annual_return": 62.0,
    "max_drawdown": -8.3,
    "sharpe_ratio": 1.2,
    "sortino_ratio": 1.8,
    "calmar_ratio": 7.5,
    "avg_trade_profit": 620.00,
    "avg_trade_loss": -414.29,
    "largest_win": 5000.00,
    "largest_loss": -2900.00,
    "consecutive_wins": 7,
    "consecutive_losses": 3
  },
  "equity_curve": [
    {"date": "2025-10-01", "capital": 100000.00},
    {"date": "2025-10-02", "capital": 101500.00},
    ...
    {"date": "2025-12-12", "capital": 115500.00}
  ],
  "monthly_returns": {
    "2025-10": 5.2,
    "2025-11": 6.8,
    "2025-12": 3.5
  }
}

Usage:
- Validate strategy before use
- Show historical performance
- Build confidence in strategy
```

---

## 7. WEBHOOKS & WEBSOCKET APIs

### 7.1 WebSocket: wss://ws.upstox.com
**Real-time Data Streaming**

```
Base URL: wss://ws.upstox.com

Subscribe Message:
{
  "guid": "unique_client_id",
  "method": "sub",
  "data": {
    "mode": "quote",
    "instrumentTokens": ["12345", "67890"]
  }
}

Unsubscribe Message:
{
  "guid": "unique_client_id",
  "method": "unsub",
  "data": {
    "mode": "quote",
    "instrumentTokens": ["12345"]
  }
}

Received Data (Quote):
{
  "type": "quote",
  "data": {
    "instrument_token": "12345",
    "ltp": 542.50,
    "bid": 542.40,
    "ask": 542.60,
    "bid_qty": 1000,
    "ask_qty": 1500,
    "volume": 5000000,
    "oi": 0,
    "iv": 18.5,
    "change": 2.50,
    "change_percent": 0.46,
    "timestamp": "2025-12-12T15:30:00Z"
  }
}

Received Data (Order Update):
{
  "type": "order_update",
  "data": {
    "order_id": "ord_12345",
    "status": "COMPLETE",
    "filled_quantity": 1,
    "filled_price": 542.60,
    "timestamp": "2025-12-12T15:30:05Z"
  }
}

Max Subscriptions: 100 per WebSocket connection
Ping/Pong: Every 30 seconds
Reconnection: Exponential backoff (1s, 2s, 4s, 8s max)

Usage:
- Live price updates for charts
- Real-time P&L calculation
- Order status updates
- Greeks updates for options
```

### 7.2 POST /api/v1/webhooks/subscribe
**Subscribe to Events**

```
Method: POST
Path: /api/v1/webhooks/subscribe
Authentication: Bearer token

Request:
{
  "event_type": "ORDER_UPDATE",
  "webhook_url": "https://your-app.com/webhooks/orders",
  "events": ["ORDER_PLACED", "ORDER_EXECUTED", "ORDER_CANCELLED"],
  "retry_count": 3,
  "timeout_seconds": 10
}

Response (200 OK):
{
  "webhook_id": "webhook_uuid",
  "event_type": "ORDER_UPDATE",
  "webhook_url": "https://your-app.com/webhooks/orders",
  "status": "active",
  "created_at": "2025-12-12T15:30:00Z"
}

Webhook Payload:
{
  "webhook_id": "webhook_uuid",
  "event_type": "ORDER_UPDATE",
  "timestamp": "2025-12-12T15:30:05Z",
  "data": {
    "order_id": "ord_12345",
    "status": "COMPLETE",
    "filled_quantity": 1,
    "filled_price": 542.60
  },
  "retry_count": 0
}

Usage:
- Real-time order updates
- Portfolio change notifications
- Price alert triggers
```

---

## 8. SETTINGS & CONFIGURATION APIs

### 8.1 GET /api/v1/settings
**Fetch User Settings**

```
Method: GET
Path: /api/v1/settings
Authentication: Bearer token

Response (200 OK):
{
  "general": {
    "theme": "dark",
    "language": "en",
    "currency": "INR",
    "time_zone": "IST"
  },
  "trading": {
    "default_segment": "DERIVATIVES",
    "default_product": "MIS",
    "auto_confirm_orders": false,
    "risk_per_trade_percent": 1.5,
    "max_position_size": 100000,
    "max_daily_loss_percent": -5.0
  },
  "notifications": {
    "price_alerts": true,
    "order_updates": true,
    "ai_recommendations": true,
    "push_enabled": true,
    "email_enabled": false
  },
  "ai_config": {
    "auto_strategy_generation": false,
    "ai_confidence_threshold": 0.75,
    "preferred_indicators": ["RSI", "MACD", "EMA"],
    "preferred_strategies": ["IRON_CONDOR", "CALL_SPREAD"],
    "auto_exit_on_profit_percent": 50,
    "auto_exit_on_loss_percent": -2
  }
}

Usage:
- Load settings on app initialization
- Display on settings page
```

### 8.2 PUT /api/v1/settings
**Update User Settings**

```
Method: PUT
Path: /api/v1/settings
Authentication: Bearer token

Request:
{
  "general": {
    "theme": "light",
    "language": "hi"
  },
  "trading": {
    "risk_per_trade_percent": 2.0
  }
}

Response (200 OK):
{
  "status": "success",
  "message": "Settings updated successfully"
}

Usage:
- Save user preferences
- Called from settings page
```

---

## 9. TECHNICAL INDICATORS APIs

### 9.1 POST /api/v1/indicators/calculate
**Calculate Technical Indicators**

```
Method: POST
Path: /api/v1/indicators/calculate
Authentication: Bearer token

Request:
{
  "instrument_token": "12345",
  "indicators": ["RSI", "MACD", "BB", "EMA", "SMA"],
  "periods": {
    "RSI": 14,
    "EMA": [20, 50],
    "SMA": [50, 200],
    "BB": 20
  },
  "timeframe": "5m"
}

Response (200 OK):
{
  "instrument_token": "12345",
  "timestamp": "2025-12-12T15:30:00Z",
  "indicators": {
    "RSI": 65.5,
    "MACD": {
      "macd": 0.85,
      "signal": 0.72,
      "histogram": 0.13,
      "trend": "BULLISH"
    },
    "Bollinger_Bands": {
      "upper": 550.00,
      "middle": 540.00,
      "lower": 530.00,
      "position": "above_middle"
    },
    "EMA": {
      "20": 541.50,
      "50": 540.00
    },
    "SMA": {
      "50": 540.00,
      "200": 535.00
    }
  }
}

Usage:
- Display indicators on chart
- Calculate for analysis
- Input for AI strategy generation
```

---

## 10. ERROR HANDLING & RATE LIMITS

### Error Response Format
```json
{
  "error": "error_code",
  "message": "Human-readable error message",
  "details": {
    "field": "order_price",
    "reason": "Price exceeds limit up/down"
  },
  "timestamp": "2025-12-12T15:30:00Z",
  "request_id": "req_uuid"
}
```

### Common Error Codes
```
400 Bad Request: Invalid parameters
401 Unauthorized: Token expired or invalid
403 Forbidden: Insufficient permissions
404 Not Found: Resource not found
429 Too Many Requests: Rate limit exceeded
500 Internal Server Error: Server error
503 Service Unavailable: Maintenance
```

### Rate Limits
```
Authentication Endpoints: 10 req/min per IP
Market Data Endpoints: 100 req/min per token
Orders Endpoints: 50 req/min per token
WebSocket: Unlimited (bandwidth throttled)
Webhooks: 1000 deliveries/hour per webhook
```

### Retry Strategy
```
HTTP 429: Exponential backoff (1s, 2s, 4s, 8s, 16s)
HTTP 503: Retry after 60 seconds
HTTP 5xx: Exponential backoff max 5 retries
WebSocket: Auto-reconnect with backoff
```

---

## INTEGRATION CHECKLIST

✅ Implement all 46 endpoints
✅ Setup WebSocket connection handler
✅ Create error handling middleware
✅ Implement rate limiting
✅ Setup request/response logging
✅ Create authentication guards
✅ Build caching strategy
✅ Implement retry logic
✅ Setup monitoring/alerting
✅ Create API documentation
✅ Perform load testing
✅ Security audit

---

**Last Updated**: 2025-12-12
**API Version**: v1.0
**Status**: Production Ready

