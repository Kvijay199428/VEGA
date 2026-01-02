package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.repository.TokenRepository;
import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.integration.AuthenticationOrchestrator;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.service.ApiCredentialsDiscovery.UpstoxAppCredentials;
import com.vegatrader.upstox.auth.config.AuthConstants;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.event.AuthProgressEvent;
import com.vegatrader.upstox.auth.event.AuthProgressPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.stream.Collectors;
import com.vegatrader.util.time.LocaleConstants;
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
    private final AuthProgressPublisher progressPublisher;
    private final com.vegatrader.upstox.auth.state.AuthSessionState authSessionState;
    private final com.vegatrader.util.time.TimeProvider timeProvider;

    @Value("${upstox.redirect-uri:http://localhost:28020/api/v1/auth/upstox/callback}")
    private String redirectUri;

    // Progress tracking
    private final AtomicInteger generatedTokenCount = new AtomicInteger(0);
    private volatile boolean authenticationInProgress = false;
    private volatile String currentApiName = "";

    public BatchAuthenticationService(ApiCredentialsDiscovery apiDiscovery,
            TokenStorageService tokenStorageService,
            TokenRepository tokenRepository,
            TokenCacheService tokenCacheService,
            CooldownService cooldownService,
            AuthProgressPublisher progressPublisher,
            com.vegatrader.upstox.auth.state.AuthSessionState authSessionState,
            com.vegatrader.util.time.TimeProvider timeProvider) {
        this.apiDiscovery = apiDiscovery;
        this.tokenStorageService = tokenStorageService;
        this.tokenRepository = tokenRepository;
        this.tokenCacheService = tokenCacheService;
        this.cooldownService = cooldownService;
        this.progressPublisher = progressPublisher;
        this.authSessionState = authSessionState;
        this.timeProvider = timeProvider;
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
     * Async batch login: Generate PRIMARY token first, remaining in background.
     * 
     * @param headless Whether to use headless browser
     * @return Result with PRIMARY token generation status
     */
    public BatchAuthResult startBatchLoginAsync(boolean headless) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("ASYNC BATCH LOGIN: PRIMARY first, remaining in background");
        logger.info("═══════════════════════════════════════════════════════");

        // Get all missing APIs
        List<String> validTokens = getValidTokenNames();
        List<ApiCredentialsDiscovery.UpstoxAppCredentials> allApps = apiDiscovery.getAllApps();

        // Separate PRIMARY from remaining
        ApiCredentialsDiscovery.UpstoxAppCredentials primaryApp = null;
        List<ApiCredentialsDiscovery.UpstoxAppCredentials> remainingApps = new ArrayList<>();

        for (ApiCredentialsDiscovery.UpstoxAppCredentials app : allApps) {
            if (!validTokens.contains(app.getPurpose())) {
                if (app.getPurpose().equals("PRIMARY")) {
                    primaryApp = app;
                } else {
                    remainingApps.add(app);
                }
            }
        }

        // Generate PRIMARY token synchronously
        BatchAuthResult primaryResult;
        if (primaryApp != null) {
            logger.info("Step 1: Generating PRIMARY token (synchronous)...");
            List<ApiCredentialsDiscovery.UpstoxAppCredentials> primaryList = new ArrayList<>();
            primaryList.add(primaryApp);
            primaryResult = processAuthLoop(primaryList, headless, new ArrayList<>());
            logger.info("✓ PRIMARY token generation complete");
        } else {
            logger.info("⚠ PRIMARY token already exists, skipping sync generation");
            primaryResult = new BatchAuthResult(0, 0, List.of());
        }

        // Launch background generation for remaining tokens
        if (!remainingApps.isEmpty()) {
            logger.info("Step 2: Launching background generation for {} remaining APIs", remainingApps.size());
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                logger.info("═══ BACKGROUND GENERATION STARTED ═══");
                try {
                    List<String> errors = new ArrayList<>();
                    processAuthLoop(remainingApps, headless, errors);
                    logger.info("═══ BACKGROUND GENERATION COMPLETE ═══");
                    if (!errors.isEmpty()) {
                        logger.warn("Background generation errors: {}", errors);
                    }
                } catch (Exception e) {
                    logger.error("Background generation failed: {}", e.getMessage(), e);
                }
            });
        } else {
            logger.info("No remaining tokens to generate in background");
        }

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("ASYNC BATCH LOGIN: PRIMARY complete, background launched");
        logger.info("═══════════════════════════════════════════════════════");

        return primaryResult;
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
        // Create shared credentials from discovery
        LoginCredentials credentials = apiDiscovery.getCommonCredentials();

        // Create Selenium config
        SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", headless);

        try {
            for (ApiCredentialsDiscovery.UpstoxAppCredentials app : appsToProcess) {
                currentApiName = app.getPurpose();
                logger.info("────────────────────────────────────────────────────");
                logger.info("[{}/{}] Authenticating: {}",
                        generatedTokenCount.get() + 1, appsToProcess.size(), app.getPurpose());
                logger.info("────────────────────────────────────────────────────");

                // Emit STARTED
                progressPublisher.emit(new AuthProgressEvent(
                        currentApiName,
                        "STARTED",
                        getValidTokenNames().size(),
                        AuthConstants.TOTAL_UPSTOX_APIS,
                        timeProvider.now(),
                        null));

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

                        // Update AuthSessionState immediately
                        authSessionState.registerValidToken(app.getPurpose());

                        logger.info("✓ Token generated for {} in {}ms ({}/{})",
                                app.getPurpose(), tokenDuration, generatedTokenCount.get(), appsToProcess.size());

                        // Emit SUCCESS
                        progressPublisher.emit(new AuthProgressEvent(
                                app.getPurpose(),
                                "SUCCESS",
                                getValidTokenNames().size(),
                                AuthConstants.TOTAL_UPSTOX_APIS,
                                timeProvider.now(),
                                null));
                    } else {
                        errors.add(app.getPurpose() + ": Token response was null");
                        logger.error("✗ Failed to get token for {} after {}ms", app.getPurpose(), tokenDuration);

                        // Emit FAILED
                        progressPublisher.emit(new AuthProgressEvent(
                                app.getPurpose(),
                                "FAILED",
                                getValidTokenNames().size(),
                                AuthConstants.TOTAL_UPSTOX_APIS,
                                timeProvider.now(),
                                "Token response was null"));
                    }

                } catch (Exception e) {
                    errors.add(app.getPurpose() + ": " + e.getMessage());
                    logger.error("✗ Authentication failed for {}: {}", app.getPurpose(), e.getMessage());

                    // Emit FAILED
                    progressPublisher.emit(new AuthProgressEvent(
                            app.getPurpose(),
                            "FAILED",
                            getValidTokenNames().size(),
                            AuthConstants.TOTAL_UPSTOX_APIS,
                            timeProvider.now(),
                            e.getMessage()));
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
    private static final List<String> CONFIGURED_API_NAMES = AuthConstants.API_ORDER;

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
                canGenerateRemaining,
                authenticationInProgress && currentApiName != null && !currentApiName.isEmpty()
                        ? "Generating: " + currentApiName
                        : null,
                buildApiProgress(validTokens, currentApiName));
    }

    private List<ApiProgress> buildApiProgress(List<String> validTokens, String currentApiName) {
        List<ApiProgress> progress = new ArrayList<>();
        for (String apiName : CONFIGURED_API_NAMES) {
            boolean complete = validTokens.contains(apiName);
            boolean inProgress = authenticationInProgress && apiName.equals(currentApiName);
            progress.add(new ApiProgress(apiName, complete, inProgress));
        }
        return progress;
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

            // Build map of current configured Client IDs for validation
            Map<String, String> configuredClientIds = apiDiscovery.getAllApps().stream()
                    .collect(Collectors.toMap(
                            ApiCredentialsDiscovery.UpstoxAppCredentials::getPurpose,
                            ApiCredentialsDiscovery.UpstoxAppCredentials::getClientId));

            for (UpstoxTokenEntity token : activeTokens) {
                if (isTokenExpired(token)) {
                    logger.warn("Token expired/invalid: {} validityAt={}", token.getApiName(), token.getValidityAt());
                    continue;
                }

                // Verify that the token in DB matches the currently configured Client ID
                String expectedClientId = configuredClientIds.get(token.getApiName());
                if (expectedClientId == null) {
                    // API might have been removed from config
                    logger.warn("Token found for unknown API: {}. Deleting from DB.", token.getApiName());
                    tokenRepository.delete(token);
                    continue;
                }

                if (!expectedClientId.equals(token.getClientId())) {
                    logger.warn(
                            "Config mismatch for {}: DB has ClientID={}, but Config expects {}. Deleting invalid token.",
                            token.getApiName(), token.getClientId(), expectedClientId);
                    tokenRepository.delete(token);
                    continue;
                }

                validNames.add(token.getApiName());
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
            return timeProvider.now().atZone(LocaleConstants.IST).toLocalDateTime()
                    .isAfter(expiryTime);
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
    public static class ApiProgress {
        private final String apiName;
        private final boolean complete;
        private final boolean inProgress;

        public ApiProgress(String apiName, boolean complete, boolean inProgress) {
            this.apiName = apiName;
            this.complete = complete;
            this.inProgress = inProgress;
        }

        public String getApiName() {
            return apiName;
        }

        public boolean isComplete() {
            return complete;
        }

        public boolean isInProgress() {
            return inProgress;
        }
    }

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
        // Real-time progress fields
        private final String currentOperation; // "Generating: WEBSOCKET_1" or null
        private final List<ApiProgress> apiProgress; // Per-API status

        public AuthStatus(int configuredApis, int generatedTokens, boolean authenticated,
                boolean inProgress, String currentApi,
                boolean dbLocked, int pendingInCache, boolean recoveryInProgress,
                List<String> validTokens, List<String> missingApis,
                boolean rateLimited, Long cooldownEndsAt, String cooldownMessage,
                boolean primaryReady, boolean fullyReady, boolean canProceed, boolean canGenerateRemaining,
                String currentOperation, List<ApiProgress> apiProgress) {
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
            this.currentOperation = currentOperation;
            this.apiProgress = apiProgress;
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

        public String getCurrentOperation() {
            return currentOperation;
        }

        public List<ApiProgress> getApiProgress() {
            return apiProgress;
        }
    }
}
