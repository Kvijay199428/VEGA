package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Resume Orchestrator - Deterministic resume-from-failure.
 * 
 * Features:
 * - Persists execution state
 * - Enforces 11-minute cooldown
 * - Resumes from exact failure point
 * - Never regenerates successful tokens
 *
 * @since 2.4.0
 */
@Service
public class ResumeOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(ResumeOrchestrator.class);
    private static final Logger resumeLogger = LoggerFactory.getLogger("resume.flow");

    private final ExecutionStateRepository stateRepo;
    private final CooldownManager cooldown;
    private final TokenGenerationService generationService;

    public ResumeOrchestrator(ExecutionStateRepository stateRepo,
            CooldownManager cooldown,
            TokenGenerationService generationService) {
        this.stateRepo = stateRepo;
        this.cooldown = cooldown;
        this.generationService = generationService;
    }

    /**
     * Start fresh execution - generates all tokens with resume capability.
     */
    public TokenExecutionResult executeWithResume() {
        String executionId = UUID.randomUUID().toString();
        TokenExecutionState state = new TokenExecutionState(executionId);

        return executeFromApi(state, ApiName.PRIMARY);
    }

    /**
     * Resume from a specific API.
     */
    public TokenExecutionResult resumeFromState(TokenExecutionState state) {
        if (state == null) {
            logger.warn("No state to resume from. Starting fresh.");
            return executeWithResume();
        }

        if (state.isInCooldown()) {
            cooldown.enforce(state);
        }

        state.setStatus(TokenExecutionState.ExecutionStatus.RESUMING);
        stateRepo.save(state);

        String nextApi = state.getNextApiToGenerate();
        if (nextApi == null) {
            logger.info("No next API to generate. Execution complete.");
            state.setStatus(TokenExecutionState.ExecutionStatus.COMPLETED);
            stateRepo.save(state);
            return TokenExecutionResult.noop(null);
        }

        ApiName startFrom = ApiName.fromString(nextApi);
        return executeFromApi(state, startFrom);
    }

    /**
     * Execute token generation starting from a specific API.
     */
    private TokenExecutionResult executeFromApi(TokenExecutionState state, ApiName startFrom) {
        List<ApiName> apisToGenerate = getRemainingApis(startFrom);

        resumeLogger.info("[RESUME_STARTED]");
        resumeLogger.info("Execution ID   : {}", state.getExecutionId());
        resumeLogger.info("Starting from  : {}", startFrom);
        resumeLogger.info("Remaining APIs : {}", apisToGenerate);

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("        RESUME ORCHESTRATOR - STARTED");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Execution ID: {}", state.getExecutionId());
        logger.info("Generating {} tokens: {}", apisToGenerate.size(), apisToGenerate);

        state.setStatus(TokenExecutionState.ExecutionStatus.RUNNING);
        stateRepo.save(state);

        int successCount = 0;
        int failedCount = 0;
        String lastSuccess = state.getLastSuccessfulApi();

        for (ApiName api : apisToGenerate) {
            try {
                logger.info("→ Generating: {}", api);

                List<TokenGenerationService.GenerationResult> results = generationService.generatePartial(List.of(api));

                if (!results.isEmpty() && results.get(0).isSuccess()) {
                    logger.info("✓ Generated: {}", api);
                    lastSuccess = api.name();
                    successCount++;

                    // Update state after each success
                    state.setLastSuccessfulApi(api.name());
                    state.setNextApiToGenerate(getNextApiName(api));
                    stateRepo.save(state);
                } else {
                    logger.warn("✗ Failed: {} - {}", api,
                            results.isEmpty() ? "No result" : results.get(0).getMessage());
                    failedCount++;
                }

            } catch (Exception e) {
                // Check if this is a broker throttling error
                if (isBrokerCooldownError(e)) {
                    logger.warn("⚠ Broker throttling detected for: {}", api);

                    cooldown.startCooldown(state, api.name(), lastSuccess);
                    stateRepo.save(state);

                    // Enforce cooldown and resume
                    cooldown.enforce(state);

                    // Recursive resume from failed API
                    return resumeFromState(state);
                } else {
                    logger.error("✗ Error generating {}: {}", api, e.getMessage());
                    failedCount++;
                }
            }
        }

        // Mark completed
        state.setStatus(TokenExecutionState.ExecutionStatus.COMPLETED);
        state.setNextApiToGenerate(null);
        stateRepo.save(state);

        resumeLogger.info("[RESUME_COMPLETED]");
        resumeLogger.info("Success count  : {}", successCount);
        resumeLogger.info("Failed count   : {}", failedCount);

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("        RESUME ORCHESTRATOR - COMPLETE");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Success: {}, Failed: {}", successCount, failedCount);

        return new TokenExecutionResult(
                failedCount == 0,
                null, null,
                successCount, failedCount);
    }

    /**
     * Get remaining APIs starting from a specific one.
     */
    private List<ApiName> getRemainingApis(ApiName startFrom) {
        ApiName[] all = ApiName.values();
        int startIndex = startFrom.ordinal();
        return Arrays.asList(Arrays.copyOfRange(all, startIndex, all.length));
    }

    /**
     * Get next API name after the given one.
     */
    private String getNextApiName(ApiName current) {
        int nextIndex = current.ordinal() + 1;
        ApiName[] all = ApiName.values();
        if (nextIndex >= all.length) {
            return null;
        }
        return all[nextIndex].name();
    }

    /**
     * Check if exception is a broker cooldown error.
     */
    private boolean isBrokerCooldownError(Exception e) {
        if (e instanceof BrokerCooldownException) {
            return true;
        }
        String message = e.getMessage();
        if (message == null)
            return false;

        // Timeout errors that indicate broker throttling
        return message.contains("TimeoutException") ||
                message.contains("waiting for") ||
                message.contains("pinCode") ||
                message.contains("redirect") ||
                message.contains("authorization code");
    }

    /**
     * Check if there's a resumable state.
     */
    public TokenExecutionState getResumableState() {
        TokenExecutionState latest = stateRepo.getLatest();
        if (latest == null)
            return null;

        // Only resumable if in COOLDOWN or has next API
        if (latest.getStatus() == TokenExecutionState.ExecutionStatus.COOLDOWN ||
                (latest.getNextApiToGenerate() != null &&
                        latest.getStatus() != TokenExecutionState.ExecutionStatus.COMPLETED)) {
            return latest;
        }
        return null;
    }
}
