package com.vegatrader.upstox.auth.response;

import java.util.Set;

public class AuthStatusResponse {
    private String state;
    private boolean authenticated;
    private boolean primaryReady;
    private boolean fullyReady;
    private int generatedTokens;
    private int requiredTokens;
    private Set<String> validTokens;
    private Set<String> missingApis;
    // Phase 9: Token generation progress fields
    private boolean inProgress;
    private String currentApi;
    private boolean cooldownActive;
    private long remainingSeconds;

    public AuthStatusResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isPrimaryReady() {
        return primaryReady;
    }

    public boolean isFullyReady() {
        return fullyReady;
    }

    public int getGeneratedTokens() {
        return generatedTokens;
    }

    public int getRequiredTokens() {
        return requiredTokens;
    }

    public Set<String> getValidTokens() {
        return validTokens;
    }

    public Set<String> getMissingApis() {
        return missingApis;
    }

    public String getState() {
        return state;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public String getCurrentApi() {
        return currentApi;
    }

    public boolean isCooldownActive() {
        return cooldownActive;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public static class Builder {
        private final AuthStatusResponse instance = new AuthStatusResponse();

        public Builder state(String s) {
            instance.state = s;
            return this;
        }

        public Builder authenticated(boolean b) {
            instance.authenticated = b;
            return this;
        }

        public Builder primaryReady(boolean b) {
            instance.primaryReady = b;
            return this;
        }

        public Builder fullyReady(boolean b) {
            instance.fullyReady = b;
            return this;
        }

        public Builder generatedTokens(int i) {
            instance.generatedTokens = i;
            return this;
        }

        public Builder requiredTokens(int i) {
            instance.requiredTokens = i;
            return this;
        }

        public Builder validTokens(Set<String> s) {
            instance.validTokens = s;
            return this;
        }

        public Builder missingApis(Set<String> s) {
            instance.missingApis = s;
            return this;
        }

        public Builder inProgress(boolean b) {
            instance.inProgress = b;
            return this;
        }

        public Builder currentApi(String s) {
            instance.currentApi = s;
            return this;
        }

        public Builder cooldownActive(boolean b) {
            instance.cooldownActive = b;
            return this;
        }

        public Builder remainingSeconds(long l) {
            instance.remainingSeconds = l;
            return this;
        }

        public AuthStatusResponse build() {
            return instance;
        }
    }
}
