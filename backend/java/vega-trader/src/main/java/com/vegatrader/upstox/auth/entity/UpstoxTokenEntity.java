package com.vegatrader.upstox.auth.entity;

import java.time.LocalDateTime;

/**
 * JPA Entity for Upstox token storage.
 * Maps to the upstox_tokens table in vega_trader.db.
 *
 * @since 2.0.0
 */
public class UpstoxTokenEntity {

    private Integer id;
    private String accessToken;
    private String apiName;
    private String clientId;
    private String clientSecret;
    private LocalDateTime createdAt;
    private Long expiresIn;
    private Boolean isPrimary;
    private LocalDateTime lastRefreshed;
    private String redirectUri;
    private String refreshToken;
    private String tokenType;
    private Integer apiIndex;
    private String generatedAt;
    private Integer isActive;
    private String purpose;
    private Long updatedAt;
    private Integer userId;
    private String validityAt;

    public UpstoxTokenEntity() {
    }

    // Getters/Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public LocalDateTime getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(LocalDateTime lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
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

    public Integer getApiIndex() {
        return apiIndex;
    }

    public void setApiIndex(Integer apiIndex) {
        this.apiIndex = apiIndex;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getValidityAt() {
        return validityAt;
    }

    public void setValidityAt(String validityAt) {
        this.validityAt = validityAt;
    }

    /**
     * Checks if token is active.
     *
     * @return true if is_active = 1
     */
    public boolean isActive() {
        return isActive != null && isActive == 1;
    }

    /**
     * Checks if this is the primary token.
     *
     * @return true if is_primary = true
     */
    public boolean isPrimary() {
        return isPrimary != null && isPrimary;
    }

    /**
     * Gets authorization header value.
     *
     * @return "Bearer {access_token}"
     */
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }

    /**
     * Gets the age of the token in minutes since it was last updated or created.
     * 
     * @return age in minutes
     */
    public long getAgeMinutes() {
        if (updatedAt != null && updatedAt > 0) {
            long nowMs = System.currentTimeMillis();
            return (nowMs - updatedAt) / (1000 * 60);
        }
        return 999999; // Assume old if no timestamp
    }

    /**
     * Checks if token is older than specified duration.
     * 
     * @param duration duration to compare against
     * @return true if older
     */
    public boolean isOlderThan(java.time.Duration duration) {
        return getAgeMinutes() > duration.toMinutes();
    }

    @Override
    public String toString() {
        return String.format("UpstoxToken{id=%d, apiName='%s', isPrimary=%b, isActive=%d, age=%d min}",
                id, apiName, isPrimary, isActive, getAgeMinutes());
    }
}
