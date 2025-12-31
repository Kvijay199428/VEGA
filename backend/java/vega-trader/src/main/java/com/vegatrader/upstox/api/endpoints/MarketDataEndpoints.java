package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox Market Data endpoints.
 * <p>
 * Comprehensive market data endpoints including:
 * <ul>
 * <li>Market quotes (full, OHLC, LTP)</li>
 * <li>Option Greeks</li>
 * <li>Historical data (candlestick, OHLC)</li>
 * <li>Market information (status, brokers)</li>
 * <li>Instruments</li>
 * <li>Charges and Margins</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 */
public enum MarketDataEndpoints implements UpstoxEndpoint {

    // Quotes
    /**
     * Get full market quote with depth.
     * <p>
     * GET /market-quote/quotes
     * </p>
     */
    FULL_QUOTE("/market-quote/quotes", HttpMethod.GET, "Get full market quote with OHLC, depth, and Greeks"),

    /**
     * Get OHLC (Open, High, Low, Close) data.
     * <p>
     * GET /market-quote/ohlc
     * </p>
     */
    OHLC_QUOTE("/market-quote/ohlc", HttpMethod.GET, "Get OHLC (Open, High, Low, Close) data"),

    /**
     * Get Last Traded Price (LTP).
     * <p>
     * GET /market-quote/ltp
     * </p>
     */
    LTP_QUOTE("/market-quote/ltp", HttpMethod.GET, "Get Last Traded Price (LTP)"),

    /**
     * Get Option Greeks (Delta, Gamma, Theta, Vega).
     * <p>
     * GET /market-quote/option-greeks
     * </p>
     */
    OPTION_GREEKS("/market-quote/option-greeks", HttpMethod.GET, "Get Option Greeks (Delta, Gamma, Theta, Vega, Rho)"),

    // Historical Data
    /**
     * Get historical candlestick data.
     * <p>
     * GET /market-quote/candlestick
     * </p>
     */
    CANDLESTICK_DATA("/market-quote/candlestick", HttpMethod.GET, "Get historical candlestick data (OHLCV)"),

    /**
     * Get historical OHLC data.
     * <p>
     * GET /market-quote/historical
     * </p>
     */
    HISTORICAL_OHLC("/market-quote/historical", HttpMethod.GET, "Get historical OHLC data"),

    /**
     * Get index historical data.
     * <p>
     * GET /market-quote/index-historical
     * </p>
     */
    INDEX_HISTORICAL("/market-quote/index-historical", HttpMethod.GET, "Get historical data for indices"),

    // Market Information
    /**
     * Get list of brokers.
     * <p>
     * GET /market-information/brokers
     * </p>
     */
    GET_BROKERS("/market-information/brokers", HttpMethod.GET, "Get list of brokers"),

    /**
     * Get market status (open/closed).
     * <p>
     * GET /market-information/market-status
     * </p>
     */
    MARKET_STATUS("/market-information/market-status", HttpMethod.GET,
            "Get market open/close status for all exchanges"),

    // Instruments
    /**
     * Get list of BOD instruments.
     * <p>
     * GET /instruments
     * </p>
     */
    GET_INSTRUMENTS("/instruments", HttpMethod.GET, "Get list of Beginning-of-Day (BOD) instruments"),

    /**
     * Get expired option contracts.
     * <p>
     * GET /instruments/expired
     * </p>
     */
    GET_EXPIRED_INSTRUMENTS("/instruments/expired", HttpMethod.GET, "Get list of expired option contracts"),

    // Charges & Margins
    /**
     * Get brokerage charges.
     * <p>
     * GET /charges
     * </p>
     */
    GET_CHARGES("/charges", HttpMethod.GET, "Get brokerage charges for an order"),

    /**
     * Get margin requirements.
     * <p>
     * GET /margins
     * </p>
     */
    GET_MARGINS("/margins", HttpMethod.GET, "Get margin requirements for an order");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "Market Data";

    MarketDataEndpoints(String path, HttpMethod method, String description) {
        this.path = path;
        this.method = method;
        this.description = description;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
