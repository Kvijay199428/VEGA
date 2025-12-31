package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox WebSocket streaming endpoints.
 * <p>
 * WebSocket endpoints provide real-time streaming data:
 * <ul>
 * <li><b>Market Stream</b> - Real-time market quotes, LTP, depth, Greeks</li>
 * <li><b>Portfolio Stream</b> - Real-time order and position updates</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> These are WebSocket endpoints (ws:// or wss://), not REST
 * endpoints.
 * Use {@link com.vegatrader.upstox.api.config.UpstoxWebSocketConfig} to get
 * complete WebSocket URLs.
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 * @see com.vegatrader.upstox.api.config.UpstoxWebSocketConfig
 */
public enum WebSocketEndpoints implements UpstoxEndpoint {

    /**
     * Market data WebSocket stream.
     * <p>
     * WebSocket: wss://api.upstox.com/v2/market/stream
     * <br>
     * Provides real-time market data updates including LTP, depth, OHLC, and Option
     * Greeks.
     * </p>
     */
    MARKET_STREAM(
            "/market/stream",
            HttpMethod.GET,
            "WebSocket stream for real-time market data (LTP, depth, Greeks)"),

    /**
     * Portfolio data WebSocket stream.
     * <p>
     * WebSocket: wss://api.upstox.com/v2/portfolio/stream
     * <br>
     * Provides real-time portfolio updates including order status changes and
     * position updates.
     * </p>
     */
    PORTFOLIO_STREAM(
            "/portfolio/stream",
            HttpMethod.GET,
            "WebSocket stream for real-time portfolio updates (orders, positions)");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "WebSocket";

    WebSocketEndpoints(String path, HttpMethod method, String description) {
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

    /**
     * Returns true as WebSocket endpoints are always based on GET handshake.
     *
     * @return true
     */
    @Override
    public boolean isGetMethod() {
        return true;
    }
}
