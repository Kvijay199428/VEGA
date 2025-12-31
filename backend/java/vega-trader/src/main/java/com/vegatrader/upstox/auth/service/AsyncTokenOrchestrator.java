package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepository;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepositoryImpl;
import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Async Token Orchestrator - Zero-delay, non-blocking execution.
 * 
 * Features:
 * - Fast-path short-circuit (< 300ms if all valid)
 * - Parallel profile verification (6 threads)
 * - Parallel token generation (max 2 browsers)
 * - Non-blocking operator mode
 *
 * @since 2.3.0
 */
public class AsyncTokenOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTokenOrchestrator.class);

    // 6 threads for parallel profile verification
    private final ExecutorService verifyPool = Executors.newFixedThreadPool(6);

    // SINGLE Selenium browser per c2.md: prevents OTP/PIN session contention
    private final ExecutorService seleniumPool = Executors.newSingleThreadExecutor();

    private final UpstoxTokenRepository tokenRepository;
    private final TokenValidityService validityService;
    private final ProfileVerificationService profileService;
    private final TokenGenerationService generationService;

    public AsyncTokenOrchestrator() {
        this.tokenRepository = new UpstoxTokenRepositoryImpl();
        this.validityService = new TokenValidityService();
        this.profileService = new ProfileVerificationService();
        this.generationService = new TokenGenerationService();
    }

    /**
     * Execute token refresh asynchronously.
     * Caller never blocks unless they explicitly .join()
     */
    public CompletableFuture<TokenExecutionResult> execute(TokenExecutionRequest request) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("        ASYNC TOKEN ORCHESTRATOR - STARTED");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Mode: {}", request.getMode());

        long startTime = System.currentTimeMillis();

        return CompletableFuture
                .supplyAsync(this::loadTokens)
                .thenCompose(this::parallelVerify)
                .thenCompose(report -> decideAndGenerate(report, request))
                .thenApply(result -> finalizeResult(result, startTime))
                .exceptionally(e -> {
                    logger.error("Orchestration failed: {}", e.getMessage());
                    return TokenExecutionResult.failure(e.getMessage());
                });
    }

    /**
     * Execute synchronously (for CLI).
     */
    public TokenExecutionResult executeSync(TokenExecutionRequest request) {
        try {
            return execute(request).get(300, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Sync execution failed: {}", e.getMessage());
            return TokenExecutionResult.failure(e.getMessage());
        }
    }

    /**
     * Load all tokens from database.
     */
    private List<UpstoxTokenEntity> loadTokens() {
        logger.info("Loading tokens from database...");
        List<UpstoxTokenEntity> tokens = tokenRepository.findAll();
        logger.info("✓ Loaded {} tokens", tokens.size());
        return tokens;
    }

    /**
     * Parallel profile verification (5-10x speedup).
     * All 6 tokens verified concurrently.
     */
    private CompletableFuture<TokenDecisionReport> parallelVerify(List<UpstoxTokenEntity> tokens) {
        logger.info("Parallel verification starting...");
        long start = System.currentTimeMillis();

        Map<String, UpstoxTokenEntity> tokenMap = new ConcurrentHashMap<>();
        tokens.forEach(t -> tokenMap.put(t.getApiName(), t));

        List<CompletableFuture<TokenCheckResult>> futures = new ArrayList<>();

        for (ApiName api : ApiName.values()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                UpstoxTokenEntity token = tokenMap.get(api.name());
                if (token == null) {
                    return new TokenCheckResult(api, TokenStatus.MISSING, null);
                }

                // Layer 1: Time check
                boolean timeValid = validityService.isTimeValid(token);
                if (!timeValid) {
                    return new TokenCheckResult(api, TokenStatus.EXPIRED, token);
                }

                // Layer 2: Profile API check
                boolean profileValid = profileService.isValid(token.getAccessToken());
                if (profileValid) {
                    return new TokenCheckResult(api, TokenStatus.VALID, token);
                } else {
                    return new TokenCheckResult(api, TokenStatus.INVALID, token);
                }
            }, verifyPool).orTimeout(3, TimeUnit.SECONDS)
                    .exceptionally(e -> new TokenCheckResult(
                            api, TokenStatus.INVALID, tokenMap.get(api.name()))));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<ApiName> valid = new ArrayList<>();
                    List<ApiName> invalid = new ArrayList<>();
                    List<ApiName> missing = new ArrayList<>();

                    for (CompletableFuture<TokenCheckResult> future : futures) {
                        try {
                            TokenCheckResult result = future.get();
                            switch (result.status) {
                                case VALID -> valid.add(result.api);
                                case INVALID, EXPIRED -> invalid.add(result.api);
                                case MISSING -> missing.add(result.api);
                            }
                        } catch (Exception e) {
                            // Already handled in exceptionally
                        }
                    }

                    long duration = System.currentTimeMillis() - start;
                    logger.info("✓ Parallel verification complete in {} ms", duration);
                    return new TokenDecisionReport(valid, invalid, missing);
                });
    }

    /**
     * Decide and generate tokens.
     * FAST EXIT if all valid.
     */
    private CompletableFuture<TokenExecutionResult> decideAndGenerate(
            TokenDecisionReport report, TokenExecutionRequest request) {

        logger.info("\n{}", report.prettyPrint());

        // FAST PATH: All valid = immediate exit (< 300ms total)
        if (report.allValid()) {
            logger.info("╔═══════════════════════════════════════════════════════════════╗");
            logger.info("║       FAST EXIT: All {} tokens valid. No regeneration.       ║", report.getValidCount());
            logger.info("╚═══════════════════════════════════════════════════════════════╝");
            return CompletableFuture.completedFuture(
                    TokenExecutionResult.noop(report));
        }

        // Determine which APIs to generate
        List<ApiName> apisToGenerate = switch (request.getMode()) {
            case ALL -> List.of(ApiName.values());
            case INVALID_ONLY -> report.getNeedRegeneration();
            case PARTIAL -> request.getApiNames();
        };

        logger.info("Generating {} tokens: {}", apisToGenerate.size(), apisToGenerate);

        // Parallel generation (max 2 concurrent browsers)
        List<CompletableFuture<TokenGenerationService.GenerationResult>> jobs = apisToGenerate.stream()
                .map(api -> CompletableFuture.supplyAsync(
                        () -> generateSingle(api), seleniumPool))
                .toList();

        return CompletableFuture.allOf(jobs.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<TokenGenerationService.GenerationResult> results = new ArrayList<>();
                    int success = 0;
                    int failed = 0;

                    for (CompletableFuture<TokenGenerationService.GenerationResult> job : jobs) {
                        try {
                            TokenGenerationService.GenerationResult result = job.get();
                            results.add(result);
                            if (result.isSuccess())
                                success++;
                            else
                                failed++;
                        } catch (Exception e) {
                            failed++;
                        }
                    }

                    return new TokenExecutionResult(
                            success > 0 && failed == 0,
                            report, results, success, failed);
                });
    }

    /**
     * Generate single token with immediate persist.
     * CAPTCHA on one API does not stall others.
     */
    private TokenGenerationService.GenerationResult generateSingle(ApiName api) {
        try {
            logger.info("→ Generating: {}", api);
            List<TokenGenerationService.GenerationResult> results = generationService.generatePartial(List.of(api));

            if (!results.isEmpty()) {
                TokenGenerationService.GenerationResult result = results.get(0);
                if (result.isSuccess()) {
                    logger.info("✓ Generated: {}", api);
                } else {
                    logger.warn("✗ Failed: {} - {}", api, result.getMessage());
                }
                return result;
            }
            return new TokenGenerationService.GenerationResult(api, false, "No result");
        } catch (Exception e) {
            logger.error("✗ Error generating {}: {}", api, e.getMessage());
            return new TokenGenerationService.GenerationResult(api, false, e.getMessage());
        }
    }

    private TokenExecutionResult finalizeResult(TokenExecutionResult result, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        result.setDurationMs(duration);

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("        ASYNC TOKEN ORCHESTRATOR - COMPLETE");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Duration: {} ms", duration);
        logger.info("Success: {}, Failed: {}", result.getSuccessCount(), result.getFailedCount());

        return result;
    }

    /**
     * Shutdown pools.
     */
    public void shutdown() {
        verifyPool.shutdown();
        seleniumPool.shutdown();
    }

    // Helper classes
    private record TokenCheckResult(ApiName api, TokenStatus status, UpstoxTokenEntity token) {
    }

    private enum TokenStatus {
        VALID, INVALID, EXPIRED, MISSING
    }
}
