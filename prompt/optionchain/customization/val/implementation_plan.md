# Implementation Plan - Option Chain Valuation Module

## Goal Description
Implement an enterprise-grade Option Chain Valuation engine to provide real-time fair value assessments, Greeks calculation (Delta, Gamma, Theta, Vega), and confidence scoring. This feature will enrich the existing Option Chain feed with "OVERVALUED", "UNDERVALUED", or "FAIR" status, along with actionable signals.

## User Review Required
> [!IMPORTANT]
> **Performance Impact**: Real-time Black-Scholes calculation for all strikes may add latency. We will safeguard this with `disableOnLatencyMs` settings.
> **Configuration**: Valuation is driven by strict priority: Admin Defaults > Admin Overrides > User Overrides.

## Proposed Changes

### 1. New Package: `com.vegatrader.analytics.valuation`
This package will contain the core mathematical and logic components.

#### [NEW] [BlackScholesPricer.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/analytics/valuation/BlackScholesPricer.java)
- Core Black-Scholes-Merton formula implementation across Call and Put options.
- Includes `normalCdf` and `erf` approximations.

#### [NEW] [ImpliedVolatilitySolver.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/analytics/valuation/ImpliedVolatilitySolver.java)
- Newton-Raphson solver to back-calculate IV from market price.
- Handled edge cases (low price, zero time).

#### [NEW] [ValuationSettings.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/analytics/valuation/ValuationSettings.java)
- Immutable Configuration record.
- Settings: risk-free rate, thresholds, enable flags.

#### [NEW] [ConfidenceScorer.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/analytics/valuation/ConfidenceScorer.java)
- Scores confidence (HIGH/MEDIUM/LOW) based on Spread, Volume, and OI.

#### [NEW] [ValuationEngine.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/analytics/valuation/ValuationEngine.java)
- Orchestrates pricing, IV calculation, and confidence scoring.
- Returns `ValuationResult` with status and action signals.

#### [NEW] [OptionChainValuationService.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/analytics/valuation/OptionChainValuationService.java)
- Integration service to `enrich` the Option Chain DTOs.
- Calculates ATM IV and applies valuation to all rows.

### 2. Integration with Option Chain

#### [MODIFY] [OptionChainStrike.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/model/OptionChainStrike.java)
- Add `ValuationResult` field to [OptionData](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/model/OptionChainStrike.java#25-30) record.

#### [MODIFY] [OptionChainFeedStreamV3.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/stream/OptionChainFeedStreamV3.java)
- Call `OptionChainValuationService.enrich` logic before finalizing updates (or as a post-processing step before broadcast).
- **Note**: To keep [FeedStreamV3](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/stream/OptionChainFeedStreamV3.java#18-137) pure, we might apply this enrichment in the [OptionChainFeeder](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/stream/OptionChainFeeder.java#14-113) or [WebSocketHandler](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/stream/WebSocketConfig.java#22-27) depending on architecture. The plan suggests [OptionChainFeedStreamV3](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/stream/OptionChainFeedStreamV3.java#18-137) -> `OptionChainNormalizer` -> `OptionChainValuationService`. We will inspect [OptionChainStreamManager](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/optionchain/stream/OptionChainStreamManager.java#19-102) to confirm the best hook.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `BlackScholesPricerTest`: Verify against known option calculator values.
    - `ImpliedVolatilitySolverTest`: Verify IV convergence.
    - `ValuationEngineTest`: Test over/undervalued logic.
- **Integration Tests**:
    - `OptionChainValuationTest`: Verify enrichment of `OptionChainRow`.

### Manual Verification
- **API Response**: call `GET /api/option-chain` and check for `valuation` fields.
- **WebSocket Feed**: Connect to WS and verify real-time valuation updates.
