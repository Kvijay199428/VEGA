package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.repository.TokenRepository;
import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.integration.AuthenticationOrchestrator;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.service.ApiCredentialsDiscovery.UpstoxAppCredentials;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Batch Authentication Service.
 * Orchestrates authentication for all discovered Upstox APIs.
 *
 * @since 2.0.0
 */
@Service
public class BatchAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(BatchAuthenticationService.class);
    private static final DateTimeFormatter VALIDITY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ApiCredentialsDiscovery apiDiscovery;
    private final TokenStorageService tokenStorageService;
    private final TokenRepository tokenRepository;
    private final TokenCacheService tokenCacheService;
    private final CooldownService cooldownService;

    @Value("${upstox.redirect-uri:http://localhost:28020/api/v1/auth/upstox/callback}")
    private String redirectUri;

    @Value("${upstox.auth.auto.username:}")
    private String autoUsername;

    @Value("${upstox.auth.auto.password:}")
    private String autoPassword;

    @Value("${upstox.auth.auto.totp-secret:}")
    private String autoTotpSecret;

    // Progress tracking
    private final AtomicInteger generatedTokenCount = new AtomicInteger(0);
    private volatile boolean authenticationInProgress = false;
    private volatile String currentApiName = "";

    public BatchAuthenticationService(ApiCredentialsDiscovery apiDiscovery,
            TokenStorageService tokenStorageService,
            TokenRepository tokenRepository,
            TokenCacheService tokenCacheService,
            CooldownService cooldownService) {
        this.apiDiscovery = apiDiscovery;
        this.tokenStorageService = tokenStorageService;
        this.tokenRepository = tokenRepository;
        this.tokenCacheService = tokenCacheService;
        this.cooldownService = cooldownService;
    }

    /**
     * Result of batch authentication.
     */
    public static class BatchAuthResult {
        private final int totalApis;
        private final int successfulTokens;
        private final List<String> errors;
        private final boolean fullyAuthenticated;

        public BatchAuthResult(int totalApis, int successfulTokens, List<String> errors) {
            this.totalApis = totalApis;
            this.successfulTokens = successfulTokens;
            this.errors = errors;
            this.fullyAuthenticated = successfulTokens == totalApis;
        }

        public int getTotalApis() {
            return totalApis;
        }

        public int getSuccessfulTokens() {
            return successfulTokens;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean isFullyAuthenticated() {
            return fullyAuthenticated;
        }
    }

    /**
     * Authenticate all discovered APIs sequentially.
     * Uses single Selenium session with user credentials reused.
     */
    public BatchAuthResult authenticateAllApis(boolean headless) {
        List<UpstoxAppCredentials> apps = apiDiscovery.getAllApps();

        if (apps.isEmpty()) {
            logger.error("No APIs discovered - cannot authenticate");
            return new BatchAuthResult(0, 0, List.of("No APIs configured"));
        }

        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  BATCH AUTHENTICATION - {} APIs  ║", apps.size());
        logger.info("╚═══════════════════════════════════════════════════════╝");

        authenticationInProgress = true;
        generatedTokenCount.set(0);
        List<String> errors = new ArrayList<>();

        // Create shared credentials
        LoginCredentials credentials = new LoginCredentials(
                autoUsername,
                autoPassword,
                autoTotpSecret);

        // Create Selenium config
        SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", headless);

        try {
            for (UpstoxAppCredentials app : apps) {
                currentApiName = app.getPurpose();
                logger.info("────────────────────────────────────────────────────");
                logger.info("[{}/{}] Authenticating: {}",
                        generatedTokenCount.get() + 1, apps.size(), app.getPurpose());
                logger.info("────────────────────────────────────────────────────");

                try {
                    // Create orchestrator for this API
                    AuthenticationOrchestrator orchestrator = new AuthenticationOrchestrator(
                            seleniumConfig, tokenStorageService);

                    // Authenticate
                    TokenResponse token = orchestrator.authenticate(
                            app.getPurpose(),
                            app.getClientId(),
                            app.getClientSecret(),
                            redirectUri,
                            credentials,
                            app.getIndex() == 0 // isPrimary
                    );

                    if (token != null && token.getAccessToken() != null) {
                        generatedTokenCount.incrementAndGet();
                        logger.info("✓ Token generated for {} ({}/{})",
                                app.getPurpose(), generatedTokenCount.get(), apps.size());
                    } else {
                        errors.add(app.getPurpose() + ": Token response was null");
                        logger.error("✗ Failed to get token for {}", app.getPurpose());
                    }

                } catch (Exception e) {
                    errors.add(app.getPurpose() + ": " + e.getMessage());
                    logger.error("✗ Authentication failed for {}: {}", app.getPurpose(), e.getMessage());
                }
            }
        } finally {
            authenticationInProgress = false;
            currentApiName = "";
        }

        int successCount = generatedTokenCount.get();

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("BATCH AUTHENTICATION COMPLETE: {}/{} tokens generated", successCount, apps.size());
        if (!errors.isEmpty()) {
            logger.warn("Errors encountered: {}", errors);
        }
        logger.info("═══════════════════════════════════════════════════════");

        return new BatchAuthResult(apps.size(), successCount, errors);
    }

    /**
     * Get current authentication status.
     * Includes cache status, cooldown info, valid tokens list, and missing APIs.
     * 
     * Authentication is based on PRIMARY token validity (MANDATORY).
     * Other tokens (WEBSOCKET_x, OPTION_CHAIN_x) are OPTIONAL.
     */
    public AuthStatus getStatus() {
        TokenCacheService.CacheStatus cacheStatus = tokenCacheService.getStatus();
        CooldownService.CooldownStatus cooldownStatus = cooldownService.getStatus();

        // Get valid tokens and missing APIs
        List<String> validTokens = getValidTokenNames();
        List<String> missingApis = getMissingApiNames(validTokens);
        int generated = validTokens.size();

        // Configured APIs: use discovery count OR fallback to 6 (standard config)
        int configuredApis = apiDiscovery.getAppCount();
        if (configuredApis == 0) {
            configuredApis = 6; // Fallback: PRIMARY + 3 WEBSOCKET + 2 OPTION_CHAIN
        }

        // PRIMARY token determines dashboard access (MANDATORY token)
        boolean primaryReady = validTokens.contains("PRIMARY");

        // Fully ready means ALL configured APIs have valid tokens
        boolean fullyReady = generated == configuredApis && configuredApis > 0;

        // Can proceed to dashboard if PRIMARY is valid
        boolean canProceed = primaryReady;

        // Can generate remaining ONLY if PRIMARY is ready AND there are missing tokens
        boolean canGenerateRemaining = primaryReady && !missingApis.isEmpty();

        return new AuthStatus(
                configuredApis,
                generated,
                primaryReady, // authenticated = primaryReady for backward compatibility
                authenticationInProgress,
                currentApiName,
                cacheStatus.isDbLocked(),
                cacheStatus.getPendingInCache(),
                cacheStatus.isRecoveryInProgress(),
                validTokens,
                missingApis,
                cooldownStatus.isActive(),
                cooldownStatus.getEndsAt(),
                cooldownStatus.getMessage(),
                primaryReady,
                fullyReady,
                canProceed,
                canGenerateRemaining);
    }

    /**
     * Get list of valid token names (API names with active, non-expired tokens).
     */
    private List<String> getValidTokenNames() {
        List<String> validNames = new ArrayList<>();
        try {
            List<UpstoxTokenEntity> activeTokens = tokenRepository.findAllActive();
            for (UpstoxTokenEntity token : activeTokens) {
                if (!isTokenExpired(token)) {
                    validNames.add(token.getApiName());
                }
            }
        } catch (Exception e) {
            logger.warn("Error fetching valid tokens: {}", e.getMessage());
        }
        return validNames;
    }

    /**
     * Get list of missing API names (required but not yet authenticated).
     */
    private List<String> getMissingApiNames(List<String> validTokens) {
        List<String> missingApis = new ArrayList<>();
        for (ApiCredentialsDiscovery.UpstoxAppCredentials app : apiDiscovery.getAllApps()) {
            if (!validTokens.contains(app.getPurpose())) {
                missingApis.add(app.getPurpose());
            }
        }
        return missingApis;
    }

    /**
     * Count valid tokens (DB + cache).
     */
    private int countValidTokens() {
        int dbCount = 0;
        try {
            List<UpstoxTokenEntity> activeTokens = tokenRepository.findAllActive();
            dbCount = (int) activeTokens.stream()
                    .filter(token -> !isTokenExpired(token))
                    .count();
        } catch (Exception e) {
            logger.warn("Error counting DB tokens: {}", e.getMessage());
        }

        // Add cached tokens
        int cacheCount = tokenCacheService.getPendingCount();

        return dbCount + cacheCount;
    }

    /**
     * Check if token is expired based on validityAt field.
     * Format: "2025-12-31 03:30:00"
     */
    private boolean isTokenExpired(UpstoxTokenEntity token) {
        String validityAt = token.getValidityAt();
        if (validityAt == null || validityAt.isEmpty()) {
            return true; // No validity = expired
        }
        try {
            LocalDateTime expiryTime = LocalDateTime.parse(validityAt, VALIDITY_FORMATTER);
            return LocalDateTime.now().isAfter(expiryTime);
        } catch (Exception e) {
            logger.warn("Error parsing validityAt '{}': {}", validityAt, e.getMessage());
            return true; // Assume expired if can't parse
        }
    }

    /**
     * Get cooldown service (for triggering cooldown from controllers).
     */
    public CooldownService getCooldownService() {
        return cooldownService;
    }

    /**
     * Authentication status DTO.
     * Includes database lock, cache status, valid tokens, missing APIs, cooldown,
     * and new fields for dashboard access control.
     * 
     * Authentication model:
     * - MANDATORY: PRIMARY token (required for dashboard access)
     * - OPTIONAL: WEBSOCKET_x, OPTION_CHAIN_x (can be generated in background)
     */
    public static class AuthStatus {
        private final int configuredApis; // Total configured APIs
        private final int generatedTokens;
        private final boolean authenticated; // @deprecated - use primaryReady
        private final boolean inProgress;
        private final String currentApi;
        private final boolean dbLocked;
        private final int pendingInCache;
        private final boolean recoveryInProgress;
        private final List<String> validTokens;
        private final List<String> missingApis;
        private final boolean rateLimited;
        private final Long cooldownEndsAt;
        private final String cooldownMessage;
        // Explicit auth state fields
        private final boolean primaryReady; // PRIMARY token valid
        private final boolean fullyReady; // ALL tokens valid
        private final boolean canProceed; // Can access dashboard
        private final boolean canGenerateRemaining; // Has missing tokens

        public AuthStatus(int configuredApis, int generatedTokens, boolean authenticated,
                boolean inProgress, String currentApi,
                boolean dbLocked, int pendingInCache, boolean recoveryInProgress,
                List<String> validTokens, List<String> missingApis,
                boolean rateLimited, Long cooldownEndsAt, String cooldownMessage,
                boolean primaryReady, boolean fullyReady, boolean canProceed, boolean canGenerateRemaining) {
            this.configuredApis = configuredApis;
            this.generatedTokens = generatedTokens;
            this.authenticated = authenticated;
            this.inProgress = inProgress;
            this.currentApi = currentApi;
            this.dbLocked = dbLocked;
            this.pendingInCache = pendingInCache;
            this.recoveryInProgress = recoveryInProgress;
            this.validTokens = validTokens;
            this.missingApis = missingApis;
            this.rateLimited = rateLimited;
            this.cooldownEndsAt = cooldownEndsAt;
            this.cooldownMessage = cooldownMessage;
            this.primaryReady = primaryReady;
            this.fullyReady = fullyReady;
            this.canProceed = canProceed;
            this.canGenerateRemaining = canGenerateRemaining;
        }

        /** @deprecated Use getConfiguredApis() instead */
        @Deprecated
        public int getRequiredTokens() {
            return configuredApis;
        }

        public int getConfiguredApis() {
            return configuredApis;
        }

        public int getGeneratedTokens() {
            return generatedTokens;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public boolean isInProgress() {
            return inProgress;
        }

        public String getCurrentApi() {
            return currentApi;
        }

        public boolean isDbLocked() {
            return dbLocked;
        }

        public int getPendingInCache() {
            return pendingInCache;
        }

        public boolean isRecoveryInProgress() {
            return recoveryInProgress;
        }

        public List<String> getValidTokens() {
            return validTokens;
        }

        public List<String> getMissingApis() {
            return missingApis;
        }

        public boolean isRateLimited() {
            return rateLimited;
        }

        public Long getCooldownEndsAt() {
            return cooldownEndsAt;
        }

        public String getCooldownMessage() {
            return cooldownMessage;
        }

        public boolean isPrimaryReady() {
            return primaryReady;
        }

        public boolean isFullyReady() {
            return fullyReady;
        }

        public boolean isCanProceed() {
            return canProceed;
        }

        public boolean isCanGenerateRemaining() {
            return canGenerateRemaining;
        }
    }
}
