package com.vegatrader.upstox.api.rms.client;

/**
 * Real-time risk state for a client.
 * Mutable tracking of exposure and MTM.
 * 
 * @since 4.1.0
 */
public record ClientRiskState(
        String clientId,
        double grossExposure,
        double netExposure,
        double intradayTurnover,
        double currentMtm,
        int openPositions) {

    /**
     * Creates zero state for new client.
     */
    public static ClientRiskState zero(String clientId) {
        return new ClientRiskState(clientId, 0, 0, 0, 0, 0);
    }

    /**
     * Calculates projected state after order execution.
     */
    public ClientRiskState withOrder(double orderValue, int side, boolean isNewPosition) {
        double newGross = grossExposure + Math.abs(orderValue);
        double newNet = netExposure + (side > 0 ? orderValue : -orderValue);
        double newTurnover = intradayTurnover + Math.abs(orderValue);
        int newPositions = isNewPosition ? openPositions + 1 : openPositions;

        return new ClientRiskState(clientId, newGross, newNet, newTurnover, currentMtm, newPositions);
    }

    /**
     * Updates MTM (called on tick update).
     */
    public ClientRiskState withMtm(double mtm) {
        return new ClientRiskState(clientId, grossExposure, netExposure, intradayTurnover, mtm, openPositions);
    }

    /**
     * Calculates current intraday loss.
     */
    public double intradayLoss() {
        return Math.max(0, -currentMtm);
    }

    /**
     * Checks if in profit.
     */
    public boolean isInProfit() {
        return currentMtm > 0;
    }
}
