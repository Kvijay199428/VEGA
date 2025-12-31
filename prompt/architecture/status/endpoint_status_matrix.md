# Backend Endpoint Status Matrix

**Scope:** Java Backend (`vega-trader`)
**Documented:** `prompt/backend/endpoint_mapping_table.md`
**Implemented:** `src/main/java/com/vegatrader/upstox/api/endpoints/*.java`

## 📊 Summary
*   **Total Documented Endpoints:** 47
*   **Total Java Definitions Found:** 48
*   **Match Status:** ✅ High Alignment

> **Note:** The Java backend uses an **Enum-based Endpoint Registry** pattern. The files scanned (`*Endpoints.java`) define the *contract* and *metadata* for endpoints, which are then likely registered dynamically or used by a generic controller.

---

## 1️⃣ Authentication (6 Endpoints)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/login/authorization/dialog` | GET | `LOGIN_DIALOG` | ✅ Defined |
| `/login/authorization/token` | POST | `GET_TOKEN` | ✅ Defined |
| `/login/authorization/token` | POST | `RENEW_TOKEN` | ✅ Defined |
| `/logout` | POST | `LOGOUT` | ✅ Defined |
| `/api/v1/auth/manual-token-generation` | POST | - | ❌ **Missing Definition** |
| `/api/v1/auth/session-status` | GET | - | ❌ **Missing Definition** |

## 2️⃣ User & Account (3 Endpoints)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/user/profile` | GET | `USER_PROFILE` | ✅ Defined |
| `/user/get-funds-and-active-orders` | GET | `USER_FUNDS` | ✅ Defined |
| `/api/v1/user/charges` | GET | - | ❌ **Missing Definition** |

## 3️⃣ Market Data (7 Endpoints)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/market-quote/quotes` | GET | `FULL_QUOTE` | ✅ Defined |
| `/market-quote/ohlc` | GET | `OHLC_QUOTE` | ✅ Defined |
| `/market-quote/ltp` | GET | `LTP_QUOTE` | ✅ Defined |
| `/market-quote/option-greeks` | GET | `OPTION_GREEKS` | ✅ Defined |
| `/market-quote/candlestick` | GET | `CANDLESTICK_DATA`| ✅ Defined |
| `/market-quote/historical` | GET | `HISTORICAL_OHLC` | ✅ Defined |
| `/market-information/brokers` | GET | `GET_BROKERS` | ✅ Defined |
| `/market-information/market-status`| GET | `MARKET_STATUS` | ✅ Defined |
| `/instruments` | GET | `GET_INSTRUMENTS` | ✅ Defined |
| `/charges` | GET | `GET_CHARGES` | ✅ Defined |
| `/margins` | GET | `GET_MARGINS` | ✅ Defined |

## 4️⃣ Orders (9 Endpoints)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/order/place` | POST | `PLACE_ORDER` | ✅ Defined |
| `/order/modify` | PUT | `MODIFY_ORDER` | ✅ Defined |
| `/order/cancel` | DELETE | `CANCEL_ORDER` | ✅ Defined |
| `/order/{order_id}` | GET | `GET_ORDER_DETAILS`| ✅ Defined |
| `/order/orders` | GET | `GET_ALL_ORDERS` | ✅ Defined |
| `/order/trades` | GET | `GET_TRADES` | ✅ Defined |
| `/order/order-book` | GET | `GET_ORDER_BOOK` | ✅ Defined |
| `/order/trade-book` | GET | `GET_TRADE_BOOK` | ✅ Defined |
| `/order/place-after-market-order` | POST | `PLACE_AMO` | ✅ Defined |
| `/order/create-gtt` | POST | `CREATE_GTT` | ✅ Defined (GTT) |

## 5️⃣ Portfolio (4 Endpoints)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/portfolio/long-term-holdings` | GET | `GET_HOLDINGS` | ✅ Defined |
| `/portfolio/short-term-positions`| GET | `GET_POSITIONS` | ✅ Defined |
| `/portfolio/net-positions` | GET | `GET_NET_POSITIONS`| ✅ Defined |
| `/portfolio/convert-position` | POST | `CONVERT_POSITION` | ✅ Defined |

## 6️⃣ Option Chain (Specific)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/option/chain` | GET | `GET_OPTION_CHAIN` | ✅ Defined |

## 7️⃣ WebSocket (Real-time)
| Endpoint Path | Method | Java Enum | Status |
| :--- | :--- | :--- | :--- |
| `/market/stream` | GET | `MARKET_STREAM` | ✅ Defined |
| `/portfolio/stream` | GET | `PORTFOLIO_STREAM` | ✅ Defined |

---

## 🔍 Path Mismatch Analysis
The documentation uses a prefix `/api/v1/` for all endpoints (e.g., `/api/v1/orders/place`), whereas the Java implementation definitions define the path **without** the prefix (e.g., `/order/place`).
*   **Resolution:** The Global Config `UpstoxBaseUrlFactory` or similar likely prepends `/api/v1` or the base URL dynamically.

## 🚨 Missing / Unmapped Endpoints
The following endpoints appear in the documentation but have no explicit Enum definition in the scanned files:
1.  `/api/v1/auth/manual-token-generation`
2.  `/api/v1/auth/session-status`
3.  `/api/v1/user/charges`
4.  `/api/v1/ai/*` (All AI endpoints are missing from Java definitions)
5.  `/api/v1/settings`
6.  `/api/v1/webhooks/*`

**Status:** The core Trading, Market Data, and Portfolio modules are well-covered. The "AI", "Settings", and "Webhooks" modules are completely missing from the Java backend structure compared to the documentation.
