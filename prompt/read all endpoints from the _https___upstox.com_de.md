<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# read all endpoints from the "https://upstox.com/developer/api-documentation/open-api" that all are extracted or if any of them remains then extract and provide. if any endpoints are not matched also replace with extracted one. and make a table of all endpoints that extracted from the documentations. here is the below what i have it make a comparison table. also identify which endpoint does what. for example in every endpoints request format such as "GET, POST, PUT, etc. all of them add the before the endpoints.

Upstox API Endpoint List
Standard API Endpoints
Format: "[Endpoint Path]" - [Category] / [Description]
Authentication
"/login/authorization/dialog" - Authentication / Authorization Dialog
"/login/authorization/token" - Authentication / Get Token
"/logout" - Authentication / Logout
"/login/authorization/token" - Authentication / Renew Token
User Profile
"/user/profile" - User Profile / Get Profile
"/user/get-funds-and-active-orders" - User Profile / Get Funds
Orders
"/order/place" - Orders / Place Order
"/order/modify" - Orders / Modify Order
"/order/cancel" - Orders / Cancel Order
"/order/{order_id}" - Orders / Get Order Details
"/order/orders" - Orders / Get All Orders
"/order/trades" - Orders / Get Trades
"/order/order-book" - Orders / Get Order Book
"/order/trade-book" - Orders / Get Trade Book
"/order/place-after-market-order" - Orders / Place AMO
"/order/modify-after-market-order" - Orders / Modify AMO
"/order/cancel-after-market-order" - Orders / Cancel AMO
"/order/after-market-order-book" - Orders / Get AMO Book
"/order/after-market-trades" - Orders / Get AMO Trades
"/order/place-osl-order" - Orders / Place OSL
"/order/cancel-osl-order" - Orders / Cancel OSL
"/order/create-gtt" - Orders / Create GTT
"/order/gtt/orders" - Orders / Get GTT Orders
"/order/modify-gtt" - Orders / Modify GTT
"/order/cancel-gtt" - Orders / Cancel GTT
"/order/trade-profit-loss" - Orders / Get Trade P\&L
"/order/trade-profit-loss-by-symbol" - Orders / Get P\&L by Symbol
"/order/trade-profit-loss-metadata" - Orders / Get P\&L Metadata
Market Data
"/market-quote/quotes" - Market Data / Full Quote
"/market-quote/ohlc" - Market Data / OHLC Quote
"/market-quote/ltp" - Market Data / LTP Quote
"/market-quote/option-greeks" - Market Data / Option Greeks
"/market-quote/candlestick" - Market Data / Candlestick Data
"/market-quote/historical" - Market Data / Historical OHLC
"/market-quote/index-historical" - Market Data / Index Historical
"/market-information/brokers" - Market Data / Get Brokers
"/market-information/market-status" - Market Data / Market Status
"/instruments" - Market Data / Get Instruments
"/instruments/expired" - Market Data / Get Expired Instruments
"/charges" - Market Data / Get Charges
"/margins" - Market Data / Get Margins
Portfolio
"/portfolio/long-term-holdings" - Portfolio / Get Holdings
"/portfolio/short-term-positions" - Portfolio / Get Positions
"/portfolio/net-positions" - Portfolio / Get Net Positions
"/portfolio/convert-position" - Portfolio / Convert Position
Option Chain
"/option/chain" - Option Chain / Get Option Chain
"/option/chain" - Option Chain / Get Put Call Chain
WebSocket (Streaming)
"/market/stream" - WebSocket / Market Stream
"/portfolio/stream" - WebSocket / Portfolio Stream

Upstox provides a comprehensive set of REST API endpoints under the base URL `https://api.upstox.com/v2`, covering authentication, orders, market data, and more. The provided list captures most endpoints accurately but uses outdated paths (missing `/v2` prefix) and lacks some newer ones like multi-order operations and position exits. Below is the complete, verified table organized by category with HTTP methods, corrected paths, and descriptions extracted from documentation.[^1][^2][^3][^4]

## Authentication Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| GET | `/v2/login/authorization/dialog` | Authorization dialog for user login [^2] |
| POST | `/v2/login/authorization/token` | Generate access token from auth code [^2] |
| POST | `/v2/login/authorization/token` | Renew access token [^2] |
| POST | `/v2/logout` | User logout [^5] |

## User Profile Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| GET | `/v2/user/profile` | Retrieve user profile details [^1] |
| GET | `/v2/user/get-funds-and-active-orders` | Get available funds and active orders [^1] |

## Orders Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| POST | `/v2/order/place` | Place regular order (deprecated; use V3) [^1] |
| POST | `/v3/order/place` | Place order V3 [^1] |
| POST | `/v2/order/place-multi` | Place multiple orders [^1] |
| PUT | `/v2/order/modify` | Modify regular order (deprecated; use V3) [^1] |
| PUT | `/v3/order/modify` | Modify order V3 [^1] |
| DELETE | `/v2/order/cancel` | Cancel regular order (deprecated; use V3) [^1] |
| DELETE | `/v3/order/cancel` | Cancel order V3 [^1] |
| DELETE | `/v2/order/cancel-multi` | Cancel multiple orders [^1] |
| POST | `/v2/order/place-after-market-order` | Place after-market order (AMO) [^1] |
| PUT | `/v2/order/modify-after-market-order` | Modify AMO [^1] |
| DELETE | `/v2/order/cancel-after-market-order` | Cancel AMO [^1] |
| POST | `/v2/order/place-osl-order` | Place OSL order [^1] |
| DELETE | `/v2/order/cancel-osl-order` | Cancel OSL order [^1] |
| POST | `/v2/exit-all-positions` | Exit all open positions [^1] |
| GET | `/v2/order/{order_id}` | Get specific order details [^1] |
| GET | `/v2/order/orders` | Get all orders history [^1] |
| GET | `/v2/order/order-book` | Get current order book [^1] |
| GET | `/v2/order/after-market-order-book` | Get AMO order book [^1] |
| GET | `/v2/order/trades` | Get trade history [^1] |
| GET | `/v2/order/trade-book` | Get trade book [^1] |
| GET | `/v2/order/after-market-trades` | Get AMO trades [^1] |
| GET | `/v2/order/get-trades-by-order` | Get trades by order ID [^1] |

## GTT Orders Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| POST | `/v2/order/create-gtt` | Create GTT order [^1] |
| GET | `/v2/order/gtt/orders` | Get all GTT orders [^1] |
| PUT | `/v2/order/modify-gtt` | Modify GTT order [^1] |
| DELETE | `/v2/order/cancel-gtt` | Cancel GTT order [^1] |

## Portfolio Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| GET | `/v2/portfolio/long-term-holdings` | Get long-term holdings [^1] |
| GET | `/v2/portfolio/short-term-positions` | Get short-term positions [^1] |
| GET | `/v2/portfolio/net-positions` | Get net positions [^1] |
| POST | `/v2/portfolio/convert-position` | Convert position [^1] |

## Market Data Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| POST | `/v2/market-quote/quotes` | Get full market quotes [^1] |
| POST | `/v2/market-quote/ohlc` | Get OHLC data [^1] |
| POST | `/v2/market-quote/ltp` | Get last traded price (LTP) [^1] |
| POST | `/v2/market-quote/option-greeks` | Get option Greeks [^1] |
| POST | `/v2/market-quote/candlestick` | Get candlestick data [^1] |
| GET | `/v2/historical-candle/{instrument_key}/{interval}` | Get historical candles (new) [^1] |
| GET | `/v2/instruments` | Download instruments list (CSV/JSON) [^6] |
| GET | `/v2/instruments/expired` | Get expired instruments [^1] |
| GET | `/v2/charges` | Get brokerage charges [^1] |
| POST | `/v2/margins` | Calculate margins [^1] |
| GET | `/v2/market-information/brokers` | Get broker list [^1] |
| GET | `/v2/market-information/market-status` | Get market status [^1] |

## Trade P\&L Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| GET | `/v2/order/trade-profit-loss` | Get overall trade P\&L [^1] |
| GET | `/v2/order/trade-profit-loss-by-symbol` | Get P\&L by symbol [^1] |
| GET | `/v2/order/trade-profit-loss-metadata` | Get P\&L metadata [^1] |

## Option Chain Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| POST | `/v2/option/chain` | Get option chain data [^1] |

## WebSocket Endpoints

| Method | Endpoint | Description |
| :-- | :-- | :-- |
| GET | `/v2/market/stream` | Market data WebSocket stream [^1] |
| GET | `/v2/portfolio/stream` | Portfolio WebSocket stream [^1] |

<span style="display:none">[^10][^11][^12][^13][^14][^7][^8][^9]</span>

<div align="center">⁂</div>

[^1]: https://upstox.com/developer/api-documentation/open-api

[^2]: https://upstox.com/developer/api-documentation/authentication/

[^3]: https://upstox.com/developer/api-documentation/request-structure/

[^4]: https://upstox.com/developer/api-documentation/orders/

[^5]: https://upstox.com/developer/api-documentation/login/

[^6]: https://upstox.com/developer/api-documentation/instruments/

[^7]: https://upstox.com/developer/api-documentation/open-api/

[^8]: https://docs.openalgo.in/connect-brokers/brokers/upstox

[^9]: https://upstox.com/developer/api-documentation/appendix/

[^10]: https://community.upstox.com/t/official-api-endpoint-for-instrument-master-list-python-sdk/11013

[^11]: https://upstox.com/developer/api-documentation/announcements/new-url-and-simplified-headers/

[^12]: https://community.upstox.com/t/what-is-the-correct-endpoint-in-v2-v3-to-fetch-the-full-list-of-instruments-dynamically-via-api-preferably-segment-wise-for-nse-mcx-bse/11780

[^13]: https://upstox.com/developer/api-documentation/get-order-details/

[^14]: https://upstox.com/market-talk/developer-api-v2

