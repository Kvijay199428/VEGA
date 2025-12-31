package com.vegatrader.upstox.auth.dto;

import java.time.Instant;

/**
 * Login success response DTO for frontend.
 *
 * @since 2.3.0
 */
public class LoginSuccessResponse {

    private boolean tokenInserted;
    private String apiName;
    private Instant tokenExpiry;
    private String generatedAt;
    private ProfileView profile;
    private String message;

    public LoginSuccessResponse() {
    }

    public static LoginSuccessResponse success(String apiName, Instant tokenExpiry,
            String generatedAt, ProfileView profile) {
        LoginSuccessResponse response = new LoginSuccessResponse();
        response.tokenInserted = true;
        response.apiName = apiName;
        response.tokenExpiry = tokenExpiry;
        response.generatedAt = generatedAt;
        response.profile = profile;
        response.message = "Access token generated and stored successfully";
        return response;
    }

    public static LoginSuccessResponse notFound(String apiName) {
        LoginSuccessResponse response = new LoginSuccessResponse();
        response.tokenInserted = false;
        response.apiName = apiName;
        response.message = "Token not found for: " + apiName;
        return response;
    }

    // Getters and Setters
    public boolean isTokenInserted() {
        return tokenInserted;
    }

    public void setTokenInserted(boolean tokenInserted) {
        this.tokenInserted = tokenInserted;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public Instant getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Instant tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public ProfileView getProfile() {
        return profile;
    }

    public void setProfile(ProfileView profile) {
        this.profile = profile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
