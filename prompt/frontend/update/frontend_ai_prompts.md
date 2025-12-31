# 🎯 FRONTEND AI PROMPTS - SPECIALIZED TEMPLATES

**For Frontend Developers Using AI Code Generation**  
**Platform**: ChatGPT, Claude, Copilot, etc.  
**Date**: December 13, 2025, 9:21 AM IST

---

## 📋 5 READY-TO-USE AI PROMPTS FOR VEGA TRADER'S

### **PROMPT 1: Setup Wizard Complete Implementation**

```
Create a React TypeScript Setup Wizard component for VEGA TRADER'S trading platform.

Requirements:
- 5-step wizard with progress indicator
- Step 1: Upstox API Credentials (API Key, API Secret, Redirect URI)
- Step 2: Database Configuration (PostgreSQL connection details)
- Step 3: AI LLM Configuration (Claude/GPT provider selection, API key)
- Step 4: Theme Selection (Light/Dark/Extra Dark + Glass/Normal style)
- Step 5: Review and Complete

Features:
- Form validation on each step (real-time feedback)
- Back/Next/Skip buttons
- Save credentials encrypted to localStorage
- Test connection buttons for API/Database/AI endpoints
- Loading states during validation
- Error handling with user-friendly messages
- Responsive design (mobile-first)
- Accessibility (WCAG 2.1 AA)
- After completion, redirect to /dashboard

Styling:
- Use Tailwind CSS
- Light/Dark/Extra Dark theme support
- Glass morphism option enabled via CSS class
- Clean, minimal design
- Consistent spacing (8px base unit)

Tech Stack:
- React 18+
- TypeScript strict mode
- Tailwind CSS for styling
- React Router for navigation
- Zustand or Redux for state management
- Axios for API calls

Structure:
- Main component: SetupWizard.tsx
- Sub-components: Step1.tsx, Step2.tsx, Step3.tsx, Step4.tsx, Step5.tsx
- Custom hooks: useSetupWizard.ts for state management
- Services: setupService.ts for validation and saving

Include proper TypeScript interfaces for form data.
Add comprehensive error boundaries.
Include loading skeletons while fetching.
```

---

### **PROMPT 2: Real-Time Dashboard with WebSocket**

```
Create a React TypeScript Dashboard component with real-time market data integration.

Features:
1. Market Overview Widget:
   - Display NIFTY, SENSEX, MIDCAP indices
   - Show price, change amount, change percentage
   - Green for gains, red for losses
   - Update via WebSocket every 1 second

2. Portfolio Summary Widget:
   - Total portfolio value
   - Cash available
   - Total invested amount
   - Today's P&L (amount and percentage)
   - Real-time calculation

3. Positions Table:
   - Columns: Symbol, Quantity, Buy Price, Current Price, P&L, P&L %
   - Real-time price updates via WebSocket
   - Color-coded P&L (green/red)
   - Sortable columns
   - Click to view full details

4. Recent Trades Table:
   - Columns: Symbol, Type (Buy/Sell), Quantity, Price, Time
   - Show last 10 trades
   - Pagination for more trades

5. Portfolio Chart:
   - Area chart showing portfolio value over 30 days
   - Interactive hover tooltips
   - Zoom and pan capabilities
   - Daily snapshots from database

Backend Integration:
- WebSocket connection: /ws/market/live-quotes
- API: GET /api/v1/portfolio/summary
- API: GET /api/v1/portfolio/positions
- API: GET /api/v1/portfolio/snapshots?period=30days

Styling:
- Responsive grid layout
- Cards with shadow effects
- Light/Dark/Extra Dark theme support
- Glass morphism optional
- Tailwind CSS

Performance:
- Virtual scrolling for large tables
- Memoized components to prevent unnecessary re-renders
- Debounced WebSocket updates
- Efficient state management with Redux

Include error handling for WebSocket disconnections with auto-reconnect.
Add loading states and skeleton screens.
Implement proper cleanup in useEffect hooks.
```

---

### **PROMPT 3: Advanced Order Placement Form**

```
Create a React TypeScript Order Placement Form component for VEGA TRADER'S.

Features:
1. Symbol Selection:
   - Autocomplete input (search by name or symbol)
   - Fetch from /api/v1/market/instruments
   - Show company name, exchange, last traded price
   - Click to select

2. Order Type Selection:
   - Market Order
   - Limit Order (with price input)
   - Stop-Loss Order (with trigger price and price)
   - Each type has different parameter requirements

3. Form Inputs:
   - Quantity (numeric input with validation)
   - Price (if Limit or SL order)
   - Side: Buy / Sell toggle
   - Validity: Day / IOC / GTT
   - If GTT, show calendar picker for GTT expiry date

4. Smart Features:
   - Auto-fill last traded price
   - Real-time margin calculation
   - Show available margin
   - Show margin required for this order
   - Show estimated total cost
   - Prevent order if insufficient margin
   - Show price suggestions (current bid/ask)

5. Order Confirmation:
   - Modal with order summary
   - Display all order parameters
   - Show total cost/credit
   - Confirm button with loading state
   - Cancel button

Backend Integration:
- GET /api/v1/market/quote?symbol=X (get live price)
- POST /api/v1/orders/place (place order)
- GET /api/v1/user/account-info (get margin)

Validation:
- Symbol required
- Quantity must be positive
- Price validation for limit orders
- Margin availability check
- Real-time validation feedback

Styling:
- Clean form layout
- Proper spacing and alignment
- Light/Dark/Extra Dark support
- Glass morphism option
- Clear input labels and help text
- Error states with red borders
- Success states with check icons

Accessibility:
- Label for every input
- Clear focus states
- ARIA labels for complex elements
- Keyboard navigation
- Screen reader support

Include loading states during API calls.
Handle API errors gracefully.
Show success notification after order placement.
Clear form on successful submission.
Option to place another order immediately.
```

---

### **PROMPT 4: Portfolio Analysis & Charts**

```
Create a React TypeScript Portfolio Analysis component with multiple charts.

Features:
1. Holdings Table:
   - Columns: Symbol, Quantity, Buy Price, Current Price, Total Value, P&L, P&L %
   - Sortable by any column
   - Filterable by symbol or sector
   - Show total rows at bottom
   - Click row to view detailed breakdown

2. Positions Table (Intraday):
   - Columns: Symbol, Qty, Entry Price, Current Price, P&L, P&L %, Time
   - Real-time updates
   - Color-coded P&L

3. Performance Charts:
   Chart 1: Portfolio Value Over Time (30/90/180 days)
   - Line chart showing total portfolio value
   - Interactive area chart
   - Tooltip with date and value
   - Zoom and pan

   Chart 2: Asset Allocation (Pie/Doughnut)
   - Show percentage allocation by sector/stock
   - Click to filter positions by sector
   - Show legend with percentages

   Chart 3: Daily Returns (Bar chart)
   - Show daily P&L for last 30 days
   - Green for gains, red for losses
   - Average return line

   Chart 4: Risk Metrics (Gauge charts)
   - Sharpe Ratio (0-3 scale)
   - Max Drawdown (0-100% scale)
   - Win Rate (0-100% scale)

Backend Integration:
- GET /api/v1/portfolio/holdings
- GET /api/v1/portfolio/positions
- GET /api/v1/portfolio/snapshots?period=30days
- GET /api/v1/portfolio/performance

Styling:
- Tailwind CSS
- Light/Dark/Extra Dark themes
- Glass morphism option
- Responsive grid layout
- Chart colors match theme

Libraries:
- Recharts for charts (React wrapper for Chart.js)
- React Query for data fetching
- Redux for state management

Features:
- Date range selection (7/30/90/180 days)
- Export data as CSV
- Print portfolio summary
- Responsive on mobile (charts stack vertically)

Include loading states for charts.
Add error boundaries.
Implement proper data caching with React Query.
```

---

### **PROMPT 5: AI Strategy Generator & Backtest UI**

```
Create a React TypeScript AI Strategy Generator and Backtest Results component.

Features:
1. Strategy Generator:
   - Text input for strategy description
   - "Generate Strategy" button
   - Show loading state with spinner
   - Display generated strategy details:
     * Strategy name
     * Description
     * Rules (buy/sell conditions)
     * Historical performance metrics
   - Buttons: Save / Backtest / Edit / Cancel

2. Strategy Form (for manual creation):
   - Name input
   - Description textarea
   - Buy condition input (with syntax hints)
   - Sell condition input
   - Risk parameters:
     * Max loss per trade (%)
     * Take profit target (%)
     * Stop loss (%)
   - Indicators selection (RSI, MACD, MA, Bollinger Bands)
   - Indicator parameters

3. Backtest Modal:
   - Date range selector (start/end date)
   - Initial capital input
   - Backtest button
   - Show loading state
   - Display results:
     * Total return (%)
     * Number of trades
     * Win rate (%)
     * Max drawdown (%)
     * Sharpe ratio
     * Profit factor
   - Charts:
     * Equity curve (line chart)
     * Drawdown chart
     * Monthly returns (bar chart)
   - Trade list (date, entry, exit, P&L)

4. Active Strategies List:
   - Table with: Name, Status, Current P&L, Win Rate, Sharpe Ratio
   - Action buttons: View, Edit, Pause, Delete
   - Click to view full details

Backend Integration:
- POST /api/v1/strategies/ai/generate (AI strategy generation)
- POST /api/v1/strategies/user (save strategy)
- POST /api/v1/strategies/{id}/backtest (run backtest)
- GET /api/v1/strategies/user (list user strategies)
- PUT /api/v1/strategies/{id} (update strategy)
- DELETE /api/v1/strategies/{id} (delete strategy)

Validation:
- Strategy name required
- At least one condition required
- Risk parameters validation
- Date range validation for backtest

Styling:
- Clean form layout
- Color-coded results (green for gains, red for losses)
- Light/Dark/Extra Dark theme support
- Glass morphism optional
- Responsive design

Error Handling:
- Show error message if strategy generation fails
- Show error if backtest fails
- Retry options
- User-friendly error messages

Include comprehensive TypeScript interfaces.
Add loading skeleton screens.
Implement proper error boundaries.
Add success toasts after save.
```

---

## 🎨 PROMPT 6: Theme System & Settings Page

```
Create a React TypeScript Theme System with Settings page for VEGA TRADER'S.

Features:
1. Theme Context:
   - Three themes: Light, Dark, Extra Dark
   - CSS variables for colors
   - Persistent storage (localStorage)
   - System preference detection

2. Style System:
   - Two styles: Glass Morphism, Normal
   - Toggleable independently from theme
   - CSS class-based implementation
   - Smooth transitions between styles

3. Settings Page Layout:
   - Tabs: Appearance, Credentials, Notifications, Risk, Data
   
   Tab 1: Appearance Settings
   ┌─────────────────────────┐
   │ Theme Selection         │
   │ ○ Light  ● Dark  ○ Extra Dark │
   │                         │
   │ Style Selection         │
   │ ○ Glass ● Normal        │
   │                         │
   │ Accent Color: [blue ▼]  │
   │                         │
   │ Font Size: [Medium ▼]   │
   │                         │
   │ [Apply] [Reset]         │
   └─────────────────────────┘

   Tab 2: Credentials Management
   - Edit Upstox API (key, secret)
   - Edit Database config
   - Edit AI LLM config
   - Test connection buttons
   - Encrypted storage

   Tab 3: Notifications
   - Email notifications toggle
   - Push notifications toggle
   - In-app notifications toggle
   - Notification types (trades, alerts, news)

   Tab 4: Risk Management
   - Max daily loss ($)
   - Per trade risk (%)
   - Portfolio allocation limits
   - Position size limits

   Tab 5: Data Management
   - Export portfolio CSV
   - Export backtest results CSV
   - Import previous data
   - Clear all data (with confirmation)

4. Theme Implementation:
   - CSS variables for all colors
   - Smooth transitions
   - System preference detection
   - Manual override capability

Backend Integration:
- PUT /api/v1/user/account-settings (save settings)
- GET /api/v1/user/account-settings (load settings)
- PUT /api/v1/settings/general (save theme/style)

Styling:
- Consistent with VEGA TRADER'S design system
- Light/Dark/Extra Dark themes work perfectly
- Glass morphism looks great
- Responsive on all devices

Components:
- ThemeProvider (Context)
- useTheme hook
- ColorPicker component
- SettingsPage
- Each settings tab as sub-component

Include smooth transitions between themes.
Persist user preferences to localStorage.
Add reset to defaults option.
Include theme preview before applying.
```

---

## 📦 ADDITIONAL SPECIALIZED PROMPTS

### **PROMPT 7: Real-time Market Data Component**

```
Create a Market Data component with:
- Watchlist with add/remove functionality
- Live price quotes (WebSocket)
- 1-minute price change indicators
- Technical charts (candlestick, line)
- Order book visualization
- Market heatmap (sector performance)
- Indices tracking
- Search and filter
```

### **PROMPT 8: Order History & Trade Execution**

```
Create Order History component with:
- All orders table (pending, executed, cancelled)
- Trade execution status
- Real-time WebSocket updates
- Order details modal
- Order modification/cancellation
- Trade slip printing
- Export orders as CSV
```

### **PROMPT 9: Responsive Navigation & Layout**

```
Create responsive navigation system:
- Collapsible sidebar (full on desktop, hamburger on mobile)
- Top header with user menu
- Breadcrumbs navigation
- Mobile bottom navigation
- Smooth transitions
- Theme-aware styling
- Active route highlighting
```

### **PROMPT 10: WebSocket Real-time Feed Manager**

```
Create WebSocket service for:
- Market data updates (quotes)
- Order status updates
- Portfolio P&L updates
- Trade execution notifications
- Auto-reconnect with exponential backoff
- Message queuing during disconnection
- Proper cleanup on unmount
```

---

## 🚀 USAGE INSTRUCTIONS

1. **Copy the relevant prompt** from above
2. **Paste into your AI code generator** (ChatGPT, Claude, etc.)
3. **Add context**: "I'm building VEGA TRADER'S, an AI-enabled trading platform"
4. **Customize** if needed for your specific requirements
5. **Review generated code** and integrate into your project

---

## 💡 PROMPT CUSTOMIZATION TIPS

**To make prompts even better:**

1. Add your existing code patterns as reference
2. Include your project's folder structure
3. Specify any custom utilities you're using
4. Add example API responses you want to handle
5. Include accessibility requirements specific to your users
6. Add performance budgets (bundle size, load time)
7. Include error scenarios you want handled

**Example Customization**:
```
[Original Prompt] + 

Additional Context:
- We use Redux Toolkit for state management
- Axios is configured with interceptors
- We have custom hooks in hooks/ folder
- API base URL: https://api.vegatraders.com
- WebSocket: wss://ws.vegatraders.com
- Our theme colors are stored in globals.css
```

---

## 📋 COMPONENT CHECKLIST

When implementing each component, ensure:

- ✅ Full TypeScript type safety
- ✅ Proper error boundaries
- ✅ Loading states with skeletons
- ✅ Accessibility (WCAG 2.1 AA)
- ✅ Responsive design (mobile first)
- ✅ Light/Dark/Extra Dark themes
- ✅ Glass morphism option
- ✅ Real-time updates where needed
- ✅ WebSocket integration (if applicable)
- ✅ Redux state management
- ✅ React Query for server state
- ✅ Proper error handling
- ✅ Success notifications
- ✅ Loading spinners
- ✅ Keyboard navigation
- ✅ Focus management
- ✅ Performance optimization
- ✅ Code documentation
- ✅ Storybook stories (if applicable)
- ✅ Unit tests

---

## 🎯 FINAL NOTES

These prompts are designed to:
- ✅ Work with latest React 18+ features
- ✅ Follow TypeScript best practices
- ✅ Include accessibility by default
- ✅ Support all three themes (Light/Dark/Extra Dark)
- ✅ Enable Glass Morphism style option
- ✅ Integrate with your backend APIs
- ✅ Use WebSocket for real-time data
- ✅ Be production-ready
- ✅ Have proper error handling
- ✅ Support responsive design

---

**Frontend Development Prompts Ready**: December 13, 2025, 9:21 AM IST  
**Status**: Production-Ready Templates  
**Next**: Start coding with these prompts!

