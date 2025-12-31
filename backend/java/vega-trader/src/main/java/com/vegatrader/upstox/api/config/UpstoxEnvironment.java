package com.vegatrader.upstox.api.config;

/**
 * Represents the Upstox API environment.
 * <p>
 * The Upstox API provides two environments:
 * <ul>
 * <li><b>PRODUCTION</b> - Live trading environment with real market data and
 * actual order execution</li>
 * <li><b>SANDBOX</b> - Testing environment for development and testing without
 * real trading</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxBaseUrlConfig
 */
public enum UpstoxEnvironment {
    /**
     * Production environment for live trading
     */
    PRODUCTION,

    /**
     * Sandbox environment for testing and development
     */
    SANDBOX;

    /**
     * Returns true if this is the production environment.
     *
     * @return true if PRODUCTION, false otherwise
     */
    public boolean isProduction() {
        return this == PRODUCTION;
    }

    /**
     * Returns true if this is the sandbox environment.
     *
     * @return true if SANDBOX, false otherwise
     */
    public boolean isSandbox() {
        return this == SANDBOX;
    }
}
