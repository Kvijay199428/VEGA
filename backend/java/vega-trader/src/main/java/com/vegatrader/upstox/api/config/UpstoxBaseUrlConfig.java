package com.vegatrader.upstox.api.config;

/**
 * Immutable configuration class for Upstox API base URLs.
 * <p>
 * This class holds all base URLs for different Upstox API endpoints based on
 * environment
 * (Production vs Sandbox) and API version (v2 Standard vs v3 HFT).
 * </p>
 * <p>
 * <b>Base URLs:</b>
 * <ul>
 * <li>Production v2: https://api.upstox.com/v2</li>
 * <li>Production v3 HFT: https://api-hft.upstox.com/v3</li>
 * <li>Sandbox v2: https://api-sandbox.upstox.com/v2</li>
 * <li>Sandbox v3 HFT: https://api-sandbox.upstox.com/v3</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEnvironment
 * @see UpstoxApiVersion
 * @see UpstoxBaseUrlFactory
 */
public final class UpstoxBaseUrlConfig {

    // Production Base URLs
    private static final String PRODUCTION_V2_BASE = "https://api.upstox.com";
    private static final String PRODUCTION_V3_HFT_BASE = "https://api-hft.upstox.com";

    // Sandbox Base URLs
    private static final String SANDBOX_V2_BASE = "https://api-sandbox.upstox.com";
    private static final String SANDBOX_V3_HFT_BASE = "https://api-sandbox.upstox.com";

    // WebSocket Base URLs
    private static final String PRODUCTION_WEBSOCKET_BASE = "wss://api.upstox.com";
    private static final String SANDBOX_WEBSOCKET_BASE = "wss://api-sandbox.upstox.com";

    private final UpstoxEnvironment environment;
    private final UpstoxApiVersion apiVersion;
    private final String restApiBaseUrl;
    private final String webSocketBaseUrl;

    /**
     * Private constructor to enforce factory pattern.
     *
     * @param environment the environment (PRODUCTION or SANDBOX)
     * @param apiVersion  the API version (V2_STANDARD or V3_HFT)
     */
    UpstoxBaseUrlConfig(UpstoxEnvironment environment, UpstoxApiVersion apiVersion) {
        this.environment = environment;
        this.apiVersion = apiVersion;
        this.restApiBaseUrl = buildRestApiBaseUrl(environment, apiVersion);
        this.webSocketBaseUrl = buildWebSocketBaseUrl(environment);
    }

    /**
     * Builds the REST API base URL based on environment and version.
     *
     * @param environment the environment
     * @param apiVersion  the API version
     * @return the complete base URL
     */
    private String buildRestApiBaseUrl(UpstoxEnvironment environment, UpstoxApiVersion apiVersion) {
        String baseUrl;

        if (environment == UpstoxEnvironment.PRODUCTION) {
            baseUrl = apiVersion == UpstoxApiVersion.V3_HFT ? PRODUCTION_V3_HFT_BASE : PRODUCTION_V2_BASE;
        } else {
            // Sandbox uses same base for both v2 and v3
            baseUrl = SANDBOX_V2_BASE;
        }

        return baseUrl + apiVersion.getPathPrefix();
    }

    /**
     * Builds the WebSocket base URL based on environment.
     *
     * @param environment the environment
     * @return the WebSocket base URL
     */
    private String buildWebSocketBaseUrl(UpstoxEnvironment environment) {
        return environment == UpstoxEnvironment.PRODUCTION
                ? PRODUCTION_WEBSOCKET_BASE
                : SANDBOX_WEBSOCKET_BASE;
    }

    /**
     * Gets the REST API base URL.
     * <p>
     * Examples:
     * <ul>
     * <li>Production v2: https://api.upstox.com/v2</li>
     * <li>Production v3 HFT: https://api-hft.upstox.com/v3</li>
     * <li>Sandbox v2: https://api-sandbox.upstox.com/v2</li>
     * </ul>
     * </p>
     *
     * @return the REST API base URL
     */
    public String getRestApiBaseUrl() {
        return restApiBaseUrl;
    }

    /**
     * Gets the WebSocket base URL without path prefix.
     * <p>
     * Examples:
     * <ul>
     * <li>Production: wss://api.upstox.com</li>
     * <li>Sandbox: wss://api-sandbox.upstox.com</li>
     * </ul>
     * </p>
     *
     * @return the WebSocket base URL
     */
    public String getWebSocketBaseUrl() {
        return webSocketBaseUrl;
    }

    /**
     * Gets the complete WebSocket URL with version path.
     * <p>
     * Example: wss://api.upstox.com/v2
     * </p>
     *
     * @return the complete WebSocket URL
     */
    public String getWebSocketUrlWithVersion() {
        return webSocketBaseUrl + apiVersion.getPathPrefix();
    }

    /**
     * Gets the environment for this configuration.
     *
     * @return the environment
     */
    public UpstoxEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Gets the API version for this configuration.
     *
     * @return the API version
     */
    public UpstoxApiVersion getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns true if this configuration is for production environment.
     *
     * @return true if production, false if sandbox
     */
    public boolean isProduction() {
        return environment.isProduction();
    }

    /**
     * Returns true if this configuration is for sandbox environment.
     *
     * @return true if sandbox, false if production
     */
    public boolean isSandbox() {
        return environment.isSandbox();
    }

    @Override
    public String toString() {
        return String.format("UpstoxBaseUrlConfig[environment=%s, apiVersion=%s, restApiBaseUrl=%s]",
                environment, apiVersion, restApiBaseUrl);
    }
}
