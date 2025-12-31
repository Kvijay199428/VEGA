package com.vegatrader.upstox.auth.selenium.integration;

import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example usage of Selenium login automation.
 * Demonstrates single and multi-login flows.
 *
 * @since 2.0.0
 */
public class LoginAutomationExample {

    private static final Logger logger = LoggerFactory.getLogger(LoginAutomationExample.class);

    /**
     * Example 1: Single API login.
     */
    public static void singleLoginExample() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("EXAMPLE 1: Single API Login");
        logger.info("═══════════════════════════════════════════════════════");

        // Configuration
        String clientId = "YOUR_CLIENT_ID";
        String clientSecret = "YOUR_CLIENT_SECRET";
        String redirectUri = "YOUR_REDIRECT_URI";

        // Credentials
        LoginCredentials credentials = new LoginCredentials(
                "YOUR_USERNAME", // Mobile number or username
                "YOUR_PASSWORD");

        // Selenium config (headless = true for production, false for debugging)
        SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", true);

        // Create orchestrator
        TokenStorageService tokenStorage = new TokenStorageService(
                new com.vegatrader.upstox.auth.repository.TokenRepository(),
                new com.vegatrader.upstox.auth.service.TokenCacheService());
        AuthenticationOrchestrator orchestrator = new AuthenticationOrchestrator(
                seleniumConfig, tokenStorage);

        try {
            // Perform authentication
            TokenResponse token = orchestrator.authenticate(
                    "PRIMARY", // API name
                    clientId,
                    clientSecret,
                    redirectUri,
                    credentials,
                    true // isPrimary
            );

            logger.info("✓ Login successful!");
            logger.info("Access token: {}...", token.getAccessToken().substring(0, 20));
            logger.info("Token type: {}", token.getTokenType());
            logger.info("Expires in: {} seconds", token.getExpiresIn());

        } catch (Exception e) {
            logger.error("✗ Login failed", e);
        }
    }

    /**
     * Example 2: Multi-login for 6 APIs.
     */
    public static void multiLoginExample() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("EXAMPLE 2: Multi-Login (6 APIs)");
        logger.info("═══════════════════════════════════════════════════════");

        // Create API configurations
        java.util.List<ApiConfig> configs = java.util.Arrays.asList(
                new ApiConfig("PRIMARY", "client_id_1", "secret_1", "redirect_1", true),
                new ApiConfig("WEBSOCKET1", "client_id_2", "secret_2", "redirect_2", false),
                new ApiConfig("WEBSOCKET2", "client_id_3", "secret_3", "redirect_3", false),
                new ApiConfig("WEBSOCKET3", "client_id_4", "secret_4", "redirect_4", false),
                new ApiConfig("OPTIONCHAIN1", "client_id_5", "secret_5", "redirect_5", false),
                new ApiConfig("OPTIONCHAIN2", "client_id_6", "secret_6", "redirect_6", false));

        // Credentials
        LoginCredentials credentials = new LoginCredentials("USERNAME", "PASSWORD");

        // Selenium config
        SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", true);

        // Create services
        TokenStorageService tokenStorage = new TokenStorageService(
                new com.vegatrader.upstox.auth.repository.TokenRepository(),
                new com.vegatrader.upstox.auth.service.TokenCacheService());
        AuthenticationOrchestrator authOrchestrator = new AuthenticationOrchestrator(
                seleniumConfig, tokenStorage);

        // Create multi-login orchestrator
        com.vegatrader.upstox.auth.selenium.workflow.MultiLoginOrchestrator multiLogin = new com.vegatrader.upstox.auth.selenium.workflow.MultiLoginOrchestrator(
                configs, credentials, seleniumConfig, authOrchestrator);

        try {
            // Perform all logins
            var result = multiLogin.loginAll();

            logger.info("═══════════════════════════════════════════════════════");
            logger.info("Multi-Login Results:");
            logger.info("  Total: {}", result.getTotalCount());
            logger.info("  Successful: {}", result.getSuccessCount());
            logger.info("  Failed: {}", result.getFailedCount());
            logger.info("═══════════════════════════════════════════════════════");

            if (result.isAllSuccessful()) {
                logger.info("✓ All logins successful!");
            } else {
                logger.warn("⚠ Some logins failed:");
                result.getFailed().forEach(api -> logger.warn("  - {}", api));
            }

        } catch (Exception e) {
            logger.error("✗ Multi-login failed", e);
        }
    }

    /**
     * Main method for testing.
     */
    public static void main(String[] args) {
        // Run single login example
        // singleLoginExample();

        // Run multi-login example
        // multiLoginExample();

        logger.info("Examples ready to run - uncomment desired example in main()");
    }
}
