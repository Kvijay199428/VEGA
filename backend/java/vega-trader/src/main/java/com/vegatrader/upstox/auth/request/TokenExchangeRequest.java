package com.vegatrader.upstox.auth.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for exchanging authorization code for access token.
 *
 * @since 2.0.0
 */
public class TokenExchangeRequest {

    @SerializedName("grant_type")
    private String grantType;

    @SerializedName("code")
    private String code;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("client_secret")
    private String clientSecret;

    @SerializedName("redirect_uri")
    private String redirectUri;

    public TokenExchangeRequest() {
        this.grantType = "authorization_code";
    }

    public static TokenExchangeRequestBuilder builder() {
        return new TokenExchangeRequestBuilder();
    }

    // Getters/Setters
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * Validates the token exchange request.
     */
    public void validate() {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Authorization code is required");
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalArgumentException("Client secret is required");
        }
        if (redirectUri == null || redirectUri.isEmpty()) {
            throw new IllegalArgumentException("Redirect URI is required");
        }
        if (!"authorization_code".equals(grantType)) {
            throw new IllegalArgumentException("Grant type must be 'authorization_code'");
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
        body.append("&code=").append(urlEncode(code));
        body.append("&client_id=").append(urlEncode(clientId));
        body.append("&client_secret=").append(urlEncode(clientSecret));
        body.append("&redirect_uri=").append(urlEncode(redirectUri));

        return body.toString();
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static class TokenExchangeRequestBuilder {
        private String code, clientId, clientSecret, redirectUri;

        public TokenExchangeRequestBuilder code(String code) {
            this.code = code;
            return this;
        }

        public TokenExchangeRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenExchangeRequestBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenExchangeRequestBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenExchangeRequest build() {
            TokenExchangeRequest request = new TokenExchangeRequest();
            request.grantType = "authorization_code";
            request.code = this.code;
            request.clientId = this.clientId;
            request.clientSecret = this.clientSecret;
            request.redirectUri = this.redirectUri;
            return request;
        }
    }

    @Override
    public String toString() {
        return String.format("TokenExchangeRequest{code='***', clientId='%s'}",
                clientId != null ? clientId.substring(0, Math.min(8, clientId.length())) + "..." : null);
    }
}
