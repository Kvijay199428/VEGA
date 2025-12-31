package com.vegatrader.upstox.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Profile verification service - Layer 2 (API check).
 * Verifies token by calling GET /v2/user/profile.
 *
 * @since 2.2.0
 */
public class ProfileVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileVerificationService.class);
    private static final String PROFILE_URL = "https://api.upstox.com/v2/user/profile";

    private final HttpClient httpClient;

    public ProfileVerificationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Profile check result.
     */
    public enum ProfileCheckResult {
        VALID, // 200 OK - token works
        INVALID, // 401 Unauthorized - token expired/invalid
        UNKNOWN // Network error or other issue
    }

    /**
     * Verify access token by calling profile API.
     */
    public ProfileCheckResult verify(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ProfileCheckResult.INVALID;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PROFILE_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode == 200) {
                logger.debug("✓ Profile verification passed");
                return ProfileCheckResult.VALID;
            } else if (statusCode == 401) {
                logger.debug("✗ Profile verification failed: 401 Unauthorized");
                return ProfileCheckResult.INVALID;
            } else {
                logger.warn("Profile verification unknown status: {}", statusCode);
                return ProfileCheckResult.UNKNOWN;
            }

        } catch (Exception e) {
            logger.error("Profile verification error: {}", e.getMessage());
            return ProfileCheckResult.UNKNOWN;
        }
    }

    /**
     * Simple boolean check.
     */
    public boolean isValid(String accessToken) {
        return verify(accessToken) == ProfileCheckResult.VALID;
    }
}
