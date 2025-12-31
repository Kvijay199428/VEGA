# 🎨 BEST AI PROMPT ENGINEER - FRONTEND DEVELOPMENT GUIDE

**For**: VEGA TRADER'S - AI-Enabled Trading Platform  
**Date**: December 13, 2025, 9:21 AM IST  
**Status**: Production-Ready Prompt Engineering & Development Guide

---

## 🤖 BEST AI ENGINEERED PROMPT FOR FRONTEND DEVELOPMENT

### **Master Prompt Template (Copy-Paste Ready)**

```
You are an expert frontend developer specializing in financial trading platforms, 
real-time data visualization, and responsive design systems.

PROJECT: VEGA TRADER'S - AI-Enabled Trading Dashboard
- Name: "VEGA TRADER'S" powered by "VEGA SOL"
- Tagline: "VEGA AI ENABLED"
- Tech Stack: React 18+, TypeScript, Tailwind CSS, Redux
- Design System: Dual Theme (Light/Dark/Extra Dark) with Glass Morphism option

BRANDING REQUIREMENTS:
1. Primary Colors: 
   - Light Mode: Clean whites (#FFFFFF), soft grays (#F5F7FA), accent blue (#3B82F6)
   - Dark Mode: Deep charcoal (#1F2937), slate (#374151), accent blue (#60A5FA)
   - Extra Dark: Pure black (#000000), darker slate (#111827), accent cyan (#06B6D4)
   
2. Design Styles:
   - Style 1: Glass Morphism - Semi-transparent with backdrop blur, minimalist borders
   - Style 2: Normal - Solid colors, traditional card-based layout
   - Both styles independently toggleable in settings

3. Typography:
   - Headers: Inter Bold, size 32px (H1) → 14px (body)
   - Monospace: 'Monaco' or 'Menlo' for trading data, charts, numbers
   - Line Height: 1.6 for readability

4. Spacing System:
   - Base unit: 8px
   - Padding: 8px, 16px, 24px, 32px, 48px
   - Margins: Follow padding system
   - Gaps: 12px (tight), 20px (normal), 32px (loose)

COMPONENT ARCHITECTURE:
1. Navigation System:
   - Top Header: Logo | Market Overview | User Profile | Theme Toggle | Settings
   - Sidebar Navigation: Collapsible (hamburger menu on <768px)
   - Mobile Responsive: Hamburger menu, bottom navigation on phones
   - Breadcrumbs: Secondary navigation for deep pages

2. Dashboard Layout:
   - Grid System: 12-column, 16px gutters
   - Header: 80px (desktop), 60px (tablet), 50px (mobile)
   - Sidebar: 280px (desktop expanded), 80px (collapsed), hidden on mobile
   - Main Content: Flexible, responsive regions
   - Footer: Optional, minimal (legal links, version)

3. Color Tokens (CSS Variables):
   Light Mode:
   --color-bg-primary: #FFFFFF
   --color-bg-secondary: #F5F7FA
   --color-text-primary: #1F2937
   --color-text-secondary: #6B7280
   --color-accent: #3B82F6
   --color-success: #10B981
   --color-warning: #F59E0B
   --color-error: #EF4444
   --color-border: #E5E7EB
   
   Dark Mode:
   --color-bg-primary: #1F2937
   --color-bg-secondary: #111827
   --color-text-primary: #F3F4F6
   --color-text-secondary: #D1D5DB
   --color-accent: #60A5FA
   --color-success: #34D399
   --color-warning: #FBBF24
   --color-error: #F87171
   --color-border: #374151
   
   Extra Dark Mode:
   --color-bg-primary: #000000
   --color-bg-secondary: #111827
   --color-text-primary: #FFFFFF
   --color-text-secondary: #E5E7EB
   --color-accent: #06B6D4
   --color-success: #10B981
   --color-warning: #F59E0B
   --color-error: #EF4444
   --color-border: #1F2937

SETUP WIZARD (First-Time User Flow):
1. Welcome Screen
   - Logo, welcome message
   - "Get Started" button

2. Step 1: Upstox API Credentials
   - Form fields: API Key, API Secret, Redirect URI
   - Help text with links to Upstox docs
   - Validation: Real-time feedback
   - Save button

3. Step 2: Database Configuration (Optional for Desktop Users)
   - Database Host, Port, Username, Password
   - Test Connection button
   - Auto-detect local PostgreSQL option

4. Step 3: AI LLM Configuration
   - Provider Selection: Claude, GPT-4, Local LLM
   - API Key / Localhost Configuration
   - Model Selection dropdown
   - Test Connection

5. Step 4: Theme & Preferences
   - Theme Selection (Light/Dark/Extra Dark)
   - Style Selection (Glass/Normal)
   - Language Selection
   - Notifications Preference

6. Step 5: Review & Complete
   - Summary of all configurations
   - Edit button for each section
   - "Complete Setup" → Redirect to Dashboard

USER FLOWS TO IMPLEMENT:
1. Authentication:
   - Login with Upstox OAuth
   - Session management (23-hour sessions)
   - Auto-logout with warning
   - "Remember me" option

2. Dashboard:
   - Market Overview Widget (indices, gainers/losers)
   - Portfolio Summary (equity, balance, P&L)
   - Open Positions Table (real-time updates via WebSocket)
   - Recent Trades Table
   - Chart: Portfolio value over time
   - Quick Action Buttons

3. Market Data Page:
   - Watchlist (customizable)
   - Real-time quotes (color-coded green/red)
   - Technical charts (candlestick, line)
   - Order Book depth
   - Market Heatmap (sector performance)

4. Trading Page:
   - Order Placement Form
   - Order Type Selection (Market, Limit, Stop-Loss)
   - Smart Pre-fill from selected security
   - Real-time margin calculation
   - Confirmation modal before submission

5. Portfolio Page:
   - Holdings Table (sortable, filterable)
   - Positions Table (with P&L real-time)
   - Performance Charts (daily/weekly/monthly/yearly)
   - Asset Allocation Pie Chart
   - Risk Metrics Widget

6. Strategy Page (AI-Powered):
   - Predefined Strategies Grid
   - Create Custom Strategy Form
   - AI Strategy Generator Prompt Input
   - Backtest Results Visualization
   - Active Strategies Manager

7. Settings Page:
   - Credentials Management (Edit Upstox API, Database, AI LLM)
   - Theme Switcher (Light/Dark/Extra Dark)
   - Style Switcher (Glass/Normal)
   - Notification Preferences
   - Risk Management Settings
   - Data Export/Import

INTERACTIONS & ANIMATIONS:
1. Transitions:
   - Page transitions: 300ms fade
   - Sidebar collapse: 250ms slide
   - Modals: 200ms scale from center
   - Hover states: 150ms color change

2. Real-time Updates:
   - WebSocket for live market data
   - Pulse animation for new updates
   - Shimmer loading states
   - Toast notifications for events

3. Micro-interactions:
   - Button ripple on click
   - Form validation feedback (inline)
   - Loading spinners on API calls
   - Success/Error states with icons

RESPONSIVE DESIGN BREAKPOINTS:
- Extra Small: <640px (mobile phones)
- Small: 640px-768px (large phones, small tablets)
- Medium: 768px-1024px (tablets)
- Large: 1024px-1280px (small laptops)
- Extra Large: 1280px+ (desktops, large monitors)

Sidebar Behavior:
- >1024px: Visible sidebar, collapsible
- <1024px: Hamburger menu, slide-out drawer
- <640px: Full-screen menu overlay

ACCESSIBILITY STANDARDS (WCAG 2.1 AA):
- Keyboard navigation (Tab, Enter, Esc)
- ARIA labels on all interactive elements
- Focus indicators visible
- Color contrast ratio ≥4.5:1
- Screen reader support
- Skip links for main navigation

DATA VISUALIZATION:
- Charts: Chart.js or Recharts (React wrapper)
- Real-time updates: Smooth transitions, no jumping
- Large datasets: Virtual scrolling, pagination
- Mobile: Responsive charts, touch-friendly interactions

FORM VALIDATION:
- Real-time feedback (no submit blocking)
- Client-side validation (format, range)
- Server-side validation (duplicate, permissions)
- Error messages: Clear, actionable
- Success feedback: Checkmarks, color changes

STATE MANAGEMENT:
- Redux for global state (user, portfolio, settings)
- React Query for server state (API data, caching)
- Local state for UI (modals, forms, filters)
- WebSocket connection management

PERFORMANCE TARGETS:
- First Contentful Paint (FCP): <1.5s
- Largest Contentful Paint (LCP): <2.5s
- Cumulative Layout Shift (CLS): <0.1
- Time to Interactive (TTI): <3.5s
- Core Web Vitals: All green

SECURITY REQUIREMENTS:
- JWT tokens in secure, httpOnly cookies
- CSRF protection on forms
- XSS prevention (sanitize user input)
- Content Security Policy headers
- No sensitive data in local storage
- Credential encryption before storage

When developing each page/component:
1. Start with responsive mobile design
2. Implement accessibility features first
3. Add theme support (light/dark/extra dark)
4. Implement glass morphism option
5. Connect to backend APIs
6. Add real-time WebSocket updates
7. Implement error states and loading states
8. Optimize performance and bundle size

Code Style:
- TypeScript strict mode enabled
- Functional components with hooks
- Custom hooks for logic reuse
- CSS Modules or Tailwind for styling
- Proper error boundaries
- Component composition over inheritance

Now proceed to create the frontend based on this specification.
```

---

## 📋 FRONTEND DEVELOPMENT GUIDE - COMPREHENSIVE

### **PROJECT STRUCTURE**

```
frontend/
├── public/
│   ├── favicon.ico
│   ├── logo.svg                    # VEGA TRADER'S logo
│   └── robots.txt
├── src/
│   ├── components/
│   │   ├── common/
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Navigation.tsx
│   │   │   ├── ThemeToggle.tsx
│   │   │   ├── Loading.tsx
│   │   │   ├── ErrorBoundary.tsx
│   │   │   └── Toast.tsx
│   │   ├── layout/
│   │   │   ├── MainLayout.tsx
│   │   │   ├── AuthLayout.tsx
│   │   │   └── SettingsLayout.tsx
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── OAuthCallback.tsx
│   │   │   └── SessionManager.tsx
│   │   ├── setup/
│   │   │   ├── SetupWizard.tsx
│   │   │   ├── Step1_Upstox.tsx
│   │   │   ├── Step2_Database.tsx
│   │   │   ├── Step3_AI.tsx
│   │   │   ├── Step4_Theme.tsx
│   │   │   └── Step5_Review.tsx
│   │   ├── dashboard/
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── MarketOverview.tsx
│   │   │   ├── PortfolioSummary.tsx
│   │   │   ├── PositionsTable.tsx
│   │   │   ├── TradesTable.tsx
│   │   │   ├── PortfolioChart.tsx
│   │   │   └── QuickActions.tsx
│   │   ├── market/
│   │   │   ├── MarketPage.tsx
│   │   │   ├── Watchlist.tsx
│   │   │   ├── QuotesList.tsx
│   │   │   ├── TechnicalChart.tsx
│   │   │   ├── OrderBook.tsx
│   │   │   ├── MarketHeatmap.tsx
│   │   │   └── MarketIndices.tsx
│   │   ├── trading/
│   │   │   ├── TradingPage.tsx
│   │   │   ├── OrderForm.tsx
│   │   │   ├── OrderTypeSelector.tsx
│   │   │   ├── MarginCalculator.tsx
│   │   │   ├── OrderConfirmation.tsx
│   │   │   └── OrderHistory.tsx
│   │   ├── portfolio/
│   │   │   ├── PortfolioPage.tsx
│   │   │   ├── HoldingsTable.tsx
│   │   │   ├── PositionsTable.tsx
│   │   │   ├── PerformanceCharts.tsx
│   │   │   ├── AllocationChart.tsx
│   │   │   └── RiskMetrics.tsx
│   │   ├── strategies/
│   │   │   ├── StrategiesPage.tsx
│   │   │   ├── PredefinedStrategies.tsx
│   │   │   ├── CreateStrategy.tsx
│   │   │   ├── AIGenerator.tsx
│   │   │   ├── BacktestResults.tsx
│   │   │   └── ActiveStrategies.tsx
│   │   ├── indicators/
│   │   │   ├── IndicatorsPage.tsx
│   │   │   ├── IndicatorsList.tsx
│   │   │   └── CustomIndicator.tsx
│   │   ├── settings/
│   │   │   ├── SettingsPage.tsx
│   │   │   ├── CredentialsManager.tsx
│   │   │   ├── ThemeSettings.tsx
│   │   │   ├── NotificationSettings.tsx
│   │   │   ├── RiskSettings.tsx
│   │   │   └── DataManagement.tsx
│   │   └── shared/
│   │       ├── Modal.tsx
│   │       ├── Form.tsx
│   │       ├── Button.tsx
│   │       ├── Input.tsx
│   │       ├── Select.tsx
│   │       ├── Table.tsx
│   │       ├── Card.tsx
│   │       ├── Badge.tsx
│   │       ├── Tooltip.tsx
│   │       └── Skeleton.tsx
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
│   │   └── Error.tsx
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useWebSocket.ts
│   │   ├── useMarketData.ts
│   │   ├── usePortfolio.ts
│   │   ├── useTheme.ts
│   │   ├── useApi.ts
│   │   ├── useLocalStorage.ts
│   │   └── useMediaQuery.ts
│   ├── services/
│   │   ├── api.ts                  # Axios instance
│   │   ├── auth.service.ts
│   │   ├── market.service.ts
│   │   ├── trading.service.ts
│   │   ├── portfolio.service.ts
│   │   ├── strategies.service.ts
│   │   ├── websocket.service.ts
│   │   └── storage.service.ts
│   ├── store/
│   │   ├── index.ts                # Redux store
│   │   ├── slices/
│   │   │   ├── authSlice.ts
│   │   │   ├── uiSlice.ts
│   │   │   ├── settingsSlice.ts
│   │   │   └── notificationSlice.ts
│   │   └── hooks.ts
│   ├── styles/
│   │   ├── globals.css
│   │   ├── themes.css              # Light, Dark, Extra Dark
│   │   ├── glass-morphism.css      # Glass effect styles
│   │   ├── animations.css
│   │   ├── responsive.css
│   │   └── accessibility.css
│   ├── utils/
│   │   ├── format.ts               # Number, date, currency formatting
│   │   ├── validation.ts           # Form validation
│   │   ├── constants.ts            # App constants
│   │   ├── types.ts                # TypeScript types
│   │   └── helpers.ts
│   ├── contexts/
│   │   ├── AuthContext.tsx
│   │   ├── ThemeContext.tsx
│   │   └── WebSocketContext.tsx
│   ├── App.tsx
│   ├── index.tsx
│   └── config.ts
├── package.json
├── tsconfig.json
├── tailwind.config.js
├── postcss.config.js
└── .env.example
```

---

## 🎯 PAGE-BY-PAGE DEVELOPMENT GUIDE

### **1. SETUP WIZARD (First-Time User)**

**File**: `pages/Setup.tsx` + `components/setup/*`

**Flow**:
```
Start → Step 1 (Upstox) → Step 2 (Database) → Step 3 (AI LLM) → 
Step 4 (Theme) → Step 5 (Review) → Dashboard
```

**Step 1: Upstox API Credentials**
```
┌─────────────────────────────────────────┐
│  🔐 VEGA TRADER'S - Setup Wizard        │
│  Step 1 of 5: Upstox API Credentials    │
├─────────────────────────────────────────┤
│                                         │
│  API Key:       [________________]     │
│                 Help: Get from Upstox   │
│                                         │
│  API Secret:    [________________]     │
│                 (Encrypted on save)     │
│                                         │
│  Redirect URI:  [________________]     │
│                 Default: localhost:28020 │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │ Test Connection                 │  │ (Button)
│  └─────────────────────────────────┘  │
│                                         │
│  [Previous] [Skip] [Next →]             │
│                                         │
└─────────────────────────────────────────┘
```

**Step 2: Database Configuration (Optional)**
```
├─ Connection Type: PostgreSQL / SQLite / Skip
├─ Host: [localhost]
├─ Port: [5432]
├─ Database: [vegatrades]
├─ Username: [postgres]
├─ Password: [••••••••]
├─ [Auto-detect Local] [Test Connection]
└─ [Previous] [Skip] [Next →]
```

**Step 3: AI LLM Configuration**
```
├─ AI Provider: [Claude ▼] / GPT-4 / Local
├─ API Key: [________________] (Encrypted)
├─ Model: [Claude-3-Opus ▼]
├─ Connection Type: API / Localhost
├─ If Localhost:
│  ├─ Host: [localhost]
│  └─ Port: [28021]
├─ [Test Connection]
└─ [Previous] [Skip] [Next →]
```

**Step 4: Theme & Preferences**
```
├─ Theme: [Light ○] [Dark ●] [Extra Dark ○]
├─ Style: [Glass ●] [Normal ○]
├─ Language: [English ▼]
├─ Notifications: [✓] Enable push notifications
└─ [Previous] [Skip] [Next →]
```

**Step 5: Review**
```
├─ Upstox API: ✓ Configured
├─ Database: ✓ Configured
├─ AI LLM: ✓ Claude-3-Opus
├─ Theme: Dark + Glass Morphism
├─ Notifications: Enabled
├─ [Edit Each Section]
└─ [← Previous] [Complete Setup →]
```

---

### **2. AUTHENTICATION FLOW**

**Login Page**:
- Upstox OAuth button (prominent)
- "Or login with email" option
- Remember me checkbox
- Forgot password link

**Session Management**:
- 23-hour session timeout warning at 22 hours
- Auto-logout with countdown
- Session extend option
- Secure token storage (httpOnly cookies)

---

### **3. MAIN DASHBOARD**

**Layout**:
```
┌────────────────────────────────────────────────┐
│ 🔵 VEGA TRADER'S        [Market: +2.5%]  👤 ⚙️ │  Header
├──────────┬────────────────────────────────────┤
│  Dashboard ─► Market │ Trading │ Portfolio    │
│  Market   │ 📊 Market Overview                │
│  Trading  │ ┌────────────────────────────────┐│
│  Portfolio│ │ NIFTY: 23,450.50 +1.2% │ BSESENSEX
│  Strategy │ │ MIDCAP: 8,900.20 +0.8% │ BANKNIFT
│  Indicat. │ └────────────────────────────────┘│
│  Settings │                                    │
│           │ 💰 Portfolio Summary              │
│           │ ┌────────────────────────────────┐│
│           │ │ Total Value: ₹5,50,000         ││
│           │ │ Cash: ₹2,50,000  Invested: ...  ││
│           │ │ Today's P&L: +₹12,500 (+2.3%)  ││
│           │ └────────────────────────────────┘│
│           │                                    │
│           │ 📈 Open Positions (Real-time)     │
│           │ ┌─────────────────────────────────┤
│           │ │ Symbol │ Qty │ Price │ P&L │%   │
│           │ │ RELIANCE│100│ 2,850│+850│+3.1%│
│           │ │ TCS    │50 │ 3,200│-200│-1.2%│
│           │ └─────────────────────────────────┤
│           │                                    │
│           │ 📊 Portfolio Value Chart          │
│           │ (Area chart showing 30-day trend) │
│           │                                    │
└──────────┴────────────────────────────────────┘
```

**Key Widgets**:
1. Market Overview (indices, gainers/losers)
2. Portfolio Summary (total value, cash, P&L)
3. Positions Table (real-time, WebSocket)
4. Trades Table (recent 10)
5. Portfolio Chart (interactive, 7/30/90 days)
6. Quick Actions (Trade, View Holdings, Strategies)

---

### **4. MARKET DATA PAGE**

**Features**:
- **Watchlist**: Custom watchlist with add/remove
- **Live Quotes**: Real-time price updates (WebSocket)
- **Technical Charts**: Candlestick, line charts with indicators
- **Order Book**: Market depth visualization
- **Heatmap**: Sector performance grid
- **Indices**: Major indices overview

**Chart Interactions**:
- Zoom in/out
- Pan
- Crosshair with tooltips
- Compare multiple symbols
- Add technical indicators
- Annotation tools

---

### **5. TRADING PAGE**

**Order Form**:
```
┌──────────────────────────────┐
│ Place Order                  │
├──────────────────────────────┤
│ Symbol: [RELIANCE    ▼]      │
│ Qty: [___] (Auto-fill)       │
│ Price: [___] (Auto-fill)     │
│                              │
│ Order Type:                  │
│ ○ Market  ● Limit  ○ SL      │
│                              │
│ Side: ● Buy  ○ Sell          │
│                              │
│ Validity:                    │
│ ● Day  ○ IOC  ○ GTT          │
│                              │
│ Available Margin: ₹2,50,000  │
│ Required Margin: ₹2,850      │
│                              │
│ [Place Order] [Cancel]       │
│                              │
│ Order Confirmation Modal:    │
│ ┌─────────────────────────┐ │
│ │ Confirm Order?          │ │
│ │ Buy 100 RELIANCE @2,850 │ │
│ │ Est. Cost: ₹2,85,000    │ │
│ │ [Confirm] [Cancel]      │ │
│ └─────────────────────────┘ │
│                              │
└──────────────────────────────┘
```

**Real-time Updates**:
- Margin calculation as user inputs
- Price suggestions (bid/ask)
- Position updates after order placement
- Order status tracking

---

### **6. PORTFOLIO PAGE**

**Sections**:
1. **Holdings**: Delivery holdings with quantities
2. **Positions**: Intraday open positions
3. **Performance**: Daily/weekly/monthly/yearly returns
4. **Allocation**: Pie chart of sector/stock allocation
5. **Risk Metrics**: Sharpe ratio, Max Drawdown, Win Rate

**Charts**:
- Portfolio value over time (line chart)
- Asset allocation (pie chart)
- Sector allocation (horizontal bar)
- Performance metrics (gauge charts)

---

### **7. STRATEGIES PAGE**

**Features**:
1. **Predefined Strategies**: Grid of strategy templates
2. **Create Strategy**: Form-based strategy builder
3. **AI Generator**: Prompt input for AI-generated strategies
4. **Backtest**: Run backtest on selected date range
5. **Active Strategies**: Management and monitoring

**AI Strategy Generation**:
```
Enter your strategy idea:
[What's your trading strategy? E.g., "Buy when RSI < 30"]

AI Response:
Generated Strategy:
- Name: "RSI Mean Reversion"
- Rules: Buy RSI < 30, Sell RSI > 70
- Historical Return: +12.5%
- Max Drawdown: -5.2%
- Sharpe Ratio: 1.8

[Execute] [Backtest] [Save] [Cancel]
```

---

### **8. SETTINGS PAGE**

**Tabs**:
1. **Credentials**: Edit Upstox API, Database, AI LLM
2. **Appearance**: Theme (Light/Dark/Extra Dark), Style (Glass/Normal)
3. **Notifications**: Email, push, in-app preferences
4. **Risk Management**: Max daily loss, per-trade risk, portfolio allocation limits
5. **Data**: Export portfolio, backtest results, import data

---

## 🎨 DESIGN SYSTEM DETAILS

### **Color Tokens - CSS Variables**

**Light Mode**:
```css
:root {
  --bg-primary: #FFFFFF;
  --bg-secondary: #F5F7FA;
  --bg-tertiary: #EEEFF2;
  --text-primary: #1F2937;
  --text-secondary: #6B7280;
  --text-tertiary: #9CA3AF;
  --accent: #3B82F6;
  --accent-light: #93C5FD;
  --success: #10B981;
  --warning: #F59E0B;
  --error: #EF4444;
  --border: #E5E7EB;
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
```

**Dark Mode**:
```css
@media (prefers-color-scheme: dark) {
  :root {
    --bg-primary: #1F2937;
    --bg-secondary: #111827;
    --bg-tertiary: #0F172A;
    --text-primary: #F3F4F6;
    --text-secondary: #D1D5DB;
    --text-tertiary: #9CA3AF;
    --accent: #60A5FA;
    --accent-light: #93C5FD;
    --success: #34D399;
    --warning: #FBBF24;
    --error: #F87171;
    --border: #374151;
    --shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }
}
```

**Extra Dark Mode**:
```css
[data-theme="extra-dark"] {
  --bg-primary: #000000;
  --bg-secondary: #111827;
  --bg-tertiary: #1F2937;
  --text-primary: #FFFFFF;
  --text-secondary: #E5E7EB;
  --text-tertiary: #D1D5DB;
  --accent: #06B6D4;
  --accent-light: #22D3EE;
  --success: #10B981;
  --warning: #F59E0B;
  --error: #EF4444;
  --border: #2D3748;
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}
```

### **Glass Morphism Style**

```css
.glass {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

/* Dark mode glass */
@media (prefers-color-scheme: dark) {
  .glass {
    background: rgba(31, 41, 55, 0.1);
    border-color: rgba(255, 255, 255, 0.1);
  }
}
```

### **Responsive Breakpoints**

```css
/* Mobile First */
@media (min-width: 640px) { /* sm */ }
@media (min-width: 768px) { /* md */ }
@media (min-width: 1024px) { /* lg */ }
@media (min-width: 1280px) { /* xl */ }
@media (min-width: 1536px) { /* 2xl */ }
```

### **Sidebar Responsive**

```typescript
// Hook for media query
const isDesktop = useMediaQuery("(min-width: 1024px)");
const isMobile = useMediaQuery("(max-width: 640px)");

// Sidebar behavior
- Desktop (>1024px): Visible, collapsible to 80px icon sidebar
- Tablet (768-1024px): Hamburger menu, slide-out drawer
- Mobile (<768px): Hamburger menu, full-screen overlay
- Extra small (<640px): Bottom navigation bar for main sections
```

---

## 🔌 BACKEND INTEGRATION

### **API Endpoint Mapping**

**Authentication**:
```typescript
// POST /api/v1/auth/login
export const loginWithUpstox = (code: string) => api.post('/auth/login', { code });

// GET /api/v1/auth/session-status
export const getSessionStatus = () => api.get('/auth/session-status');

// POST /api/v1/auth/logout
export const logout = () => api.post('/auth/logout');
```

**Market Data**:
```typescript
// GET /api/v1/market/quote?symbol=RELIANCE
export const getQuote = (symbol: string) => api.get(`/market/quote`, { params: { symbol } });

// GET /api/v1/market/ohlc?symbol=RELIANCE&interval=5min
export const getOHLC = (symbol: string, interval: string) => 
  api.get(`/market/ohlc`, { params: { symbol, interval } });

// WebSocket: /ws/market/live-quotes
websocket connection for real-time quotes
```

**Orders**:
```typescript
// POST /api/v1/orders/place
export const placeOrder = (orderData) => api.post('/orders/place', orderData);

// GET /api/v1/orders?user_id=123
export const getOrders = (userId: string) => api.get('/orders', { params: { user_id: userId } });

// PUT /api/v1/orders/{order_id}
export const modifyOrder = (orderId: string, updates) => 
  api.put(`/orders/${orderId}`, updates);
```

**Portfolio**:
```typescript
// GET /api/v1/portfolio/summary
export const getPortfolioSummary = () => api.get('/portfolio/summary');

// GET /api/v1/portfolio/positions
export const getPositions = () => api.get('/portfolio/positions');

// GET /api/v1/portfolio/performance?period=1month
export const getPerformance = (period: string) => 
  api.get('/portfolio/performance', { params: { period } });
```

**Strategies**:
```typescript
// POST /api/v1/strategies/ai/generate
export const generateAIStrategy = (prompt: string, userId: string) =>
  api.post('/strategies/ai/generate', { prompt, user_id: userId });

// POST /api/v1/strategies/{id}/backtest
export const backtestStrategy = (strategyId: string, startDate: string, endDate: string) =>
  api.post(`/strategies/${strategyId}/backtest`, { start_date: startDate, end_date: endDate });
```

---

## 🔄 REAL-TIME UPDATES WITH WEBSOCKET

```typescript
// hooks/useWebSocket.ts
export const useWebSocket = (url: string) => {
  useEffect(() => {
    const ws = new WebSocket(`wss://${API_URL}${url}`);
    
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      // Update Redux store or local state
    };
    
    return () => ws.close();
  }, [url]);
};

// Usage in component
const { quotes } = useWebSocket('/ws/market/live-quotes');
```

---

## 🚀 DEPLOYMENT & PERFORMANCE

### **Build Optimization**:
- Code splitting by route
- Tree shaking unused exports
- Image optimization (next/image)
- CSS minification and autoprefixing
- Font subsetting

### **Performance Targets**:
- First Contentful Paint: <1.5s
- Largest Contentful Paint: <2.5s
- Cumulative Layout Shift: <0.1
- Time to Interactive: <3.5s

### **SEO & Meta Tags**:
```html
<title>VEGA TRADER'S - AI-Enabled Trading Dashboard</title>
<meta name="description" content="Advanced trading platform powered by VEGA AI">
<meta name="theme-color" content="#3B82F6">
```

---

## ✨ KEY FEATURES SUMMARY

✅ **Setup Wizard**: First-time credential configuration  
✅ **Multi-Theme Support**: Light, Dark, Extra Dark  
✅ **Glass Morphism**: Optional design style  
✅ **Real-time Updates**: WebSocket integration  
✅ **Responsive Design**: Mobile-first, all screen sizes  
✅ **Dark Mode**: Full dark mode support  
✅ **Collapsible Sidebar**: Auto-collapse on small screens  
✅ **AI Integration**: Strategy generation and analysis  
✅ **WebSocket Feeds**: Live market data, orders, portfolio  
✅ **Secure Storage**: Encrypted credential storage  
✅ **Accessibility**: WCAG 2.1 AA compliance  
✅ **Performance**: Optimized bundle, Core Web Vitals  
✅ **Error Handling**: Comprehensive error boundaries  

---

## 📦 DEPENDENCIES

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.8.0",
    "redux": "^4.2.0",
    "react-redux": "^8.0.5",
    "@reduxjs/toolkit": "^1.9.1",
    "axios": "^1.3.2",
    "react-query": "^3.39.3",
    "chart.js": "^4.2.1",
    "react-chartjs-2": "^5.2.0",
    "recharts": "^2.7.2",
    "date-fns": "^2.29.3",
    "zustand": "^4.3.4",
    "tailwindcss": "^3.2.7",
    "typescript": "^4.9.5"
  },
  "devDependencies": {
    "vite": "^4.1.4",
    "@vitejs/plugin-react": "^3.1.0",
    "@types/react": "^18.0.27",
    "@types/react-dom": "^18.0.10",
    "eslint": "^8.33.0",
    "prettier": "^2.8.3"
  }
}
```

---

**Frontend Development Status**: Ready for Implementation  
**Date**: December 13, 2025, 9:21 AM IST  
**Next Step**: Begin component development based on this guide

