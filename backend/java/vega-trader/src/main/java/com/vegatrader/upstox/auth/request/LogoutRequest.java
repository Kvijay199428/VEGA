package com.vegatrader.upstox.auth.request;

/**
 * Request DTO for logout/token revocation.
 *
 * @since 2.0.0
 */
public class LogoutRequest {

    private String accessToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Validates the logout request.
     */
    public void validate() {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token is required for logout");
        }
    }

    /**
     * Gets the Authorization header value.
     *
     * @return "Bearer {access_token}"
     */
    public String getAuthorizationHeader() {
        return "Bearer " + accessToken;
    }

    @Override
    public String toString() {
        return "LogoutRequest{token='***'}";
    }
}
