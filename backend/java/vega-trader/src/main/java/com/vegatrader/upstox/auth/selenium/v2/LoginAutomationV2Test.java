package com.vegatrader.upstox.auth.selenium.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test for V2 login automation.
 * Loads credentials from .env file using:
 * - UPSTOX_MOBILE_NUMBER
 * - UPSTOX_PIN
 * - UPSTOX_TOTP
 * - UPSTOX_CLIENT_ID_0, UPSTOX_CLIENT_SECRET_0 (for PRIMARY)
 * 
 * Run this as a standalone Java application to test the complete login flow.
 *
 * @since 2.1.0
 */
public class LoginAutomationV2Test {

    private static final Logger logger = LoggerFactory.getLogger(LoginAutomationV2Test.class);

    public static void main(String[] args) {
        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║          Login Automation V2 Integration Test                 ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");

        // Load configuration from .env file
        EnvConfigLoaderV2 envConfig = new EnvConfigLoaderV2();

        // Print configuration summary
        envConfig.printSummary();

        // Validate configuration
        if (!envConfig.isConfigured()) {
            logger.error("⚠️  Configuration is incomplete!");
            logger.error("   Please ensure your .env file contains:");
            logger.error("   - UPSTOX_MOBILE_NUMBER (10-digit mobile)");
            logger.error("   - UPSTOX_PIN (6-digit PIN)");
            logger.error("   - UPSTOX_TOTP (Base32 TOTP secret)");
            logger.error("   - UPSTOX_CLIENT_ID_0 (Client ID)");
            logger.error("   - UPSTOX_CLIENT_SECRET_0 (Client Secret)");
            return;
        }

        try {
            // Build configuration from .env
            LoginConfigV2 config = envConfig.buildLoginConfig(0, false); // API 0 = PRIMARY, headless = false

            logger.info("\n");
            logger.info("Configuration loaded:");
            logger.info("  API Name:     {}", config.getApiName());
            logger.info("  Client ID:    {}...", config.getClientId().substring(0, 8));
            logger.info("  Redirect URI: {}", config.getRedirectUri());
            logger.info("  Headless:     {}", config.isHeadless());

            // Create automation instance
            OAuthLoginAutomationV2 automation = new OAuthLoginAutomationV2();

            // Perform login
            logger.info("\nStarting login flow...\n");
            LoginResultV2 result = automation.performLogin(config);

            // Print result
            printResult(result);

        } catch (Exception e) {
            logger.error("Test failed with exception", e);
        }
    }

    /**
     * Run multi-login for all 6 APIs.
     */
    public static void runMultiLogin() {
        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║          Multi-Login V2 Integration Test                      ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");

        EnvConfigLoaderV2 envConfig = new EnvConfigLoaderV2();
        envConfig.printSummary();

        if (!envConfig.isConfigured()) {
            logger.error("Configuration is incomplete!");
            return;
        }

        OAuthLoginAutomationV2 automation = new OAuthLoginAutomationV2();

        for (int apiIndex = 0; apiIndex < 6; apiIndex++) {
            try {
                String clientId = envConfig.getClientId(apiIndex);
                if (clientId == null || clientId.isEmpty()) {
                    logger.warn("Skipping API {} - no client ID configured", apiIndex);
                    continue;
                }

                logger.info("\n═══════════════════════════════════════════════════════════════");
                logger.info("Processing API {}: {}", apiIndex, envConfig.getApiName(apiIndex));
                logger.info("═══════════════════════════════════════════════════════════════");

                LoginConfigV2 config = envConfig.buildLoginConfig(apiIndex, false);
                LoginResultV2 result = automation.performLogin(config);
                printResult(result);

                // Small delay between logins
                Thread.sleep(2000);

            } catch (Exception e) {
                logger.error("Failed to process API {}: {}", apiIndex, e.getMessage());
            }
        }

        logger.info("\n╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║          Multi-Login Complete                                 ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");
    }

    /**
     * Print login result.
     */
    private static void printResult(LoginResultV2 result) {
        logger.info("\n");
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("                       TEST RESULT");
        logger.info("═══════════════════════════════════════════════════════════════");

        if (result.isSuccess()) {
            logger.info("Status:       ✅ SUCCESS");
            logger.info("API Name:     {}", result.getApiName());
            logger.info("Token Type:   {}", result.getTokenType());
            logger.info("Expires In:   {} seconds", result.getExpiresIn());
            logger.info("Valid Until:  {}", result.getValidityAt());
            logger.info("Duration:     {} ms", result.getDurationMs());

            if (result.getProfile() != null) {
                logger.info("");
                logger.info("Profile:");
                logger.info("  User ID:    {}", result.getProfile().getUserId());
                logger.info("  User Name:  {}", result.getProfile().getUserName());
                logger.info("  Email:      {}", result.getProfile().getEmail());
                logger.info("  Broker:     {}", result.getProfile().getBroker());
                logger.info("  Exchanges:  {}", result.getProfile().getExchanges());
                logger.info("  Active:     {}", result.getProfile().isActive());
            }

            logger.info("");
            logger.info("Access Token (first 50 chars):");
            String token = result.getAccessToken();
            logger.info("  {}...", token.substring(0, Math.min(50, token.length())));

        } else {
            logger.error("Status:       ❌ FAILED");
            logger.error("Error:        {}", result.getErrorMessage());
            logger.error("Duration:     {} ms", result.getDurationMs());
        }

        logger.info("═══════════════════════════════════════════════════════════════");
    }

    /**
     * Test TOTP code generation without running full login.
     */
    public static void testTotpGeneration() {
        logger.info("Testing TOTP generation...");

        EnvConfigLoaderV2 envConfig = new EnvConfigLoaderV2();
        LoginCredentialsV2 credentials = envConfig.buildCredentials();

        logger.info("Mobile: {}", credentials.getMobileNumber());
        logger.info("TOTP Secret configured: {}", credentials.hasTotpSecret());

        for (int i = 0; i < 3; i++) {
            String code = credentials.generateTotpCode();
            logger.info("Generated TOTP code {}: {}", i + 1, code);

            try {
                Thread.sleep(10000); // Wait 10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Test token expiry calculator.
     */
    public static void testTokenExpiryCalculator() {
        logger.info("Testing token expiry calculator...");

        String validityAt = TokenExpiryCalculatorV2.calculateValidityAtString();
        logger.info("Validity At:         {}", validityAt);

        long secondsUntilExpiry = TokenExpiryCalculatorV2.calculateSecondsUntilExpiry();
        logger.info("Seconds to Expiry:   {}", secondsUntilExpiry);

        String timeRemaining = TokenExpiryCalculatorV2.getTimeUntilExpiryString(validityAt);
        logger.info("Time Remaining:      {}", timeRemaining);

        boolean isExpired = TokenExpiryCalculatorV2.isExpired(validityAt);
        logger.info("Is Expired:          {}", isExpired);

        boolean needsRefresh = TokenExpiryCalculatorV2.needsRefresh(validityAt);
        logger.info("Needs Refresh:       {}", needsRefresh);
    }
}
