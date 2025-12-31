# Option Chain & Market Data TODOs

This document details the pending tasks related to market data streaming, option chain fetching, and historical data handling.

## 1. OptionChainService.java

**Location**: `com.vegatrader.upstox.api.optionchain.service.OptionChainService`

### API Integration
-   **Line 79**: `// TODO: Actual HTTP call to Upstox API`
    -   **Context**: `fetchFromUpstox` method.
    -   **Task**: Implement the REST call to `https://api.upstox.com/v2/option/chain`.
    -   **Params**: `instrument_key`, `expiry_date`.
    -   **Headers**: Authorization Bearer token.
-   **Line 93**: `// TODO: Implement expiry fetching`
    -   **Context**: `getExpiries` method.
    -   **Task**: Implement logic to get all available expiry dates for an underlying (e.g., NIFTY, BANKNIFTY) from the contract master or API.

### Persistence
-   **Line 140**: `// TODO: Persist to option_chain_audit table`
    -   **Context**: `logAudit` method.
    -   **Task**: Store metadata about option chain requests (latency, success/failure, token used) for analysis.

## 2. HistoricalMarketDataServiceImpl.java

**Location**: `com.vegatrader.upstox.api.expired.service.HistoricalMarketDataServiceImpl`

### Data Fetching
-   **Line 56**: `// TODO: Call Upstox API: GET /v2/expired-instruments/historical-candle`
    -   **Context**: Fetching candles for expired contracts/backtesting.
    -   **Task**: Implement the API call for historical data.

## 3. ExpiredInstrumentServiceImpl.java

**Location**: `com.vegatrader.upstox.api.expired.service.ExpiredInstrumentServiceImpl`

### API Integration
-   **Line 44**: `// TODO: Call Upstox API: GET /v2/expired-instruments/expiries`
    -   **Task**: Fetch list of historical expiries.
-   **Line 63**: `// TODO: Call Upstox API: GET /v2/expired-instruments/option/contract`
    -   **Task**: Fetch contract details for expired options.
-   **Line 81**: `// TODO: Call Upstox API: GET /v2/expired-instruments/future/contract`
    -   **Task**: Fetch contract details for expired futures.

## 4. ExpiryCalendarService.java & BrokerInstrumentPrewarmJob.java

**Locations**: 
- `com.vegatrader.upstox.api.expiry.service.ExpiryCalendarService`
- `com.vegatrader.upstox.api.broker.service.BrokerInstrumentPrewarmJob`

### Calendar & Scheduling
-   **ExpiryCalendarService Line 102**: `// TODO: Integrate with trading calendar for holiday check`
    -   **Task**: Improve expiry calculation by checking for trading holidays (NSE holidays) to shift expiry dates correctly.
-   **BrokerInstrumentPrewarmJob Line 87**: `// TODO: Integrate with trading holiday calendar`
    -   **Task**: Skip pre-warming jobs on holidays.
-   **BrokerInstrumentPrewarmJob Line 92**: `// TODO: Query from fo_contract_lifecycle where expiry_date = targetExpiry`
    -   **Task**: Database query to find which instruments are expiring soon to prioritize them.
