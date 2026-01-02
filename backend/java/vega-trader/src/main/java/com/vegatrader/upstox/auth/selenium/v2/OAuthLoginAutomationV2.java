package com.vegatrader.upstox.auth.selenium.v2;

import com.vegatrader.upstox.auth.selenium.v2.ProfileVerifierV2.ProfileDataV2;
import com.vegatrader.upstox.auth.selenium.v2.TokenExchangeClientV2.TokenResponseV2;
import com.vegatrader.upstox.auth.selenium.v2.exception.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Main OAuth login automation orchestrator (V2).
 * Coordinates complete login flow: navigate → login → consent → capture code →
 * exchange → verify.
 * 
 * Enterprise features:
 * - CAPTCHA detection with immediate abort
 * - Failure classification and quarantine
 * - Audit logging
 * - Kill switch support
 *
 * @since 2.2.0
 */
public class OAuthLoginAutomationV2 {

    private static final Logger logger = LoggerFactory.getLogger(OAuthLoginAutomationV2.class);

    private WebDriver driver;
    private final TokenExchangeClientV2 tokenExchangeClient;
    private final ProfileVerifierV2 profileVerifier;
    private final TokenAuditService auditService;
    private final Map<String, TokenStateV2> tokenStates;
    private AuthConfigV2 authConfig;
    private final com.vegatrader.util.time.TimeProvider timeProvider;

    public OAuthLoginAutomationV2(com.vegatrader.util.time.TimeProvider timeProvider) {
        this.tokenExchangeClient = new TokenExchangeClientV2();
        this.profileVerifier = new ProfileVerifierV2();
        this.auditService = new TokenAuditService(timeProvider);
        this.tokenStates = new ConcurrentHashMap<>();
        this.authConfig = new AuthConfigV2();
        this.timeProvider = timeProvider;
    }

    public OAuthLoginAutomationV2(AuthConfigV2 authConfig, com.vegatrader.util.time.TimeProvider timeProvider) {
        this(timeProvider);
        this.authConfig = authConfig;
    }

    /**
     * Perform complete OAuth login and return result.
     * 
     * @param config login configuration
     * @return LoginResultV2 with success/failure and token details
     */
    public LoginResultV2 performLogin(LoginConfigV2 config) {
        long startTime = System.currentTimeMillis();
        String apiName = config.getApiName();

        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║     UPSTOX OAuth Login Automation V2 (Enterprise)        ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");
        logger.info("API Name: {}", apiName);
        logger.info("Client ID: {}", config.getClientId());
        logger.info("Browser: {} (headless: {})", config.getBrowser(), config.isHeadless());

        // Check kill switch
        if (!authConfig.isSeleniumEnabled()) {
            logger.warn("⚠️ Selenium automation is disabled (kill switch)");
            return LoginResultV2.failure(apiName, "Selenium automation disabled");
        }

        // Check token state (quarantine/cooldown)
        TokenStateV2 tokenState = getOrCreateTokenState(apiName);
        if (!tokenState.canAttemptRegeneration()) {
            String reason = tokenState.isQuarantined()
                    ? "Token quarantined: " + tokenState.getQuarantineReason()
                    : "Token in cooldown: " + tokenState.getCooldownRemainingSeconds() + "s remaining";
            logger.warn("⚠️ Cannot regenerate token: {}", reason);
            return LoginResultV2.failure(apiName, reason);
        }

        try {
            // Validate config
            config.validate();

            // Step 1: Create WebDriver
            logger.info("\n[Step 1/6] Creating WebDriver");
            SeleniumConfigV2 seleniumConfig = new SeleniumConfigV2(
                    config.getBrowser(),
                    config.isHeadless(),
                    config.getTimeoutSeconds());
            driver = seleniumConfig.createWebDriver();

            // Step 2: Navigate to authorization URL
            logger.info("\n[Step 2/6] Navigating to authorization URL");
            String authUrl = config.buildAuthorizationUrl();
            LoginPageV2 loginPage = new LoginPageV2(driver, Duration.ofSeconds(config.getTimeoutSeconds()));
            loginPage.navigateToAuthUrl(authUrl);

            // Step 3: Perform login (mobile → OTP → TOTP → PIN)
            logger.info("\n[Step 3/6] Performing login flow");
            loginPage.performLogin(config.getCredentials());

            // Step 4: Handle consent (if required)
            logger.info("\n[Step 4/6] Handling consent page");
            ConsentPageV2 consentPage = new ConsentPageV2(driver);
            consentPage.grantConsentIfPresent();

            // Step 5: Capture authorization code
            logger.info("\n[Step 5/6] Capturing authorization code");
            AuthCodeCaptureV2 authCodeCapture = new AuthCodeCaptureV2(driver, Duration.ofSeconds(30));
            String authCode = authCodeCapture.captureAuthCode(config.getRedirectUri());
            logger.info("Authorization code captured (length: {})", authCode.length());

            // Step 6: Exchange code for access token
            logger.info("\n[Step 6/6] Exchanging code for access token");
            TokenResponseV2 tokenResponse = tokenExchangeClient.exchangeCode(
                    authCode,
                    config.getClientId(),
                    config.getClientSecret(),
                    config.getRedirectUri());

            // Verify token with Profile API
            logger.info("\n[Verification] Verifying token with Profile API");
            ProfileDataV2 profile = profileVerifier.getProfile(tokenResponse.getAccessToken());

            long durationMs = System.currentTimeMillis() - startTime;

            // Build success result
            LoginResultV2 result = LoginResultV2.success(config.getApiName(), tokenResponse, profile);
            result.setDurationMs(durationMs);

            logger.info("\n╔═══════════════════════════════════════════════════════════════╗");
            logger.info("║                    LOGIN SUCCESSFUL                           ║");
            logger.info("╚═══════════════════════════════════════════════════════════════╝");
            logger.info("API Name:     {}", result.getApiName());
            logger.info("User ID:      {}", profile.getUserId());
            logger.info("User Name:    {}", profile.getUserName());
            logger.info("Valid Until:  {}", result.getValidityAt());
            logger.info("Duration:     {} ms", durationMs);

            // Mark success and log audit
            tokenState.markSuccess();
            auditService.logGenerated(apiName, profile.getUserId());

            return result;

        } catch (CaptchaDetectedException e) {
            // CRITICAL: CAPTCHA = immediate abort + quarantine
            logger.error("\n╔═══════════════════════════════════════════════════════════════╗");
            logger.error("║                CAPTCHA DETECTED - ABORT                      ║");
            logger.error("╚═══════════════════════════════════════════════════════════════╝");
            logger.error("CAPTCHA Type: {}", e.getCaptchaType());
            logger.error("Action: Token quarantined. Manual intervention required.");

            // Quarantine token
            tokenState.markFailure(TokenFailureReason.CAPTCHA);
            auditService.logQuarantined(apiName, "CAPTCHA: " + e.getCaptchaType());

            captureErrorScreenshot(apiName, TokenFailureReason.CAPTCHA);

            long durationMs = System.currentTimeMillis() - startTime;
            LoginResultV2 result = LoginResultV2.failure(apiName, e.getMessage());
            result.setDurationMs(durationMs);
            return result;

        } catch (Exception e) {
            // Classify failure
            TokenFailureReason reason = classifyFailure(e);

            logger.error("\n╔═══════════════════════════════════════════════════════════════╗");
            logger.error("║                    LOGIN FAILED                               ║");
            logger.error("╚═══════════════════════════════════════════════════════════════╝");
            logger.error("Failure Reason: {}", reason);
            logger.error("Error: {}", e.getMessage(), e);

            // Mark failure (may trigger quarantine)
            tokenState.markFailure(reason);
            auditService.logFailed(apiName, reason.name());

            captureErrorScreenshot(apiName, reason);

            long durationMs = System.currentTimeMillis() - startTime;
            LoginResultV2 result = LoginResultV2.failure(apiName, e.getMessage());
            result.setDurationMs(durationMs);
            return result;

        } finally {
            // Always close browser
            if (driver != null) {
                SeleniumConfigV2.quitDriver(driver);
                driver = null;
            }
        }
    }

    /**
     * Classify exception into failure reason.
     */
    private TokenFailureReason classifyFailure(Exception e) {
        if (e instanceof CaptchaDetectedException) {
            return TokenFailureReason.CAPTCHA;
        }
        if (e instanceof TimeoutException) {
            return TokenFailureReason.NETWORK_TIMEOUT;
        }
        if (e instanceof NoSuchElementException) {
            return TokenFailureReason.SELENIUM_DOM_CHANGE;
        }
        if (e.getMessage() != null) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("invalid")
                    && (msg.contains("credential") || msg.contains("password") || msg.contains("pin"))) {
                return TokenFailureReason.INVALID_CREDENTIALS;
            }
            if (msg.contains("rate limit") || msg.contains("too many")) {
                return TokenFailureReason.RATE_LIMIT;
            }
            if (msg.contains("timeout")) {
                return TokenFailureReason.NETWORK_TIMEOUT;
            }
        }
        return TokenFailureReason.UNKNOWN;
    }

    /**
     * Get or create token state for API.
     */
    private TokenStateV2 getOrCreateTokenState(String apiName) {
        return tokenStates.computeIfAbsent(apiName, k -> new TokenStateV2(k, timeProvider));
    }

    /**
     * Capture screenshot on error with failure context.
     * 
     * @param apiName API name for filename
     * @param reason  failure reason
     */
    private void captureErrorScreenshot(String apiName, TokenFailureReason reason) {
        if (driver == null) {
            return;
        }

        try {
            File screenshotDir = new File("logs/screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            String filename = String.format("error_%s_%s_%s.png",
                    apiName,
                    reason,
                    LocalDateTime.now().toString().replaceAll("[:.\\-T]", "_"));

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destination = new File(screenshotDir, filename);

            // Copy file
            java.nio.file.Files.copy(screenshot.toPath(), destination.toPath());

            logger.info("Screenshot saved: {}", destination.getAbsolutePath());

        } catch (Exception e) {
            logger.warn("Failed to capture screenshot: {}", e.getMessage());
        }
    }

    /**
     * Get current WebDriver instance (for testing/debugging).
     * 
     * @return WebDriver or null
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Quick login with minimal config using builder.
     * 
     * @param mobileNumber 10-digit mobile
     * @param pin          6-digit PIN
     * @param totpSecret   TOTP secret
     * @param clientId     Upstox client ID
     * @param clientSecret Upstox client secret
     * @param redirectUri  callback URI
     * @return LoginResultV2
     */
    public LoginResultV2 quickLogin(String mobileNumber, String pin, String totpSecret,
            String clientId, String clientSecret, String redirectUri) {
        LoginConfigV2 config = LoginConfigV2.builder()
                .apiName("PRIMARY")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .credentials(mobileNumber, pin, totpSecret)
                .headless(false)
                .browser("chrome")
                .primary(true)
                .build();

        return performLogin(config);
    }

    /**
     * Get token state for API (for external monitoring).
     */
    public TokenStateV2 getTokenState(String apiName) {
        return tokenStates.get(apiName);
    }

    /**
     * Clear quarantine for API (manual intervention).
     */
    public void clearQuarantine(String apiName, String operator) {
        TokenStateV2 state = tokenStates.get(apiName);
        if (state != null) {
            state.clearQuarantine();
            auditService.logUnquarantined(apiName, operator);
            logger.info("✓ Quarantine cleared for {} by {}", apiName, operator);
        }
    }

    /**
     * Set auth configuration.
     */
    public void setAuthConfig(AuthConfigV2 authConfig) {
        this.authConfig = authConfig;
    }

    /**
     * Get audit service.
     */
    public TokenAuditService getAuditService() {
        return auditService;
    }
}
