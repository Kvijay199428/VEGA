package com.vegatrader.upstox.api.rms.client;

import com.vegatrader.upstox.api.rms.validation.RmsException;

/**
 * Client-level risk rejection exception.
 * 
 * @since 4.1.0
 */
public class RiskRejectException extends RmsException {

    public RiskRejectException(String code) {
        super(code);
    }

    public RiskRejectException(String code, String message) {
        super(code, message);
    }

    // Common rejection codes
    public static RiskRejectException clientDisabled(String clientId) {
        return new RiskRejectException("CLIENT_DISABLED",
                "Trading disabled for client: " + clientId);
    }

    public static RiskRejectException orderValueExceeded(double value, double limit) {
        return new RiskRejectException("ORDER_VALUE_LIMIT",
                String.format("Order value %.2f exceeds limit %.2f", value, limit));
    }

    public static RiskRejectException grossExposureExceeded(double projected, double limit) {
        return new RiskRejectException("GROSS_EXPOSURE_LIMIT",
                String.format("Projected gross exposure %.2f exceeds limit %.2f", projected, limit));
    }

    public static RiskRejectException netExposureExceeded(double projected, double limit) {
        return new RiskRejectException("NET_EXPOSURE_LIMIT",
                String.format("Projected net exposure %.2f exceeds limit %.2f", projected, limit));
    }

    public static RiskRejectException turnoverExceeded(double projected, double limit) {
        return new RiskRejectException("TURNOVER_LIMIT",
                String.format("Intraday turnover %.2f exceeds limit %.2f", projected, limit));
    }

    public static RiskRejectException positionCountExceeded(int projected, int limit) {
        return new RiskRejectException("POSITION_COUNT_LIMIT",
                String.format("Position count %d exceeds limit %d", projected, limit));
    }

    public static RiskRejectException maxLossHit(double loss, double limit) {
        return new RiskRejectException("MAX_LOSS_HIT",
                String.format("Intraday loss %.2f exceeds limit %.2f", loss, limit));
    }
}
