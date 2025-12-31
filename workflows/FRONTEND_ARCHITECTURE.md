# Frontend Architecture Documentation

This document describes the refactored frontend architecture for the VEGA TRADER application.

## Architecture Overview

![Frontend Architecture](./frontend-architecture.png)

## Directory Structure

```
frontend/src/
├── types/                          # Shared TypeScript interfaces
│   ├── index.ts                    # Main types (Auth, Portfolio, Orders, etc.)
│   ├── optionChain.ts              # Option Chain types
│   └── equity.ts                   # Equity types
│
├── utils/                          # Utility functions
│   ├── formatters.ts               # Number/currency formatting
│   ├── optionChainHelpers.ts       # Option chain calculations
│   └── upstoxErrors.ts             # Upstox API error handling
│
├── hooks/                          # Custom React hooks
│   ├── useOptionChainData.ts       # Option chain data fetching
│   ├── useOptionChainWebSocket.ts  # WebSocket real-time updates
│   ├── useOptionChainSettings.ts   # Column visibility settings
│   ├── useUpstoxAuth.ts            # Authentication logic
│   ├── useMarketDataFeed.ts        # Market data hook
│   └── useIndexSpotPrices.ts       # Index prices hook
│
├── components/                     # Reusable UI components
│   ├── optionchain/                # Option Chain components
│   │   ├── OptionChainControls.tsx # Header controls
│   │   ├── OptionChainTableHeader.tsx
│   │   ├── OptionChainRow.tsx
│   │   └── index.ts
│   │
│   ├── equity/                     # Equity components
│   │   ├── SectorFilter.tsx
│   │   ├── EquityTableRow.tsx
│   │   ├── Pagination.tsx
│   │   └── index.ts
│   │
│   ├── charts/                     # Chart components
│   │   ├── BidAskBar.tsx
│   │   └── index.ts
│   │
│   └── layout/                     # Layout components
│       ├── Header.tsx
│       ├── Sidebar.tsx
│       └── Layout.tsx
│
├── pages/                          # Page components
│   ├── OptionsChain.tsx            # Main option chain page
│   ├── OptionChainPopupPage.tsx    # Popup window version
│   ├── EquityLiveFeed.tsx          # Equity live feed page
│   ├── EquityPopupPage.tsx         # Popup window version
│   ├── Login.tsx                   # Authentication page
│   ├── Dashboard.tsx               # Main dashboard
│   └── ...
│
├── services/                       # API services
│   └── api.ts                      # Axios instance
│
├── store/                          # Redux store
│   ├── slices/                     # Redux slices
│   └── hooks.ts                    # Typed Redux hooks
│
└── theme/                          # Theme configuration
```

## Data Flow

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   API Service   │────▶│     Hooks       │────▶│     Pages       │
│   (api.ts)      │     │ (useOptionChain │     │ (OptionsChain)  │
│                 │◀────│     Data.ts)    │◀────│                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        │                       ▼                       ▼
        │              ┌─────────────────┐     ┌─────────────────┐
        │              │   Utilities     │     │   Components    │
        │              │ (formatters.ts) │     │ (OptionChain    │
        │              │                 │     │  Controls.tsx)  │
        │              └─────────────────┘     └─────────────────┘
        │
        ▼
┌─────────────────┐
│   WebSocket     │
│   (Real-time)   │
└─────────────────┘
```

## Key Design Patterns

### 1. **Separation of Concerns**
- **Types**: Centralized in `types/` directory
- **Logic**: Extracted into custom hooks
- **UI**: Split into reusable components
- **Formatting**: Centralized in utility functions

### 2. **Component Composition**
Pages compose smaller components:
```tsx
<OptionsChain>
  <OptionChainControls />
  <OptionChainTableHeader />
  {data.map(row => <OptionChainRow />)}
</OptionsChain>
```

### 3. **Custom Hooks Pattern**
Business logic encapsulated in hooks:
```tsx
const {
  optionChain,
  spotPrice,
  isLoading,
  refreshOptionChain
} = useOptionChainData();
```

### 4. **Shared Components**
Components work for both main page and popup:
```tsx
<OptionChainControls isPopup={false} />  // Main page
<OptionChainControls isPopup={true} />   // Popup window
```

## Refactoring Results

| Page | Before | After | Reduction |
|------|--------|-------|-----------|
| OptionsChain.tsx | 781 lines | ~140 lines | 82% |
| OptionChainPopupPage.tsx | 601 lines | ~140 lines | 77% |
| EquityLiveFeed.tsx | 618 lines | ~330 lines | 47% |
| Login.tsx | 415 lines | ~210 lines | 49% |

## Related Documentation

- [WebSocket Documentation](./WEBSOCKET_DOCUMENTATION.md)
