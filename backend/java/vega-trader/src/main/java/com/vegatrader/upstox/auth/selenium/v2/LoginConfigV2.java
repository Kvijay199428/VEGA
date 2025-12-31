package com.vegatrader.upstox.auth.selenium.v2;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Configuration DTO for V2 login automation.
 * Contains all parameters needed for OAuth login flow.
 *
 * @since 2.1.0
 */
public class LoginConfigV2 {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String apiName; // API category: PRIMARY, WEBSOCKET1, etc.
    private String clientId; // Upstox API client ID
    private String clientSecret; // Upstox API client secret
    private String redirectUri; // OAuth callback redirect URI
    private LoginCredentialsV2 credentials; // Login credentials
    private boolean headless; // Run browser in headless mode
    private String browser; // Browser type: chrome, firefox
    private boolean isPrimary; // Is this the primary API token
    private int timeoutSeconds; // Page load timeout in seconds
    private String state; // OAuth state parameter (CSRF protection)

    public LoginConfigV2() {
        // Defaults
        this.browser = "chrome";
        this.headless = false;
        this.timeoutSeconds = 60;
        // Generate random state for CSRF protection
        this.state = generateSecureState();
    }

    /**
     * Generate a secure random state parameter for CSRF protection.
     * Per migration guide: state MUST be generated + validated.
     */
    private static String generateSecureState() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Build authorization URL with client_id, redirect_uri, response_type, and
     * state.
     * Per migration guide: use proper OAuth /v2/login/authorization/dialog
     * endpoint.
     * 
     * @return complete authorization URL
     */
    public String buildAuthorizationUrl() {
        // Migration guide specifies the correct OAuth endpoint
        return String.format(
                "https://api.upstox.com/v2/login/authorization/dialog?client_id=%s&redirect_uri=%s&response_type=code&state=%s",
                urlEncode(clientId),
                urlEncode(redirectUri),
                urlEncode(state));
    }

    /**
     * Alternative: Build legacy login URL (current behavior) for backwards
     * compatibility.
     */
    public String buildLegacyLoginUrl() {
        return String.format(
                "https://login.upstox.com/?client_id=%s&platform_id=UPT&redirect_uri=%s",
                clientId,
                urlEncode(redirectUri));
    }

    /**
     * URL encode a string.
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Validate configuration is complete.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (apiName == null || apiName.isEmpty()) {
            throw new IllegalArgumentException("apiName is required");
        }
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("clientId is required");
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalArgumentException("clientSecret is required");
        }
        if (redirectUri == null || redirectUri.isEmpty()) {
            throw new IllegalArgumentException("redirectUri is required");
        }
        if (credentials == null) {
            throw new IllegalArgumentException("credentials are required");
        }
        credentials.validate();
    }

    // Getters and Setters

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

    public LoginCredentialsV2 getCredentials() {
        return credentials;
    }

    public void setCredentials(LoginCredentialsV2 credentials) {
        this.credentials = credentials;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return "LoginConfigV2{" +
                "apiName='" + apiName + '\'' +
                ", clientId='" + clientId + '\'' +
                ", browser='" + browser + '\'' +
                ", headless=" + headless +
                ", isPrimary=" + isPrimary +
                '}';
    }

    /**
     * Builder for LoginConfigV2.
     */
    public static class Builder {
        private final LoginConfigV2 config = new LoginConfigV2();

        public Builder apiName(String apiName) {
            config.setApiName(apiName);
            return this;
        }

        public Builder clientId(String clientId) {
            config.setClientId(clientId);
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            config.setClientSecret(clientSecret);
            return this;
        }

        public Builder redirectUri(String redirectUri) {
            config.setRedirectUri(redirectUri);
            return this;
        }

        public Builder credentials(LoginCredentialsV2 credentials) {
            config.setCredentials(credentials);
            return this;
        }

        public Builder credentials(String mobileNumber, String pin, String totpSecret) {
            config.setCredentials(new LoginCredentialsV2(mobileNumber, pin, totpSecret));
            return this;
        }

        public Builder headless(boolean headless) {
            config.setHeadless(headless);
            return this;
        }

        public Builder browser(String browser) {
            config.setBrowser(browser);
            return this;
        }

        public Builder primary(boolean isPrimary) {
            config.setPrimary(isPrimary);
            return this;
        }

        public Builder timeoutSeconds(int timeout) {
            config.setTimeoutSeconds(timeout);
            return this;
        }

        public LoginConfigV2 build() {
            config.validate();
            return config;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
