package com.vegatrader.upstox.api.config;

/**
 * Specialized configuration for Upstox WebSocket connections.
 * <p>
 * Upstox provides two WebSocket streams:
 * <ul>
 * <li><b>Market Data Stream</b> - Real-time market quotes, LTP, and full market
 * depth</li>
 * <li><b>Portfolio Stream</b> - Real-time portfolio updates, positions, and
 * orders</li>
 * </ul>
 * </p>
 * <p>
 * <b>WebSocket URLs:</b>
 * <ul>
 * <li>Production Market Stream: wss://api.upstox.com/v2/market/stream</li>
 * <li>Production Portfolio Stream:
 * wss://api.upstox.com/v2/portfolio/stream</li>
 * <li>Sandbox Market Stream: wss://api-sandbox.upstox.com/v2/market/stream</li>
 * <li>Sandbox Portfolio Stream:
 * wss://api-sandbox.upstox.com/v2/portfolio/stream</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Get market stream URL for production
 * String marketUrl = UpstoxWebSocketConfig.getMarketStreamUrl(UpstoxEnvironment.PRODUCTION);
 * 
 * // Get portfolio stream URL for sandbox
 * String portfolioUrl = UpstoxWebSocketConfig.getPortfolioStreamUrl(UpstoxEnvironment.SANDBOX);
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEnvironment
 * @see UpstoxBaseUrlConfig
 */
public final class UpstoxWebSocketConfig {

    // WebSocket path constants
    private static final String MARKET_STREAM_PATH = "/market/stream";
    private static final String PORTFOLIO_STREAM_PATH = "/portfolio/stream";

    // Private constructor to prevent instantiation
    private UpstoxWebSocketConfig() {
        throw new AssertionError("UpstoxWebSocketConfig cannot be instantiated");
    }

    /**
     * Gets the complete WebSocket URL for market data stream.
     * <p>
     * This stream provides real-time market data including:
     * <ul>
     * <li>Last Traded Price (LTP)</li>
     * <li>Full market depth (order book)</li>
     * <li>OHLC data</li>
     * <li>Option Greeks (for option contracts)</li>
     * </ul>
     * </p>
     *
     * @param environment the environment (PRODUCTION or SANDBOX)
     * @return the complete market stream WebSocket URL
     * @throws IllegalArgumentException if environment is null
     */
    public static String getMarketStreamUrl(UpstoxEnvironment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }

        UpstoxBaseUrlConfig config = UpstoxBaseUrlFactory.create(environment, UpstoxApiVersion.V2_STANDARD);
        return config.getWebSocketUrlWithVersion() + MARKET_STREAM_PATH;
    }

    /**
     * Gets the complete WebSocket URL for portfolio stream.
     * <p>
     * This stream provides real-time portfolio updates including:
     * <ul>
     * <li>Order updates (placed, modified, executed, cancelled)</li>
     * <li>Position updates</li>
     * <li>Holdings changes</li>
     * </ul>
     * </p>
     *
     * @param environment the environment (PRODUCTION or SANDBOX)
     * @return the complete portfolio stream WebSocket URL
     * @throws IllegalArgumentException if environment is null
     */
    public static String getPortfolioStreamUrl(UpstoxEnvironment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }

        UpstoxBaseUrlConfig config = UpstoxBaseUrlFactory.create(environment, UpstoxApiVersion.V2_STANDARD);
        return config.getWebSocketUrlWithVersion() + PORTFOLIO_STREAM_PATH;
    }

    /**
     * Gets the market stream path (without base URL).
     *
     * @return the market stream path
     */
    public static String getMarketStreamPath() {
        return MARKET_STREAM_PATH;
    }

    /**
     * Gets the portfolio stream path (without base URL).
     *
     * @return the portfolio stream path
     */
    public static String getPortfolioStreamPath() {
        return PORTFOLIO_STREAM_PATH;
    }

    /**
     * Builds a custom WebSocket URL with a custom path.
     * <p>
     * This method is useful when Upstox introduces new WebSocket endpoints.
     * </p>
     *
     * @param environment the environment
     * @param customPath  the custom path (e.g., "/custom/stream")
     * @return the complete WebSocket URL
     * @throws IllegalArgumentException if environment or customPath is null
     */
    public static String buildCustomWebSocketUrl(UpstoxEnvironment environment, String customPath) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }
        if (customPath == null || customPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Custom path cannot be null or empty");
        }

        UpstoxBaseUrlConfig config = UpstoxBaseUrlFactory.create(environment, UpstoxApiVersion.V2_STANDARD);
        String path = customPath.startsWith("/") ? customPath : "/" + customPath;
        return config.getWebSocketUrlWithVersion() + path;
    }
}
