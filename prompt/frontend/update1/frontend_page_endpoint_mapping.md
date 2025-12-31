# 📖 FRONTEND DEVELOPMENT - PAGE TO ENDPOINT MAPPING REFERENCE

**For**: VEGA TRADER'S - AI-Enabled Trading Platform  
**Date**: December 13, 2025, 11:14 AM IST  
**Purpose**: Quick lookup for page-to-endpoint relationships

---

## 🗺️ COMPLETE MAPPING TABLE

```
┌─────────────────────────────────────────────────────────────────────┐
│              FRONTEND PAGE → BACKEND ENDPOINT MAPPING               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ PAGE 1: SETUP WIZARD                                               │
│ ├─ Step 1: Upstox API                                              │
│ │  └─ POST /api/v1/auth/login (test credentials)                  │
│ ├─ Step 2: Database Configuration                                 │
│ │  └─ PUT /api/v1/settings/general (save config)                  │
│ ├─ Step 3: AI LLM Configuration                                   │
│ │  └─ PUT /api/v1/settings/general (save config)                  │
│ ├─ Step 4: Theme & Style Selection                                │
│ │  └─ PUT /api/v1/settings/general (save preferences)             │
│ └─ Step 5: Review & Complete                                      │
│    └─ PUT /api/v1/user/account-settings (final save)              │
│                                                                     │
│ PAGE 2: DASHBOARD                                                  │
│ ├─ Market Overview Widget                                          │
│ │  ├─ GET /api/v1/market/indices                                  │
│ │  └─ GET /api/v1/market/quote?symbol=X                           │
│ ├─ Portfolio Summary Widget                                        │
│ │  └─ GET /api/v1/portfolio/summary                               │
│ ├─ Open Positions Widget                                           │
│ │  ├─ GET /api/v1/portfolio/positions                             │
│ │  └─ WS /ws/market/live-quotes (real-time updates)               │
│ ├─ Recent Trades Widget                                            │
│ │  └─ GET /api/v1/orders/trades                                   │
│ ├─ Portfolio Value Chart                                           │
│ │  └─ GET /api/v1/portfolio/snapshots?period=30days               │
│ └─ Quick Actions                                                   │
│    └─ Navigation links to other pages                              │
│                                                                     │
│ PAGE 3: MARKET DATA                                                │
│ ├─ Watchlist Widget                                                │
│ │  ├─ GET /api/v1/market/instruments                              │
│ │  ├─ GET /api/v1/market/quote?symbol=X                           │
│ │  └─ WS /ws/market/live-quotes (real-time quotes)                │
│ ├─ Market Heatmap Widget                                           │
│ │  └─ GET /api/v1/market/heatmap                                  │
│ ├─ Technical Chart Widget                                          │
│ │  └─ GET /api/v1/market/ohlc?symbol=X&interval=Y                 │
│ ├─ Order Book Widget                                               │
│ │  ├─ GET /api/v1/market/depth?symbol=X                           │
│ │  └─ WS /ws/market/depth (real-time updates)                     │
│ ├─ Market Indices Widget                                           │
│ │  └─ GET /api/v1/market/indices                                  │
│ └─ Live Quotes Grid Widget                                         │
│    ├─ GET /api/v1/market/instruments                              │
│    ├─ GET /api/v1/market/quote?symbol=X                           │
│    └─ WS /ws/market/live-quotes (real-time updates)               │
│                                                                     │
│ PAGE 4: TRADING                                                    │
│ ├─ Order Form                                                      │
│ │  ├─ GET /api/v1/market/quote?symbol=X (smart price)             │
│ │  └─ POST /api/v1/orders/place (submit order)                    │
│ ├─ Open Orders Table                                               │
│ │  ├─ GET /api/v1/orders (fetch open orders)                      │
│ │  ├─ PUT /api/v1/orders/{id} (modify order)                      │
│ │  ├─ POST /api/v1/orders/{id}/cancel (cancel order)              │
│ │  └─ WS /ws/market/orders (status updates)                       │
│ └─ Order History                                                   │
│    └─ GET /api/v1/orders/trades (closed orders)                   │
│                                                                     │
│ PAGE 5: PORTFOLIO                                                  │
│ ├─ Holdings Table                                                  │
│ │  ├─ GET /api/v1/portfolio/holdings                              │
│ │  └─ GET /api/v1/market/quote?symbol=X (current prices)          │
│ ├─ Positions Table                                                 │
│ │  ├─ GET /api/v1/portfolio/positions                             │
│ │  └─ WS /ws/market/live-quotes (price updates)                   │
│ ├─ Performance Chart                                               │
│ │  └─ GET /api/v1/portfolio/performance?period=X                  │
│ ├─ Asset Allocation Chart                                          │
│ │  └─ GET /api/v1/portfolio/allocation                            │
│ └─ Risk Metrics                                                    │
│    └─ GET /api/v1/portfolio/snapshots (for calculations)          │
│                                                                     │
│ PAGE 6: STRATEGIES                                                 │
│ ├─ Predefined Strategies Grid                                      │
│ │  ├─ GET /api/v1/strategies/predefined                           │
│ │  └─ GET /api/v1/strategies/predefined/{id}                      │
│ ├─ Create Custom Strategy Form                                     │
│ │  ├─ POST /api/v1/strategies/user (create)                       │
│ │  ├─ GET /api/v1/strategies/user/{id}                            │
│ │  └─ PUT /api/v1/strategies/user/{id} (update)                   │
│ ├─ AI Strategy Generator                                           │
│ │  ├─ POST /api/v1/strategies/ai/generate (generate from prompt)  │
│ │  └─ GET /api/v1/strategies/ai/{user_id} (fetch history)         │
│ ├─ Backtest Results                                                │
│ │  └─ POST /api/v1/strategies/{id}/backtest (run backtest)        │
│ └─ Active Strategies Manager                                       │
│    ├─ GET /api/v1/strategies/user?status=active                   │
│    ├─ POST /api/v1/strategies/{id}/execute (start)                │
│    ├─ GET /api/v1/strategies/{id}/performance (tracking)          │
│    └─ DELETE /api/v1/strategies/user/{id} (delete)                │
│                                                                     │
│ PAGE 7: INDICATORS                                                 │
│ ├─ Built-in Indicators                                             │
│ │  └─ GET /api/v1/indicators (list all)                           │
│ ├─ Calculate Indicator                                             │
│ │  └─ POST /api/v1/indicators/{id} (run calculation)              │
│ ├─ Custom Indicators List                                          │
│ │  └─ GET /api/v1/indicators/user                                 │
│ ├─ Create Custom Indicator                                         │
│ │  └─ POST /api/v1/indicators/user (create custom)                │
│ ├─ Edit Custom Indicator                                           │
│ │  └─ PUT /api/v1/indicators/user/{id} (update)                   │
│ └─ Delete Custom Indicator                                         │
│    └─ DELETE /api/v1/indicators/user/{id}                         │
│                                                                     │
│ PAGE 8: SETTINGS                                                   │
│ ├─ Credentials Tab                                                 │
│ │  ├─ GET /api/v1/user/account-settings (fetch current)           │
│ │  ├─ PUT /api/v1/user/account-settings (update credentials)      │
│ │  └─ GET /api/v1/user/account-info (verify account)              │
│ ├─ Appearance Tab                                                  │
│ │  ├─ GET /api/v1/settings/general (fetch preferences)            │
│ │  └─ PUT /api/v1/settings/general (save theme/style)             │
│ ├─ Notifications Tab                                               │
│ │  ├─ GET /api/v1/settings/notifications                          │
│ │  └─ PUT /api/v1/settings/notifications                          │
│ ├─ Risk Tab                                                        │
│ │  ├─ GET /api/v1/user/risk-preferences                           │
│ │  └─ PUT /api/v1/user/risk-preferences                           │
│ └─ Data Tab                                                        │
│    ├─ GET /api/v1/user/profile (export data)                      │
│    └─ POST /api/v1/user/profile (import data)                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📊 BY ENDPOINT - WHICH PAGES USE IT

```
ENDPOINTS A-C:
├─ POST /api/v1/auth/login
│  ├─ Setup Page (Step 1 - verify credentials)
│  └─ Market Page (implicit in WebSocket auth)
├─ GET /api/v1/market/depth?symbol=X
│  └─ Market Page (Order Book Widget)
└─ DELETE /api/v1/indicators/user/{id}
   └─ Indicators Page

ENDPOINTS D-H:
├─ GET /api/v1/indicators
│  └─ Indicators Page (Built-in Indicators)
├─ POST /api/v1/indicators/{id}
│  └─ Indicators Page (Calculate)
├─ GET /api/v1/indicators/user
│  └─ Indicators Page (Custom Indicators)
├─ POST /api/v1/indicators/user
│  └─ Indicators Page (Create Custom)
└─ PUT /api/v1/indicators/user/{id}
   └─ Indicators Page (Edit Custom)

ENDPOINTS I-M:
├─ GET /api/v1/market/heatmap
│  └─ Market Page (Market Heatmap Widget)
├─ GET /api/v1/market/indices
│  ├─ Dashboard Page (Market Overview)
│  └─ Market Page (Market Indices Widget)
├─ GET /api/v1/market/instruments
│  ├─ Market Page (Watchlist & Live Quotes)
│  └─ Trading Page (Symbol search)
├─ GET /api/v1/market/ohlc?symbol=X&interval=Y
│  └─ Market Page (Technical Chart)
└─ GET /api/v1/market/quote?symbol=X
   ├─ Dashboard Page (Market Overview, Positions)
   ├─ Market Page (Watchlist, Live Quotes, Order Book)
   └─ Trading Page (Smart Price)

ENDPOINTS O-S:
├─ POST /api/v1/orders/batch
│  └─ Trading Page (Batch Order)
├─ POST /api/v1/orders/place
│  └─ Trading Page (Order Form)
├─ GET /api/v1/orders
│  └─ Trading Page (Open Orders)
├─ PUT /api/v1/orders/{id}
│  └─ Trading Page (Modify Order)
├─ POST /api/v1/orders/{id}/cancel
│  └─ Trading Page (Cancel Order)
└─ GET /api/v1/orders/trades
   ├─ Dashboard Page (Recent Trades)
   └─ Trading Page (Order History)

ENDPOINTS T-Z (Portfolio):
├─ GET /api/v1/portfolio/allocation
│  └─ Portfolio Page (Asset Allocation)
├─ GET /api/v1/portfolio/holdings
│  └─ Portfolio Page (Holdings Table)
├─ GET /api/v1/portfolio/performance?period=X
│  └─ Portfolio Page (Performance Chart)
├─ GET /api/v1/portfolio/positions
│  ├─ Dashboard Page (Open Positions)
│  └─ Portfolio Page (Positions Table)
├─ GET /api/v1/portfolio/snapshots
│  ├─ Dashboard Page (Portfolio Chart)
│  └─ Portfolio Page (Risk Metrics calc)
└─ GET /api/v1/portfolio/summary
   ├─ Dashboard Page (Portfolio Summary)
   └─ Portfolio Page (Summary data)

ENDPOINTS (Settings & User):
├─ GET /api/v1/settings/general
│  └─ Settings Page (Appearance Tab)
├─ PUT /api/v1/settings/general
│  ├─ Setup Page (Step 4)
│  └─ Settings Page (Appearance Tab)
├─ GET /api/v1/settings/notifications
│  └─ Settings Page (Notifications Tab)
├─ PUT /api/v1/settings/notifications
│  └─ Settings Page (Notifications Tab)
├─ GET /api/v1/user/account-info
│  └─ Setup Page (Step 1 verify)
├─ GET /api/v1/user/account-settings
│  └─ Settings Page (Credentials Tab)
├─ PUT /api/v1/user/account-settings
│  ├─ Setup Page (Final save)
│  └─ Settings Page (Credentials Tab)
├─ GET /api/v1/user/risk-preferences
│  └─ Settings Page (Risk Tab)
├─ PUT /api/v1/user/risk-preferences
│  └─ Settings Page (Risk Tab)
└─ GET /api/v1/user/ai-preferences
   └─ Settings Page (AI Config)

ENDPOINTS (Strategies):
├─ GET /api/v1/strategies/predefined
│  └─ Strategies Page
├─ GET /api/v1/strategies/predefined/{id}
│  └─ Strategies Page
├─ POST /api/v1/strategies/user
│  └─ Strategies Page (Create Custom)
├─ GET /api/v1/strategies/user
│  └─ Strategies Page
├─ GET /api/v1/strategies/user/{id}
│  └─ Strategies Page
├─ PUT /api/v1/strategies/user/{id}
│  └─ Strategies Page (Edit)
├─ DELETE /api/v1/strategies/user/{id}
│  └─ Strategies Page (Delete)
├─ POST /api/v1/strategies/ai/generate
│  └─ Strategies Page (AI Generator)
├─ GET /api/v1/strategies/ai/{user_id}
│  └─ Strategies Page
├─ POST /api/v1/strategies/execute
│  └─ Strategies Page (Execute)
├─ GET /api/v1/strategies/{id}/performance
│  └─ Strategies Page
└─ POST /api/v1/strategies/{id}/backtest
   └─ Strategies Page (Backtest)

WEBSOCKET CONNECTIONS:
├─ WS /ws/market/live-quotes
│  ├─ Dashboard Page (real-time updates)
│  └─ Market Page (real-time quotes)
├─ WS /ws/market/depth
│  └─ Market Page (Order Book updates)
├─ WS /ws/market/orders
│  └─ Trading Page (Order status)
└─ WS /ws/portfolio/pnl
   └─ Dashboard Page (P&L updates)
```

---

## 🎯 DEVELOPER WORKFLOW BY BACKEND SCRIPT

### **user.py (21.6K) - Used by Pages:**
```
Setup Page → Dashboard → Settings
├─ GET /api/v1/user/account-info
├─ PUT /api/v1/user/account-settings
├─ GET /api/v1/user/risk-preferences
├─ PUT /api/v1/user/risk-preferences
└─ GET /api/v1/user/ai-preferences
```

### **market.py (8.5K) - Used by Pages:**
```
Dashboard → Market → Trading
├─ GET /api/v1/market/indices
├─ GET /api/v1/market/quote
├─ GET /api/v1/market/ohlc
├─ GET /api/v1/market/depth
├─ GET /api/v1/market/heatmap
├─ GET /api/v1/market/instruments
├─ WS /ws/market/live-quotes
└─ WS /ws/market/depth
```

### **orders.py (14.0K) - Used by Pages:**
```
Dashboard → Trading
├─ POST /api/v1/orders/place
├─ GET /api/v1/orders
├─ GET /api/v1/orders/{id}
├─ PUT /api/v1/orders/{id}
├─ POST /api/v1/orders/{id}/cancel
├─ GET /api/v1/orders/trades
├─ POST /api/v1/orders/batch
└─ WS /ws/market/orders
```

### **portfolio.py (13.1K) - Used by Pages:**
```
Dashboard → Portfolio
├─ GET /api/v1/portfolio/summary
├─ GET /api/v1/portfolio/positions
├─ GET /api/v1/portfolio/holdings
├─ GET /api/v1/portfolio/performance
├─ GET /api/v1/portfolio/snapshots
└─ GET /api/v1/portfolio/allocation
```

### **strategies.py (27.2K) - Used by Pages:**
```
Strategies
├─ GET /api/v1/strategies/predefined
├─ GET /api/v1/strategies/predefined/{id}
├─ GET /api/v1/strategies/user
├─ POST /api/v1/strategies/user
├─ GET /api/v1/strategies/user/{id}
├─ PUT /api/v1/strategies/user/{id}
├─ DELETE /api/v1/strategies/user/{id}
├─ POST /api/v1/strategies/ai/generate
├─ GET /api/v1/strategies/ai/{user_id}
├─ POST /api/v1/strategies/execute
├─ GET /api/v1/strategies/{id}/performance
└─ POST /api/v1/strategies/{id}/backtest
```

### **indicators.py (16.6K) - Used by Pages:**
```
Indicators
├─ GET /api/v1/indicators
├─ POST /api/v1/indicators/{id}
├─ GET /api/v1/indicators/user
├─ POST /api/v1/indicators/user
├─ PUT /api/v1/indicators/user/{id}
└─ DELETE /api/v1/indicators/user/{id}
```

### **settings.py (9.1K) - Used by Pages:**
```
Setup → Settings
├─ GET /api/v1/settings/general
├─ PUT /api/v1/settings/general
├─ GET /api/v1/settings/notifications
├─ PUT /api/v1/settings/notifications
├─ GET /api/v1/settings/derivatives-expiry
└─ PUT /api/v1/settings/derivatives-expiry
```

---

## 🚀 PARALLEL DEVELOPMENT STRATEGY

**If you have a team of 4 developers:**

```
Developer 1: Setup Page (user.py, settings.py)
  ├─ Interfaces with: Auth, User settings
  └─ Timeline: Week 1

Developer 2: Dashboard + Market (market.py, portfolio.py)
  ├─ Interfaces with: Real-time quotes, WebSockets
  └─ Timeline: Week 2 (parallel with Dev 1 after day 3)

Developer 3: Trading + Portfolio (orders.py, portfolio.py)
  ├─ Interfaces with: Order execution, P&L calculation
  └─ Timeline: Week 3-4

Developer 4: Strategies + Indicators (strategies.py, indicators.py)
  ├─ Interfaces with: AI service, backtesting
  └─ Timeline: Week 5

Settings Page: Developer 1 (Week 6)
  ├─ Refactor after other pages done
  └─ Integration of all settings

Coordination: 15-min daily sync
  ├─ Frontend lead coordinates endpoints
  ├─ Resolve API contract issues
  └─ Share common utilities
```

---

## 📋 CHECKLIST FOR EACH PAGE

When implementing any page, follow this checklist:

```
□ Read page guide completely
□ List all endpoints to integrate
□ Review endpoint response structures
□ Check WebSocket requirements
□ Create all components/sub-components
□ Implement form validations
□ Add loading states
□ Add error states
□ Add success notifications
□ Integrate all endpoints
□ Test all theme variations
□ Test responsive design (XS to 2XL)
□ Test WebSocket connections
□ Add accessibility (keyboard, ARIA)
□ Write unit tests
□ Add React Query integration
□ Add Redux state management
□ Optimize performance
□ Code review
□ Merge to main
```

---

**Total Endpoints**: 86  
**Total Pages**: 8  
**Backend Scripts**: 11  
**WebSocket Connections**: 4  
**Development Time**: 6 weeks  

---

**This is your complete reference guide for page-to-endpoint relationships!**

