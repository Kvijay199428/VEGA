# Analytics Service

The Analytics module provides real-time quantitative analysis, specifically Black-Scholes-Merton valuation for options.

## 1. Black-Scholes Pricer
**Class**: `com.vegatrader.analytics.valuation.BlackScholesPricer`

Standard implementation of the Black-Scholes model.
- **Pricing**: Calculates theoretical Price for Calls and Puts.
- **Greeks**:
  - `Delta`: Sensitivity to underlying price change.
  - `Gamma`: Sensitivity to Delta change.
  - `Theta`: Time decay (daily).
  - `Vega`: Sensitivity to Volatility.
  - `Rho`: Sensitivity to Interest Rate.

## 2. Option Chain Valuation
**Class**: `com.vegatrader.analytics.valuation.OptionChainValuationService`

Enriches raw Option Chain data with calculated valuation metrics.

### Process
1. **Spot Identification**: Approximates spot price from ATM strikes or underlying feed.
2. **IV Calculation**: Solves for Implied Volatility (IV) using the current market price of ATM options (Newton-Raphson approximation via `ImpliedVolatilitySolver`).
3. **Full Chain Valuation**: Applies the calculated ATM IV (or a volatility skew curve) to price all other strikes in the chain.

### Configuration (`ValuationSettings`)
- **Risk Free Rate**: Default 10% (0.10).
- **Default Volatility**: Fallback if IV solver fails (Default 15%).
