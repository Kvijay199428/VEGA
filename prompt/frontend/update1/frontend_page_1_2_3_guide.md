# 📖 PAGE-BY-PAGE FRONTEND DEVELOPMENT GUIDE

**For**: VEGA TRADER'S - AI-Enabled Trading Platform  
**Date**: December 13, 2025, 11:14 AM IST  
**Purpose**: Detailed frontend page development with backend endpoint mapping

---

## 🎯 QUICK REFERENCE - PAGE TO ENDPOINT MAPPING

| Page | Backend Router | Backend Script | Endpoints Count | Status |
|------|----------------|----------------|-----------------|--------|
| 1. Setup Page | N/A | user.py, ai_service.py | 3 | NEW |
| 2. Dashboard | market.py, portfolio.py | market.py, portfolio.py | 5 | NEW |
| 3. Market Data | market.py | market.py | 8 | ENHANCED |
| 4. Trading | orders.py | orders.py, upstox_service.py | 7 | NEW |
| 5. Portfolio | portfolio.py | portfolio.py, portfolio_service.py | 6 | NEW |
| 6. Strategies | strategies.py | strategies.py, ai_service.py | 13 | NEW |
| 7. Indicators | indicators.py | indicators.py | 6 | NEW |
| 8. Settings | settings.py, user.py | user.py, settings.py | 12 | NEW |

---

# PAGE 1: SETUP WIZARD PAGE

**File**: `pages/Setup.tsx`  
**Components**: `components/setup/*`  
**Backend Router**: `routers/user.py`, `routers/settings.py`  
**Backend Script Size**: user.py (21.6K), settings.py (9.1K)  
**Related Services**: `upstox_service.py`, `ai_service.py`

---

## 🎯 PAGE PURPOSE

First-time user configuration page. Appears only on first login. Collects:
1. Upstox API credentials
2. Database configuration (optional)
3. AI LLM configuration
4. Theme & style preferences
5. Review and complete

**After completion**: Redirect to `/dashboard`

---

## 📋 STEP-BY-STEP IMPLEMENTATION

### **STEP 1: Upstox API Credentials**

**File**: `components/setup/Step1_Upstox.tsx`

**Form Fields**:
```typescript
interface UpstoxCredentials {
  api_key: string;           // Required, min 20 chars
  api_secret: string;        // Required, min 20 chars
  redirect_uri: string;      // Default: http://localhost:28020/callback
}
```

**UI Layout**:
```
┌─────────────────────────────────────┐
│ Step 1 of 5: Upstox API Setup       │
├─────────────────────────────────────┤
│                                     │
│ 🔐 API Key                          │
│ [_________________________________] │ (password input)
│ Get from: https://upstox.com/dev   │
│                                     │
│ 🔐 API Secret                       │
│ [_________________________________] │ (password input)
│ Keep this secure                    │
│                                     │
│ 🔗 Redirect URI                     │
│ [http://localhost:28020/callback___] │
│ Auto-detected from current URL      │
│                                     │
│ [Test Connection] [Clear Form]      │
│                                     │
│ ✓ Connection successful!            │
│                                     │
│ [← Back] [Skip] [Next →]            │
│                                     │
└─────────────────────────────────────┘
```

**Validation Rules**:
- API Key: Required, min 20 characters
- API Secret: Required, min 20 characters
- Redirect URI: Required, valid URL format
- Test connection before proceeding

**Backend Integration**:
```typescript
// Test connection
POST /api/v1/auth/login (with code from OAuth)
GET /api/v1/user/account-info (verify credentials)

// These validate that Upstox credentials work
```

**State Management**:
```typescript
// useSetupWizard hook
const [step1Data, setStep1Data] = useState({
  api_key: '',
  api_secret: '',
  redirect_uri: 'http://localhost:28020/callback'
});

// Store encrypted in Redux
dispatch(setSetupStep1(encryptData(step1Data)));
```

**Error Handling**:
```typescript
// Handle common errors
- Invalid credentials
- Network timeout
- Wrong credentials format
- Upstox service unavailable

// Show: Toast with error message + retry button
```

---

### **STEP 2: Database Configuration (Optional)**

**File**: `components/setup/Step2_Database.tsx`

**Form Fields**:
```typescript
interface DatabaseConfig {
  enabled: boolean;
  db_type: 'postgresql' | 'sqlite' | 'skip';
  host?: string;
  port?: number;
  database?: string;
  username?: string;
  password?: string;
}
```

**UI Layout**:
```
┌──────────────────────────────────────┐
│ Step 2 of 5: Database Setup          │
├──────────────────────────────────────┤
│                                      │
│ Enable Database: [✓] Yes  [ ] No     │
│                                      │
│ Database Type:                       │
│ ● PostgreSQL  ○ SQLite  ○ Skip       │
│                                      │
│ ┌──────────────────────────────────┐ │
│ │ Host: [localhost_____________]   │ │
│ │ Port: [5432___]                  │ │
│ │ Database: [vegatrades____]      │ │
│ │ Username: [postgres_______]       │ │
│ │ Password: [••••••••_____]         │ │
│ └──────────────────────────────────┘ │
│                                      │
│ [Auto-detect Local] [Test Conn]      │
│                                      │
│ ✓ Database connected successfully!   │
│                                      │
│ [← Back] [Skip] [Next →]             │
│                                      │
└──────────────────────────────────────┘
```

**Features**:
- Auto-detect local PostgreSQL
- Test connection button
- Option to skip (local SQLite will be used)
- Show connection status

**Validation Rules**:
- If enabled: All fields required
- Port: 1-65535
- Host: Valid hostname
- Test connection must succeed

**Backend Integration**:
```typescript
// Send to backend to test connection
POST /api/v1/settings/general
{
  database_config: {
    host: 'localhost',
    port: 5432,
    // ... etc
  }
}

// Verify connection on backend
Response: { success: true, message: 'Connected' }
```

---

### **STEP 3: AI LLM Configuration**

**File**: `components/setup/Step3_AI.tsx`

**Form Fields**:
```typescript
interface AIConfig {
  provider: 'claude' | 'gpt4' | 'local' | 'ollama';
  api_key?: string;           // For Claude/GPT
  api_url?: string;           // For local/Ollama
  model_name?: string;        // e.g., 'claude-3-opus'
  temperature?: number;       // 0-1
  max_tokens?: number;        // Default: 2048
}
```

**UI Layout**:
```
┌──────────────────────────────────────┐
│ Step 3 of 5: AI LLM Setup            │
├──────────────────────────────────────┤
│                                      │
│ AI Provider:                         │
│ ○ Claude     ○ GPT-4                 │
│ ○ Local LLM  ○ Ollama                │
│                                      │
│ ┌──────────────────────────────────┐ │
│ │ API Key: [____________________] │ │
│ │ (Get from https://claude.ai)     │ │
│ │                                  │ │
│ │ Model: [Claude-3-Opus ▼]         │ │
│ │                                  │ │
│ │ Temperature: [████░░░░░░] 0.7    │ │
│ │ Max Tokens: [2048______]         │ │
│ └──────────────────────────────────┘ │
│                                      │
│ [Test Connection]                    │
│                                      │
│ ✓ Model responding correctly!        │
│                                      │
│ [← Back] [Skip] [Next →]             │
│                                      │
└──────────────────────────────────────┘
```

**Alternative: Local LLM**:
```
┌──────────────────────────────────────┐
│ Local LLM Configuration              │
├──────────────────────────────────────┤
│                                      │
│ Host: [localhost________]            │
│ Port: [28021_]                        │
│                                      │
│ Model Name: [mistral▼]               │
│                                      │
│ [Test Connection]                    │
│                                      │
└──────────────────────────────────────┘
```

**Validation Rules**:
- Provider: Required
- API Key: Required if using Claude/GPT (min 20 chars)
- Local URL: Valid format (http://localhost:port)
- Test connection must succeed

**Backend Integration**:
```typescript
// Save AI config
POST /api/v1/settings/general
{
  ai_config: {
    provider: 'claude',
    api_key: 'sk-...', // Encrypted on backend
    model: 'claude-3-opus'
  }
}

// Test by generating sample strategy
POST /api/v1/strategies/ai/generate
{
  prompt: 'test',
  user_id: current_user_id
}
```

---

### **STEP 4: Theme & Style Selection**

**File**: `components/setup/Step4_Theme.tsx`

**Form Fields**:
```typescript
interface ThemePreferences {
  theme: 'light' | 'dark' | 'extra-dark';
  style: 'glass' | 'normal';
  font_size: 'small' | 'medium' | 'large';
  language: 'en' | 'es' | 'fr'; // Future
  notifications_enabled: boolean;
}
```

**UI Layout**:
```
┌──────────────────────────────────────┐
│ Step 4 of 5: Preferences             │
├──────────────────────────────────────┤
│                                      │
│ Theme Selection:                     │
│ ┌──────────┐ ┌──────────┐ ┌────────┐│
│ │   Light  │ │   Dark   │ │ Extra  ││
│ │  (○)     │ │  (●)     │ │ Dark   ││
│ │          │ │          │ │  (○)   ││
│ └──────────┘ └──────────┘ └────────┘│
│                                      │
│ Design Style:                        │
│ ┌──────────────┐ ┌──────────────┐   │
│ │ Glass Morph  │ │ Normal       │   │
│ │   (●)        │ │   (○)        │   │
│ │ Frosted      │ │ Traditional  │   │
│ └──────────────┘ └──────────────┘   │
│                                      │
│ Font Size: [Medium ▼]                │
│                                      │
│ Notifications: [✓] Enable            │
│                                      │
│ PREVIEW:                             │
│ ┌──────────────────────────────────┐ │
│ │ This is how it will look         │ │
│ │ with your selected preferences   │ │
│ └──────────────────────────────────┘ │
│                                      │
│ [← Back] [Skip] [Next →]             │
│                                      │
└──────────────────────────────────────┘
```

**Preview Feature**:
- Show real preview of selected theme + style
- Update live as user changes selections
- Show in current selection

**State Management**:
```typescript
// Theme changes
dispatch(setTheme('dark'));
dispatch(setStyle('glass'));

// Apply to Redux store
store.dispatch(setThemePreferences({...}));
```

---

### **STEP 5: Review & Complete**

**File**: `components/setup/Step5_Review.tsx`

**UI Layout**:
```
┌────────────────────────────────────────┐
│ Step 5 of 5: Review Configuration      │
├────────────────────────────────────────┤
│                                        │
│ 📋 REVIEW YOUR SETTINGS                │
│                                        │
│ ┌────────────────────────────────────┐ │
│ │ Upstox API                         │ │
│ │ ✓ Configured and tested            │ │
│ │ [Edit]                             │ │
│ └────────────────────────────────────┘ │
│                                        │
│ ┌────────────────────────────────────┐ │
│ │ Database                           │ │
│ │ ✓ PostgreSQL - localhost:5432      │ │
│ │ [Edit]                             │ │
│ └────────────────────────────────────┘ │
│                                        │
│ ┌────────────────────────────────────┐ │
│ │ AI LLM                             │ │
│ │ ✓ Claude 3 Opus                    │ │
│ │ [Edit]                             │ │
│ └────────────────────────────────────┘ │
│                                        │
│ ┌────────────────────────────────────┐ │
│ │ Theme & Style                      │ │
│ │ Dark Mode + Glass Morphism         │ │
│ │ [Edit]                             │ │
│ └────────────────────────────────────┘ │
│                                        │
│ All set! You're ready to trade.       │
│                                        │
│ [← Previous] [Complete Setup →]        │
│                                        │
│ After clicking Complete Setup:        │
│ → Credentials saved (encrypted)       │
│ → Redirected to Dashboard             │
│ → Setup form never shown again        │
│                                        │
└────────────────────────────────────────┘
```

**Edit Feature**:
- Click [Edit] to go back to that step
- Make changes
- Return to Step 5
- Repeat as needed

**On Complete**:
```typescript
// Save all configuration
POST /api/v1/user/account-settings
{
  upstox_config: {...encrypted...},
  database_config: {...encrypted...},
  ai_config: {...encrypted...},
  theme_preferences: {...}
}

// Set flag in database: setup_completed = true
// Redirect to dashboard
navigate('/dashboard');
```

---

## 🔐 SECURITY IMPLEMENTATION

**Credential Encryption**:
```typescript
// Frontend (before sending)
const encrypted = await encryptAES256(credentials, masterPassword);

// Backend receives encrypted, stores in secure manner
// Never log sensitive data
```

**Storage**:
- Redux: Encrypted state
- LocalStorage: Only non-sensitive preferences (theme)
- Secure HTTP-only cookies: JWT tokens

**Error Handling**:
```typescript
// Don't show:
// ❌ "Invalid API Secret: sk-123..."
// ✅ Show: "API credentials invalid. Please check and try again."

// Handle all API errors gracefully
try {
  await testConnection(credentials);
} catch (error) {
  if (error.code === 'INVALID_CREDENTIALS') {
    setError('Invalid credentials. Please verify and try again.');
  } else if (error.code === 'NETWORK_ERROR') {
    setError('Network error. Check your connection.');
  }
}
```

---

## 📱 RESPONSIVE DESIGN

**Mobile (<768px)**:
- Full-screen form
- Larger touch targets (48px minimum)
- Single column layout
- Bigger font sizes
- Horizontal scrolling for code/tokens

**Tablet (768-1024px)**:
- 2-column form layout where applicable
- Medium touch targets (40px)
- Centered form max-width

**Desktop (>1024px)**:
- Wizard container: 600px width
- Centered on screen
- All steps visible in sidebar

---

## 🎨 THEME SUPPORT

**Setup page should work in all themes**:
- ✅ Light mode (readable, clean)
- ✅ Dark mode (high contrast)
- ✅ Extra dark mode (comfortable for long use)
- ✅ Glass morphism (frosted effect)
- ✅ Normal style (traditional cards)

**Theme toggle during setup**: Show real preview

---

## ✅ COMPLETION CHECKLIST FOR SETUP PAGE

- [ ] All 5 steps implemented
- [ ] Form validation working
- [ ] Test connection buttons functional
- [ ] Error messages user-friendly
- [ ] Loading states during API calls
- [ ] Encryption before sending to backend
- [ ] All themes tested
- [ ] Responsive on mobile/tablet/desktop
- [ ] Keyboard navigation working
- [ ] Accessibility (WCAG 2.1 AA)
- [ ] Skip option available (except Step 5)
- [ ] Edit buttons on Step 5 functional
- [ ] Redirect to dashboard on completion
- [ ] Setup never shown again after completion

---

---

# PAGE 2: DASHBOARD PAGE

**File**: `pages/Dashboard.tsx`  
**Components**: `components/dashboard/*`  
**Backend Routers**: `routers/market.py`, `routers/portfolio.py`  
**Backend Scripts**: market.py (8.5K), portfolio.py (13.1K)  
**Related Services**: market_data_service.py, portfolio_service.py  

---

## 🎯 PAGE PURPOSE

Main landing page after login. Shows comprehensive overview of:
1. Market indices and performance
2. Portfolio summary and P&L
3. Open positions with real-time updates
4. Recent trades
5. Portfolio performance chart
6. Quick action buttons

**Refresh Rate**: Real-time WebSocket updates for quotes and positions

---

## 📊 DASHBOARD LAYOUT

```
┌─────────────────────────────────────────────────────────────┐
│ 🔵 VEGA TRADER'S        [Market: ↑ 2.5%]  ⚙️  👤  🌙     │ Header
├──────────────┬──────────────────────────────────────────────┤
│ Dashboard    │                                              │
│ Market       │ ┌────────────────────────────────────────┐  │
│ Trading      │ │  📊 MARKET OVERVIEW (1)                │  │
│ Portfolio    │ ├────────────────────────────────────────┤  │
│ Strategies   │ │ NIFTY 50     │ 23,450.50  │ ↑ +1.2%   │  │
│ Indicators   │ │ BANK NIFTY   │ 48,800.25  │ ↑ +0.8%   │  │
│ Settings     │ │ MIDCAP 50    │ 8,900.20   │ ↓ -0.3%   │  │
│              │ │ SENSEX       │ 75,000.00  │ ↑ +1.5%   │  │
│              │ └────────────────────────────────────────┘  │
│              │                                              │
│              │ ┌────────────────────────────────────────┐  │
│              │ │  💰 PORTFOLIO SUMMARY (2)              │  │
│              │ ├────────────────────────────────────────┤  │
│              │ │ Total Value:     ₹ 5,50,000           │  │
│              │ │ Invested:        ₹ 3,00,000           │  │
│              │ │ Cash Available:  ₹ 2,50,000           │  │
│              │ │ Today's P&L:     ₹ 12,500 (+2.3%)     │  │
│              │ │ All-Time Return: +18.5%               │  │
│              │ └────────────────────────────────────────┘  │
│              │                                              │
│              │ ┌────────────────────────────────────────┐  │
│              │ │  📈 OPEN POSITIONS (3)                 │  │
│              │ ├────────────────────────────────────────┤  │
│              │ │ Sym │ Qty │  Entry │ Current│ P&L │ %  │  │
│              │ │─────┼─────┼────────┼────────┼─────┼────│  │
│              │ │REL  │100  │ 2,800  │ 2,850  │+850 │+3% │  │
│              │ │TCS  │50   │ 3,250  │ 3,200  │-200 │-1% │  │
│              │ │INFY │25   │ 1,950  │ 2,000  │+500 │+2% │  │
│              │ └────────────────────────────────────────┘  │
│              │                                              │
│              │ ┌────────────────────────────────────────┐  │
│              │ │  📋 RECENT TRADES (Last 5) (4)         │  │
│              │ ├────────────────────────────────────────┤  │
│              │ │ Symbol │ Type│ Qty │ Price│ Time       │  │
│              │ │─────────┼─────┼─────┼──────┼────────────│  │
│              │ │RELIANCE │ BUY │ 100 │2,850 │09:15 AM   │  │
│              │ │TCS      │ SELL│ 50  │3,200 │08:45 AM   │  │
│              │ └────────────────────────────────────────┘  │
│              │                                              │
│              │ ┌────────────────────────────────────────┐  │
│              │ │  📊 PORTFOLIO VALUE TREND (5)          │  │
│              │ ├────────────────────────────────────────┤  │
│              │ │                                        │  │
│              │ │     ╱╲                                 │  │
│              │ │    ╱  ╲       ╱╲                       │  │
│              │ │   ╱    ╲     ╱  ╲    ╱╲  ╱╲           │  │
│              │ │  ╱      ╲   ╱    ╲╲╱  ╲╱  ╲╲          │  │
│              │ │         ╲╱          ╱╲    ╲╲         │  │
│              │ │                                        │  │
│              │ │ [7D] [30D] [90D] [1Y] [ALL]            │  │
│              │ └────────────────────────────────────────┘  │
│              │                                              │
│              │ ┌────────────────────────────────────────┐  │
│              │ │  ⚡ QUICK ACTIONS (6)                  │  │
│              │ ├────────────────────────────────────────┤  │
│              │ │ [Place Order] [View Holdings]          │  │
│              │ │ [AI Strategy] [Market Analysis]        │  │
│              │ └────────────────────────────────────────┘  │
│              │                                              │
└──────────────┴──────────────────────────────────────────────┘
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

### **Widget 1: Market Overview**

**Component**: `components/dashboard/MarketOverview.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/indices
GET /api/v1/market/quote?symbol=NIFTY
GET /api/v1/market/quote?symbol=BANKNIFTY
GET /api/v1/market/quote?symbol=MIDCAP50
GET /api/v1/market/quote?symbol=SENSEX
```

**Data Structure**:
```typescript
interface IndexData {
  symbol: string;
  name: string;
  current_price: number;
  change_amount: number;
  change_percent: number;
  timestamp: string;
}
```

**Refresh Strategy**:
- Initial load: API call
- Real-time: WebSocket `/ws/market/live-quotes`
- Update every quote received from WebSocket
- Color: Green if +, Red if -

**Error Handling**:
```typescript
if (error) {
  return <ErrorWidget message="Failed to load market data" />;
}
```

---

### **Widget 2: Portfolio Summary**

**Component**: `components/dashboard/PortfolioSummary.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/summary
```

**Data Structure**:
```typescript
interface PortfolioSummary {
  total_value: number;
  invested_amount: number;
  cash_available: number;
  today_pnl: {
    amount: number;
    percentage: number;
  };
  all_time_return: number;
  last_updated: string;
}
```

**Calculation Logic**:
```typescript
const totalValue = investedAmount + cashAvailable;
const todayPnL = currentValue - yesterdayValue;
const todayPnLPercent = (todayPnL / yesterdayValue) * 100;
```

**Display Format**:
```typescript
// Colors
const pnlColor = todayPnL >= 0 ? 'green' : 'red';
const pnlIcon = todayPnL >= 0 ? '↑' : '↓';

// Format: ₹5,50,000 or ₹55.5L
const formatted = formatCurrency(totalValue);
```

---

### **Widget 3: Open Positions**

**Component**: `components/dashboard/PositionsTable.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/positions
```

**Real-time Updates**:
```
WebSocket: /ws/market/live-quotes (price updates)
WebSocket: /ws/portfolio/pnl (P&L recalculation)
```

**Data Structure**:
```typescript
interface Position {
  symbol: string;
  quantity: number;
  entry_price: number;
  current_price: number;
  pnl_amount: number;
  pnl_percent: number;
  entry_time: string;
}
```

**Real-time Calculation**:
```typescript
// When price updates via WebSocket
const newPnL = (currentPrice - entryPrice) * quantity;
const newPnLPercent = ((newPnL / (entryPrice * quantity)) * 100);

// Update with animation
animateChange(pnlElement, newPnL);
```

**Table Features**:
- Sortable columns (click header)
- Filterable by symbol
- Click row for detailed view
- Real-time price color flash (green/red)
- Remove position option (requires close confirmation)

---

### **Widget 4: Recent Trades**

**Component**: `components/dashboard/TradesTable.tsx`

**Backend Endpoints**:
```
GET /api/v1/orders/trades
```

**Data Structure**:
```typescript
interface Trade {
  trade_id: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  executed_at: string;
  execution_time: string;
}
```

**Display**:
- Show last 5 trades
- Link to "View All" in Orders page
- Buy = Green, Sell = Red icon
- Time formatted: "09:15 AM"

---

### **Widget 5: Portfolio Value Chart**

**Component**: `components/dashboard/PortfolioChart.tsx`

**Backend Endpoints**:
```
GET /api/v1/portfolio/snapshots?period=30days
```

**Data Structure**:
```typescript
interface PortfolioSnapshot {
  date: string;
  total_value: number;
  invested_amount: number;
  cash: number;
}
```

**Chart Type**: Area chart (Recharts)

```typescript
<AreaChart data={snapshots} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
  <defs>
    <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
      <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.8}/>
      <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
    </linearGradient>
  </defs>
  <XAxis dataKey="date" />
  <YAxis />
  <Tooltip />
  <Area type="monotone" dataKey="total_value" stroke="#3B82F6" fillOpacity={1} fill="url(#colorValue)" />
</AreaChart>
```

**Period Options**: [7D] [30D] [90D] [1Y] [ALL]

**Interactive Features**:
- Hover to see tooltip
- Click period to change data
- Zoom in/out (optional)

---

### **Widget 6: Quick Actions**

**Component**: `components/dashboard/QuickActions.tsx`

**Actions**:
1. Place Order → Navigate to `/trading`
2. View Holdings → Navigate to `/portfolio`
3. AI Strategy → Navigate to `/strategies`
4. Market Analysis → Navigate to `/market`

**Implementation**:
```typescript
const buttons = [
  { label: 'Place Order', icon: '📊', action: () => navigate('/trading') },
  { label: 'View Holdings', icon: '📋', action: () => navigate('/portfolio') },
  { label: 'AI Strategy', icon: '🤖', action: () => navigate('/strategies') },
  { label: 'Market Analysis', icon: '📈', action: () => navigate('/market') }
];
```

---

## 🔄 REAL-TIME UPDATES

**WebSocket Integration**:

```typescript
// hooks/useWebSocket.ts
const { data: liveQuotes } = useWebSocket('/ws/market/live-quotes');
const { data: pnlUpdates } = useWebSocket('/ws/portfolio/pnl');

// Update positions when price changes
useEffect(() => {
  if (liveQuotes) {
    updatePosition(liveQuotes.symbol, liveQuotes.price);
  }
}, [liveQuotes]);

// Update portfolio summary when P&L changes
useEffect(() => {
  if (pnlUpdates) {
    updatePortfolioSummary(pnlUpdates);
  }
}, [pnlUpdates]);
```

**Update Frequency**:
- Market data: Every 1 second
- Portfolio P&L: Every 2 seconds
- Positions: Real-time on every price update

---

## 📱 RESPONSIVE DESIGN

**Desktop (>1024px)**:
- 2-column grid layout
- All widgets visible
- Full-width chart
- Normal table

**Tablet (768-1024px)**:
- 2-column grid
- Stacked widgets
- Scrollable table

**Mobile (<768px)**:
- Full-width stacked
- Cards take full width
- Horizontal scrollable tables
- Smaller charts

---

## ⚡ PERFORMANCE OPTIMIZATION

**Lazy Loading**:
```typescript
const MarketOverview = lazy(() => import('./MarketOverview'));
const PortfolioChart = lazy(() => import('./PortfolioChart'));

// Load in order of importance
<Suspense fallback={<Skeleton />}>
  <PortfolioSummary /> {/* High priority */}
</Suspense>

<Suspense fallback={<Skeleton />}>
  <MarketOverview /> {/* Medium priority */}
</Suspense>
```

**Memoization**:
```typescript
// Prevent unnecessary re-renders
const PositionsTable = memo(({ positions }: Props) => {
  return <Table data={positions} />;
}, (prev, next) => {
  // Only re-render if positions actually changed
  return JSON.stringify(prev.positions) === JSON.stringify(next.positions);
});
```

**Redux Selectors**:
```typescript
// Use selectors to prevent full component re-renders
const positions = useSelector(selectPositions);
const summary = useSelector(selectPortfolioSummary);

// Selector with memoization (using reselect)
const selectTotalValue = createSelector(
  selectPortfolioSummary,
  summary => summary.total_value
);
```

---

## 🎨 THEME SUPPORT

All dashboard widgets support:
- ✅ Light mode (white backgrounds, dark text)
- ✅ Dark mode (dark backgrounds, light text)
- ✅ Extra dark mode (black backgrounds, white text)
- ✅ Glass morphism (frosted cards)
- ✅ Normal style (solid cards)

**Color Application**:
```typescript
// Use CSS variables for colors
const backgroundColor = 'var(--bg-secondary)';
const textColor = 'var(--text-primary)';
const accentColor = 'var(--accent)';
```

---

## ✅ COMPLETION CHECKLIST FOR DASHBOARD

- [ ] All 6 widgets implemented
- [ ] Real-time WebSocket updates working
- [ ] Market overview updating correctly
- [ ] Portfolio summary calculating correctly
- [ ] Positions table showing real-time P&L
- [ ] Recent trades displaying correctly
- [ ] Chart rendering with correct data
- [ ] Quick action buttons functional
- [ ] All API endpoints integrated
- [ ] Error states handled gracefully
- [ ] Loading states with skeletons
- [ ] Responsive on all screen sizes
- [ ] All themes tested
- [ ] Performance optimized (no lag)
- [ ] Accessibility (WCAG 2.1 AA)
- [ ] Auto-refresh working (real-time updates)

---

---

# PAGE 3: MARKET DATA PAGE

**File**: `pages/Market.tsx`  
**Components**: `components/market/*`  
**Backend Router**: `routers/market.py`  
**Backend Script**: market.py (8.5K)  
**Related Services**: market_data_service.py, upstox_service.py

---

## 🎯 PAGE PURPOSE

Comprehensive market data viewing page. Features:
1. Watchlist (customizable, real-time quotes)
2. Market heatmap (sector performance)
3. Technical charts (candlestick, OHLC)
4. Order book (market depth)
5. Market indices tracking
6. Live quotes grid

**Refresh Rate**: Real-time WebSocket updates

---

## 📊 MARKET PAGE LAYOUT

```
┌─────────────────────────────────────────────────────────┐
│ 🔵 VEGA TRADER'S  [Search......]  ⚙️  👤  🌙       │
├──────────────┬──────────────────────────────────────────┤
│ Dashboard    │                                          │
│ Market       │ ┌─────────────────────────────────────┐ │
│ Trading ────►│ │  📋 WATCHLIST (1)                   │ │
│ Portfolio    │ ├─────────────────────────────────────┤ │
│ Strategies   │ │ Sym │ Price │ Change│ % │ Volume   │ │
│ Indicators   │ ├─────┼───────┼───────┼───┼──────────┤ │
│ Settings     │ │INFY │2,000  │ +50   │+3%│ 1.2 Cr   │ │
│              │ │TCS  │3,200  │ -100  │-3%│ 800 K    │ │
│              │ │REL  │2,850  │ +200  │+7%│ 2 Cr     │ │
│              │ │[+ Add more]                         │ │
│              │ └─────────────────────────────────────┘ │
│              │                                          │
│              │ ┌─────────────────────────────────────┐ │
│              │ │  🔥 MARKET HEATMAP (2)              │ │
│              │ ├─────────────────────────────────────┤ │
│              │ │ [IT] [Banking] [Auto] [Pharma] ..   │ │
│              │ │  +5%    +2%     -1%    +3%         │ │
│              │ │                                     │ │
│              │ │ (Color-coded grid visualization)    │ │
│              │ └─────────────────────────────────────┘ │
│              │                                          │
│              │ ┌─────────────────────────────────────┐ │
│              │ │  📈 TECHNICAL CHART (3)             │ │
│              │ ├─────────────────────────────────────┤ │
│              │ │ Symbol: [INFY ▼] Period: [1D ▼]    │ │
│              │ │                                     │ │
│              │ │  ▐█▌  ▐█▌      ▐█▌                │ │
│              │ │  ▐█▌  ▐█▌  ▐█▌ ▐█▌                │ │
│              │ │  ▐█▌  ▐█▌  ▐█▌ ▐█▌                │ │
│              │ │                                     │ │
│              │ │ [1M] [5M] [15M] [1H] [1D] [1W] [1M]│ │
│              │ │                                     │ │
│              │ │ [Add Indicators ▼]                 │ │
│              │ │                                     │ │
│              │ └─────────────────────────────────────┘ │
│              │                                          │
│              │ ┌─────────────────────────────────────┐ │
│              │ │  📊 ORDER BOOK (4)                  │ │
│              │ ├─────────────────────────────────────┤ │
│              │ │ BID        │ ASK                     │ │
│              │ │─────────────┼──────────────          │ │
│              │ │ 2,100  (50) │ (50)  2,110           │ │
│              │ │ 2,090  (100)│ (100) 2,120           │ │
│              │ │ 2,080  (75) │ (75)  2,130           │ │
│              │ └─────────────────────────────────────┘ │
│              │                                          │
│              │ ┌─────────────────────────────────────┐ │
│              │ │  📈 LIVE QUOTES GRID (5)            │ │
│              │ ├─────────────────────────────────────┤ │
│              │ │ ┌──────┐ ┌──────┐ ┌──────┐ ┌────┐ │ │
│              │ │ │INFY  │ │TCS   │ │REL   │ │... │ │ │
│              │ │ │2,000 │ │3,200 │ │2,850 │ │    │ │ │
│              │ │ │  +3% │ │  -3% │ │  +7% │ │    │ │ │
│              │ │ └──────┘ └──────┘ └──────┘ └────┘ │ │
│              │ │                                    │ │
│              │ │ [← Previous] [Next →]              │ │
│              │ └─────────────────────────────────────┘ │
│              │                                          │
└──────────────┴──────────────────────────────────────────┘
```

---

## 🔌 BACKEND ENDPOINT INTEGRATION

### **Widget 1: Watchlist**

**Component**: `components/market/Watchlist.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/instruments
GET /api/v1/market/quote?symbol=X
GET /api/v1/market/quote?symbol=Y
WebSocket: /ws/market/live-quotes (real-time updates)
```

**Data Structure**:
```typescript
interface WatchlistItem {
  symbol: string;
  name: string;
  current_price: number;
  change_amount: number;
  change_percent: number;
  volume: number;
  timestamp: string;
}
```

**Features**:
- Add to watchlist (search and select)
- Remove from watchlist
- Real-time price updates
- Sortable columns
- Click symbol to view chart
- Buy/Sell quick buttons

**Storage**:
```typescript
// Save watchlist to Redux + localStorage
dispatch(addToWatchlist(symbol));

// Persist across sessions
localStorage.setItem('watchlist', JSON.stringify(watchlist));
```

---

### **Widget 2: Market Heatmap**

**Component**: `components/market/MarketHeatmap.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/heatmap
GET /api/v1/market/heat-map?filter=sector
```

**Data Structure**:
```typescript
interface SectorData {
  sector_name: string;
  change_percent: number;
  color_code: string; // Based on percentage
  stocks_count: number;
}
```

**Color Coding**:
```typescript
const getHeatmapColor = (changePercent: number) => {
  if (changePercent >= 5) return '#10B981';      // Strong green
  if (changePercent >= 2) return '#34D399';      // Light green
  if (changePercent >= 0) return '#D1FAE5';      // Very light green
  if (changePercent >= -2) return '#FEE2E2';     // Very light red
  if (changePercent >= -5) return '#FCA5A5';     // Light red
  return '#EF4444';                              // Strong red
};
```

**Interactive**:
- Hover to see tooltip with percentage
- Click sector to filter stocks
- Tooltip shows: Sector name, change %, number of stocks

---

### **Widget 3: Technical Chart**

**Component**: `components/market/TechnicalChart.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/ohlc?symbol=X&interval=1m
GET /api/v1/market/ohlc?symbol=X&interval=5m
GET /api/v1/market/ohlc?symbol=X&interval=15m
GET /api/v1/market/ohlc?symbol=X&interval=1h
GET /api/v1/market/ohlc?symbol=X&interval=1d
GET /api/v1/market/ohlc?symbol=X&interval=1w
GET /api/v1/market/ohlc?symbol=X&interval=1M
```

**Data Structure**:
```typescript
interface OHLC {
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}
```

**Chart Library**: Recharts with Candlestick

```typescript
<ComposedChart data={ohlcData}>
  <XAxis dataKey="timestamp" />
  <YAxis />
  <Tooltip />
  <Bar dataKey="open" />
  <Bar dataKey="close" />
  {/* Candlestick visualization */}
</ComposedChart>
```

**Features**:
- Multiple time intervals [1M, 5M, 15M, 1H, 1D, 1W, 1M]
- Add technical indicators (MA, RSI, MACD, Bollinger)
- Zoom in/out
- Pan left/right
- Full screen view option
- Download as image

---

### **Widget 4: Order Book**

**Component**: `components/market/OrderBook.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/depth?symbol=X
WebSocket: /ws/market/depth (real-time order book)
```

**Data Structure**:
```typescript
interface OrderBookLevel {
  price: number;
  quantity: number;
  orders: number;
}

interface OrderBook {
  bids: OrderBookLevel[];
  asks: OrderBookLevel[];
}
```

**Display**:
```
BID SIDE                ASK SIDE
─────────────           ─────────────
Price  │ Qty   Orders   Price  │ Qty   Orders
       │                       │
2,100  │ 50    25       2,110  │ 50    20
2,090  │ 100   45       2,120  │ 100   35
2,080  │ 75    15       2,130  │ 75    10
```

**Color Coding**:
- Bid side: Green (green = buying interest)
- Ask side: Red (red = selling pressure)
- Size indicates quantity (larger blocks = more interest)

**Updates**: Real-time from WebSocket

---

### **Widget 5: Market Indices**

**Component**: `components/market/MarketIndices.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/indices
GET /api/v1/market/quote?symbol=NIFTY
GET /api/v1/market/quote?symbol=SENSEX
... (for each major index)
```

**Data Structure**:
```typescript
interface IndexQuote {
  symbol: string;
  name: string;
  current_price: number;
  change_amount: number;
  change_percent: number;
  timestamp: string;
}
```

**Display**:
- Grid or carousel format
- Show NIFTY 50, BANK NIFTY, SENSEX, MIDCAP 50
- Color-coded (green/red) based on change
- Click to view detailed chart

---

### **Widget 6: Live Quotes Grid**

**Component**: `components/market/LiveQuotesGrid.tsx`

**Backend Endpoints**:
```
GET /api/v1/market/instruments
GET /api/v1/market/quote?symbol=X
WebSocket: /ws/market/live-quotes (continuous updates)
```

**Features**:
- Show multiple quotes in grid/card format
- Paginated (e.g., 12 per page)
- Real-time price updates with color flash
- Click card to open chart
- Add to watchlist quick action
- Search and filter

**Display Format**:
```
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│ INFY     │ │ TCS      │ │ REL      │ │ LT       │
│ 2,000    │ │ 3,200    │ │ 2,850    │ │ 1,800    │
│ ↑ +3%    │ │ ↓ -3%    │ │ ↑ +7%    │ │ ↑ +1%    │
│ Volume: X│ │ Volume: Y│ │ Volume: Z│ │ Volume: A│
└──────────┘ └──────────┘ └──────────┘ └──────────┘
```

---

## 🔄 REAL-TIME UPDATES

**WebSocket Connections**:

```typescript
// Market quotes
const { quotes } = useWebSocket('/ws/market/live-quotes');

// Order book
const { orderBook } = useWebSocket('/ws/market/depth?symbol=INFY');

// Update components
useEffect(() => {
  if (quotes) {
    updateWatchlist(quotes);
    updateQuotesGrid(quotes);
  }
}, [quotes]);

useEffect(() => {
  if (orderBook) {
    updateOrderBook(orderBook);
  }
}, [orderBook]);
```

---

## 🎨 THEME SUPPORT

- ✅ Light mode: Clean white cards, dark text
- ✅ Dark mode: Dark cards, light text
- ✅ Extra dark: Black cards, white text
- ✅ Glass morphism: Frosted effect
- ✅ Normal: Solid cards

---

## ✅ COMPLETION CHECKLIST FOR MARKET PAGE

- [ ] Watchlist implemented (add/remove)
- [ ] Market heatmap displaying correctly
- [ ] Technical chart with multiple intervals
- [ ] Order book showing bid/ask
- [ ] Market indices updating
- [ ] Live quotes grid functional
- [ ] All WebSocket connections working
- [ ] Real-time price updates visible
- [ ] Responsive on all sizes
- [ ] All themes working
- [ ] Search functionality working
- [ ] Filter functionality working
- [ ] Loading states shown
- [ ] Error states handled
- [ ] Accessibility compliant

---

*(Continue in similar detailed manner for Pages 4-8)*

**Due to length limits, I'll create a second file with Pages 4-8**

