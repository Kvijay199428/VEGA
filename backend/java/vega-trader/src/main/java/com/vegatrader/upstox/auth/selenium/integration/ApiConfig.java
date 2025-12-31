package com.vegatrader.upstox.auth.selenium.integration;

/**
 * API configuration for multi-login support.
 *
 * @since 2.0.0
 */
public class ApiConfig {

    private String apiName;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private boolean isPrimary;

    public ApiConfig() {
    }

    public ApiConfig(String apiName, String clientId, String clientSecret,
            String redirectUri, boolean isPrimary) {
        this.apiName = apiName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.isPrimary = isPrimary;
    }

    // Getters/Setters
    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
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

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Override
    public String toString() {
        return String.format("ApiConfig{apiName='%s', isPrimary=%b}", apiName, isPrimary);
    }
}
