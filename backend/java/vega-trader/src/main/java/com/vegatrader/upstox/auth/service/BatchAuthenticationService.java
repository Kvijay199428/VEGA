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
     * Start batch authentication for all missing APIs.
     * Guarded: Skips if PRIMARY token is already valid.
     */
    public BatchAuthResult startBatchLogin(boolean headless) {
        // 1. Identify missing APIs (Skip already valid ones)
        List<String> validTokens = getValidTokenNames();
        List<ApiCredentialsDiscovery.UpstoxAppCredentials> allApps = apiDiscovery.getAllApps();
        List<ApiCredentialsDiscovery.UpstoxAppCredentials> appsToProcess = new ArrayList<>();

        for (ApiCredentialsDiscovery.UpstoxAppCredentials app : allApps) {
            if (!validTokens.contains(app.getPurpose())) {
                appsToProcess.add(app);
            }
        }

        if (appsToProcess.isEmpty()) {
            logger.info("All APIs already authenticated. Skipping login.");
            authenticationInProgress = false;
            return new BatchAuthResult(CONFIGURED_API_NAMES.size(), CONFIGURED_API_NAMES.size(), List.of());
        }

        List<String> missingApiNames = new ArrayList<>();
        for (ApiCredentialsDiscovery.UpstoxAppCredentials app : appsToProcess) {
            missingApiNames.add(app.getPurpose());
        }

        logger.info("Batch authentication started for {} missing APIs: {} (headless={})",
                appsToProcess.size(), missingApiNames, headless);

        authenticationInProgress = true;
        List<String> errors = new ArrayList<>();

        return processAuthLoop(appsToProcess, headless, errors);
    }

    /**
     * Generate only missing tokens (Background Mode).
     * Skips PRIMARY and processes only missing APIs.
     */
    public void generateRemainingTokens(boolean headless) {
        logger.info("Background token generation started (Missing APIs only)");
        authenticationInProgress = true;

        List<String> validTokens = getValidTokenNames();
        List<ApiCredentialsDiscovery.UpstoxAppCredentials> apps = apiDiscovery.getAllApps();

        List<ApiCredentialsDiscovery.UpstoxAppCredentials> missingApps = new ArrayList<>();
        for (ApiCredentialsDiscovery.UpstoxAppCredentials app : apps) {
            if (!validTokens.contains(app.getPurpose())) {
                missingApps.add(app);
            }
        }

        if (missingApps.isEmpty()) {
            logger.info("No missing tokens to generate.");
            authenticationInProgress = false;
            return;
        }

        // Run async or blocking? The controller will wrap this in async usually,
        // or we can just run the loop here if the caller expects it.
        // For simplicity reusing the logic.
        processAuthLoop(missingApps, headless, new ArrayList<>());
    }

    private BatchAuthResult processAuthLoop(List<ApiCredentialsDiscovery.UpstoxAppCredentials> appsToProcess,
            boolean headless, List<String> errors) {
        authenticationInProgress = true;
        generatedTokenCount.set(0);

        // Create shared credentials
        LoginCredentials credentials = new LoginCredentials(
                autoUsername,
                autoPassword,
                autoTotpSecret);

        // Create Selenium config
        SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", headless);

        try {
            for (ApiCredentialsDiscovery.UpstoxAppCredentials app : appsToProcess) {
                currentApiName = app.getPurpose();
                logger.info("────────────────────────────────────────────────────");
                logger.info("[{}/{}] Authenticating: {}",
                        generatedTokenCount.get() + 1, appsToProcess.size(), app.getPurpose());
                logger.info("────────────────────────────────────────────────────");

                try {
                    // Create orchestrator for this API
                    AuthenticationOrchestrator orchestrator = new AuthenticationOrchestrator(
                            seleniumConfig, tokenStorageService);

                    // Authenticate with timing
                    long tokenStartTime = System.currentTimeMillis();
                    TokenResponse token = orchestrator.authenticate(
                            app.getPurpose(),
                            app.getClientId(),
                            app.getClientSecret(),
                            redirectUri,
                            credentials,
                            app.getIndex() == 0 // isPrimary
                    );
                    long tokenDuration = System.currentTimeMillis() - tokenStartTime;

                    if (token != null && token.getAccessToken() != null) {
                        generatedTokenCount.incrementAndGet();
                        logger.info("✓ Token generated for {} in {}ms ({}/{})",
                                app.getPurpose(), tokenDuration, generatedTokenCount.get(), appsToProcess.size());
                    } else {
                        errors.add(app.getPurpose() + ": Token response was null");
                        logger.error("✗ Failed to get token for {} after {}ms", app.getPurpose(), tokenDuration);
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
        logger.info("BATCH AUTHENTICATION COMPLETE: {}/{} tokens generated", successCount, appsToProcess.size());
        if (!errors.isEmpty()) {
            logger.warn("Errors encountered: {}", errors);
        }
        logger.info("═══════════════════════════════════════════════════════");

        return new BatchAuthResult(appsToProcess.size(), successCount, errors);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONFIGURED API NAMES (AUTHORITATIVE SOURCE OF TRUTH)
    // ═══════════════════════════════════════════════════════════════════════════
    // These are the 6 APIs that MUST be configured for full functionality.
    // This is the SINGLE source of truth - not environment variables, not database.
    private static final List<String> CONFIGURED_API_NAMES = List.of(
            "PRIMARY", // Main trading API (MANDATORY)
            "WEBSOCKET_1", // Market data stream 1
            "WEBSOCKET_2", // Market data stream 2
            "WEBSOCKET_3", // Market data stream 3
            "OPTION_CHAIN_1", // Option chain data 1
            "OPTION_CHAIN_2" // Option chain data 2
    );

    /**
     * Get list of all configured API names.
     * This is the authoritative source of truth for required APIs.
     */
    public List<String> getConfiguredApiNames() {
        return CONFIGURED_API_NAMES;
    }

    /**
     * Get current authentication status.
     * Uses CONFIG-DRIVEN logic: missingApis = configuredApis - validTokens
     * 
     * Authentication model:
     * - primaryReady: PRIMARY token is valid (MANDATORY for dashboard access)
     * - fullyReady: ALL configured APIs have valid tokens
     * - canProceed: Same as primaryReady
     * - canGenerateRemaining: primaryReady AND missing tokens exist
     */
    public AuthStatus getStatus() {
        TokenCacheService.CacheStatus cacheStatus = tokenCacheService.getStatus();
        CooldownService.CooldownStatus cooldownStatus = cooldownService.getStatus();

        // Get valid tokens from database
        List<String> validTokens = getValidTokenNames();
        int generated = validTokens.size();

        // AUTHORITATIVE: Use static configured API names
        int configuredApis = CONFIGURED_API_NAMES.size(); // Always 6

        // CORRECT: missingApis = configuredApis - validTokens
        List<String> missingApis = new ArrayList<>();
        for (String apiName : CONFIGURED_API_NAMES) {
            if (!validTokens.contains(apiName)) {
                missingApis.add(apiName);
            }
        }

        // PRIMARY token determines dashboard access (MANDATORY token)
        boolean primaryReady = validTokens.contains("PRIMARY");

        // Fully ready means ALL configured APIs have valid tokens
        boolean fullyReady = missingApis.isEmpty();

        // Can proceed to dashboard if PRIMARY is valid
        boolean canProceed = primaryReady;

        // Can generate remaining ONLY if PRIMARY is ready AND there are missing tokens
        boolean canGenerateRemaining = primaryReady && !missingApis.isEmpty();

        return new AuthStatus(
                configuredApis,
                generated,
                fullyReady, // authenticated = fullyReady (ALL tokens) per production spec
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
     * Check if PRIMARY token is valid.
     */
    public boolean isPrimaryReady() {
        return getValidTokenNames().contains("PRIMARY");
    }

    /**
     * Get list of valid token names (API names with active, non-expired tokens).
     */
    private List<String> getValidTokenNames() {
        List<String> validNames = new ArrayList<>();
        try {
            logger.debug("Fetching valid tokens from repository...");
            List<UpstoxTokenEntity> activeTokens = tokenRepository.findAllActive();
            logger.debug("Found {} active tokens in DB", activeTokens.size());

            for (UpstoxTokenEntity token : activeTokens) {
                if (!isTokenExpired(token)) {
                    validNames.add(token.getApiName());
                } else {
                    logger.warn("Token expired/invalid: {} validityAt={}", token.getApiName(), token.getValidityAt());
                }
            }
        } catch (Exception e) {
            logger.warn("Error fetching valid tokens: {}", e.getMessage());
        }
        logger.debug("Valid tokens count: {}", validNames.size());
        return validNames;
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
