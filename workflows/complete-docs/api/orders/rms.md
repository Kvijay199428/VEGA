# Risk Management System (RMS)

The RMS module performs critical pre-trade validations to ensure compliance, safety, and solvency.

## Services

### 1. Client Risk Service
**Class**: `com.vegatrader.upstox.api.rms.client.ClientRiskService`

Manages financial limits at the client level.

**Key Features**:
- **Limit Management**: Retrieves `ClientRiskLimit` (Gross Exposure, Net Exposure, Position Count).
- **State Tracking**: Tracks distinct `ClientRiskState` for each client (Current MTM, Open Positions).
- **Validation**: `evaluator.validate()` ensures operations stay within defined limits.
- **Kill-Switch**: Can hierarchically disable a specific client or **ALL** clients (`disableAllClients`) in emergencies.
- **Lifecycle**: Handles BOD state resets and MTM updates.

### 2. RMS Validation Service
**Class**: `com.vegatrader.upstox.api.rms.validation.RmsValidationService`

Performs order-level validation against market rules and contract specifications.

**Validation Pipeline**:
1. **Product Eligibility**: Checks if the product (MIS, CNC, MTF) is allowed for the instrument.
2. **Price Bands**: Ensures order price is within the exchange-defined Lower/Upper circuit limits.
3. **Quantity Caps**: Enforces maximum quantity per order and value caps.
4. **Contract Status**: Rejects orders for expired or inactive F&O contracts.
5. **T2T Check**: Blocks intraday square-off for Trade-to-Trade (T2T) scrips if configured.
6. **Margin Calculation**: Computes required margin based on product type (e.g., MIS=20%, CNC=100%).

## Architecture
The RMS is designed as a gatekeeper. No order reaches the broker adapter unless it passes both `ClientRiskService` checks (Solvency) and `RmsValidationService` checks (Compliance).
