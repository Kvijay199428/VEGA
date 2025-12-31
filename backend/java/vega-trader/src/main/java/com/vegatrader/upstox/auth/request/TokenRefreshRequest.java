package com.vegatrader.upstox.auth.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for refreshing expired access tokens.
 *
 * @since 2.0.0
 */
public class TokenRefreshRequest {

    @SerializedName("grant_type")
    private String grantType;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("client_secret")
    private String clientSecret;

    public TokenRefreshRequest() {
        this.grantType = "refresh_token";
    }

    public static TokenRefreshRequestBuilder builder() {
        return new TokenRefreshRequestBuilder();
    }

    // Getters/Setters
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Validates the refresh token request.
     */
    public void validate() {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalArgumentException("Client secret is required");
        }
        if (!"refresh_token".equals(grantType)) {
            throw new IllegalArgumentException("Grant type must be 'refresh_token'");
        }
    }

    /**
     * Converts to form-urlencoded body for POST request.
     *
     * @return form-encoded string
     */
    public String toFormBody() {
        validate();

        StringBuilder body = new StringBuilder();
        body.append("grant_type=").append(grantType);
        body.append("&refresh_token=").append(urlEncode(refreshToken));
        body.append("&client_id=").append(urlEncode(clientId));
        body.append("&client_secret=").append(urlEncode(clientSecret));

        return body.toString();
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static class TokenRefreshRequestBuilder {
        private String refreshToken, clientId, clientSecret;

        public TokenRefreshRequestBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenRefreshRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenRefreshRequestBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenRefreshRequest build() {
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.grantType = "refresh_token";
            request.refreshToken = this.refreshToken;
            request.clientId = this.clientId;
            request.clientSecret = this.clientSecret;
            return request;
        }
    }

    @Override
    public String toString() {
        return "TokenRefreshRequest{refreshToken='***', clientId='***'}";
    }
}
