package com.vegatrader.upstox.auth.selenium.integration;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;

import com.vegatrader.upstox.auth.selenium.config.EnvConfigLoader;
import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.workflow.MultiLoginOrchestrator;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete login test using .env configuration.
 * Tests login flow with actual Upstox credentials.
 *
 * RUN THIS TO TEST LOGIN AUTOMATION!
 *
 * @since 2.0.0
 */
public class CompleteLoginTest {

    private static final Logger logger = LoggerFactory.getLogger(CompleteLoginTest.class);

    /**
     * Test single API login (PRIMARY - API 0).
     */
    public static void testSingleLogin() {
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  TEST: Single API Login (PRIMARY)  ║");
        logger.info("╚═══════════════════════════════════════════════════════╝");

        // Load configuration
        EnvConfigLoader config = new EnvConfigLoader();
        config.printSummary();

        if (!config.isConfigured()) {
            logger.error("✗ Configuration incomplete - check backend/.env");
            return;
        }

        try {
            // Create credentials
            LoginCredentials credentials = new LoginCredentials(
                    config.getMobileNumber(),
                    config.getPin(),
                    config.getTotpSecret());

            // Selenium config (headless = false to watch automation)
            SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", false);

            // Create orchestrator
            TokenStorageService tokenStorage = new TokenStorageService(
                    new com.vegatrader.upstox.auth.repository.TokenRepository(),
                    new com.vegatrader.upstox.auth.service.TokenCacheService());
            AuthenticationOrchestrator orchestrator = new AuthenticationOrchestrator(
                    seleniumConfig, tokenStorage);

            // Perform login for PRIMARY (API 0)
            logger.info("\n🚀 Starting login automation for PRIMARY...\n");

            TokenResponse token = orchestrator.authenticate(
                    "PRIMARY",
                    config.getClientId(0),
                    config.getClientSecret(0),
                    config.getRedirectUri(),
                    credentials,
                    true // isPrimary
            );

            logger.info("\n╔═══════════════════════════════════════════════════════╗");
            logger.info("║  ✓ LOGIN SUCCESSFUL!  ║");
            logger.info("╠═══════════════════════════════════════════════════════╣");
            logger.info("║  API: PRIMARY                                         ║");
            logger.info("║  Token Type: {}                                  ║", token.getTokenType());
            logger.info("║  Token Length: {} chars                            ║", token.getAccessToken().length());
            logger.info("║  Expires In: {} seconds                          ║", token.getExpiresIn());
            logger.info("╚═══════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            logger.error("\n╔═══════════════════════════════════════════════════════╗");
            logger.error("║  ✗ LOGIN FAILED  ║");
            logger.error("╚═══════════════════════════════════════════════════════╝");
            logger.error("Error:", e);
        }
    }

    /**
     * Test multi-login for all 6 APIs.
     */
    public static void testMultiLogin() {
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  TEST: Multi-Login (6 APIs)  ║");
        logger.info("╚═══════════════════════════════════════════════════════╝");

        // Load configuration
        EnvConfigLoader config = new EnvConfigLoader();
        config.printSummary();

        if (!config.isConfigured()) {
            logger.error("✗ Configuration incomplete - check backend/.env");
            return;
        }

        try {
            // STEP 1: Clean up ALL existing tokens before starting
            logger.info("\n════════════════════════════════════════════════════════");
            logger.info("  STEP 1: Cleaning up existing tokens...");
            logger.info("════════════════════════════════════════════════════════");

            TokenStorageService tokenStorage = new TokenStorageService(
                    new com.vegatrader.upstox.auth.repository.TokenRepository(),
                    new com.vegatrader.upstox.auth.service.TokenCacheService());
            List<UpstoxTokenEntity> existingTokens = tokenStorage.getAllActiveTokens();
            logger.info("Found {} existing tokens in database", existingTokens.size());

            // Delete all existing tokens
            for (UpstoxTokenEntity token : existingTokens) {
                logger.info("  Deactivating: {}", token.getApiName());
                tokenStorage.deactivateToken(token.getApiName());
            }
            logger.info("✓ All existing tokens cleaned up\n");

            // STEP 2: Create API configurations from .env
            logger.info("════════════════════════════════════════════════════════");
            logger.info("  STEP 2: Loading API configurations...");
            logger.info("════════════════════════════════════════════════════════");

            List<ApiConfig> apiConfigs = new ArrayList<>();

            for (int i = 0; i < 6; i++) {
                String clientId = config.getClientId(i);
                String clientSecret = config.getClientSecret(i);
                String apiName = config.getApiName(i);

                if (clientId == null || clientSecret == null) {
                    logger.error("✗ Missing configuration for API {}: {}", i, apiName);
                    logger.error("  Client ID: {}", clientId != null ? "present" : "MISSING");
                    logger.error("  Client Secret: {}", clientSecret != null ? "present" : "MISSING");
                    throw new IllegalStateException("Missing configuration for API " + i);
                }

                apiConfigs.add(new ApiConfig(
                        apiName,
                        clientId,
                        clientSecret,
                        config.getRedirectUri(),
                        i == 0 // Only API 0 is primary
                ));

                logger.info("  ✓ API {}: {} - Client ID: {}...", i, apiName, clientId.substring(0, 8));
            }
            logger.info("✓ Loaded {} API configurations\n", apiConfigs.size());

            // Create credentials
            LoginCredentials credentials = new LoginCredentials(
                    config.getMobileNumber(),
                    config.getPin(),
                    config.getTotpSecret());

            // Selenium config (visible browser for multi-login)
            SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", false);

            // STEP 3: Create services and start multi-login
            logger.info("════════════════════════════════════════════════════════");
            logger.info("  STEP 3: Starting Multi-Login Automation");
            logger.info("════════════════════════════════════════════════════════\n");

            AuthenticationOrchestrator authOrchestrator = new AuthenticationOrchestrator(
                    seleniumConfig, tokenStorage);

            // Create multi-login orchestrator
            MultiLoginOrchestrator multiLogin = new MultiLoginOrchestrator(
                    apiConfigs, credentials, seleniumConfig, authOrchestrator);

            logger.info("\n🚀 Starting multi-login automation for 6 APIs...\n");

            // Perform all logins
            MultiLoginOrchestrator.MultiLoginResult result = multiLogin.loginAll();

            // Print results
            logger.info("\n╔═══════════════════════════════════════════════════════╗");
            logger.info("║  MULTI-LOGIN COMPLETE  ║");
            logger.info("╠═══════════════════════════════════════════════════════╣");
            logger.info("║  Total APIs: {}                                        ║", result.getTotalCount());
            logger.info("║  Successful: {}                                        ║", result.getSuccessCount());
            logger.info("║  Failed: {}                                            ║", result.getFailedCount());
            logger.info("╚═══════════════════════════════════════════════════════╝");

            if (!result.getSuccessful().isEmpty()) {
                logger.info("\n✓ Successful logins:");
                result.getSuccessful().forEach(api -> logger.info("  ✓ {}", api));
            }

            if (!result.getFailed().isEmpty()) {
                logger.error("\n✗ Failed logins:");
                result.getFailed().forEach(api -> logger.error("  ✗ {}", api));
            }

            if (result.isAllSuccessful()) {
                logger.info("\n🎉 ALL 6 APIS LOGGED IN SUCCESSFULLY!");
            }

        } catch (Exception e) {
            logger.error("\n✗ Multi-login failed", e);
        }
    }

    /**
     * Main method - Run this to test login automation!
     */
    public static void main(String[] args) {
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║                                                       ║");
        logger.info("║  UPSTOX LOGIN AUTOMATION TEST                         ║");
        logger.info("║  Using backend/.env configuration                     ║");
        logger.info("║                                                       ║");
        logger.info("╚═══════════════════════════════════════════════════════╝");

        // Check which test to run
        if (args.length > 0 && "multi".equals(args[0])) {
            // Run multi-login test
            testMultiLogin();
        } else {
            // Default: Run single login test
            logger.info("\nRunning SINGLE LOGIN test (PRIMARY API)");
            logger.info("For multi-login, run: java CompleteLoginTest multi\n");

            testSingleLogin();
        }

        logger.info("\n\nTest complete!");
    }
}
