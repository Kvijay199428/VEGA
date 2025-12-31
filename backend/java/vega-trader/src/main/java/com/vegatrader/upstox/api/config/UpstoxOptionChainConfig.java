package com.vegatrader.upstox.api.config;

/**
 * Specialized configuration for Upstox Option Chain endpoints.
 * <p>
 * The option chain endpoint provides comprehensive option data including:
 * <ul>
 * <li>Call and Put option contracts</li>
 * <li>Strike prices</li>
 * <li>Expiry dates</li>
 * <li>Open Interest</li>
 * <li>LTP, bid/ask prices</li>
 * </ul>
 * </p>
 * <p>
 * <b>Option Chain Endpoint:</b>
 * <ul>
 * <li>Production: https://api.upstox.com/v2/option/chain</li>
 * <li>Sandbox: https://api-sandbox.upstox.com/v2/option/chain</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Get option chain base URL for production
 * String baseUrl = UpstoxOptionChainConfig.getOptionChainBaseUrl(UpstoxEnvironment.PRODUCTION);
 * 
 * // Build complete option chain URL with parameters
 * String url = UpstoxOptionChainConfig.buildOptionChainUrl(
 *         UpstoxEnvironment.PRODUCTION,
 *         "NSE_INDEX|Nifty 50",
 *         "2025-02-27");
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEnvironment
 * @see UpstoxBaseUrlConfig
 */
public final class UpstoxOptionChainConfig {

    // Option chain endpoint path
    private static final String OPTION_CHAIN_PATH = "/option/chain";

    // Query parameter names
    private static final String PARAM_INSTRUMENT_KEY = "instrument_key";
    private static final String PARAM_EXPIRY_DATE = "expiry_date";

    // Private constructor to prevent instantiation
    private UpstoxOptionChainConfig() {
        throw new AssertionError("UpstoxOptionChainConfig cannot be instantiated");
    }

    /**
     * Gets the base URL for option chain endpoint.
     * <p>
     * This returns the REST API base URL suitable for option chain queries.
     * </p>
     *
     * @param environment the environment (PRODUCTION or SANDBOX)
     * @return the option chain base URL
     * @throws IllegalArgumentException if environment is null
     */
    public static String getOptionChainBaseUrl(UpstoxEnvironment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }

        UpstoxBaseUrlConfig config = UpstoxBaseUrlFactory.create(environment, UpstoxApiVersion.V2_STANDARD);
        return config.getRestApiBaseUrl() + OPTION_CHAIN_PATH;
    }

    /**
     * Builds a complete option chain URL with instrument key and expiry date
     * parameters.
     * <p>
     * <b>Example:</b><br>
     * {@code buildOptionChainUrl(UpstoxEnvironment.PRODUCTION, "NSE_INDEX|Nifty 50", "2025-02-27")}
     * <br>
     * Returns:
     * {@code https://api.upstox.com/v2/option/chain?instrument_key=NSE_INDEX|Nifty 50&expiry_date=2025-02-27}
     * </p>
     *
     * @param environment   the environment
     * @param instrumentKey the underlying instrument key (e.g., "NSE_INDEX|Nifty
     *                      50")
     * @param expiryDate    the expiry date in YYYY-MM-DD format
     * @return the complete option chain URL with parameters
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public static String buildOptionChainUrl(UpstoxEnvironment environment, String instrumentKey, String expiryDate) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }
        if (instrumentKey == null || instrumentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Instrument key cannot be null or empty");
        }
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date cannot be null or empty");
        }

        String baseUrl = getOptionChainBaseUrl(environment);

        // URL encode parameters
        try {
            String encodedInstrument = java.net.URLEncoder.encode(instrumentKey, "UTF-8");
            String encodedExpiry = java.net.URLEncoder.encode(expiryDate, "UTF-8");

            return String.format("%s?%s=%s&%s=%s",
                    baseUrl,
                    PARAM_INSTRUMENT_KEY, encodedInstrument,
                    PARAM_EXPIRY_DATE, encodedExpiry);
        } catch (java.io.UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            throw new RuntimeException("Failed to encode URL parameters", e);
        }
    }

    /**
     * Builds an option chain URL with only instrument key (no expiry filter).
     * <p>
     * This will return all available expiries for the given instrument.
     * </p>
     *
     * @param environment   the environment
     * @param instrumentKey the underlying instrument key
     * @return the option chain URL with instrument key parameter
     * @throws IllegalArgumentException if environment or instrumentKey is
     *                                  null/empty
     */
    public static String buildOptionChainUrl(UpstoxEnvironment environment, String instrumentKey) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }
        if (instrumentKey == null || instrumentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Instrument key cannot be null or empty");
        }

        String baseUrl = getOptionChainBaseUrl(environment);

        try {
            String encodedInstrument = java.net.URLEncoder.encode(instrumentKey, "UTF-8");
            return String.format("%s?%s=%s", baseUrl, PARAM_INSTRUMENT_KEY, encodedInstrument);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode URL parameters", e);
        }
    }

    /**
     * Gets the option chain endpoint path.
     *
     * @return the option chain path
     */
    public static String getOptionChainPath() {
        return OPTION_CHAIN_PATH;
    }

    /**
     * Gets the instrument key parameter name.
     *
     * @return the instrument key parameter name
     */
    public static String getInstrumentKeyParam() {
        return PARAM_INSTRUMENT_KEY;
    }

    /**
     * Gets the expiry date parameter name.
     *
     * @return the expiry date parameter name
     */
    public static String getExpiryDateParam() {
        return PARAM_EXPIRY_DATE;
    }
}
