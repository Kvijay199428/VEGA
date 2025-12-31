package com.vegatrader.upstox.auth.selenium.v2;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures authorization code from redirect URL after OAuth login.
 * Waits for browser to redirect to callback URI and extracts the code
 * parameter.
 *
 * @since 2.1.0
 */
public class AuthCodeCaptureV2 {

    private static final Logger logger = LoggerFactory.getLogger(AuthCodeCaptureV2.class);

    private static final String CODE_PARAM = "code";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private final WebDriver driver;
    private final Duration timeout;

    /**
     * Create AuthCodeCaptureV2 with default 30-second timeout.
     * 
     * @param driver WebDriver instance
     */
    public AuthCodeCaptureV2(WebDriver driver) {
        this(driver, DEFAULT_TIMEOUT);
    }

    /**
     * Create AuthCodeCaptureV2 with custom timeout.
     * 
     * @param driver  WebDriver instance
     * @param timeout wait timeout duration
     */
    public AuthCodeCaptureV2(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.timeout = timeout;
    }

    /**
     * Wait for redirect and capture authorization code.
     * 
     * @param expectedRedirectUri the callback URI to wait for
     * @return authorization code
     * @throws RuntimeException if timeout or code not found
     */
    public String captureAuthCode(String expectedRedirectUri) {
        return captureAuthCode(expectedRedirectUri, null);
    }

    /**
     * Wait for redirect and capture authorization code with state validation.
     * 
     * Per migration guide: state MUST be generated + validated (CSRF protection).
     * 
     * @param expectedRedirectUri the callback URI to wait for
     * @param expectedState       state parameter sent in authorize request (for
     *                            CSRF validation)
     * @return authorization code
     * @throws RuntimeException if timeout, code not found, or state mismatch
     */
    public String captureAuthCode(String expectedRedirectUri, String expectedState) {
        logger.info("Waiting for redirect to: {}", expectedRedirectUri);
        if (expectedState != null) {
            logger.info("State validation enabled (CSRF protection)");
        }

        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            String currentUrl = driver.getCurrentUrl();

            // Check if we've been redirected to the callback URI
            if (currentUrl != null && currentUrl.startsWith(expectedRedirectUri)) {
                logger.info("✓ Redirect detected");
                logger.debug("Redirect URL: {}", currentUrl);

                Map<String, String> params = parseQueryParams(currentUrl);

                // Validate state parameter (CSRF protection) - per migration guide
                if (expectedState != null) {
                    String returnedState = params.get("state");
                    if (returnedState == null || !returnedState.equals(expectedState)) {
                        logger.error("❌ State mismatch! Expected: {}, Got: {}",
                                expectedState, returnedState);
                        throw new RuntimeException("State validation failed (possible CSRF attack)");
                    }
                    logger.info("✓ State validated (CSRF check passed)");
                }

                // Extract authorization code from URL
                String code = params.get(CODE_PARAM);
                if (code != null && !code.isEmpty()) {
                    logger.info("✓ Authorization code captured (length: {})", code.length());
                    return code;
                } else {
                    // Check for error in redirect
                    String error = extractErrorFromUrl(currentUrl);
                    if (error != null) {
                        throw new RuntimeException("OAuth error: " + error);
                    }
                    throw new RuntimeException("Authorization code not found in redirect URL");
                }
            }

            // Poll interval
            sleep(POLL_INTERVAL.toMillis());
        }

        // HARD RULE per migration guide: If code is not received → ABORT (do not retry)
        logger.error("❌ Auth code capture timeout - ABORTING (no retry per migration policy)");
        throw new RuntimeException("Timeout waiting for redirect to " + expectedRedirectUri);
    }

    /**
     * Extract 'code' parameter from URL.
     * 
     * @param url redirect URL
     * @return authorization code or null
     */
    private String extractCodeFromUrl(String url) {
        try {
            Map<String, String> params = parseQueryParams(url);
            return params.get(CODE_PARAM);
        } catch (Exception e) {
            logger.error("Failed to extract code from URL", e);
            return null;
        }
    }

    /**
     * Extract 'error' parameter from URL (for OAuth error handling).
     * 
     * @param url redirect URL
     * @return error message or null
     */
    private String extractErrorFromUrl(String url) {
        try {
            Map<String, String> params = parseQueryParams(url);
            String error = params.get("error");
            String description = params.get("error_description");

            if (error != null) {
                return description != null ? error + ": " + description : error;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse query parameters from URL.
     * 
     * @param url URL string
     * @return map of parameter names to values
     */
    private Map<String, String> parseQueryParams(String url) {
        Map<String, String> params = new HashMap<>();

        try {
            URI uri = URI.create(url);
            String query = uri.getQuery();

            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        params.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to parse URL query params: {}", e.getMessage());
        }

        return params;
    }

    /**
     * Sleep for specified milliseconds.
     * 
     * @param millis milliseconds to sleep
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
