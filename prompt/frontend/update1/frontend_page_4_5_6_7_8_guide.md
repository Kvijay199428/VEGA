# 📖 PAGE 4-8 FRONTEND DEVELOPMENT GUIDE

**Continuation**: VEGA TRADER'S - AI-Enabled Trading Platform  
**Date**: December 13, 2025, 11:14 AM IST  
**Pages**: Trading, Portfolio, Strategies, Indicators, Settings

---

# PAGE 4: TRADING PAGE

**File**: `pages/Trading.tsx`  
**Components**: `components/trading/*`  
**Backend Router**: `routers/orders.py`  
**Backend Scripts**: orders.py (14.0K), order_service.py (19.4K)  
**Related Services**: upstox_service.py  

---

## 🎯 PAGE PURPOSE

Order placement and management interface. Features:
1. Order form with multiple order types
2. Real-time margin calculation
3. Smart price suggestions
4. Order confirmation modal
5. Open orders table
6. Order history

---

## 📊 TRADING PAGE LAYOUT

```
┌────────────────────────────────────────────────────────┐
│ 🔵 VEGA TRADER'S              ⚙️  👤  🌙            │
├──────────────┬────────────────────────────────────────┤
│ Dashboard    │                                        │
│ Market       │ ┌──────────────────────────────────┐  │
│ Trading ────►│ │  📊 PLACE ORDER (1)              │  │
│ Portfolio    │ ├──────────────────────────────────┤  │
│ Strategies   │ │ Symbol: [INFY_________] [Search] │  │
│ Indicators   │ │                                  │  │
│ Settings     │ │ Order Type: ● Market  ○ Limit  │  │
│              │ │            ○ Stop-Loss ○ OCO   │  │
│              │ │                                  │  │
│              │ │ Side: ● BUY    ○ SELL           │  │
│              │ │                                  │  │
│              │ │ Quantity: [100________]          │  │
│              │ │                                  │  │
│              │ │ Price: [2,100________]           │  │
│              │ │ Market Price: 2,098              │  │
│              │ │                                  │  │
│              │ │ ┌──────────────────────────────┐ │  │
│              │ │ │ MARGIN REQUIRED: ₹2,10,000   │ │  │
│              │ │ │ Available Margin: ₹2,50,000  │ │  │
│              │ │ │ Margin Used: 84%              │ │  │
│              │ │ │ [████████░░░░░░] Safe        │ │  │
│              │ │ └──────────────────────────────┘ │  │
│              │ │                                  │  │
│              │ │ Validity: ● Day  ○ IOC  ○ GTT  │  │
│              │ │                                  │  │
│              │ │ [✓ Same as Market]               │  │
│              │ │                                  │  │
│              │ │ [Preview] [Clear] [PLACE ORDER] │  │
│              │ └──────────────────────────────────┘  │
│              │                                        │
│              │ ┌──────────────────────────────────┐  │
│              │ │  📋 OPEN ORDERS (2)              │  │
│              │ ├──────────────────────────────────┤  │
│              │ │ Ord │ Sym │ Side│ Qty │ Price   │  │
│              │ ├─────┼─────┼─────┼─────┼─────────┤  │
│              │ │ 101 │INFY │ BUY │ 100 │ 2,100   │  │
│              │ │ 102 │TCS  │SELL │ 50  │ 3,200   │  │
│              │ │                                  │  │
│              │ │ [Modify] [Cancel]                │  │
│              │ └──────────────────────────────────┘  │
│              │                                        │
│              │ ┌──────────────────────────────────┐  │
│              │ │  📊 ORDER HISTORY (3)            │  │
│              │ ├──────────────────────────────────┤  │
│              │ │ Ord │ Sym │ Status│ Time        │  │
│              │ ├─────┼─────┼───────┼─────────────┤  │
│              │ │ 100 │INFY │Closed │ 09:15 AM    │  │
│              │ │ 99  │REL  │Filled │ 08:30 AM    │  │
│              │ └──────────────────────────────────┘  │
│              │                                        │
└──────────────┴────────────────────────────────────────┘
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

### **Widget 1: Place Order Form**

**Component**: `components/trading/OrderForm.tsx`

**Backend Endpoints**:
```
POST /api/v1/orders/place
GET /api/v1/market/quote?symbol=X (for smart price)
```

**Data Structure**:
```typescript
interface OrderRequest {
  symbol: string;
  side: 'BUY' | 'SELL';
  order_type: 'MARKET' | 'LIMIT' | 'STOP_LOSS' | 'OCO';
  quantity: number;
  price?: number;  // For limit orders
  stop_price?: number;  // For stop-loss
  validity: 'DAY' | 'IOC' | 'GTT';
  metadata?: {
    source: 'web' | 'mobile' | 'api';
  };
}
```

**Form Features**:
1. **Symbol Input**: Autocomplete from market data
2. **Order Type Selection**: 
   - Market (instant execution)
   - Limit (at specific price)
   - Stop-Loss (trigger at price)
   - OCO (One-Cancels-Other)
3. **Quantity Input**: Integer validation
4. **Price Input**: Decimal validation, smart suggestions
5. **Validity Options**: Day, IOC, GTT
6. **Margin Calculation**: Real-time based on order

**Validation Rules**:
```typescript
// Validation before submission
const validateOrder = (order: OrderRequest) => {
  if (!order.symbol) throw new Error('Symbol required');
  if (order.quantity <= 0) throw new Error('Qty must be > 0');
  if (order.quantity > 100000) throw new Error('Qty exceeds max');
  
  if (order.order_type === 'LIMIT' && !order.price) {
    throw new Error('Price required for limit orders');
  }
  
  const marginRequired = calculateMargin(order);
  if (marginRequired > availableMargin) {
    throw new Error(`Insufficient margin. Need ₹${marginRequired}`);
  }
  
  return true;
};
```

**Smart Price Suggestion**:
```typescript
// When user selects symbol, fetch latest quote
const getSmartPrice = async (symbol: string) => {
  const quote = await api.get(`/market/quote?symbol=${symbol}`);
  return quote.current_price;
};

// Update price input with market price
setFormData(prev => ({
  ...prev,
  price: quote.current_price
}));
```

**Margin Calculation**:
```typescript
const calculateMargin = (order: OrderRequest) => {
  // Simplified (actual logic depends on broker)
  const leverage = order.order_type === 'MARKET' ? 2 : 4;
  return (order.quantity * order.price) / leverage;
};

// Color code margin usage
const marginPercent = (marginRequired / availableMargin) * 100;
const marginColor = 
  marginPercent < 50 ? 'green' : 
  marginPercent < 75 ? 'orange' : 'red';
```

**Order Submission**:
```typescript
const submitOrder = async (order: OrderRequest) => {
  setLoading(true);
  try {
    const response = await api.post('/orders/place', order);
    
    // Show success message with order ID
    showSuccess(`Order placed! ID: ${response.order_id}`);
    
    // Open confirmation modal
    setConfirmation(response);
    
    // Clear form
    resetForm();
  } catch (error) {
    showError(error.message);
  } finally {
    setLoading(false);
  }
};
```

---

### **Widget 2: Open Orders Table**

**Component**: `components/trading/OpenOrders.tsx`

**Backend Endpoints**:
```
GET /api/v1/orders
WebSocket: /ws/market/orders (status updates)
```

**Data Structure**:
```typescript
interface OpenOrder {
  order_id: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  filled_quantity: number;
  price: number;
  status: 'PENDING' | 'PARTIAL' | 'REJECTED';
  order_type: string;
  timestamp: string;
}
```

**Features**:
- Show all open orders
- Real-time status updates via WebSocket
- Modify order (change price/quantity)
- Cancel order (with confirmation)
- Sort by symbol/time/status
- Filter by symbol/side

**Modify Order**:
```typescript
const modifyOrder = async (orderId: string, newPrice: number, newQty: number) => {
  const response = await api.put(`/orders/${orderId}`, {
    price: newPrice,
    quantity: newQty
  });
  
  // Refresh table
  fetchOpenOrders();
};
```

**Cancel Order**:
```typescript
const cancelOrder = async (orderId: string) => {
  const confirmed = await showConfirmation(
    'Cancel this order?',
    'This action cannot be undone'
  );
  
  if (confirmed) {
    await api.post(`/orders/${orderId}/cancel`);
    fetchOpenOrders();
  }
};
```

---

### **Widget 3: Order History**

**Component**: `components/trading/OrderHistory.tsx`

**Backend Endpoints**:
```
GET /api/v1/orders/trades
GET /api/v1/orders?status=CLOSED
```

**Features**:
- Show past orders (closed/filled/rejected)
- Pagination (20 per page)
- Filters: Date range, symbol, status
- Export to CSV
- Click to see details

**Display**:
```
Order ID | Symbol | Side | Qty | Price | Status | Time
─────────┼────────┼──────┼─────┼───────┼────────┼──────────
100      | INFY   | BUY  | 100 | 2,050 | FILLED | 09:15 AM
99       | TCS    | SELL | 50  | 3,200 | FILLED | 08:30 AM
98       | REL    | BUY  | 75  | 2,900 | CANCELLED | 07:45 AM
```

---

## 🔄 REAL-TIME UPDATES

**WebSocket for Order Status**:

```typescript
const { orderUpdates } = useWebSocket('/ws/market/orders');

useEffect(() => {
  if (orderUpdates) {
    // Update open orders table
    updateOpenOrders(orderUpdates);
    
    // Show notification if order filled
    if (orderUpdates.status === 'FILLED') {
      showSuccess(`Order ${orderUpdates.order_id} FILLED`);
    }
  }
}, [orderUpdates]);
```

---

## 📱 RESPONSIVE DESIGN

**Mobile**: Form takes full width, table scrolls horizontally
**Tablet**: 2-column layout (form left, table right)
**Desktop**: Same as tablet, all visible

---

## ✅ COMPLETION CHECKLIST FOR TRADING PAGE

- [ ] Order form implemented
- [ ] All order types supported
- [ ] Real-time margin calculation
- [ ] Smart price suggestions
- [ ] Order placement working
- [ ] Confirmation modal showing
- [ ] Open orders table populated
- [ ] Order modification working
- [ ] Order cancellation working
- [ ] Order history showing
- [ ] WebSocket updates working
- [ ] All validations in place
- [ ] Error messages clear
- [ ] Responsive design working
- [ ] All themes supported

---

---

# PAGE 5: PORTFOLIO PAGE

**File**: `pages/Portfolio.tsx`  
**Components**: `components/portfolio/*`  
**Backend Router**: `routers/portfolio.py`  
**Backend Scripts**: portfolio.py (13.1K), portfolio_service.py (23.4K)  

---

## 🎯 PAGE PURPOSE

Comprehensive portfolio management and analysis. Features:
1. Holdings (delivery stocks)
2. Positions (intraday, real-time P&L)
3. Performance charts (various timeframes)
4. Asset allocation pie chart
5. Risk metrics
6. Export functionality

---

## 📊 PORTFOLIO PAGE LAYOUT

```
┌──────────────────────────────────────────────────────┐
│ 🔵 VEGA TRADER'S          ⚙️  👤  🌙             │
├──────────────┬───────────────────────────────────────┤
│ Dashboard    │                                       │
│ Market       │ ┌───────────────────────────────────┐ │
│ Trading      │ │  📊 HOLDINGS (Delivery) (1)       │ │
│ Portfolio ──►│ ├───────────────────────────────────┤ │
│ Strategies   │ │ Symbol │ Qty │ Avg │ Current │ Gain│
│ Indicators   │ ├────────┼─────┼─────┼─────────┼────┤ │
│ Settings     │ │ INFY   │ 100 │2,000│ 2,050   │+850│ │
│              │ │ TCS    │ 50  │3,100│ 3,200   │+500│ │
│              │ │ REL    │ 75  │2,700│ 2,850   │+900│ │
│              │ │                                   │ │
│              │ │ [Sell] [View Chart]               │ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
│              │ ┌───────────────────────────────────┐ │
│              │ │  📈 POSITIONS (Intraday) (2)      │ │
│              │ ├───────────────────────────────────┤ │
│              │ │ Symbol │ Qty │ Entry │ Current │ P&L│
│              │ ├────────┼─────┼───────┼─────────┼────┤ │
│              │ │ MARUTI │ 10  │ 8,500 │ 8,600   │ +1K│ │
│              │ │ HDFC   │ 25  │ 2,400 │ 2,350   │ -1K│ │
│              │ │                                   │ │
│              │ │ [Exit Position]                   │ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
│              │ ┌───────────────────────────────────┐ │
│              │ │  📊 PERFORMANCE CHART (3)         │ │
│              │ ├───────────────────────────────────┤ │
│              │ │ [1D] [1W] [1M] [3M] [6M] [1Y]    │ │
│              │ │                                   │ │
│              │ │    ╱╲                             │ │
│              │ │   ╱  ╲  ╱╲  ╱╲                    │ │
│              │ │  ╱    ╲╱  ╲╱  ╲╱                  │ │
│              │ │                                   │ │
│              │ │ Return: +18.5% | Max DD: -5.2%  │ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
│              │ ┌───────────────────────────────────┐ │
│              │ │  🎯 ASSET ALLOCATION (4)          │ │
│              │ ├───────────────────────────────────┤ │
│              │ │        ╱╲                         │ │
│              │ │       ╱  ╲    IT: 35%             │ │
│              │ │      ╱    ╲   Banking: 25%        │ │
│              │ │     ╱ 35%  ╲  Auto: 20%           │ │
│              │ │    ╱        ╲ Pharma: 20%         │ │
│              │ │   ╱──────────╲                    │ │
│              │ │              ╲                    │ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
│              │ ┌───────────────────────────────────┐ │
│              │ │  📊 RISK METRICS (5)              │ │
│              │ ├───────────────────────────────────┤ │
│              │ │ Sharpe Ratio:    1.45             │ │
│              │ │ Max Drawdown:    -5.2%            │ │
│              │ │ Win Rate:        58%              │ │
│              │ │ Avg Win/Loss:    2.3x             │ │
│              │ │ Risk-Free Rate:  5.5% (assumed)   │ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
└──────────────┴───────────────────────────────────────┘
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

### **Widget 1: Holdings Table**

**Component**: `components/portfolio/Holdings.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/holdings
GET /api/v1/market/quote?symbol=X (for current prices)
```

**Data Structure**:
```typescript
interface Holding {
  symbol: string;
  quantity: number;
  average_price: number;
  current_price: number;
  pnl_amount: number;
  pnl_percent: number;
  value: number;
  entry_date: string;
}
```

**Features**:
- Sell button for each holding
- View detailed chart
- Sort by column
- Calculate total holding value
- Show total P&L

---

### **Widget 2: Positions Table**

**Component**: `components/portfolio/Positions.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/positions
WebSocket: /ws/market/live-quotes (price updates)
WebSocket: /ws/portfolio/pnl (P&L updates)
```

**Real-time P&L Calculation**:
```typescript
const calculatePnL = (position) => {
  const pnl = (position.current_price - position.entry_price) * position.quantity;
  const pnlPercent = ((pnl / (position.entry_price * position.quantity)) * 100);
  
  return { pnl, pnlPercent };
};

// Update on every price change
useEffect(() => {
  if (priceUpdate) {
    const updatedPositions = positions.map(pos => 
      pos.symbol === priceUpdate.symbol
        ? { ...pos, current_price: priceUpdate.price }
        : pos
    );
    setPositions(updatedPositions);
  }
}, [priceUpdate]);
```

---

### **Widget 3: Performance Chart**

**Component**: `components/portfolio/PerformanceChart.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/performance?period=1d
GET /api/v1/portfolio/performance?period=1w
GET /api/v1/portfolio/performance?period=1m
GET /api/v1/portfolio/performance?period=3m
GET /api/v1/portfolio/performance?period=6m
GET /api/v1/portfolio/performance?period=1y
```

**Data Structure**:
```typescript
interface PerformanceData {
  date: string;
  portfolio_value: number;
  invested_amount: number;
  return_percent: number;
}
```

**Chart Features**:
- Multiple timeframe buttons
- Area chart with gradient
- Hover tooltip with values
- Zoom and pan capability
- Export chart as PNG

---

### **Widget 4: Asset Allocation Pie Chart**

**Component**: `components/portfolio/AllocationChart.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/allocation
```

**Data Structure**:
```typescript
interface AllocationData {
  sector: string;
  value: number;
  percentage: number;
}
```

**Chart Features**:
- Pie chart (Recharts)
- Click slice to filter holdings
- Show percentages
- Legend with sector colors

---

### **Widget 5: Risk Metrics**

**Component**: `components/portfolio/RiskMetrics.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/performance (to calculate metrics)
GET /api/v1/portfolio/snapshots (historical data)
```

**Calculations**:

```typescript
// Sharpe Ratio
const sharpeRatio = calculateSharpeRatio(returns, riskFreeRate);

// Max Drawdown
const maxDrawdown = calculateMaxDrawdown(portfolioValues);

// Win Rate
const winRate = (winningTrades / totalTrades) * 100;

// Avg Win/Loss Ratio
const avgWinLossRatio = avgWinAmount / avgLossAmount;
```

---

## 🔄 REAL-TIME UPDATES

```typescript
// Real-time price updates for holdings/positions
const { liveQuotes } = useWebSocket('/ws/market/live-quotes');

useEffect(() => {
  if (liveQuotes) {
    updateHoldings(liveQuotes);
    updatePositions(liveQuotes);
    recalculateAllocation();
  }
}, [liveQuotes]);
```

---

## ✅ COMPLETION CHECKLIST FOR PORTFOLIO PAGE

- [ ] Holdings table implemented
- [ ] Positions table with real-time P&L
- [ ] Performance chart with multiple timeframes
- [ ] Asset allocation pie chart
- [ ] Risk metrics calculated correctly
- [ ] WebSocket updates working
- [ ] All calculations verified
- [ ] Export functionality added
- [ ] Responsive design working
- [ ] All themes supported
- [ ] Mobile-friendly tables
- [ ] Loading states shown
- [ ] Error handling in place

---

---

# PAGE 6: STRATEGIES PAGE

**File**: `pages/Strategies.tsx`  
**Components**: `components/strategies/*`  
**Backend Router**: `routers/strategies.py`  
**Backend Scripts**: strategies.py (27.2K), ai_service.py (24.3K)  

---

## 🎯 PAGE PURPOSE

AI-powered strategy creation and management. Features:
1. Browse predefined strategies
2. Create custom strategies
3. AI strategy generation (text prompt)
4. Backtest results visualization
5. Active strategies manager
6. Performance tracking

---

## 📊 STRATEGIES PAGE LAYOUT

```
┌──────────────────────────────────────────────────────┐
│ 🔵 VEGA TRADER'S          ⚙️  👤  🌙             │
├──────────────┬───────────────────────────────────────┤
│ Dashboard    │ ┌───────────────────────────────────┐ │
│ Market       │ │ [Predefined] [My Strategies] [AI] │ │
│ Trading      │ ├───────────────────────────────────┤ │
│ Portfolio    │                                       │
│ Strategies ─►│ ┌─────────────┐ ┌─────────────┐     │ │
│ Indicators   │ │ Moving Avg  │ │ RSI         │     │ │
│ Settings     │ │ Crossover   │ │ Oversold    │     │ │
│              │ │             │ │             │     │ │
│              │ │ Win Rate:   │ │ Win Rate:   │     │ │
│              │ │ 58% (3y)    │ │ 62% (2y)    │     │ │
│              │ │             │ │             │     │ │
│              │ │ [View] [Use]│ │ [View] [Use]│     │ │
│              │ └─────────────┘ └─────────────┘     │ │
│              │                                       │ │
│              │ ┌───────────────────────────────────┐ │
│              │ │ [+ Create New Strategy]           │ │
│              │ │ [+ AI Generate Strategy]          │ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
│              │ ┌───────────────────────────────────┐ │
│              │ │ ACTIVE STRATEGIES                 │ │
│              │ ├───────────────────────────────────┤ │
│              │ │ Strategy │ Return │ Status │ Action│
│              │ │──────────┼────────┼────────┼───────│ │
│              │ │MA Cross  │ +15.2% │ Active │[Pause]│ │
│              │ │RSI       │ +8.5%  │ Active │[Pause]│ │
│              │ └───────────────────────────────────┘ │
│              │                                       │
└──────────────┴───────────────────────────────────────┘
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

### **Widget 1: Predefined Strategies**

**Component**: `components/strategies/PredefinedStrategies.tsx`

**Backend Endpoints**:
```
GET /api/v1/strategies/predefined
GET /api/v1/strategies/predefined/{id}
POST /api/v1/strategies/predefined/{id}/backtest
```

**Data Structure**:
```typescript
interface Strategy {
  id: string;
  name: string;
  description: string;
  author: string;
  win_rate: number;
  avg_return: number;
  max_drawdown: number;
  created_at: string;
}
```

**Features**:
- Grid view of strategies
- Click to view details
- Backtest button
- Use strategy button

---

### **Widget 2: Create Custom Strategy**

**Component**: `components/strategies/CreateStrategy.tsx`

**Backend Endpoints**:
```
POST /api/v1/strategies/user
GET /api/v1/strategies/user/{id}
PUT /api/v1/strategies/user/{id}
```

**Form Fields**:
```typescript
interface CustomStrategy {
  name: string;
  description: string;
  symbol: string;
  timeframe: string;
  entry_condition: string;  // Rule
  exit_condition: string;   // Rule
  stop_loss_percent: number;
  target_profit_percent: number;
}
```

**Rule Builder**:
```
Entry Condition:
MA 20 crosses above MA 50 AND RSI < 70

Exit Condition:
Price reaches target OR RSI > 85 OR Stop loss hit
```

---

### **Widget 3: AI Strategy Generator**

**Component**: `components/strategies/AIGenerator.tsx`

**Backend Endpoints**:
```
POST /api/v1/strategies/ai/generate
POST /api/v1/strategies/ai/{id}/refine
GET /api/v1/strategies/ai/{user_id}
```

**Input**: Text prompt describing desired strategy

```typescript
interface AIStrategyRequest {
  prompt: string;
  // Example: "Create a trend-following strategy that buys when price breaks above 20-day high"
  timeframe?: string;
  risk_level?: 'low' | 'medium' | 'high';
}
```

**AI Response**:
```typescript
interface GeneratedStrategy {
  id: string;
  name: string;
  description: string;
  entry_rules: string[];
  exit_rules: string[];
  backtest_results: BacktestResult;
  confidence_score: number;
}
```

**Implementation**:
```typescript
const generateStrategy = async (prompt: string) => {
  setLoading(true);
  try {
    const response = await api.post('/strategies/ai/generate', {
      prompt,
      user_id: currentUser.id
    });
    
    // Show generated strategy
    setGeneratedStrategy(response);
    
    // Allow user to refine or accept
    setShowAcceptButton(true);
  } catch (error) {
    showError('Failed to generate strategy');
  } finally {
    setLoading(false);
  }
};
```

---

### **Widget 4: Backtest Results**

**Component**: `components/strategies/BacktestResults.tsx`

**Data Structure**:
```typescript
interface BacktestResult {
  strategy_id: string;
  total_trades: number;
  winning_trades: number;
  losing_trades: number;
  win_rate: number;
  total_return: number;
  max_drawdown: number;
  sharpe_ratio: number;
  avg_trade_value: number;
  period: string;
  start_date: string;
  end_date: string;
}
```

**Visualization**:
```
Win Rate: 58%  [████████░░] 

Total Return: +15.2%
Max Drawdown: -5.2%
Sharpe Ratio: 1.45

Equity Curve:
(Line chart showing portfolio growth)
```

---

### **Widget 5: Active Strategies**

**Component**: `components/strategies/ActiveStrategies.tsx`

**Backend Endpoints**:
```
GET /api/v1/strategies/user?status=active
POST /api/v1/strategies/user/{id}/execute
GET /api/v1/strategies/{id}/performance
```

**Features**:
- List active strategies
- Show current return
- Pause/resume strategy
- View performance
- Delete strategy

---

## 🔄 REAL-TIME UPDATES

```typescript
// Strategy performance updates
const { performanceUpdate } = useWebSocket('/ws/strategies/performance');

useEffect(() => {
  if (performanceUpdate) {
    updateStrategyPerformance(performanceUpdate);
  }
}, [performanceUpdate]);
```

---

## ✅ COMPLETION CHECKLIST FOR STRATEGIES PAGE

- [ ] Predefined strategies grid implemented
- [ ] Strategy details modal
- [ ] Create custom strategy form
- [ ] AI strategy generator working
- [ ] Backtest functionality
- [ ] Results visualization
- [ ] Active strategies manager
- [ ] Execute strategy button
- [ ] Performance tracking
- [ ] Edit/delete strategy
- [ ] All endpoints integrated
- [ ] Real-time updates working
- [ ] Responsive design
- [ ] All themes supported

---

---

# PAGE 7: INDICATORS PAGE

**File**: `pages/Indicators.tsx`  
**Components**: `components/indicators/*`  
**Backend Router**: `routers/indicators.py`  
**Backend Scripts**: indicators.py (16.6K)  

---

## 🎯 PAGE PURPOSE

Technical indicator management and calculation. Features:
1. Built-in indicators (MA, RSI, MACD, Bollinger, ATR)
2. Custom indicator creation
3. Real-time calculations
4. Visualization on charts

---

## 📊 INDICATORS PAGE LAYOUT

```
Built-in Indicators:
- Moving Average (MA)
- RSI (Relative Strength Index)
- MACD (Moving Average Convergence Divergence)
- Bollinger Bands
- ATR (Average True Range)

Custom Indicators:
- Create custom formula
- Combine existing indicators
- Backtest on historical data

Display:
- Add to chart
- Configure parameters
- Real-time updates
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

**Endpoints**:
```
GET /api/v1/indicators (list all)
POST /api/v1/indicators/{id} (calculate)
GET /api/v1/indicators/user (user's custom indicators)
POST /api/v1/indicators/user (create custom)
```

---

---

# PAGE 8: SETTINGS PAGE

**File**: `pages/Settings.tsx`  
**Components**: `components/settings/*`  
**Backend Router**: `routers/settings.py`, `routers/user.py`  
**Backend Scripts**: settings.py (9.1K), user.py (21.6K)  

---

## 🎯 PAGE PURPOSE

User configuration and credential management. Features:
1. Edit credentials (Upstox, Database, AI LLM)
2. Appearance settings (theme, style)
3. Notification preferences
4. Risk management settings
5. Data management (export/import)

---

## 📊 SETTINGS PAGE LAYOUT

```
┌──────────────────────────────────────────────────────┐
│ SETTINGS                                             │
├─────────────────────────────────────────────────────┤
│ [Credentials] [Appearance] [Notifications] [Risk] [Data]
│
│ ┌────────────────────────────────────────────────┐
│ │ CREDENTIALS                                    │
│ ├────────────────────────────────────────────────┤
│ │ ✓ Upstox API      (Configured)                │
│ │   [Edit]                                       │
│ │                                                │
│ │ ✓ Database        (Configured)                │
│ │   [Edit]                                       │
│ │                                                │
│ │ ✓ AI LLM          (Claude 3 Opus)             │
│ │   [Edit]                                       │
│ └────────────────────────────────────────────────┘
│
│ ┌────────────────────────────────────────────────┐
│ │ APPEARANCE                                     │
│ ├────────────────────────────────────────────────┤
│ │ Theme:  ● Light  ○ Dark  ○ Extra Dark        │
│ │ Style:  ● Glass  ○ Normal                     │
│ │ Font Size: [Medium ▼]                         │
│ └────────────────────────────────────────────────┘
│
│ ┌────────────────────────────────────────────────┐
│ │ NOTIFICATIONS                                  │
│ ├────────────────────────────────────────────────┤
│ │ [✓] Email Notifications                       │
│ │ [✓] Push Notifications                        │
│ │ [✓] In-App Alerts                             │
│ │ [✓] Order Confirmations                       │
│ └────────────────────────────────────────────────┘
│
│ ┌────────────────────────────────────────────────┐
│ │ RISK MANAGEMENT                                │
│ ├────────────────────────────────────────────────┤
│ │ Max Daily Loss: [2% of portfolio▼]            │
│ │ Per-Trade Risk: [1% of portfolio▼]            │
│ │ Max Position Size: [10% of portfolio▼]        │
│ │ [Save Changes]                                 │
│ └────────────────────────────────────────────────┘
│
│ ┌────────────────────────────────────────────────┐
│ │ DATA MANAGEMENT                                │
│ ├────────────────────────────────────────────────┤
│ │ [Export All Data]                              │
│ │ [Import Data]                                  │
│ │ [Clear Cache]                                  │
│ │ [Delete Account]                               │
│ └────────────────────────────────────────────────┘
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

**Credentials Tab**:
```
PUT /api/v1/user/account-settings (edit credentials)
GET /api/v1/user/account-settings (fetch current)
```

**Appearance Tab**:
```
PUT /api/v1/settings/general (save theme preferences)
GET /api/v1/settings/general (fetch current)
```

**Notifications Tab**:
```
PUT /api/v1/settings/notifications (save preferences)
GET /api/v1/settings/notifications (fetch current)
```

**Risk Tab**:
```
PUT /api/v1/user/risk-preferences (save limits)
GET /api/v1/user/risk-preferences (fetch current)
```

**Data Tab**:
```
GET /api/v1/user/profile (export data)
POST /api/v1/user/profile (import data)
```

---

## 🔐 SECURITY FOR CREDENTIALS EDITING

When user edits credentials:
1. Re-authenticate with current password
2. Validate new credentials before saving
3. Show confirmation modal
4. Update securely on backend
5. Clear sensitive data from frontend

```typescript
const editCredentials = async (newCredentials) => {
  // 1. Request authentication
  const password = await promptPassword();
  
  // 2. Verify password
  const verified = await api.post('/auth/verify-password', { password });
  
  if (!verified) {
    showError('Incorrect password');
    return;
  }
  
  // 3. Test new credentials
  const testResult = await testConnection(newCredentials);
  
  if (!testResult.success) {
    showError('Invalid credentials');
    return;
  }
  
  // 4. Show confirmation
  const confirmed = await showConfirmation(
    'Update credentials?',
    'This will restart connections'
  );
  
  if (confirmed) {
    // 5. Send to backend (encrypted)
    await api.put('/user/account-settings', {
      [credentialType]: encryptData(newCredentials)
    });
    
    showSuccess('Credentials updated');
  }
};
```

---

## ✅ COMPLETION CHECKLIST FOR SETTINGS PAGE

- [ ] All tabs implemented
- [ ] Credentials editing working
- [ ] Theme selection functional
- [ ] Notifications preferences saving
- [ ] Risk limits configuration
- [ ] Export/import data
- [ ] Authentication for sensitive edits
- [ ] All endpoints integrated
- [ ] Confirmation modals working
- [ ] Error handling in place
- [ ] Responsive design
- [ ] All themes supported
- [ ] Security measures in place

---

---

# 🎯 DEVELOPMENT WORKFLOW SUMMARY

## **Quick Start Development Order**

```
Week 1: Foundation
├─ Setup page (5-step wizard)
├─ Theme system + CSS variables
├─ Authentication
└─ Navigation layout

Week 2: Core
├─ Dashboard (6 widgets)
├─ Market data page
└─ WebSocket integration

Week 3: Trading
├─ Trading page (order form)
├─ Real-time margin calculation
└─ Order history

Week 4: Portfolio
├─ Portfolio page (holdings, positions)
├─ Performance charts
└─ Risk metrics

Week 5: Advanced
├─ Strategies page (AI generation)
├─ Indicators page
└─ Backtesting

Week 6: Finalization
├─ Settings page
├─ Error handling
├─ Performance optimization
└─ Testing & deployment
```

---

**This completes the PAGE-BY-PAGE development guide for all 8 pages!**

