package com.vegatrader.upstox.auth.db;

/**
 * Supported API Names (fixed enumeration).
 * Maps to the 6 API keys configured in .env.
 *
 * @since 2.2.0
 */
public enum ApiName {
    PRIMARY,
    WEBSOCKET1,
    WEBSOCKET2,
    WEBSOCKET3,
    OPTIONCHAIN1,
    OPTIONCHAIN2;

    /**
     * Get API index (0-5) for database storage.
     */
    public int getIndex() {
        return ordinal();
    }

    /**
     * Check if this is the primary API.
     */
    public boolean isPrimary() {
        return this == PRIMARY;
    }

    /**
     * Parse ApiName from string (case-insensitive).
     */
    public static ApiName fromString(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("API name cannot be null or empty");
        }
        try {
            return valueOf(name.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown API name: " + name);
        }
    }

    /**
     * Get total number of supported APIs.
     */
    public static int count() {
        return values().length;
    }
}
