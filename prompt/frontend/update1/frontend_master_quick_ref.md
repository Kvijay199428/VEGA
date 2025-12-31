# 🎯 MASTER FRONTEND DEVELOPMENT - QUICK REFERENCE GUIDE

**For**: VEGA TRADER'S - AI-Enabled Trading Platform  
**Date**: December 13, 2025, 11:14 AM IST  
**Status**: Complete page-by-page development strategy

---

## 📋 ALL PAGES AT A GLANCE

| # | Page | Backend Router | Backend Size | Endpoints | Time |
|---|------|----------------|--------------|-----------|------|
| 1 | Setup Wizard | user.py, settings.py | 30.7K | 3 | 1 week |
| 2 | Dashboard | market.py, portfolio.py | 21.6K | 5 | 3 days |
| 3 | Market Data | market.py | 8.5K | 8 | 4 days |
| 4 | Trading | orders.py | 27.9K | 7 | 4 days |
| 5 | Portfolio | portfolio.py | 36.5K | 6 | 4 days |
| 6 | Strategies | strategies.py, ai_service.py | 51.5K | 13 | 5 days |
| 7 | Indicators | indicators.py | 16.6K | 6 | 2 days |
| 8 | Settings | settings.py, user.py | 30.7K | 12 | 3 days |

**Total**: 248.5K of backend code, 60 endpoints, 6 weeks

---

## 🗂️ FRONTEND PROJECT STRUCTURE

```
frontend/
├── public/
│   ├── index.html
│   ├── favicon.ico
│   └── assets/
│       ├── images/
│       ├── icons/
│       └── fonts/
│
├── src/
│   ├── pages/
│   │   ├── Setup.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Market.tsx
│   │   ├── Trading.tsx
│   │   ├── Portfolio.tsx
│   │   ├── Strategies.tsx
│   │   ├── Indicators.tsx
│   │   ├── Settings.tsx
│   │   ├── NotFound.tsx
│   │   ├── Login.tsx
│   │   └── Logout.tsx
│   │
│   ├── components/
│   │   ├── common/
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Navigation.tsx
│   │   │   ├── Footer.tsx
│   │   │   ├── ThemeToggle.tsx
│   │   │   ├── Loading.tsx
│   │   │   ├── Error.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Toast.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Button.tsx
│   │   │   └── Input.tsx
│   │   │
│   │   ├── setup/
│   │   │   ├── Step1_Upstox.tsx
│   │   │   ├── Step2_Database.tsx
│   │   │   ├── Step3_AI.tsx
│   │   │   ├── Step4_Theme.tsx
│   │   │   └── Step5_Review.tsx
│   │   │
│   │   ├── dashboard/
│   │   │   ├── MarketOverview.tsx
│   │   │   ├── PortfolioSummary.tsx
│   │   │   ├── PositionsTable.tsx
│   │   │   ├── TradesTable.tsx
│   │   │   ├── PortfolioChart.tsx
│   │   │   └── QuickActions.tsx
│   │   │
│   │   ├── market/
│   │   │   ├── Watchlist.tsx
│   │   │   ├── MarketHeatmap.tsx
│   │   │   ├── TechnicalChart.tsx
│   │   │   ├── OrderBook.tsx
│   │   │   ├── MarketIndices.tsx
│   │   │   └── LiveQuotesGrid.tsx
│   │   │
│   │   ├── trading/
│   │   │   ├── OrderForm.tsx
│   │   │   ├── OpenOrders.tsx
│   │   │   ├── OrderHistory.tsx
│   │   │   └── OrderConfirmation.tsx
│   │   │
│   │   ├── portfolio/
│   │   │   ├── Holdings.tsx
│   │   │   ├── Positions.tsx
│   │   │   ├── PerformanceChart.tsx
│   │   │   ├── AllocationChart.tsx
│   │   │   └── RiskMetrics.tsx
│   │   │
│   │   ├── strategies/
│   │   │   ├── PredefinedStrategies.tsx
│   │   │   ├── CreateStrategy.tsx
│   │   │   ├── AIGenerator.tsx
│   │   │   ├── BacktestResults.tsx
│   │   │   └── ActiveStrategies.tsx
│   │   │
│   │   ├── indicators/
│   │   │   ├── BuiltInIndicators.tsx
│   │   │   ├── CustomIndicators.tsx
│   │   │   └── IndicatorCalculations.tsx
│   │   │
│   │   └── settings/
│   │       ├── CredentialsTab.tsx
│   │       ├── AppearanceTab.tsx
│   │       ├── NotificationsTab.tsx
│   │       ├── RiskTab.tsx
│   │       └── DataTab.tsx
│   │
│   ├── hooks/
│   │   ├── useWebSocket.ts
│   │   ├── useAuth.ts
│   │   ├── useTheme.ts
│   │   ├── useAPI.ts
│   │   ├── useForm.ts
│   │   ├── useToast.ts
│   │   └── useLocalStorage.ts
│   │
│   ├── services/
│   │   ├── api.ts
│   │   ├── auth.ts
│   │   ├── market.ts
│   │   ├── orders.ts
│   │   ├── portfolio.ts
│   │   ├── strategies.ts
│   │   ├── websocket.ts
│   │   └── encryption.ts
│   │
│   ├── store/
│   │   ├── store.ts
│   │   ├── slices/
│   │   │   ├── authSlice.ts
│   │   │   ├── themeSlice.ts
│   │   │   ├── marketSlice.ts
│   │   │   ├── ordersSlice.ts
│   │   │   ├── portfolioSlice.ts
│   │   │   └── uiSlice.ts
│   │   └── selectors/
│   │       ├── authSelectors.ts
│   │       ├── marketSelectors.ts
│   │       └── portfolioSelectors.ts
│   │
│   ├── types/
│   │   ├── index.ts
│   │   ├── auth.ts
│   │   ├── market.ts
│   │   ├── orders.ts
│   │   ├── portfolio.ts
│   │   ├── strategies.ts
│   │   └── api.ts
│   │
│   ├── styles/
│   │   ├── globals.css
│   │   ├── variables.css
│   │   ├── themes/
│   │   │   ├── light.css
│   │   │   ├── dark.css
│   │   │   └── extra-dark.css
│   │   └── components/
│   │       ├── buttons.css
│   │       ├── forms.css
│   │       ├── tables.css
│   │       └── cards.css
│   │
│   ├── utils/
│   │   ├── formatters.ts
│   │   ├── validators.ts
│   │   ├── calculations.ts
│   │   ├── storage.ts
│   │   ├── encryption.ts
│   │   └── logger.ts
│   │
│   ├── middleware/
│   │   ├── auth.ts
│   │   ├── errorHandler.ts
│   │   └── logger.ts
│   │
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
│
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── .env.example
├── .env.local
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── eslintrc.js
├── prettierrc.js
└── README.md
```

---

## 📡 API ENDPOINT REFERENCE BY PAGE

### **Page 1: Setup**
```
POST   /api/v1/auth/login
GET    /api/v1/user/account-info
PUT    /api/v1/user/account-settings
POST   /api/v1/settings/general
GET    /api/v1/settings/general
```

### **Page 2: Dashboard**
```
GET    /api/v1/market/indices
GET    /api/v1/market/quote?symbol=X
GET    /api/v1/portfolio/summary
GET    /api/v1/portfolio/positions
GET    /api/v1/orders/trades
GET    /api/v1/portfolio/snapshots
WS     /ws/market/live-quotes
WS     /ws/portfolio/pnl
```

### **Page 3: Market Data**
```
GET    /api/v1/market/instruments
GET    /api/v1/market/quote?symbol=X
GET    /api/v1/market/ohlc?symbol=X&interval=1m
GET    /api/v1/market/depth?symbol=X
GET    /api/v1/market/heatmap
GET    /api/v1/market/indices
WS     /ws/market/live-quotes
WS     /ws/market/depth
```

### **Page 4: Trading**
```
POST   /api/v1/orders/place
GET    /api/v1/orders
GET    /api/v1/orders/{order_id}
PUT    /api/v1/orders/{order_id}
POST   /api/v1/orders/{order_id}/cancel
GET    /api/v1/orders/trades
POST   /api/v1/orders/batch
WS     /ws/market/orders
```

### **Page 5: Portfolio**
```
GET    /api/v1/portfolio/summary
GET    /api/v1/portfolio/positions
GET    /api/v1/portfolio/holdings
GET    /api/v1/portfolio/performance?period=X
GET    /api/v1/portfolio/snapshots
GET    /api/v1/portfolio/allocation
WS     /ws/market/live-quotes
WS     /ws/portfolio/pnl
```

### **Page 6: Strategies**
```
GET    /api/v1/strategies/predefined
GET    /api/v1/strategies/predefined/{id}
GET    /api/v1/strategies/user
POST   /api/v1/strategies/user
GET    /api/v1/strategies/user/{id}
PUT    /api/v1/strategies/user/{id}
DELETE /api/v1/strategies/user/{id}
POST   /api/v1/strategies/user/{id}/clone
POST   /api/v1/strategies/ai/generate
GET    /api/v1/strategies/ai/{user_id}
POST   /api/v1/strategies/execute
GET    /api/v1/strategies/{id}/performance
POST   /api/v1/strategies/{id}/backtest
```

### **Page 7: Indicators**
```
GET    /api/v1/indicators
POST   /api/v1/indicators/{id}
GET    /api/v1/indicators/user
POST   /api/v1/indicators/user
PUT    /api/v1/indicators/user/{id}
DELETE /api/v1/indicators/user/{id}
```

### **Page 8: Settings**
```
GET    /api/v1/user/account-settings
PUT    /api/v1/user/account-settings
GET    /api/v1/user/account-info
GET    /api/v1/user/risk-preferences
PUT    /api/v1/user/risk-preferences
GET    /api/v1/user/ai-preferences
PUT    /api/v1/user/ai-preferences
GET    /api/v1/settings/general
PUT    /api/v1/settings/general
GET    /api/v1/settings/notifications
PUT    /api/v1/settings/notifications
GET    /api/v1/settings/derivatives-expiry
PUT    /api/v1/settings/derivatives-expiry
```

---

## 🔑 KEY IMPLEMENTATION PATTERNS

### **Redux Slice Pattern**
```typescript
// Store slice
export const marketSlice = createSlice({
  name: 'market',
  initialState: initialMarketState,
  reducers: {
    setQuotes: (state, action) => {
      state.quotes = action.payload;
    }
  }
});

// Selector
export const selectQuotes = (state: RootState) => state.market.quotes;

// Usage
const quotes = useSelector(selectQuotes);
```

### **API Call Pattern**
```typescript
// Service
export const fetchQuote = async (symbol: string) => {
  const response = await api.get(`/market/quote?symbol=${symbol}`);
  return response.data;
};

// Component
const [quote, setQuote] = useState(null);

useEffect(() => {
  const getQuote = async () => {
    const data = await fetchQuote('INFY');
    setQuote(data);
  };
  getQuote();
}, []);
```

### **WebSocket Pattern**
```typescript
// Hook
export const useWebSocket = (url: string) => {
  const [data, setData] = useState(null);
  
  useEffect(() => {
    const ws = new WebSocket(url);
    ws.onmessage = (event) => {
      setData(JSON.parse(event.data));
    };
    
    return () => ws.close();
  }, [url]);
  
  return { data };
};

// Usage
const { data: quotes } = useWebSocket('/ws/market/live-quotes');
```

### **Form Pattern**
```typescript
// Form handling
const [formData, setFormData] = useState({
  symbol: '',
  quantity: 0,
  price: 0
});

const [errors, setErrors] = useState({});

const handleChange = (e) => {
  const { name, value } = e.target;
  setFormData(prev => ({ ...prev, [name]: value }));
  
  // Clear error for this field
  setErrors(prev => ({ ...prev, [name]: '' }));
};

const handleSubmit = (e) => {
  e.preventDefault();
  
  // Validate
  const newErrors = validateForm(formData);
  if (Object.keys(newErrors).length > 0) {
    setErrors(newErrors);
    return;
  }
  
  // Submit
  submitForm(formData);
};
```

---

## 🎨 THEME IMPLEMENTATION

**CSS Variables**:
```css
:root {
  /* Light Mode (default) */
  --bg-primary: #FFFFFF;
  --bg-secondary: #F5F7FA;
  --text-primary: #1F2937;
  --accent: #3B82F6;
}

[data-theme='dark'] {
  --bg-primary: #1F2937;
  --bg-secondary: #111827;
  --text-primary: #F3F4F6;
  --accent: #60A5FA;
}

[data-theme='extra-dark'] {
  --bg-primary: #000000;
  --bg-secondary: #111827;
  --text-primary: #FFFFFF;
  --accent: #06B6D4;
}

[data-style='glass'] {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}
```

**Theme Hook**:
```typescript
export const useTheme = () => {
  const dispatch = useDispatch();
  const { theme, style } = useSelector(selectTheme);
  
  const setTheme = (newTheme: string) => {
    document.documentElement.setAttribute('data-theme', newTheme);
    dispatch(setThemeAction(newTheme));
  };
  
  const setStyle = (newStyle: string) => {
    document.documentElement.setAttribute('data-style', newStyle);
    dispatch(setStyleAction(newStyle));
  };
  
  return { theme, style, setTheme, setStyle };
};
```

---

## 🚀 DEVELOPMENT WORKFLOW

### **Weekly Breakdown**

**Week 1: Foundation**
- Setup page (all 5 steps)
- Theme system implementation
- Authentication flow
- Navigation (header + sidebar)
- **Deliverable**: Setup wizard working + theme toggle

**Week 2: Core Pages**
- Dashboard (all 6 widgets)
- Market data page
- WebSocket integration
- Real-time updates
- **Deliverable**: Dashboard + market data live

**Week 3: Trading**
- Trading page (order form)
- Margin calculation
- Order confirmation
- Order history
- **Deliverable**: Can place and manage orders

**Week 4: Portfolio & Analysis**
- Portfolio page
- Holdings/positions tables
- Performance charts
- Risk metrics
- **Deliverable**: Portfolio tracking working

**Week 5: Advanced Features**
- Strategies page
- AI strategy generator
- Backtesting interface
- Indicators page
- **Deliverable**: Can create and test strategies

**Week 6: Finalization**
- Settings page (all tabs)
- Error handling & recovery
- Performance optimization
- Testing & bug fixes
- **Deliverable**: Fully functional trading platform

---

## ✅ DAILY CHECKLIST TEMPLATE

Each page should be verified with:

```
□ All endpoints integrated
□ Data fetching working
□ Loading states shown
□ Error states handled
□ WebSocket (if needed) working
□ Responsive on mobile/tablet/desktop
□ Light mode tested
□ Dark mode tested
□ Extra dark mode tested
□ Glass style tested
□ Normal style tested
□ Keyboard navigation working
□ Screen reader compatible
□ No console errors
□ No memory leaks
□ Performance optimized
□ Unit tests passing
□ Integration tests passing
□ Code documented
□ PR reviewed
□ Merged to main
```

---

## 🎯 PERFORMANCE TARGETS

**Core Web Vitals**:
- FCP (First Contentful Paint): < 1.5s
- LCP (Largest Contentful Paint): < 2.5s
- CLS (Cumulative Layout Shift): < 0.1
- TTI (Time to Interactive): < 3.5s

**Bundle Size**:
- Main bundle: < 200KB
- CSS bundle: < 50KB
- Total with dependencies: < 500KB (gzipped)

---

## 🔗 QUICK NAVIGATION

**Setup Documentation**:
- File: `frontend_page_1_2_3_guide.md`
- Details: Setup, Dashboard, Market pages

**Trading & Portfolio**:
- File: `frontend_page_4_5_6_7_8_guide.md`
- Details: Trading, Portfolio, Strategies, Indicators, Settings

**Package Overview**:
- File: `frontend_package_summary.md`
- Details: High-level overview and checklists

---

## 💡 COMMON IMPLEMENTATION TIPS

1. **Always use TypeScript strict mode**
   ```typescript
   // tsconfig.json
   "strict": true,
   "strictNullChecks": true
   ```

2. **Handle all edge cases**
   - Empty states
   - Loading states
   - Error states
   - Timeout states

3. **Test WebSocket connections**
   - Mock WebSocket in tests
   - Handle disconnections
   - Implement reconnection logic

4. **Encrypt sensitive data**
   - API keys in settings
   - Database credentials
   - AI LLM tokens

5. **Optimize re-renders**
   - Use useMemo for expensive calculations
   - Use useCallback for event handlers
   - Use React.memo for components

6. **Accessibility first**
   - semantic HTML
   - ARIA labels
   - Keyboard navigation
   - Color contrast (4.5:1)

7. **Error logging**
   - Log all API errors
   - Log user actions
   - Don't log sensitive data

---

## 📞 SUPPORT & DEBUGGING

**Common Issues & Solutions**:

1. **WebSocket won't connect**
   - Check backend is running
   - Verify WebSocket URL
   - Check for CORS issues
   - Look at browser console

2. **API calls failing**
   - Check JWT token validity
   - Verify endpoint exists
   - Check request body format
   - Look at network tab

3. **Theme not applying**
   - Check CSS variables defined
   - Verify data-theme attribute set
   - Clear browser cache
   - Check CSS file loaded

4. **Real-time data not updating**
   - Check WebSocket connection
   - Verify data parsing
   - Check Redux state update
   - Look for console errors

---

**Total Documentation**: ~15,000 words  
**Status**: Complete and ready for development  
**Next**: Start building! 🚀

