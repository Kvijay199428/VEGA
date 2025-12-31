package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox Portfolio endpoints.
 * <p>
 * Endpoints for managing and retrieving portfolio information:
 * <ul>
 * <li>Holdings (long-term investments)</li>
 * <li>Positions (intraday and short-term)</li>
 * <li>Position conversion (MIS to CNC, etc.)</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 */
public enum PortfolioEndpoints implements UpstoxEndpoint {

    /**
     * Get long-term holdings.
     * <p>
     * GET /portfolio/long-term-holdings
     * <br>
     * Returns: stocks held in delivery/CNC with quantity, average price, current
     * price, P&L
     * </p>
     */
    GET_HOLDINGS(
            "/portfolio/long-term-holdings",
            HttpMethod.GET,
            "Get long-term holdings (delivery stocks)"),

    /**
     * Get short-term positions (intraday).
     * <p>
     * GET /portfolio/short-term-positions
     * <br>
     * Returns: open intraday positions with quantity, entry price, current price,
     * P&L
     * </p>
     */
    GET_POSITIONS(
            "/portfolio/short-term-positions",
            HttpMethod.GET,
            "Get short-term positions (intraday and F&O)"),

    /**
     * Get net positions (consolidated view).
     * <p>
     * GET /portfolio/net-positions
     * <br>
     * Returns: net positions across all segments with aggregated data
     * </p>
     */
    GET_NET_POSITIONS(
            "/portfolio/net-positions",
            HttpMethod.GET,
            "Get net positions with consolidated view"),

    /**
     * Convert position from one product type to another.
     * <p>
     * POST /portfolio/convert-position
     * <br>
     * Convert positions (e.g., MIS to CNC, NRML to MIS)
     * </p>
     */
    CONVERT_POSITION(
            "/portfolio/convert-position",
            HttpMethod.POST,
            "Convert position from one product type to another");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "Portfolio";

    PortfolioEndpoints(String path, HttpMethod method, String description) {
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
