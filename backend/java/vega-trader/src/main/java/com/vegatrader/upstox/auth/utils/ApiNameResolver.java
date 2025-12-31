package com.vegatrader.upstox.auth.utils;

import com.vegatrader.upstox.auth.config.AuthConstants;

/**
 * Utility for resolving endpoint to API name category.
 * Determines which token pool to use based on endpoint.
 *
 * @since 2.0.0
 */
public final class ApiNameResolver {

    private ApiNameResolver() {
        // Utility class
    }

    /**
     * Resolve endpoint to API name category.
     *
     * @param endpoint the API endpoint
     * @return API name (PRIMARY, WEBSOCKET1-3, OPTIONCHAIN1-2)
     */
    public static String resolveApiName(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return AuthConstants.TOKEN_CATEGORY_PRIMARY;
        }

        // Check if WebSocket endpoint
        if (isWebSocketEndpoint(endpoint)) {
            return AuthConstants.TOKEN_CATEGORY_WEBSOCKET_PREFIX + "1"; // Will be load-balanced
        }

        // Check if Option Chain endpoint
        if (isOptionChainEndpoint(endpoint)) {
            return AuthConstants.TOKEN_CATEGORY_OPTIONCHAIN_PREFIX + "1"; // Will be load-balanced
        }

        // Default to PRIMARY for all other endpoints
        return AuthConstants.TOKEN_CATEGORY_PRIMARY;
    }

    /**
     * Check if endpoint is WebSocket type.
     *
     * @param endpoint the API endpoint
     * @return true if WebSocket endpoint
     */
    public static boolean isWebSocketEndpoint(String endpoint) {
        for (String wsEndpoint : AuthConstants.WEBSOCKET_ENDPOINTS) {
            if (endpoint.contains(wsEndpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if endpoint is Option Chain type.
     *
     * @param endpoint the API endpoint
     * @return true if Option Chain endpoint
     */
    public static boolean isOptionChainEndpoint(String endpoint) {
        for (String ocEndpoint : AuthConstants.OPTIONCHAIN_ENDPOINTS) {
            if (endpoint.contains(ocEndpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get category from API name.
     *
     * @param apiName the API name
     * @return category (PRIMARY, WEBSOCKET, OPTIONCHAIN)
     */
    public static String getCategory(String apiName) {
        if (apiName == null || apiName.isEmpty()) {
            return AuthConstants.TOKEN_CATEGORY_PRIMARY;
        }

        if (apiName.startsWith(AuthConstants.TOKEN_CATEGORY_WEBSOCKET_PREFIX)) {
            return AuthConstants.TOKEN_CATEGORY_WEBSOCKET_PREFIX;
        }

        if (apiName.startsWith(AuthConstants.TOKEN_CATEGORY_OPTIONCHAIN_PREFIX)) {
            return AuthConstants.TOKEN_CATEGORY_OPTIONCHAIN_PREFIX;
        }

        return AuthConstants.TOKEN_CATEGORY_PRIMARY;
    }
}
