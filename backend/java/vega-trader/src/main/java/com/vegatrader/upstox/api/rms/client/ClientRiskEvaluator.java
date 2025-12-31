package com.vegatrader.upstox.api.rms.client;

import org.springframework.stereotype.Service;

/**
 * Client-level risk evaluator.
 * 
 * <p>
 * Validates orders against client exposure limits before broker submission.
 * 
 * <p>
 * Validation order:
 * <ol>
 * <li>Kill-switch (trading_enabled)</li>
 * <li>Max order value</li>
 * <li>Gross exposure limit</li>
 * <li>Net exposure limit</li>
 * <li>Intraday turnover</li>
 * <li>Position count</li>
 * <li>Max intraday loss</li>
 * </ol>
 * 
 * @since 4.1.0
 */
@Service
public class ClientRiskEvaluator {

    /**
     * Validates an order against client limits.
     * 
     * @param limit              client's risk limits
     * @param state              client's current risk state
     * @param orderValue         absolute value of the order
     * @param projectedGross     gross exposure after order
     * @param projectedNet       net exposure after order
     * @param projectedPositions position count after order
     * @throws RiskRejectException if any limit is breached
     */
    public void validate(
            ClientRiskLimit limit,
            ClientRiskState state,
            double orderValue,
            double projectedGross,
            double projectedNet,
            int projectedPositions) {
        // 1. Kill-switch check
        if (!limit.tradingEnabled()) {
            throw RiskRejectException.clientDisabled(limit.clientId());
        }

        // 2. Per-order value limit
        if (orderValue > limit.maxOrderValue()) {
            throw RiskRejectException.orderValueExceeded(orderValue, limit.maxOrderValue());
        }

        // 3. Gross exposure limit
        if (projectedGross > limit.maxGrossExposure()) {
            throw RiskRejectException.grossExposureExceeded(projectedGross, limit.maxGrossExposure());
        }

        // 4. Net exposure limit (absolute)
        if (Math.abs(projectedNet) > limit.maxNetExposure()) {
            throw RiskRejectException.netExposureExceeded(Math.abs(projectedNet), limit.maxNetExposure());
        }

        // 5. Intraday turnover limit
        double projectedTurnover = state.intradayTurnover() + orderValue;
        if (projectedTurnover > limit.maxIntradayTurnover()) {
            throw RiskRejectException.turnoverExceeded(projectedTurnover, limit.maxIntradayTurnover());
        }

        // 6. Position count limit
        if (projectedPositions > limit.maxOpenPositions()) {
            throw RiskRejectException.positionCountExceeded(projectedPositions, limit.maxOpenPositions());
        }

        // 7. Max intraday loss (pre-trade check)
        double currentLoss = state.intradayLoss();
        if (currentLoss > limit.maxIntradayLoss()) {
            throw RiskRejectException.maxLossHit(currentLoss, limit.maxIntradayLoss());
        }
    }

    /**
     * Quick check if client can trade at all.
     */
    public boolean canTrade(ClientRiskLimit limit, ClientRiskState state) {
        if (!limit.tradingEnabled())
            return false;
        if (state.intradayLoss() > limit.maxIntradayLoss())
            return false;
        return true;
    }
}
