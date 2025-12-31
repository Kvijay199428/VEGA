package com.vegatrader.service;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.auth.response.TokenResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Health checker for Upstox access tokens.
 * 
 * <p>
 * Validates token health by calling Upstox user profile endpoint.
 * This ensures tokens are valid for WebSocket connections before attempting
 * to connect, avoiding 401/410 authentication failures.
 * 
 * <p>
 * Uses a tri-state health model (HEALTHY, UNHEALTHY, UNKNOWN) to prevent
 * transient network issues from poisoning the token pool.
 * 
 * @since 3.1.0
 */
public class UpstoxTokenHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxTokenHealthChecker.class);

    private static final String USER_PROFILE_URL = "https://api.upstox.com/v2/user/profile";
    private static final int HEALTH_CHECK_TIMEOUT_MS = 2000;

    private final TokenStorageService tokenStorage;
    private final OkHttpClient httpClient;

    public UpstoxTokenHealthChecker(TokenStorageService tokenStorage) {
        this(tokenStorage, createDefaultClient());
    }

    public UpstoxTokenHealthChecker(TokenStorageService tokenStorage, OkHttpClient httpClient) {
        this.tokenStorage = tokenStorage;
        this.httpClient = httpClient;
        logger.info("UpstoxTokenHealthChecker initialized");
    }

    /**
     * Checks token health using tri-state model.
     * 
     * @param token the access token to check
     * @return token health status
     */
    public TokenHealth checkTokenHealth(String token) {
        if (token == null || token.isEmpty()) {
            logger.warn("Cannot check health of null/empty token");
            return TokenHealth.UNKNOWN;
        }

        try {
            Request request = new Request.Builder()
                    .url(USER_PROFILE_URL)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                int statusCode = response.code();

                if (statusCode == 200) {
                    return TokenHealth.HEALTHY;
                } else if (statusCode == 401 || statusCode == 410) {
                    logger.warn("✗ Token is UNHEALTHY (HTTP {})", statusCode);
                    return TokenHealth.UNHEALTHY;
                } else {
                    logger.warn("? Token health UNKNOWN (HTTP {})", statusCode);
                    return TokenHealth.UNKNOWN;
                }
            }

        } catch (IOException e) {
            logger.error("Health check UNKNOWN due to network error: {}", e.getMessage());
            return TokenHealth.UNKNOWN;
        }
    }

    /**
     * Backward compatibility method.
     */
    public boolean isTokenHealthy(String token) {
        return checkTokenHealth(token) == TokenHealth.HEALTHY;
    }

    /**
     * Finds the first healthy token from a list of API names.
     */
    public String findFirstHealthyToken(String... apiNames) {
        for (String apiName : apiNames) {
            UpstoxTokenEntity tokenEntity = tokenStorage.getToken(apiName).orElse(null);
            if (tokenEntity != null && tokenEntity.isActive()) {
                String token = tokenEntity.getAccessToken();
                if (checkTokenHealth(token) == TokenHealth.HEALTHY) {
                    logger.info("✓ Found healthy token: {}", apiName);
                    return token;
                }
            }
        }
        return null;
    }

    public TokenHealth checkAndUpdateTokenHealth(String apiName) {
        UpstoxTokenEntity tokenEntity = tokenStorage.getToken(apiName).orElse(null);
        if (tokenEntity == null)
            return TokenHealth.UNKNOWN;

        TokenHealth health = checkTokenHealth(tokenEntity.getAccessToken());
        // Only mark inactive if definitively UNHEALTHY
        if (health == TokenHealth.UNHEALTHY && tokenEntity.isActive()) {
            logger.warn("Token {} is UNHEALTHY, marking as inactive", apiName);

            // Map entity to response DTO for update
            TokenResponse response = mapToResponse(tokenEntity);
            response.setActive(false); // Mark as inactive in DTO

            // Update via storage service
            tokenStorage.updateToken(apiName, response);

            // Also update the local entity state if needed
            tokenEntity.setIsActive(0);
        }
        return health;
    }

    private TokenResponse mapToResponse(UpstoxTokenEntity entity) {
        TokenResponse response = new TokenResponse();
        response.setAccessToken(entity.getAccessToken());
        response.setRefreshToken(entity.getRefreshToken());
        response.setTokenType(entity.getTokenType());
        response.setExpiresIn(entity.getExpiresIn());
        response.setApiName(entity.getApiName());
        response.setActive(entity.isActive());
        // Map other relevant fields if necessary
        return response;
    }

    private static OkHttpClient createDefaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(HEALTH_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(HEALTH_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(HEALTH_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build();
    }
}
