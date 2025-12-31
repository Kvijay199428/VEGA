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
        long flowStartTime = System.currentTimeMillis();
        logger.info("AUDIT: STARTING AUTH FLOW | API: {}", apiName);

        try {
            // Step 1: Build authorization URL
            String authUrl = AuthUrlBuilder.buildAuthorizationUrl(clientId, redirectUri);
            logger.debug("Auth URL: {}", authUrl);

            // Step 2: Perform login automation
            logger.info("AUDIT: [Step 1/3] Selenium Login");
            long seleniumStart = System.currentTimeMillis();

            OAuthLoginAutomation automation = new OAuthLoginAutomation(seleniumConfig);
            String authCode = automation.performLogin(authUrl, credentials, redirectUri);

            long seleniumDuration = System.currentTimeMillis() - seleniumStart;
            logger.info("AUDIT: Selenium Login Completed | Duration: {}ms", seleniumDuration);

            // Step 3: Exchange code for token
            logger.info("AUDIT: [Step 2/3] Token Exchange");
            long exchangeStart = System.currentTimeMillis();

            TokenResponse tokenResponse = tokenExchangeClient.exchangeCodeForToken(
                    authCode, clientId, clientSecret, redirectUri);

            long exchangeDuration = System.currentTimeMillis() - exchangeStart;
            logger.info("AUDIT: Token Exchange Completed | Duration: {}ms", exchangeDuration);

            // Step 4: Store token in database
            logger.info("AUDIT: [Step 3/3] Token Storage");
            tokenStorageService.storeToken(tokenResponse, apiName, clientId,
                    clientSecret, redirectUri, isPrimary);

            long totalDuration = System.currentTimeMillis() - flowStartTime;
            logger.info("AUDIT: AUTH FLOW SUCCESS | API: {} | Total Duration: {}ms", apiName, totalDuration);

            return tokenResponse;

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - flowStartTime;
            logger.error("AUDIT: AUTH FLOW FAILED | API: {} | Duration: {}ms | Error: {}",
                    apiName, totalDuration, e.getMessage());
            throw new RuntimeException("Authentication failed for " + apiName, e);
        }
    }
}
