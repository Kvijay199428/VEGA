package com.vegatrader.upstox.auth.selenium.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Upstox Logout Client.
 * 
 * Use this when:
 * - Rotating credentials
 * - Recovering from corruption
 * - Manual operator intervention
 * - Before aggressive token regeneration
 *
 * @since 2.2.0
 */
public class LogoutClientV2 {

    private static final Logger logger = LoggerFactory.getLogger(LogoutClientV2.class);
    private static final String LOGOUT_URL = "https://api.upstox.com/v2/logout";

    private final HttpClient httpClient;

    public LogoutClientV2() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Logout and invalidate an existing token.
     * 
     * @param accessToken current access token to invalidate
     * @return true if logout successful, false otherwise
     */
    public boolean logout(String accessToken) {
        logger.info("Logging out current session...");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOGOUT_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .DELETE()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                logger.info("✓ Logout successful - token invalidated");
                return true;
            } else if (response.statusCode() == 401) {
                logger.warn("Token already invalid or expired");
                return true; // Consider success - token is not valid anyway
            } else {
                logger.error("Logout failed: {} - {}", response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Logout with API name for audit purposes.
     * 
     * @param accessToken current access token
     * @param apiName     API name (PRIMARY, WEBSOCKET1, etc.) for logging
     * @return true if logout successful
     */
    public boolean logout(String accessToken, String apiName) {
        logger.info("Logging out session for API: {}", apiName);
        boolean result = logout(accessToken);
        if (result) {
            logger.info("✓ Logout completed for: {}", apiName);
        } else {
            logger.warn("⚠️ Logout may have failed for: {}", apiName);
        }
        return result;
    }
}
