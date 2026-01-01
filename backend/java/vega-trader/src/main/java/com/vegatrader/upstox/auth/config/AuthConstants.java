package com.vegatrader.upstox.auth.config;

/**
 * Authentication constants for Upstox OAuth flow and token management.
 *
 * @since 2.0.0
 */
public final class AuthConstants {

    private AuthConstants() {
        // Utility class - no instantiation
    }

    // ==================== BASE URLS ====================

    /**
     * Upstox API base URL for v2 endpoints.
     */
    public static final String API_BASE_URL = "https://api.upstox.com/v2";

    /**
     * HFT API base URL for v3 endpoints (production).
     */
    public static final String HFT_BASE_URL = "https://api-hft.upstox.com/v3";

    /**
     * Sandbox API base URL for testing.
     */
    public static final String SANDBOX_BASE_URL = "https://api-sandbox.upstox.com/v3";

    // ==================== AUTHENTICATION ENDPOINTS ====================

    /**
     * OAuth authorization dialog endpoint.
     * Full URL: https://api.upstox.com/v2/login/authorization/dialog
     */
    public static final String AUTHORIZATION_ENDPOINT = "/login/authorization/dialog";

    /**
     * Token exchange endpoint (authorization code → access token).
     * Full URL: https://api.upstox.com/v2/login/authorization/token
     */
    public static final String TOKEN_ENDPOINT = "/login/authorization/token";

    /**
     * Logout/revoke token endpoint.
     * Full URL: https://api.upstox.com/v2/logout
     */
    public static final String LOGOUT_ENDPOINT = "/logout";

    // ==================== OAUTH PARAMETERS ====================

    /**
     * OAuth grant type for authorization code flow.
     */
    public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";

    /**
     * OAuth grant type for refresh token flow.
     */
    public static final String GRANT_TYPE_REFRESH = "refresh_token";

    /**
     * OAuth response type (must be "code").
     */
    public static final String RESPONSE_TYPE_CODE = "code";

    /**
     * Token type returned by Upstox (Bearer).
     */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    // ==================== TOKEN CATEGORIES ====================

    /**
     * Primary token for standard API calls.
     */
    public static final String TOKEN_CATEGORY_PRIMARY = "PRIMARY";

    /**
     * WebSocket token prefix (WEBSOCKET1, WEBSOCKET2, WEBSOCKET3).
     */
    public static final String TOKEN_CATEGORY_WEBSOCKET_PREFIX = "WEBSOCKET";

    /**
     * Option chain token prefix (OPTIONCHAIN1, OPTIONCHAIN2).
     */
    public static final String TOKEN_CATEGORY_OPTIONCHAIN_PREFIX = "OPTIONCHAIN";

    /**
     * Total number of Primary tokens.
     */
    public static final int PRIMARY_TOKEN_COUNT = 1;

    /**
     * Total number of WebSocket tokens.
     */
    public static final int WEBSOCKET_TOKEN_COUNT = 3;

    /**
     * Total number of Upstox APIs.
     */
    public static final int TOTAL_UPSTOX_APIS = 6;

    /**
     * Canonical API Order (Immutable).
     * Defines exactly which APIs exist and their sequence.
     */
    public static final java.util.List<String> API_ORDER = java.util.List.of(
            "PRIMARY",
            "WEBSOCKET_1",
            "WEBSOCKET_2",
            "WEBSOCKET_3",
            "OPTION_CHAIN_1",
            "OPTION_CHAIN_2");

    /**
     * Total number of Option Chain tokens.
     */
    public static final int OPTIONCHAIN_TOKEN_COUNT = 2;

    // ==================== TOKEN EXPIRY ====================

    /**
     * Token expiry time (3:30 AM next day).
     */
    public static final String TOKEN_EXPIRY_TIME = "03:30:00";

    /**
     * Token refresh time (2:30 AM - 1 hour before expiry).
     */
    public static final String TOKEN_REFRESH_TIME = "02:30:00";

    /**
     * Token validity duration in seconds (until 3:30 AM next day, max ~24 hours).
     */
    public static final long TOKEN_VALIDITY_SECONDS = 86400; // 24 hours

    // ==================== DATABASE ====================

    /**
     * SQLite database path.
     */
    public static final String DATABASE_PATH = "database/vega_trader.db";

    /**
     * JDBC connection string for SQLite.
     */
    public static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_PATH;

    /**
     * Table name for tokens.
     */
    public static final String TABLE_TOKENS = "upstox_tokens";

    /**
     * Table name for audit logs.
     */
    public static final String TABLE_AUDIT = "token_audit_logs";

    // ==================== SECURITY ====================

    /**
     * AES encryption algorithm.
     */
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";

    /**
     * AES key size in bits.
     */
    public static final int AES_KEY_SIZE = 256;

    /**
     * GCM IV size in bytes.
     */
    public static final int GCM_IV_SIZE = 12;

    /**
     * GCM tag size in bits.
     */
    public static final int GCM_TAG_SIZE = 128;

    // ==================== ENDPOINT CATEGORIZATION ====================

    /**
     * WebSocket endpoints that use WebSocket tokens.
     */
    public static final String[] WEBSOCKET_ENDPOINTS = {
            "/feed/market-data-feed",
            "/feed/portfolio-stream-feed",
            "/feed/order-updates"
    };

    /**
     * Option chain endpoints that use Option Chain tokens.
     */
    public static final String[] OPTIONCHAIN_ENDPOINTS = {
            "/option/contract",
            "/option/chain"
    };

    // ==================== HTTP HEADERS ====================

    /**
     * Authorization header name.
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Content-Type header name.
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Accept header name.
     */
    public static final String HEADER_ACCEPT = "Accept";

    /**
     * JSON content type.
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Form URL encoded content type.
     */
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    // ==================== ERROR CODES ====================

    /**
     * Invalid client ID or secret.
     */
    public static final String ERROR_INVALID_CLIENT = "invalid_client";

    /**
     * Invalid authorization grant (expired or invalid code).
     */
    public static final String ERROR_INVALID_GRANT = "invalid_grant";

    /**
     * Invalid redirect URI.
     */
    public static final String ERROR_INVALID_REDIRECT = "invalid_redirect_uri";

    /**
     * User denied access.
     */
    public static final String ERROR_ACCESS_DENIED = "access_denied";

    /**
     * Token expired.
     */
    public static final String ERROR_TOKEN_EXPIRED = "token_expired";

    /**
     * Token not found.
     */
    public static final String ERROR_TOKEN_NOT_FOUND = "token_not_found";

    /**
     * Unauthorized request.
     */
    public static final String ERROR_UNAUTHORIZED = "unauthorized";
}
