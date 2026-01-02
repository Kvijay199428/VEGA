package com.vegatrader.upstox.auth.dto;

import java.util.List;

public class AuthStatus {
    private String state;
    private boolean authenticated;
    private boolean primaryReady;
    private boolean fullyReady;
    private int generatedTokens;
    private int requiredTokens;
    private List<String> validTokens;
    private boolean inProgress;
    private boolean cooldownActive;
    private int remainingSeconds;
    private long expiresAt;

    public AuthStatus() {
    }

    public AuthStatus(String state, boolean authenticated, boolean primaryReady, boolean fullyReady,
            int generatedTokens,
            int requiredTokens, List<String> validTokens, boolean inProgress, boolean cooldownActive,
            int remainingSeconds, long expiresAt) {
        this.state = state;
        this.authenticated = authenticated;
        this.primaryReady = primaryReady;
        this.fullyReady = fullyReady;
        this.generatedTokens = generatedTokens;
        this.requiredTokens = requiredTokens;
        this.validTokens = validTokens;
        this.inProgress = inProgress;
        this.cooldownActive = cooldownActive;
        this.remainingSeconds = remainingSeconds;
        this.expiresAt = expiresAt;
    }

    public static AuthStatusBuilder builder() {
        return new AuthStatusBuilder();
    }

    // Getters and Setters

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isPrimaryReady() {
        return primaryReady;
    }

    public void setPrimaryReady(boolean primaryReady) {
        this.primaryReady = primaryReady;
    }

    public boolean isFullyReady() {
        return fullyReady;
    }

    public void setFullyReady(boolean fullyReady) {
        this.fullyReady = fullyReady;
    }

    public int getGeneratedTokens() {
        return generatedTokens;
    }

    public void setGeneratedTokens(int generatedTokens) {
        this.generatedTokens = generatedTokens;
    }

    public int getRequiredTokens() {
        return requiredTokens;
    }

    public void setRequiredTokens(int requiredTokens) {
        this.requiredTokens = requiredTokens;
    }

    public List<String> getValidTokens() {
        return validTokens;
    }

    public void setValidTokens(List<String> validTokens) {
        this.validTokens = validTokens;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isCooldownActive() {
        return cooldownActive;
    }

    public void setCooldownActive(boolean cooldownActive) {
        this.cooldownActive = cooldownActive;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    // Builder Class
    public static class AuthStatusBuilder {
        private String state;
        private boolean authenticated;
        private boolean primaryReady;
        private boolean fullyReady;
        private int generatedTokens;
        private int requiredTokens;
        private List<String> validTokens;
        private boolean inProgress;
        private boolean cooldownActive;
        private int remainingSeconds;
        private long expiresAt;

        AuthStatusBuilder() {
        }

        public AuthStatusBuilder state(String state) {
            this.state = state;
            return this;
        }

        public AuthStatusBuilder authenticated(boolean authenticated) {
            this.authenticated = authenticated;
            return this;
        }

        public AuthStatusBuilder primaryReady(boolean primaryReady) {
            this.primaryReady = primaryReady;
            return this;
        }

        public AuthStatusBuilder fullyReady(boolean fullyReady) {
            this.fullyReady = fullyReady;
            return this;
        }

        public AuthStatusBuilder generatedTokens(int generatedTokens) {
            this.generatedTokens = generatedTokens;
            return this;
        }

        public AuthStatusBuilder requiredTokens(int requiredTokens) {
            this.requiredTokens = requiredTokens;
            return this;
        }

        public AuthStatusBuilder validTokens(List<String> validTokens) {
            this.validTokens = validTokens;
            return this;
        }

        public AuthStatusBuilder inProgress(boolean inProgress) {
            this.inProgress = inProgress;
            return this;
        }

        public AuthStatusBuilder cooldownActive(boolean cooldownActive) {
            this.cooldownActive = cooldownActive;
            return this;
        }

        public AuthStatusBuilder remainingSeconds(int remainingSeconds) {
            this.remainingSeconds = remainingSeconds;
            return this;
        }

        public AuthStatusBuilder expiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public AuthStatus build() {
            return new AuthStatus(state, authenticated, primaryReady, fullyReady, generatedTokens, requiredTokens,
                    validTokens, inProgress, cooldownActive, remainingSeconds, expiresAt);
        }
    }
}
