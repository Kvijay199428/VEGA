package com.vegatrader.service;

import com.vegatrader.service.exception.NoHealthyTokenException;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database-backed Upstox access token provider.
 * 
 * <p>
 * Token Selection Strategy:
 * <ol>
 * <li>Try preferred API name (e.g., WEBSOCKET1)</li>
 * <li>Fallback to any active WEBSOCKET* token</li>
 * <li>Final fallback to PRIMARY token</li>
 * </ol>
 * 
 * @since 3.0.0
 */
public class UpstoxTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxTokenProvider.class);

    private final TokenStorageService tokenStorageService;
    private final String preferredApiName;
    private UpstoxTokenHealthChecker healthChecker; // Optional health checking

    /**
     * Creates token provider with database integration.
     * 
     * @param tokenStorageService the token storage service
     * @param preferredApiName    the preferred API name (e.g., "WEBSOCKET1",
     *                            "PRIMARY")
     */
    public UpstoxTokenProvider(TokenStorageService tokenStorageService, String preferredApiName) {
        this.tokenStorageService = tokenStorageService;
        this.preferredApiName = preferredApiName != null ? preferredApiName : "WEBSOCKET1";
        logger.info("UpstoxTokenProvider initialized with preferred API: {}", this.preferredApiName);
    }

    /**
     * Creates token provider with default WEBSOCKET1 preference.
     * 
     * @param tokenStorageService the token storage service
     */
    public UpstoxTokenProvider(TokenStorageService tokenStorageService) {
        this(tokenStorageService, "WEBSOCKET1");
    }

    /**
     * Sets optional health checker for token validation.
     * 
     * @param healthChecker the health checker instance
     */
    public void setHealthChecker(UpstoxTokenHealthChecker healthChecker) {
        this.healthChecker = healthChecker;
        logger.info("Health checker enabled for token validation");
    }

    /**
     * Gets access token from database.
     * 
     * @return active access token
     */
    /**
     * Gets access token from database.
     * 
     * @return active access token
     */
    public String getAccessToken() {
        return getAccessToken(com.vegatrader.upstox.auth.TokenCapability.CORE_REST);
    }

    /**
     * Gets active access token from database with capability-specific validation.
     * 
     * @param capability the required capability (REST, MARKET_DATA_WS, ORDER_WS)
     * @return validated access token
     * @throws RuntimeException if no valid/fresh token is available
     */
    public String getAccessToken(com.vegatrader.upstox.auth.TokenCapability capability) {
        logger.debug("Requesting access token for capability: {}", capability);

        // Capability Mapping Logic
        switch (capability) {
            case MARKET_DATA_WS:
            case PORTFOLIO_WS:
                return getWebSocketToken(capability);

            case OPTION_CHAIN:
                return getOptionChainToken();

            case CORE_REST:
            default:
                return getCoreRestToken();
        }
    }

    private String getWebSocketToken(com.vegatrader.upstox.auth.TokenCapability capability) {
        // Round-robin or random selection from WEBSOCKET1..3
        // For simplicity and effectiveness, we iterate and take the first active fresh
        // one.
        // In a more complex setup, we could track load.

        // 1. Try preferred if it is a WEBSOCKET token
        if (preferredApiName.startsWith("WEBSOCKET")) {
            UpstoxTokenEntity token = tokenStorageService.getToken(preferredApiName).orElse(null);
            if (token != null && token.isActive() && isFreshEnough(token, capability)) {
                return token.getAccessToken();
            }
        }

        // 2. Try all WEBSOCKET tokens (1 to 3)
        for (int i = 1; i <= 3; i++) {
            String wsName = "WEBSOCKET" + i;
            UpstoxTokenEntity token = tokenStorageService.getToken(wsName).orElse(null);
            if (token != null && token.isActive() && isFreshEnough(token, capability)) {
                logger.debug("Using {} for {}", wsName, capability);
                return token.getAccessToken();
            }
        }

        logger.warn("No active WEBSOCKET token found. Falling back to PRIMARY.");
        return getCoreRestToken();
    }

    private String getOptionChainToken() {
        // Try OPTIONCHAIN1, OPTIONCHAIN2
        for (int i = 1; i <= 2; i++) {
            String name = "OPTIONCHAIN" + i;
            UpstoxTokenEntity token = tokenStorageService.getToken(name).orElse(null);
            // Option chain is REST-based (usually), so standard freshness is fine,
            // but user wants burst handling. We check active.
            if (token != null && token.isActive()) {
                logger.debug("Using {} for OPTION_CHAIN", name);
                return token.getAccessToken();
            }
        }
        logger.warn("No active OPTIONCHAIN token found. Falling back to PRIMARY.");
        return getCoreRestToken();
    }

    private String getCoreRestToken() {
        UpstoxTokenEntity token = tokenStorageService.getToken("PRIMARY").orElse(null);
        if (token != null && token.isActive()) {
            return token.getAccessToken();
        }
        throw new RuntimeException("No active PRIMARY token available for CORE_REST");
    }

    /**
     * Enforces token freshness rules based on capability.
     */
    private boolean isFreshEnough(UpstoxTokenEntity token, com.vegatrader.upstox.auth.TokenCapability capability) {
        if (capability == com.vegatrader.upstox.auth.TokenCapability.CORE_REST
                || capability == com.vegatrader.upstox.auth.TokenCapability.OPTION_CHAIN) {
            return true; // REST tokens can be older as long as they are active
        }

        // WebSocket handshakes require fresh tokens (max 10 minutes old)
        // Upstox often rejects handshakes with older tokens even if they are 'active'
        boolean fresh = !token.isOlderThan(java.time.Duration.ofMinutes(10));
        if (!fresh) {
            logger.warn(
                    "Token {} is too old for WebSocket handshake (Age: {} min, Limit: 10 min). Please run 'run-multilogin.bat' to refresh.",
                    token.getApiName(), token.getAgeMinutes());
        }
        return fresh;
    }

    /**
     * Validates if a token string is non-null and non-empty.
     * 
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String token) {
        return token != null && !token.isEmpty();
    }

    /**
     * Gets the preferred API name.
     * 
     * @return the preferred API name
     */
    public String getPreferredApiName() {
        return preferredApiName;
    }

    /**
     * Gets access token with health validation.
     * 
     * <p>
     * Contract: This method may block for up to 2 seconds while performing
     * a remote health check (calling Upstox profile endpoint).
     * 
     * <p>
     * If health checker is set, this method will:
     * <ul>
     * <li>Get token using normal fallback logic</li>
     * <li>Validate token health before returning</li>
     * <li>Try fallback tokens if primary is unhealthy</li>
     * </ul>
     * 
     * @return validated healthy access token
     * @throws NoHealthyTokenException if no healthy token is available
     */
    public String getAccessTokenWithHealthCheck() throws NoHealthyTokenException {
        return getAccessTokenWithHealthCheck(com.vegatrader.upstox.auth.TokenCapability.CORE_REST);
    }

    /**
     * Gets healthy access token with capability-specific validation.
     */
    public String getAccessTokenWithHealthCheck(com.vegatrader.upstox.auth.TokenCapability capability)
            throws NoHealthyTokenException {
        if (healthChecker == null) {
            return getAccessToken(capability); // No health checking, use hardened flow
        }

        logger.debug("Requesting access token with health validation");

        // Try preferred API with health check
        UpstoxTokenEntity token = tokenStorageService.getToken(preferredApiName).orElse(null);
        if (token != null && token.isActive() && healthChecker.isTokenHealthy(token.getAccessToken())) {
            logger.info("✓ Using healthy {} token", preferredApiName);
            return token.getAccessToken();
        }

        // Try WEBSOCKET fallbacks with health check
        if (preferredApiName.startsWith("WEBSOCKET")) {
            for (int i = 1; i <= 3; i++) {
                String wsName = "WEBSOCKET" + i;
                if (!wsName.equals(preferredApiName)) {
                    token = tokenStorageService.getToken(wsName).orElse(null);
                    if (token != null && token.isActive() && healthChecker.isTokenHealthy(token.getAccessToken())) {
                        logger.info("✓ Using healthy fallback token: {}", wsName);
                        return token.getAccessToken();
                    }
                }
            }
        } else {
            for (int i = 1; i <= 3; i++) {
                String wsName = "WEBSOCKET" + i;
                token = tokenStorageService.getToken(wsName).orElse(null);
                if (token != null && token.isActive() && healthChecker.isTokenHealthy(token.getAccessToken())) {
                    logger.info("✓ Using healthy fallback WEBSOCKET token: {}", wsName);
                    return token.getAccessToken();
                }
            }
        }

        // Try PRIMARY with health check
        if (!"PRIMARY".equals(preferredApiName)) {
            token = tokenStorageService.getToken("PRIMARY").orElse(null);
            if (token != null && token.isActive() && healthChecker.isTokenHealthy(token.getAccessToken())) {
                logger.warn("⚠ Using healthy PRIMARY token as last resort");
                return token.getAccessToken();
            }
        }

        // No healthy token found
        String errorMsg = "No healthy access token available (all tokens failed health check)";
        logger.error(errorMsg);
        throw new NoHealthyTokenException(errorMsg);
    }

    /**
     * Checks if an active token exists for the preferred API.
     * 
     * @return true if active token exists
     */
    public boolean hasActiveToken() {
        try {
            getAccessToken();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Checks if a healthy token exists (requires health checker).
     * 
     * @return true if healthy token exists
     */
    public boolean hasHealthyToken() {
        if (healthChecker == null) {
            return hasActiveToken(); // Fallback to active check
        }

        try {
            getAccessTokenWithHealthCheck();
            return true;
        } catch (NoHealthyTokenException | RuntimeException e) {
            return false;
        }
    }
}
