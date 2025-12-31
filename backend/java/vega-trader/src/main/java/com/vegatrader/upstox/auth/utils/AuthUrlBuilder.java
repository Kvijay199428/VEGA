package com.vegatrader.upstox.auth.utils;

import com.vegatrader.upstox.auth.config.AuthConstants;
import com.vegatrader.upstox.auth.request.AuthorizationRequest;

/**
 * Utility for building authorization and redirect URLs.
 *
 * @since 2.0.0
 */
public final class AuthUrlBuilder {

    private AuthUrlBuilder() {
        // Utility class
    }

    /**
     * Build complete authorization URL.
     *
     * @param clientId    client ID (API key)
     * @param redirectUri callback URL
     * @param state       CSRF token (optional)
     * @return complete authorization URL
     */
    public static String buildAuthorizationUrl(String clientId, String redirectUri, String state) {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .clientId(clientId)
                .redirectUri(redirectUri)
                .state(state)
                .build();

        String baseUrl = AuthConstants.API_BASE_URL + AuthConstants.AUTHORIZATION_ENDPOINT;
        return request.toAuthorizationUrl(baseUrl);
    }

    /**
     * Build complete authorization URL without state.
     *
     * @param clientId    client ID
     * @param redirectUri redirect URI
     * @return authorization URL
     */
    public static String buildAuthorizationUrl(String clientId, String redirectUri) {
        return buildAuthorizationUrl(clientId, redirectUri, null);
    }

    /**
     * Get token endpoint URL.
     *
     * @return token endpoint full URL
     */
    public static String getTokenEndpointUrl() {
        return AuthConstants.API_BASE_URL + AuthConstants.TOKEN_ENDPOINT;
    }

    /**
     * Get logout endpoint URL.
     *
     * @return logout endpoint full URL
     */
    public static String getLogoutEndpointUrl() {
        return AuthConstants.API_BASE_URL + AuthConstants.LOGOUT_ENDPOINT;
    }
}
