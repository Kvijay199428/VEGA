package com.vegatrader.upstox.auth.selenium.workflow;

import com.vegatrader.upstox.auth.selenium.pages.CallbackPage;
import com.vegatrader.upstox.auth.selenium.utils.UrlParser;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures authorization code from OAuth callback.
 *
 * @since 2.0.0
 */
public class AuthCodeCapture {

    private static final Logger logger = LoggerFactory.getLogger(AuthCodeCapture.class);

    private final WebDriver driver;
    private final CallbackPage callbackPage;

    public AuthCodeCapture(WebDriver driver) {
        this.driver = driver;
        this.callbackPage = new CallbackPage(driver);
    }

    /**
     * Capture authorization code from callback URL.
     *
     * @param redirectUri expected redirect URI
     * @return authorization code
     * @throws RuntimeException if capture fails
     */
    public String captureAuthCode(String redirectUri) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Capturing Authorization Code");
        logger.info("═══════════════════════════════════════════════════════");

        // Wait for callback
        if (!callbackPage.waitForCallback(redirectUri)) {
            throw new RuntimeException("Timeout waiting for OAuth callback");
        }

        // Get current URL
        String callbackUrl = callbackPage.getCurrentUrl();
        logger.debug("Callback URL: {}", callbackUrl);

        // Check for errors
        if (callbackPage.hasError()) {
            String error = callbackPage.getError();
            logger.error("OAuth error in callback: {}", error);
            throw new RuntimeException("OAuth error: " + error);
        }

        // Extract auth code
        String authCode = UrlParser.extractAuthCode(callbackUrl);

        if (authCode == null || authCode.isEmpty()) {
            logger.error("Failed to extract authorization code from URL");
            throw new RuntimeException("Authorization code not found in callback URL");
        }

        logger.info("✓ Authorization code captured successfully");
        logger.info("Code length: {} characters", authCode.length());
        logger.info("═══════════════════════════════════════════════════════");

        return authCode;
    }

    /**
     * Capture both authorization code and state parameter.
     *
     * @param redirectUri expected redirect URI
     * @return array [authCode, state]
     */
    public String[] captureAuthCodeAndState(String redirectUri) {
        String authCode = captureAuthCode(redirectUri);

        String callbackUrl = callbackPage.getCurrentUrl();
        String state = UrlParser.extractState(callbackUrl);

        return new String[] { authCode, state };
    }
}
