package com.vegatrader.upstox.auth.service;

import java.util.List;

/**
 * Result from async token orchestrator.
 * 
 * Exit codes:
 * 0 = All valid / success
 * 1 = Partial regenerated
 * 2 = CAPTCHA encountered
 * 3 = Fatal failure
 *
 * @since 2.3.0
 */
public class TokenExecutionResult {

    private boolean success;
    private boolean noop; // Fast path - no action needed
    private TokenDecisionReport report;
    private List<TokenGenerationService.GenerationResult> generationResults;
    private int successCount;
    private int failedCount;
    private long durationMs;
    private String errorMessage;

    public TokenExecutionResult() {
    }

    public TokenExecutionResult(boolean success, TokenDecisionReport report,
            List<TokenGenerationService.GenerationResult> generationResults,
            int successCount, int failedCount) {
        this.success = success;
        this.noop = false;
        this.report = report;
        this.generationResults = generationResults;
        this.successCount = successCount;
        this.failedCount = failedCount;
    }

    public static TokenExecutionResult noop(TokenDecisionReport report) {
        TokenExecutionResult result = new TokenExecutionResult();
        result.success = true;
        result.noop = true;
        result.report = report;
        result.successCount = 0;
        result.failedCount = 0;
        return result;
    }

    public static TokenExecutionResult failure(String errorMessage) {
        TokenExecutionResult result = new TokenExecutionResult();
        result.success = false;
        result.noop = false;
        result.errorMessage = errorMessage;
        return result;
    }

    /**
     * Get CLI exit code.
     */
    public int getExitCode() {
        if (success && noop)
            return 0; // All valid
        if (success)
            return 0; // Success
        if (failedCount > 0 && successCount > 0)
            return 1; // Partial
        if (errorMessage != null && errorMessage.contains("CAPTCHA"))
            return 2;
        return 3; // Fatal
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isNoop() {
        return noop;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    public TokenDecisionReport getReport() {
        return report;
    }

    public void setReport(TokenDecisionReport report) {
        this.report = report;
    }

    public List<TokenGenerationService.GenerationResult> getGenerationResults() {
        return generationResults;
    }

    public void setGenerationResults(List<TokenGenerationService.GenerationResult> results) {
        this.generationResults = results;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "TokenExecutionResult{" +
                "success=" + success +
                ", noop=" + noop +
                ", successCount=" + successCount +
                ", failedCount=" + failedCount +
                ", durationMs=" + durationMs +
                '}';
    }
}
