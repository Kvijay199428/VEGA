package com.vegatrader.upstox.auth.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for OAuth authorization dialog.
 * Used to build the authorization URL for user consent.
 *
 * @since 2.0.0
 */
public class AuthorizationRequest {

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("redirect_uri")
    private String redirectUri;

    @SerializedName("response_type")
    private String responseType;

    @SerializedName("state")
    private String state;

    public AuthorizationRequest() {
        this.responseType = "code"; // Fixed value for OAuth
    }

    public static AuthorizationRequestBuilder builder() {
        return new AuthorizationRequestBuilder();
    }

    // Getters/Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Validates the authorization request parameters.
     */
    public void validate() {
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (redirectUri == null || redirectUri.isEmpty()) {
            throw new IllegalArgumentException("Redirect URI is required");
        }
        if (!"code".equals(responseType)) {
            throw new IllegalArgumentException("Response type must be 'code'");
        }
    }

    /**
     * Builds the complete authorization URL.
     *
     * @param baseUrl the base authorization URL
     * @return complete authorization URL with query parameters
     */
    public String toAuthorizationUrl(String baseUrl) {
        validate();

        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?client_id=").append(urlEncode(clientId));
        url.append("&redirect_uri=").append(urlEncode(redirectUri));
        url.append("&response_type=").append(responseType);

        if (state != null && !state.isEmpty()) {
            url.append("&state=").append(urlEncode(state));
        }

        return url.toString();
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static class AuthorizationRequestBuilder {
        private String clientId, redirectUri, state;

        public AuthorizationRequestBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public AuthorizationRequestBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public AuthorizationRequestBuilder state(String state) {
            this.state = state;
            return this;
        }

        public AuthorizationRequest build() {
            AuthorizationRequest request = new AuthorizationRequest();
            request.clientId = this.clientId;
            request.redirectUri = this.redirectUri;
            request.responseType = "code";
            request.state = this.state;
            return request;
        }
    }

    @Override
    public String toString() {
        return String.format("AuthorizationRequest{clientId='%s', redirectUri='%s'}",
                clientId != null ? clientId.substring(0, Math.min(8, clientId.length())) + "..." : null,
                redirectUri);
    }
}
