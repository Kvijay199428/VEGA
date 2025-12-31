package com.vegatrader.upstox.api.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for creating {@link UpstoxBaseUrlConfig} instances.
 * <p>
 * This factory uses a thread-safe singleton pattern to cache configurations
 * and ensure that only one instance exists for each environment-version
 * combination.
 * </p>
 * <p>
 * <b>Usage Examples:</b>
 * 
 * <pre>{@code
 * // Get production v2 configuration
 * UpstoxBaseUrlConfig prodConfig = UpstoxBaseUrlFactory.createProduction();
 * 
 * // Get sandbox v2 configuration
 * UpstoxBaseUrlConfig sandboxConfig = UpstoxBaseUrlFactory.createSandbox();
 * 
 * // Get production HFT v3 configuration
 * UpstoxBaseUrlConfig hftConfig = UpstoxBaseUrlFactory.createProductionHft();
 * 
 * // Get custom configuration
 * UpstoxBaseUrlConfig customConfig = UpstoxBaseUrlFactory.create(
 *         UpstoxEnvironment.SANDBOX,
 *         UpstoxApiVersion.V3_HFT);
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxBaseUrlConfig
 * @see UpstoxEnvironment
 * @see UpstoxApiVersion
 */
public final class UpstoxBaseUrlFactory {

    // Cache for configuration instances (thread-safe)
    private static final Map<String, UpstoxBaseUrlConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    private UpstoxBaseUrlFactory() {
        throw new AssertionError("UpstoxBaseUrlFactory cannot be instantiated");
    }

    /**
     * Creates or retrieves a cached configuration for the specified environment and
     * API version.
     *
     * @param environment the environment (PRODUCTION or SANDBOX)
     * @param apiVersion  the API version (V2_STANDARD or V3_HFT)
     * @return the configuration instance
     * @throws IllegalArgumentException if environment or apiVersion is null
     */
    public static UpstoxBaseUrlConfig create(UpstoxEnvironment environment, UpstoxApiVersion apiVersion) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment cannot be null");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("API version cannot be null");
        }

        String cacheKey = environment.name() + "_" + apiVersion.name();
        return CONFIG_CACHE.computeIfAbsent(cacheKey, k -> new UpstoxBaseUrlConfig(environment, apiVersion));
    }

    /**
     * Creates or retrieves the default production v2 configuration.
     * <p>
     * This is the most commonly used configuration for production trading.
     * </p>
     *
     * @return the production v2 configuration
     */
    public static UpstoxBaseUrlConfig createProduction() {
        return create(UpstoxEnvironment.PRODUCTION, UpstoxApiVersion.V2_STANDARD);
    }

    /**
     * Creates or retrieves the production HFT v3 configuration.
     * <p>
     * Use this for high-frequency trading operations that require low latency.
     * </p>
     *
     * @return the production HFT v3 configuration
     */
    public static UpstoxBaseUrlConfig createProductionHft() {
        return create(UpstoxEnvironment.PRODUCTION, UpstoxApiVersion.V3_HFT);
    }

    /**
     * Creates or retrieves the sandbox v2 configuration.
     * <p>
     * Use this for testing and development without affecting real trading.
     * </p>
     *
     * @return the sandbox v2 configuration
     */
    public static UpstoxBaseUrlConfig createSandbox() {
        return create(UpstoxEnvironment.SANDBOX, UpstoxApiVersion.V2_STANDARD);
    }

    /**
     * Creates or retrieves the sandbox HFT v3 configuration.
     * <p>
     * Use this for testing high-frequency trading logic in sandbox environment.
     * </p>
     *
     * @return the sandbox HFT v3 configuration
     */
    public static UpstoxBaseUrlConfig createSandboxHft() {
        return create(UpstoxEnvironment.SANDBOX, UpstoxApiVersion.V3_HFT);
    }

    /**
     * Creates or retrieves the default configuration (production v2).
     * <p>
     * This is an alias for {@link #createProduction()}.
     * </p>
     *
     * @return the default production v2 configuration
     */
    public static UpstoxBaseUrlConfig createDefault() {
        return createProduction();
    }

    /**
     * Clears the configuration cache.
     * <p>
     * This is primarily useful for testing purposes. In production, configurations
     * are typically created once and cached for the lifetime of the application.
     * </p>
     */
    public static void clearCache() {
        CONFIG_CACHE.clear();
    }

    /**
     * Returns the number of cached configurations.
     *
     * @return the cache size
     */
    public static int getCacheSize() {
        return CONFIG_CACHE.size();
    }
}
