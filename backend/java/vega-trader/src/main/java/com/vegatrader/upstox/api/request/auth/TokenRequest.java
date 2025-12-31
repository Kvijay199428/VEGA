package com.vegatrader.upstox.api.request.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for obtaining OAuth access token.
 * <p>
 * Used to exchange authorization code for access token after OAuth redirect.
 * </p>
 *
 * @since 2.0.0
 */
public class TokenRequest {

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("client_secret")
    private String clientSecret;

    @SerializedName("code")
    private String code;

    @SerializedName("redirect_uri")
    private String redirectUri;

    @SerializedName("grant_type")
    private String grantType;

    public TokenRequest() {
        this.grantType = "authorization_code";
    }

    public static TokenRequestBuilder builder() {
        return new TokenRequestBuilder();
    }

    // Getters/Setters
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public static class TokenRequestBuilder {
        private String clientId, clientSecret, code, redirectUri;

        public TokenRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public TokenRequestBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public TokenRequestBuilder code(String code) {
            this.code = code;
            return this;
        }

        public TokenRequestBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public TokenRequest build() {
            TokenRequest request = new TokenRequest();
            request.clientId = this.clientId;
            request.clientSecret = this.clientSecret;
            request.code = this.code;
            request.redirectUri = this.redirectUri;
            return request;
        }
    }
}
