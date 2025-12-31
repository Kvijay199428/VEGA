package com.vegatrader.upstox.auth.db.entity;

/**
 * Token entity mapping to upstox_tokens table.
 * Exact schema match - no deviations.
 *
 * @since 2.2.0
 */
public class UpstoxTokenEntity {

    private Integer id;
    private String accessToken;
    private String apiName;
    private String clientId;
    private String clientSecret;
    private Long createdAt;
    private Long expiresIn;
    private Boolean isPrimary;
    private Long lastRefreshed;
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

    // Getters
    public Integer getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getApiName() {
        return apiName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public Long getLastRefreshed() {
        return lastRefreshed;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Integer getApiIndex() {
        return apiIndex;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public String getPurpose() {
        return purpose;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getValidityAt() {
        return validityAt;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public void setLastRefreshed(Long lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setApiIndex(Integer apiIndex) {
        this.apiIndex = apiIndex;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setValidityAt(String validityAt) {
        this.validityAt = validityAt;
    }

    @Override
    public String toString() {
        return "UpstoxTokenEntity{" +
                "id=" + id +
                ", apiName='" + apiName + '\'' +
                ", isActive=" + isActive +
                ", validityAt='" + validityAt + '\'' +
                '}';
    }

    /**
     * Builder for creating entities.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final UpstoxTokenEntity entity = new UpstoxTokenEntity();

        public Builder accessToken(String v) {
            entity.accessToken = v;
            return this;
        }

        public Builder apiName(String v) {
            entity.apiName = v;
            return this;
        }

        public Builder clientId(String v) {
            entity.clientId = v;
            return this;
        }

        public Builder clientSecret(String v) {
            entity.clientSecret = v;
            return this;
        }

        public Builder createdAt(Long v) {
            entity.createdAt = v;
            return this;
        }

        public Builder expiresIn(Long v) {
            entity.expiresIn = v;
            return this;
        }

        public Builder isPrimary(Boolean v) {
            entity.isPrimary = v;
            return this;
        }

        public Builder redirectUri(String v) {
            entity.redirectUri = v;
            return this;
        }

        public Builder tokenType(String v) {
            entity.tokenType = v;
            return this;
        }

        public Builder apiIndex(Integer v) {
            entity.apiIndex = v;
            return this;
        }

        public Builder generatedAt(String v) {
            entity.generatedAt = v;
            return this;
        }

        public Builder isActive(Integer v) {
            entity.isActive = v;
            return this;
        }

        public Builder purpose(String v) {
            entity.purpose = v;
            return this;
        }

        public Builder updatedAt(Long v) {
            entity.updatedAt = v;
            return this;
        }

        public Builder validityAt(String v) {
            entity.validityAt = v;
            return this;
        }

        public UpstoxTokenEntity build() {
            return entity;
        }
    }
}
