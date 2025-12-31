package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox User Profile endpoints.
 * <p>
 * These endpoints provide access to user profile information and fund details.
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 */
public enum UserProfileEndpoints implements UpstoxEndpoint {

    /**
     * Get user profile information.
     * <p>
     * GET /user/profile
     * <br>
     * Returns: user name, email, PAN, exchanges enabled, products enabled, etc.
     * </p>
     */
    USER_PROFILE(
            "/user/profile",
            HttpMethod.GET,
            "Get user profile information including name, email, and enabled products"),

    /**
     * Get user funds and margin details.
     * <p>
     * GET /user/get-funds-and-active-orders
     * <br>
     * Returns: available margin, used margin, active orders, and fund breakdown by
     * segment.
     * </p>
     */
    USER_FUNDS(
            "/user/get-funds-and-active-orders",
            HttpMethod.GET,
            "Get user fund details including available margin and active orders");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "User Profile";

    UserProfileEndpoints(String path, HttpMethod method, String description) {
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
