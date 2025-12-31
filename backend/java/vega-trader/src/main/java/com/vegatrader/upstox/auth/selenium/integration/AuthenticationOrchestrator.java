package com.vegatrader.upstox.auth.selenium.integration;

import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.workflow.OAuthLoginAutomation;
import com.vegatrader.upstox.auth.selenium.workflow.TokenExchangeClient;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.auth.utils.AuthUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main authentication orchestrator.
 * Coordinates complete flow: login automation → token exchange → storage.
 *
 * @since 2.0.0
 */
public class AuthenticationOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationOrchestrator.class);

    private final SeleniumConfig seleniumConfig;
    private final TokenExchangeClient tokenExchangeClient;
    private final TokenStorageService tokenStorageService;

    public AuthenticationOrchestrator(SeleniumConfig seleniumConfig,
            TokenStorageService tokenStorageService) {
        this.seleniumConfig = seleniumConfig;
        this.tokenExchangeClient = new TokenExchangeClient();
        this.tokenStorageService = tokenStorageService;
    }

    /**
     * Perform complete authentication flow.
     *
     * @param apiName      API name (PRIMARY, WEBSOCKET1, etc.)
     * @param clientId     client ID
     * @param clientSecret client secret
     * @param redirectUri  redirect URI
     * @param credentials  login credentials
     * @param isPrimary    whether this is primary token
     * @return TokenResponse
     */
    public TokenResponse authenticate(String apiName, String clientId, String clientSecret,
            String redirectUri, LoginCredentials credentials,
            boolean isPrimary) {
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  COMPLETE AUTHENTICATION FLOW - {}  ║", apiName);
        logger.info("╚═══════════════════════════════════════════════════════╝");

        try {
            // Step 1: Build authorization URL
            logger.info("[1/4] Building authorization URL");
            String authUrl = AuthUrlBuilder.buildAuthorizationUrl(clientId, redirectUri);
            logger.debug("Auth URL: {}", authUrl);

            // Step 2: Perform login automation
            logger.info("[2/4] Performing login automation");
            OAuthLoginAutomation automation = new OAuthLoginAutomation(seleniumConfig);
            String authCode = automation.performLogin(authUrl, credentials, redirectUri);
            logger.info("✓ Authorization code obtained");

            // Step 3: Exchange code for token
            logger.info("[3/4] Exchanging authorization code for access token");
            TokenResponse tokenResponse = tokenExchangeClient.exchangeCodeForToken(
                    authCode, clientId, clientSecret, redirectUri);
            logger.info("✓ Access token obtained");

            // Step 4: Store token in database
            logger.info("[4/4] Storing token in database");
            tokenStorageService.storeToken(tokenResponse, apiName, clientId,
                    clientSecret, redirectUri, isPrimary);
            logger.info("✓ Token stored successfully");

            logger.info("╔═══════════════════════════════════════════════════════╗");
            logger.info("║  AUTHENTICATION COMPLETE - {}  ║", apiName);
            logger.info("╚═══════════════════════════════════════════════════════╝");

            return tokenResponse;

        } catch (Exception e) {
            logger.error("╔═══════════════════════════════════════════════════════╗");
            logger.error("║  AUTHENTICATION FAILED - {}  ║", apiName);
            logger.error("╚═══════════════════════════════════════════════════════╝");
            logger.error("Error details:", e);
            throw new RuntimeException("Authentication failed for " + apiName, e);
        }
    }
}
