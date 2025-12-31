package com.vegatrader.upstox.auth.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for token exchange and refresh operations.
 * Contains access token, refresh token, and expiry information.
 *
 * @since 2.0.0
 */
public class TokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    // Additional metadata (not from API response)
    private String apiName;
    private Long validityTimestamp;
    private boolean active;

    public TokenResponse() {
    }

    // Getters/Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public Long getValidityTimestamp() {
        return validityTimestamp;
    }

    public void setValidityTimestamp(Long validityTimestamp) {
        this.validityTimestamp = validityTimestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the Authorization header value.
     *
     * @return "Bearer {access_token}"
     */
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }

    /**
     * Checks if token is expired based on validityTimestamp.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        if (validityTimestamp == null) {
            return false; // Cannot determine expiry
        }
        return System.currentTimeMillis() > validityTimestamp;
    }

    /**
     * Gets remaining validity in seconds.
     *
     * @return seconds until expiry, or -1 if already expired
     */
    public long getRemainingValiditySeconds() {
        if (validityTimestamp == null) {
            return expiresIn != null ? expiresIn : 0;
        }

        long remaining = (validityTimestamp - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    @Override
    public String toString() {
        return String.format("TokenResponse{type='%s', expiresIn=%d, apiName='%s', active=%b}",
                tokenType, expiresIn, apiName, active);
    }
}
