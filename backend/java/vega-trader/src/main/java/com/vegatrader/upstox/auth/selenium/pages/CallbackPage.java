package com.vegatrader.upstox.auth.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Page Object for callback/redirect handling.
 * Waits for OAuth callback and extracts authorization code.
 *
 * @since 2.0.0
 */
public class CallbackPage {

    private static final Logger logger = LoggerFactory.getLogger(CallbackPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final int CALLBACK_TIMEOUT_SECONDS = 30;

    public CallbackPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(CALLBACK_TIMEOUT_SECONDS));
    }

    /**
     * Wait for redirect to callback URL containing auth code.
     *
     * @param expectedRedirectUri expected redirect URI base
     * @return true if redirected successfully
     */
    public boolean waitForCallback(String expectedRedirectUri) {
        logger.info("Waiting for callback to: {}", expectedRedirectUri);

        try {
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver d) {
                    String currentUrl = d.getCurrentUrl();
                    return currentUrl.startsWith(expectedRedirectUri) ||
                            currentUrl.contains("code=");
                }
            });

            logger.info("✓ Callback received");
            return true;
        } catch (Exception e) {
            logger.error("Timeout waiting for callback", e);
            return false;
        }
    }

    /**
     * Wait for URL to contain authorization code parameter.
     *
     * @return true if code parameter found
     */
    public boolean waitForAuthCode() {
        logger.info("Waiting for authorization code in URL");

        try {
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver d) {
                    return d.getCurrentUrl().contains("code=");
                }
            });

            logger.info("✓ Authorization code found in URL");
            return true;
        } catch (Exception e) {
            logger.error("Timeout waiting for authorization code", e);
            return false;
        }
    }

    /**
     * Get current URL (callback URL with auth code).
     *
     * @return current URL
     */
    public String getCurrentUrl() {
        String url = driver.getCurrentUrl();
        logger.debug("Current URL: {}", url);
        return url;
    }

    /**
     * Check if callback contains error parameter.
     *
     * @return true if error in URL
     */
    public boolean hasError() {
        String url = driver.getCurrentUrl();
        return url.contains("error=");
    }

    /**
     * Get error from callback URL if present.
     *
     * @return error string or null
     */
    public String getError() {
        String url = driver.getCurrentUrl();

        if (url.contains("error=")) {
            try {
                int startIndex = url.indexOf("error=") + 6;
                int endIndex = url.indexOf("&", startIndex);
                if (endIndex == -1) {
                    endIndex = url.length();
                }
                return url.substring(startIndex, endIndex);
            } catch (Exception e) {
                logger.error("Error parsing error parameter", e);
                return "unknown_error";
            }
        }

        return null;
    }
}
