package com.vegatrader.upstox.auth.service;

/**
 * Broker Cooldown Exception - Explicit signal for broker throttling.
 * 
 * Only these errors should trigger cooldown:
 * - Timeout waiting for PIN submit
 * - Timeout waiting for redirect URL
 * - Authorization code not received
 * - Selenium stuck after successful OTP + PIN
 *
 * @since 2.4.0
 */
public class BrokerCooldownException extends RuntimeException {

    private final String apiName;
    private final String reason;

    public BrokerCooldownException(String apiName, Throwable cause) {
        super("Broker cooldown required for: " + apiName, cause);
        this.apiName = apiName;
        this.reason = cause != null ? cause.getMessage() : "Unknown";
    }

    public BrokerCooldownException(String apiName, String reason) {
        super("Broker cooldown required for: " + apiName + " - " + reason);
        this.apiName = apiName;
        this.reason = reason;
    }

    public String getApiName() {
        return apiName;
    }

    public String getReason() {
        return reason;
    }
}
