package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox Option Chain endpoints.
 * <p>
 * Endpoints specifically for option chain data retrieval.
 * </p>
 * <p>
 * <b>Usage:</b><br>
 * To get option chain data, you need to provide:
 * <ul>
 * <li><b>instrument_key</b> - Underlying instrument (e.g., "NSE_INDEX|Nifty
 * 50")</li>
 * <li><b>expiry_date</b> - Option expiry date in YYYY-MM-DD format
 * (optional)</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 * @see com.vegatrader.upstox.api.config.UpstoxOptionChainConfig
 */
public enum OptionChainEndpoints implements UpstoxEndpoint {

    /**
     * Get option chain for an underlying instrument.
     * <p>
     * GET /option/chain
     * <br>
     * Query params: instrument_key (required), expiry_date (optional)
     * <br>
     * Returns: Call and Put options with strikes, Greeks, open interest, LTP
     * </p>
     */
    GET_OPTION_CHAIN(
            "/option/chain",
            HttpMethod.GET,
            "Get option chain with all strikes for an underlying instrument"),

    /**
     * Get put-call option chain (same as GET_OPTION_CHAIN).
     * <p>
     * GET /option/chain
     * <br>
     * This is an alias for the standard option chain endpoint.
     * </p>
     */
    GET_PUT_CALL_CHAIN(
            "/option/chain",
            HttpMethod.GET,
            "Get put-call option chain (alias for standard option chain)");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "Option Chain";

    OptionChainEndpoints(String path, HttpMethod method, String description) {
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
