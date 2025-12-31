package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox Authentication endpoints.
 * <p>
 * These endpoints handle user authentication, token generation, renewal, and
 * logout.
 * </p>
 * <p>
 * <b>Authentication Flow:</b>
 * <ol>
 * <li>Redirect user to {@link #LOGIN_DIALOG} to get authorization code</li>
 * <li>Exchange authorization code for access token using
 * {@link #GET_TOKEN}</li>
 * <li>Use access token for authenticated API calls</li>
 * <li>Renew token before expiry using {@link #RENEW_TOKEN}</li>
 * <li>Revoke token on logout using {@link #LOGOUT}</li>
 * </ol>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 */
public enum AuthenticationEndpoints implements UpstoxEndpoint {

    /**
     * Authorization dialog for user login.
     * <p>
     * GET /login/authorization/dialog
     * <br>
     * Redirects user to Upstox login page to authorize the application.
     * </p>
     */
    LOGIN_DIALOG(
            "/login/authorization/dialog",
            HttpMethod.GET,
            "Display authorization dialog for user login"),

    /**
     * Exchange authorization code for access token.
     * <p>
     * POST /login/authorization/token
     * <br>
     * Requires: authorization code, client_id, client_secret, redirect_uri,
     * grant_type
     * </p>
     */
    GET_TOKEN(
            "/login/authorization/token",
            HttpMethod.POST,
            "Exchange authorization code for access token"),

    /**
     * Logout and revoke access token.
     * <p>
     * POST /logout
     * <br>
     * Revokes the current access token, requiring re-authentication.
     * </p>
     */
    LOGOUT(
            "/logout",
            HttpMethod.POST,
            "Revoke access token and logout user"),

    /**
     * Renew/refresh access token.
     * <p>
     * POST /login/authorization/token
     * <br>
     * Uses refresh token to obtain a new access token.
     * </p>
     */
    RENEW_TOKEN(
            "/login/authorization/token",
            HttpMethod.POST,
            "Refresh access token using refresh token");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "Authentication";

    AuthenticationEndpoints(String path, HttpMethod method, String description) {
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
