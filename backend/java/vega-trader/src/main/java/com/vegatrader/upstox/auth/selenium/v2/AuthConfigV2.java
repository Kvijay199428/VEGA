package com.vegatrader.upstox.auth.selenium.v2;

/**
 * Enterprise-grade configuration for V2 authentication.
 * Includes kill switch and resilience settings.
 *
 * @since 2.2.0
 */
public class AuthConfigV2 {

    // Kill switch - disable Selenium entirely
    private boolean seleniumEnabled = true;

    // Cooldown period after failure (minutes)
    private int cooldownMinutes = 15;

    // Max consecutive failures before auto-quarantine
    private int maxConsecutiveFailures = 3;

    // Screenshot only on failure (not success)
    private boolean screenshotOnFailureOnly = true;

    // Headless browser mode
    private boolean headless = false;

    // Browser type (chrome/firefox)
    private String browser = "chrome";

    // Page load timeout (seconds)
    private int timeoutSeconds = 60;

    // Profile verification after token exchange
    private boolean profileVerification = true;

    // Refresh before expiry (minutes)
    private int refreshBeforeExpiryMinutes = 60;

    // Scheduled refresh time (HH:mm)
    private String scheduledRefreshTime = "02:30";

    public AuthConfigV2() {
    }

    // Getters and Setters

    public boolean isSeleniumEnabled() {
        return seleniumEnabled;
    }

    public void setSeleniumEnabled(boolean seleniumEnabled) {
        this.seleniumEnabled = seleniumEnabled;
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    public void setCooldownMinutes(int cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public int getMaxConsecutiveFailures() {
        return maxConsecutiveFailures;
    }

    public void setMaxConsecutiveFailures(int maxConsecutiveFailures) {
        this.maxConsecutiveFailures = maxConsecutiveFailures;
    }

    public boolean isScreenshotOnFailureOnly() {
        return screenshotOnFailureOnly;
    }

    public void setScreenshotOnFailureOnly(boolean screenshotOnFailureOnly) {
        this.screenshotOnFailureOnly = screenshotOnFailureOnly;
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

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isProfileVerification() {
        return profileVerification;
    }

    public void setProfileVerification(boolean profileVerification) {
        this.profileVerification = profileVerification;
    }

    public int getRefreshBeforeExpiryMinutes() {
        return refreshBeforeExpiryMinutes;
    }

    public void setRefreshBeforeExpiryMinutes(int refreshBeforeExpiryMinutes) {
        this.refreshBeforeExpiryMinutes = refreshBeforeExpiryMinutes;
    }

    public String getScheduledRefreshTime() {
        return scheduledRefreshTime;
    }

    public void setScheduledRefreshTime(String scheduledRefreshTime) {
        this.scheduledRefreshTime = scheduledRefreshTime;
    }

    /**
     * Builder for fluent configuration.
     */
    public static class Builder {
        private final AuthConfigV2 config = new AuthConfigV2();

        public Builder seleniumEnabled(boolean enabled) {
            config.setSeleniumEnabled(enabled);
            return this;
        }

        public Builder cooldownMinutes(int minutes) {
            config.setCooldownMinutes(minutes);
            return this;
        }

        public Builder maxConsecutiveFailures(int max) {
            config.setMaxConsecutiveFailures(max);
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

        public Builder timeoutSeconds(int timeout) {
            config.setTimeoutSeconds(timeout);
            return this;
        }

        public Builder profileVerification(boolean verify) {
            config.setProfileVerification(verify);
            return this;
        }

        public AuthConfigV2 build() {
            return config;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AuthConfigV2{" +
                "seleniumEnabled=" + seleniumEnabled +
                ", cooldownMinutes=" + cooldownMinutes +
                ", maxConsecutiveFailures=" + maxConsecutiveFailures +
                ", headless=" + headless +
                ", browser='" + browser + '\'' +
                '}';
    }
}
