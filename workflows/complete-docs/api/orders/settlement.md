# Settlement Module

The Settlement Service tracks trade settlement cycles (T+0, T+1) to provide accurate "Available Funds" calculations.

## Service: Settlement Service
**Class**: `com.vegatrader.upstox.api.order.settlement.SettlementService`

### Settlement Cycles
Defined by regulatory standards per segment:
- **F&O (NSE_FO, BSE_FO)**: **T+0** (Same day settlement).
- **Equity (NSE_EQ, BSE_EQ)**: **T+1** (Next business day).
- **Commodity (MCX)**: **T+1**.
- **Mutual Funds**: **T+2**.

### Features
- **Holiday Awareness**: Skips weekends and defined Indian market holidays.
- **Pending Settlements**: Calculates funds trapped in "Pending Settlement" state.
- **Calendar**: Generates settlement calendars for UI display.

### Usage
Used by the Portfolio and Risk modules to determine the actual disposable cash balance, distinguishing between "Realized P&L" and "Settled Cash".
