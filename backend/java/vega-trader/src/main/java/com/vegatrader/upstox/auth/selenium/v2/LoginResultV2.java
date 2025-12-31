package com.vegatrader.upstox.auth.selenium.v2;

import com.vegatrader.upstox.auth.selenium.v2.ProfileVerifierV2.ProfileDataV2;
import com.vegatrader.upstox.auth.selenium.v2.TokenExchangeClientV2.TokenResponseV2;

/**
 * Result DTO from V2 login automation.
 * Contains success/failure status, token details, and profile information.
 *
 * @since 2.1.0
 */
public class LoginResultV2 {

    private boolean success;
    private String apiName;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String validityAt;
    private ProfileDataV2 profile;
    private String errorMessage;
    private long durationMs;

    public LoginResultV2() {
    }

    /**
     * Create a success result.
     * 
     * @param apiName       API name
     * @param tokenResponse token exchange response
     * @param profile       verified profile data
     * @return success result
     */
    public static LoginResultV2 success(String apiName, TokenResponseV2 tokenResponse,
            ProfileDataV2 profile) {
        LoginResultV2 result = new LoginResultV2();
        result.setSuccess(true);
        result.setApiName(apiName);
        result.setAccessToken(tokenResponse.getAccessToken());
        result.setRefreshToken(tokenResponse.getRefreshToken());
        result.setTokenType(tokenResponse.getTokenType());
        result.setExpiresIn(tokenResponse.getExpiresIn());
        result.setValidityAt(TokenExpiryCalculatorV2.calculateValidityAtString());
        result.setProfile(profile);
        return result;
    }

    /**
     * Create a failure result.
     * 
     * @param apiName      API name
     * @param errorMessage error description
     * @return failure result
     */
    public static LoginResultV2 failure(String apiName, String errorMessage) {
        LoginResultV2 result = new LoginResultV2();
        result.setSuccess(false);
        result.setApiName(apiName);
        result.setErrorMessage(errorMessage);
        return result;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getValidityAt() {
        return validityAt;
    }

    public void setValidityAt(String validityAt) {
        this.validityAt = validityAt;
    }

    public ProfileDataV2 getProfile() {
        return profile;
    }

    public void setProfile(ProfileDataV2 profile) {
        this.profile = profile;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        if (success) {
            return "LoginResultV2{" +
                    "success=true" +
                    ", apiName='" + apiName + '\'' +
                    ", tokenType='" + tokenType + '\'' +
                    ", validityAt='" + validityAt + '\'' +
                    ", userId='" + (profile != null ? profile.getUserId() : "null") + '\'' +
                    ", durationMs=" + durationMs +
                    '}';
        } else {
            return "LoginResultV2{" +
                    "success=false" +
                    ", apiName='" + apiName + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}
