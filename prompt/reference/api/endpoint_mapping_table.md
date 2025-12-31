# 📊 COMPLETE ENDPOINT MAPPING TABLE

## ALL APPLICATION ENDPOINTS WITH UPSTOX API MAPPING

This document provides a comprehensive table of all 46+ endpoints in your trading platform, organized by category, with direct mapping to Upstox API endpoints and usage details.

---

## TABLE 1: AUTHENTICATION ENDPOINTS (6 Total)

| # | Your Endpoint | Method | Purpose | Upstox API Used | Response | Status |
|---|---|---|---|---|---|---|
| 1 | `/api/v1/auth/login` | POST | OAuth login initiation | `https://api.upstox.com/authorize` (OAuth) | 302 Redirect | ✅ |
| 2 | `/api/v1/auth/callback` | GET | Handle OAuth callback | OAuth token exchange | 200 OK + JWT | ✅ |
| 3 | `/api/v1/auth/refresh-token` | POST | Refresh access token | Token refresh endpoint | 200 OK + new token | ✅ |
| 4 | `/api/v1/auth/manual-token-generation` | POST | Manual token generation | Direct token API | 200 OK + token | ✅ |
| 5 | `/api/v1/auth/session-status` | GET | Check session validity | None (internal) | 200 OK + status | ✅ |
| 6 | `/api/v1/auth/logout` | POST | Logout & invalidate session | None (internal) | 200 OK | ✅ |

---

## TABLE 2: USER & ACCOUNT ENDPOINTS (3 Total)

| # | Your Endpoint | Method | Purpose | Upstox API Used | Upstox Method | Response |
|---|---|---|---|---|---|---|
| 1 | `/api/v1/user/profile` | GET | Fetch user profile | `GET /user/profile` | REST | User object |
| 2 | `/api/v1/user/margins` | GET | Fetch margin details | `GET /user/get-margin` | REST | Margin object |
| 3 | `/api/v1/user/charges` | GET | Fetch brokerage charges | `GET /user/get-charges` | REST | Charges object |

---

## TABLE 3: INSTRUMENTS & SEARCH ENDPOINTS (7 Total)

| # | Your Endpoint | Method | Purpose | Upstox API Used | Upstox Method | Notes |
|---|---|---|---|---|---|---|
| 1 | `/api/v1/instruments/all` | GET | All instruments | `GET /market/instruments` | REST | Returns 5000+ instruments |
| 2 | `/api/v1/instruments/search` | GET | Search instruments | Client-side filter on `instruments/all` | Internal | Debounced autocomplete |
| 3 | `/api/v1/instruments/options-chain` | GET | Options chain | `GET /market/instruments` + filter | REST | Filters by underlying + expiry |
| 4 | `/api/v1/market/quote/{token}` | GET | Single live quote | `GET /market/quote` | REST | Single instrument |
| 5 | `/api/v1/market/quotes` | POST | Multiple quotes | `GET /market/quote` (batched) | REST | Up to 100 tokens |
| 6 | `/api/v1/market/ohlc/{token}` | GET | OHLC historical data | `GET /market/historical-data/intraday` | REST | Stored in TimescaleDB |
| 7 | `/api/v1/market/historical` | GET | Historical + indicators | TimescaleDB + calculation | Internal | With technical indicators |

---

## TABLE 4: ORDERS & TRADING ENDPOINTS (9 Total)

| # | Your Endpoint | Method | Purpose | Upstox API Used | Upstox Method | Upstox Request Format |
|---|---|---|---|---|---|---|
| 1 | `/api/v1/orders/place` | POST | Place single order | `POST /order/place` | REST | Order object with symbol, qty, price |
| 2 | `/api/v1/orders` | GET | Fetch user orders | `GET /order/retrieve-all` | REST | Query params: status, date range |
| 3 | `/api/v1/orders/{order_id}` | GET | Get order detail | `GET /order/retrieve-all` + filter | REST | Filter by order_id |
| 4 | `/api/v1/orders/{order_id}` | PUT | Modify open order | `PUT /order/update` | REST | Updated order params |
| 5 | `/api/v1/orders/{order_id}` | DELETE | Cancel order | `DELETE /order/cancel` | REST | Order ID only |
| 6 | `/api/v1/orders/{order_id}/trades` | GET | Get order executions | `GET /order/retrieve-all` + expand | REST | Includes execution details |
| 7 | `/api/v1/orders/bulk` | POST | Place multiple orders | `POST /order/place` (batched) | REST | Array of order objects |
| 8 | `/api/v1/gtt/create` | POST | Create GTT order | `POST /order/place-gtt` | REST | Trigger + order params |
| 9 | `/api/v1/gtt` | GET | Fetch GTT orders | `GET /order/retrieve-all-gtt` | REST | All GTT orders for user |

---

## TABLE 5: PORTFOLIO ENDPOINTS (4 Total)

| # | Your Endpoint | Method | Purpose | Upstox API Used | Upstox Method | Notes |
|---|---|---|---|---|---|---|
| 1 | `/api/v1/portfolio/holdings` | GET | Delivery holdings | `GET /portfolio/long-stock` | REST | CNC positions only |
| 2 | `/api/v1/portfolio/positions` | GET | Intraday positions | `GET /portfolio/net-positions` | REST | MIS/NRML positions |
| 3 | `/api/v1/portfolio/net-worth` | GET | Net worth calculation | `/portfolio/*` (combined) | REST | Calculated from holdings + positions |
| 4 | `/api/v1/portfolio/pnl` | GET | P&L breakdown | `/portfolio/net-positions` + orders | REST | Calculated from executed trades |

---

## TABLE 6: AI & STRATEGY ENDPOINTS (7 Total)

| # | Your Endpoint | Method | Purpose | External API | Input Data Sources | Notes |
|---|---|---|---|---|---|---|
| 1 | `/api/v1/ai/analyze-contract` | POST | AI contract analysis | Claude 3 Opus / GPT-4 | Live quote + indicators | Technical analysis signal |
| 2 | `/api/v1/ai/generate-strategy` | POST | Generate trading strategy | Claude 3 Opus / GPT-4 | Live + historical + option chain | Returns multi-leg strategy |
| 3 | `/api/v1/ai/place-order-from-strategy` | POST | Execute AI strategy | `/api/v1/orders/bulk` | Generated strategy legs | Bulk order placement |
| 4 | `/api/v1/strategies/create` | POST | Save user strategy | Internal database | User inputs | Custom strategy storage |
| 5 | `/api/v1/strategies` | GET | Fetch strategies | Internal database | N/A | User + predefined strategies |
| 6 | `/api/v1/strategies/{id}/backtest` | POST | Backtest strategy | TimescaleDB + calculation | Historical OHLCV data | Performance validation |
| 7 | `/api/v1/indicators/calculate` | POST | Calculate indicators | TA-Lib (internal) | Historical OHLCV data | RSI, MACD, BB, EMA, SMA |

---

## TABLE 7: WEBSOCKET ENDPOINTS (Real-time Data)

| # | Your Endpoint | Purpose | Upstox API | Protocol | Max Subscriptions | Update Frequency |
|---|---|---|---|---|---|---|
| 1 | `WSS wss://ws.upstox.com` | Live price quotes | WebSocket (native) | Binary + JSON | 100 per connection | 1-5 seconds |
| 2 | `WSS wss://ws.upstox.com` | Order updates | WebSocket (native) | Binary + JSON | Unlimited | Real-time |
| 3 | `WSS wss://ws.upstox.com` | Portfolio updates | WebSocket (native) | Binary + JSON | Unlimited | Real-time |
| 4 | `WSS wss://ws.upstox.com` | Option chain updates | WebSocket (native) | Binary + JSON | 100 per connection | 1-5 seconds |

---

## TABLE 8: WEBHOOKS & CALLBACKS (4 Total)

| # | Your Endpoint | Method | Purpose | Trigger | Payload |
|---|---|---|---|---|---|
| 1 | `/api/v1/webhooks/subscribe` | POST | Subscribe to events | Manual setup | Event config |
| 2 | `/api/v1/webhooks` | GET | List subscriptions | Manual query | Webhook list |
| 3 | `/api/v1/webhooks/{id}` | DELETE | Unsubscribe | Manual delete | Deletion status |
| 4 | `/webhook/incoming` (external) | POST | Receive webhook | Upstox triggers | Order/trade update |

---

## TABLE 9: SETTINGS ENDPOINTS (2 Total)

| # | Your Endpoint | Method | Purpose | Storage | Scope |
|---|---|---|---|---|---|
| 1 | `/api/v1/settings` | GET | Fetch user settings | PostgreSQL | User-specific |
| 2 | `/api/v1/settings` | PUT | Update settings | PostgreSQL | User-specific |

---

## TABLE 10: TECHNICAL INDICATORS ENDPOINT (1 Total)

| # | Your Endpoint | Method | Purpose | Library Used | Indicators |
|---|---|---|---|---|---|
| 1 | `/api/v1/indicators/calculate` | POST | Calculate indicators | TA-Lib | RSI, MACD, BB, EMA, SMA |

---

## DETAILED UPSTOX API MAPPING

### MAPPING: Your Endpoints → Upstox Official APIs

#### AUTHENTICATION
```
Your /api/v1/auth/login
    ↓
Upstox: https://api.upstox.com/authorize
Method: GET
Params: client_id, redirect_uri, scope

Your /api/v1/auth/callback
    ↓
Upstox: https://api.upstox.com/oauth/token
Method: POST
Params: code, grant_type=authorization_code
```

#### USER DATA
```
Your /api/v1/user/profile
    ↓
Upstox: GET /user/profile
Headers: Authorization: Bearer {access_token}
Response: User details

Your /api/v1/user/margins
    ↓
Upstox: GET /user/get-margin
Headers: Authorization: Bearer {access_token}
Response: Margin details

Your /api/v1/user/charges
    ↓
Upstox: GET /user/get-charges
Headers: Authorization: Bearer {access_token}
Response: Brokerage charges
```

#### MARKET DATA
```
Your /api/v1/instruments/all
    ↓
Upstox: GET /market/instruments?exchange=NSE,BSE,MCX,NCDEX
Headers: Authorization: Bearer {access_token}
Response: Array of instruments (cached 1 hour)

Your /api/v1/market/quote/{token}
    ↓
Upstox: GET /market/quote?mode=full&instrumentToken={token}
Headers: Authorization: Bearer {access_token}
Response: Quote object

Your /api/v1/market/quotes (bulk)
    ↓
Upstox: GET /market/quote?mode=full&instrumentTokens={token1},{token2},...
Headers: Authorization: Bearer {access_token}
Response: Array of quotes

Your /api/v1/market/ohlc/{token}
    ↓
Upstox: GET /market/historical-data/intraday?instrumentToken={token}&interval={interval}
Headers: Authorization: Bearer {access_token}
Response: OHLCV candles (stored in TimescaleDB)
```

#### ORDERS
```
Your /api/v1/orders/place
    ↓
Upstox: POST /order/place
Headers: Authorization: Bearer {access_token}
Body: {
  "quantity": 1,
  "price": 542.50,
  "product": "MIS",
  "order_type": "REGULAR",
  "transaction_type": "BUY",
  "instrument_token": "12345"
}

Your /api/v1/orders (get all)
    ↓
Upstox: GET /order/retrieve-all?status=open
Headers: Authorization: Bearer {access_token}
Response: Array of orders

Your /api/v1/orders/{order_id}
    ↓
Upstox: GET /order/retrieve-all (filter by ID locally)
Headers: Authorization: Bearer {access_token}
Response: Single order detail

Your /api/v1/orders/{order_id} (update)
    ↓
Upstox: PUT /order/update
Headers: Authorization: Bearer {access_token}
Body: Updated order parameters

Your /api/v1/orders/{order_id} (delete/cancel)
    ↓
Upstox: DELETE /order/cancel
Headers: Authorization: Bearer {access_token}
Body: {"order_id": "..."}
```

#### PORTFOLIO
```
Your /api/v1/portfolio/holdings
    ↓
Upstox: GET /portfolio/long-stock
Headers: Authorization: Bearer {access_token}
Response: Delivery holdings array

Your /api/v1/portfolio/positions
    ↓
Upstox: GET /portfolio/net-positions
Headers: Authorization: Bearer {access_token}
Response: Intraday/derivatives positions

Your /api/v1/portfolio/net-worth
    ↓
Upstox: Combined calls to:
  - GET /user/get-margin
  - GET /portfolio/long-stock
  - GET /portfolio/net-positions
Response: Calculated net worth object
```

#### GTT (Good-Till-Triggered)
```
Your /api/v1/gtt/create
    ↓
Upstox: POST /order/place-gtt
Headers: Authorization: Bearer {access_token}
Body: {
  "trigger_price": 540.00,
  "last_price": 542.50,
  "orders": [...]
}

Your /api/v1/gtt (get all)
    ↓
Upstox: GET /order/retrieve-all-gtt
Headers: Authorization: Bearer {access_token}
Response: Array of GTT orders
```

---

## SUMMARY TABLE: ENDPOINT COUNT BY CATEGORY

| Category | Count | Upstox APIs | Internal Only | WebSocket |
|----------|-------|-------------|---------------|-----------|
| Authentication | 6 | 2 | 4 | - |
| User Account | 3 | 3 | - | - |
| Instruments & Market Data | 7 | 5 | 2 | - |
| Orders & Trading | 9 | 7 | 2 | - |
| Portfolio Management | 4 | 3 | 1 | - |
| AI & Strategies | 7 | - | 7 | - |
| WebSocket Channels | 4 | - | - | 4 |
| Webhooks | 4 | 1 | 3 | - |
| Settings | 2 | - | 2 | - |
| Indicators | 1 | - | 1 | - |
| **TOTAL** | **47** | **21** | **22** | **4** |

---

## FLOW DIAGRAM: REQUEST JOURNEY

```
USER REQUEST
    ↓
YOUR ENDPOINT (/api/v1/orders/place)
    ↓
    ├─ AUTHENTICATION CHECK
    │  └─ Verify JWT token (internal)
    ├─ VALIDATION
    │  └─ Check parameters (internal)
    ├─ UPSTOX API CALL
    │  └─ POST https://api.upstox.com/order/place
    │     (if applicable)
    ├─ DATABASE OPERATION
    │  └─ Store in PostgreSQL/TimescaleDB
    ├─ WEBSOCKET UPDATE
    │  └─ Broadcast to connected clients (if applicable)
    └─ RESPONSE
       └─ Return to user (200, 400, 401, 429, 500, etc.)

RESPONSE DATA FLOW:
    ↓
Cache (Redis) if applicable
    ↓
Frontend (React) displays result
    ↓
WebSocket subscription receives updates
    ↓
Real-time UI updates
```

---

## ENDPOINT REQUEST/RESPONSE SUMMARY

### By Request Method

| Method | Count | Examples |
|--------|-------|----------|
| GET | 22 | `/api/v1/user/profile`, `/api/v1/portfolio/holdings` |
| POST | 18 | `/api/v1/orders/place`, `/api/v1/ai/generate-strategy` |
| PUT | 3 | `/api/v1/orders/{id}`, `/api/v1/settings` |
| DELETE | 3 | `/api/v1/orders/{id}`, `/api/v1/webhooks/{id}` |
| WebSocket | 4 | `WSS wss://ws.upstox.com` |

### By Response Type

| Response Type | Count | Examples |
|---------------|-------|----------|
| JSON Object | 35 | Most REST endpoints |
| JSON Array | 8 | `/api/v1/orders`, `/api/v1/strategies` |
| 302 Redirect | 1 | `/api/v1/auth/login` |
| WebSocket Stream | 4 | Live quotes, order updates |

---

## UPSTOX API AUTHENTICATION HEADERS

All Upstox API calls use:

```
Authorization: Bearer {access_token}
Content-Type: application/json
Accept: application/json
```

Token obtained from:
- `/api/v1/auth/callback` (OAuth)
- `/api/v1/auth/refresh-token` (Token refresh)
- `/api/v1/auth/manual-token-generation` (Manual)

Token Validity: 23 hours (3:15 AM - 2:30 AM IST)

---

## ENDPOINT DEPENDENCIES & PREREQUISITES

```
Login (auth/login) ← REQUIRED FOR ALL
    ↓
Get User Profile (user/profile) ← For dashboard
Get Margins (user/margins) ← For trading
    ↓
Get Instruments (instruments/all) ← For search/analysis
Get Quotes (market/quote) ← For prices
    ↓
Place Order (orders/place) ← Trading
Get Orders (orders) ← Order history
    ↓
Get Holdings (portfolio/holdings) ← Portfolio display
Get Positions (portfolio/positions) ← Position tracking
    ↓
AI Analysis (ai/analyze-contract) ← Optional: AI features
Generate Strategy (ai/generate-strategy) ← Optional: AI strategies
```

---

## REAL-TIME DATA ENDPOINTS (WebSocket Only)

```
Subscribe to Quote Channel:
{
  "guid": "client-1",
  "method": "sub",
  "data": {
    "mode": "quote",
    "instrumentTokens": ["12345", "67890"]
  }
}

Receive Updates:
{
  "type": "quote",
  "data": {
    "instrument_token": "12345",
    "ltp": 542.50,
    "bid": 542.40,
    "ask": 542.60,
    ...
  }
}
```

---

## RATE LIMITS BY ENDPOINT CATEGORY

| Category | Rate Limit | Window | Notes |
|----------|-----------|--------|-------|
| Authentication | 10 req/min | Per IP | Login attempts |
| User Data | 100 req/min | Per token | Profile, margins |
| Market Data | 100 req/min | Per token | Quotes, OHLC |
| Orders | 50 req/min | Per token | Place, modify, cancel |
| Portfolio | 50 req/min | Per token | Holdings, positions |
| Strategies | 20 req/min | Per token | Backtest, generate |
| WebSocket | Unlimited | - | Bandwidth limited |

---

## CACHING STRATEGY FOR UPSTOX DATA

| Data | Cache Duration | Endpoint | Strategy |
|------|----------------|----------|----------|
| Instruments | 1 hour | `/instruments/all` | Static list |
| Quotes | 1-5 min | `/market/quote` | Redis key per token |
| OHLCV Data | Permanent | `/market/ohlc` | TimescaleDB |
| User Profile | 5 min | `/user/profile` | Redis + TTL |
| Margins | 1 min | `/user/margins` | Redis + TTL |
| Holdings | 5 min | `/portfolio/holdings` | Redis + TTL |
| Positions | Real-time | WebSocket | No cache |
| Orders | 1 min | `/orders` | Redis + TTL |

---

## API VERSIONING

All endpoints follow: `/api/v1/` prefix

Current Version: **v1** (Stable)

Future versions will use: `/api/v2/`, `/api/v3/`, etc.

Backward compatibility maintained for 6 months after new version release.

---

## ERROR HANDLING ACROSS ALL ENDPOINTS

```
All endpoints return standardized error format:

{
  "error": "error_code",
  "message": "Human readable message",
  "details": {
    "field": "value",
    "reason": "explanation"
  },
  "timestamp": "2025-12-12T12:06:00Z",
  "request_id": "req_uuid"
}

Common Error Codes:
- 400: Bad Request (invalid params)
- 401: Unauthorized (token invalid/expired)
- 403: Forbidden (insufficient permissions)
- 404: Not Found (resource doesn't exist)
- 429: Rate Limited (too many requests)
- 500: Internal Server Error
- 503: Service Unavailable
```

---

## CONCLUSION

**Total Endpoints: 47+**
- 21 connected to Upstox APIs
- 22 internal only (database, calculations, AI)
- 4 WebSocket channels
- Real-time capabilities with auto-reconnect
- Comprehensive error handling
- Rate limiting protection
- Redis caching layer
- TimescaleDB for time-series data

All endpoints fully documented in `API-reference-guide.md`

---

**Created**: December 12, 2025
**Version**: 1.0
**Status**: Complete & Production Ready

