package com.vegatrader.upstox.api.config;

/**
 * Represents the Upstox API version.
 * <p>
 * Upstox provides two API versions:
 * <ul>
 * <li><b>V2_STANDARD</b> - Standard REST API v2 for general trading
 * operations</li>
 * <li><b>V3_HFT</b> - High-Frequency Trading API v3 optimized for low-latency
 * operations</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxBaseUrlConfig
 */
public enum UpstoxApiVersion {
    /**
     * Standard API v2 - General purpose REST API
     */
    V2_STANDARD("/v2"),

    /**
     * HFT API v3 - High-Frequency Trading API with low latency
     */
    V3_HFT("/v3");

    private final String pathPrefix;

    UpstoxApiVersion(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    /**
     * Returns the path prefix for this API version (e.g., "/v2" or "/v3").
     *
     * @return the path prefix
     */
    public String getPathPrefix() {
        return pathPrefix;
    }

    /**
     * Returns true if this is the standard v2 API.
     *
     * @return true if V2_STANDARD, false otherwise
     */
    public boolean isStandard() {
        return this == V2_STANDARD;
    }

    /**
     * Returns true if this is the HFT v3 API.
     *
     * @return true if V3_HFT, false otherwise
     */
    public boolean isHft() {
        return this == V3_HFT;
    }
}
