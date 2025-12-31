package com.vegatrader.upstox.api.generator;

import com.vegatrader.upstox.api.config.*;
import com.vegatrader.upstox.api.endpoints.UpstoxEndpoint;

import java.util.Map;

/**
 * Main URL generator for Upstox API endpoints.
 * <p>
 * This is the primary class for generating complete URLs for all Upstox API
 * endpoints.
 * It combines base URL configuration, endpoint definitions, and parameter
 * handling
 * to produce ready-to-use API URLs.
 * </p>
 * <p>
 * <b>Usage Examples:</b>
 * 
 * <pre>{@code
 * // Create generator for production
 * UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();
 * 
 * // Generate simple URL
 * String profileUrl = generator.generateUrl(UserProfileEndpoints.USER_PROFILE);
 * 
 * // Generate URL with query parameters
 * Map<String, String> params = Map.of("instrument_key", "NSE_EQ|INE848E01016");
 * String quoteUrl = generator.generateUrl(MarketDataEndpoints.FULL_QUOTE, params);
 * 
 * // Generate URL with path parameters
 * Map<String, String> pathParams = Map.of("order_id", "240127000123456");
 * String orderUrl = generator.generateUrl(OrderEndpoints.GET_ORDER_DETAILS, pathParams, null);
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxUrlBuilder
 * @see UpstoxBaseUrlConfig
 */
public final class UpstoxEndpointGenerator {

    private final UpstoxBaseUrlConfig config;

    /**
     * Creates a new endpoint generator with the given configuration.
     *
     * @param config the base URL configuration
     */
    private UpstoxEndpointGenerator(UpstoxBaseUrlConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        this.config = config;
    }

    /**
     * Creates a generator for production environment with standard v2 API.
     *
     * @return a new UpstoxEndpointGenerator for production
     */
    public static UpstoxEndpointGenerator forProduction() {
        return new UpstoxEndpointGenerator(UpstoxBaseUrlFactory.createProduction());
    }

    /**
     * Creates a generator for sandbox environment with standard v2 API.
     *
     * @return a new UpstoxEndpointGenerator for sandbox
     */
    public static UpstoxEndpointGenerator forSandbox() {
        return new UpstoxEndpointGenerator(UpstoxBaseUrlFactory.createSandbox());
    }

    /**
     * Creates a generator for production environment with HFT v3 API.
     *
     * @return a new UpstoxEndpointGenerator for production HFT
     */
    public static UpstoxEndpointGenerator forProductionHft() {
        return new UpstoxEndpointGenerator(UpstoxBaseUrlFactory.createProductionHft());
    }

    /**
     * Creates a generator for sandbox environment with HFT v3 API.
     *
     * @return a new UpstoxEndpointGenerator for sandbox HFT
     */
    public static UpstoxEndpointGenerator forSandboxHft() {
        return new UpstoxEndpointGenerator(UpstoxBaseUrlFactory.createSandboxHft());
    }

    /**
     * Creates a generator for a custom environment and version.
     *
     * @param environment the environment
     * @param apiVersion  the API version
     * @return a new UpstoxEndpointGenerator
     */
    public static UpstoxEndpointGenerator forEnvironment(UpstoxEnvironment environment, UpstoxApiVersion apiVersion) {
        UpstoxBaseUrlConfig config = UpstoxBaseUrlFactory.create(environment, apiVersion);
        return new UpstoxEndpointGenerator(config);
    }

    /**
     * Generates a URL for the given endpoint without parameters.
     *
     * @param endpoint the endpoint
     * @return the complete URL
     * @throws IllegalArgumentException if endpoint is null
     */
    public String generateUrl(UpstoxEndpoint endpoint) {
        return generateUrl(endpoint, null, null);
    }

    /**
     * Generates a URL with query parameters.
     *
     * @param endpoint    the endpoint
     * @param queryParams query parameters (can be null)
     * @return the complete URL
     */
    public String generateUrl(UpstoxEndpoint endpoint, Map<String, String> queryParams) {
        return generateUrl(endpoint, null, queryParams);
    }

    /**
     * Generates a URL with path and query parameters.
     *
     * @param endpoint    the endpoint
     * @param pathParams  path parameters to replace in the endpoint path (can be
     *                    null)
     * @param queryParams query parameters to append (can be null)
     * @return the complete URL
     * @throws IllegalArgumentException if endpoint is null
     */
    public String generateUrl(UpstoxEndpoint endpoint, Map<String, String> pathParams,
            Map<String, String> queryParams) {
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoint cannot be null");
        }

        UpstoxUrlBuilder builder = UpstoxUrlBuilder.create()
                .baseUrl(config.getRestApiBaseUrl())
                .endpoint(endpoint);

        // Add path parameters if provided
        if (pathParams != null && !pathParams.isEmpty()) {
            builder.pathParams(pathParams);
        }

        // Add query parameters if provided
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.queryParams(queryParams);
        }

        return builder.build();
    }

    /**
     * Generates a WebSocket URL for market data stream.
     *
     * @return the WebSocket URL
     */
    public String generateMarketStreamUrl() {
        return UpstoxWebSocketConfig.getMarketStreamUrl(config.getEnvironment());
    }

    /**
     * Generates a WebSocket URL for portfolio stream.
     *
     * @return the WebSocket URL
     */
    public String generatePortfolioStreamUrl() {
        return UpstoxWebSocketConfig.getPortfolioStreamUrl(config.getEnvironment());
    }

    /**
     * Generates an option chain URL with instrument key and expiry date.
     *
     * @param instrumentKey the underlying instrument key
     * @param expiryDate    the expiry date (YYYY-MM-DD format)
     * @return the option chain URL
     * @throws IllegalArgumentException if parameters are null/empty
     */
    public String generateOptionChainUrl(String instrumentKey, String expiryDate) {
        return UpstoxOptionChainConfig.buildOptionChainUrl(config.getEnvironment(), instrumentKey, expiryDate);
    }

    /**
     * Generates an option chain URL with only instrument key.
     *
     * @param instrumentKey the underlying instrument key
     * @return the option chain URL
     * @throws IllegalArgumentException if instrumentKey is null/empty
     */
    public String generateOptionChainUrl(String instrumentKey) {
        return UpstoxOptionChainConfig.buildOptionChainUrl(config.getEnvironment(), instrumentKey);
    }

    /**
     * Gets the base URL configuration.
     *
     * @return the configuration
     */
    public UpstoxBaseUrlConfig getConfig() {
        return config;
    }

    /**
     * Gets the environment for this generator.
     *
     * @return the environment
     */
    public UpstoxEnvironment getEnvironment() {
        return config.getEnvironment();
    }

    /**
     * Gets the API version for this generator.
     *
     * @return the API version
     */
    public UpstoxApiVersion getApiVersion() {
        return config.getApiVersion();
    }

    /**
     * Returns true if this generator is configured for production.
     *
     * @return true if production
     */
    public boolean isProduction() {
        return config.isProduction();
    }

    /**
     * Returns true if this generator is configured for sandbox.
     *
     * @return true if sandbox
     */
    public boolean isSandbox() {
        return config.isSandbox();
    }
}
