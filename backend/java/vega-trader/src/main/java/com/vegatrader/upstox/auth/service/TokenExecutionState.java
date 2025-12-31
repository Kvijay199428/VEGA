package com.vegatrader.upstox.auth.service;

/**
 * Token Execution State - Persistent resume brain.
 * 
 * Persisted in SQLite for resume-from-failure capability.
 * This ensures that if a broker cooldown occurs, we can resume
 * from exactly where we left off.
 *
 * @since 2.4.0
 */
public class TokenExecutionState {

    private String executionId;
    private String lastSuccessfulApi;
    private String nextApiToGenerate;
    private long lastFailureEpoch;
    private ExecutionStatus status;

    public enum ExecutionStatus {
        RUNNING,
        COOLDOWN,
        RESUMING,
        COMPLETED
    }

    public TokenExecutionState() {
        this.status = ExecutionStatus.RUNNING;
    }

    public TokenExecutionState(String executionId) {
        this.executionId = executionId;
        this.status = ExecutionStatus.RUNNING;
    }

    /**
     * Check if currently in cooldown.
     */
    public boolean isInCooldown() {
        return status == ExecutionStatus.COOLDOWN;
    }

    /**
     * Check if cooldown period has elapsed.
     */
    public boolean canResume(long cooldownMillis) {
        return System.currentTimeMillis() - lastFailureEpoch >= cooldownMillis;
    }

    /**
     * Calculate remaining cooldown time in milliseconds.
     */
    public long getRemainingCooldownMillis(long cooldownMillis) {
        long elapsed = System.currentTimeMillis() - lastFailureEpoch;
        return Math.max(0, cooldownMillis - elapsed);
    }

    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getLastSuccessfulApi() {
        return lastSuccessfulApi;
    }

    public void setLastSuccessfulApi(String lastSuccessfulApi) {
        this.lastSuccessfulApi = lastSuccessfulApi;
    }

    public String getNextApiToGenerate() {
        return nextApiToGenerate;
    }

    public void setNextApiToGenerate(String nextApiToGenerate) {
        this.nextApiToGenerate = nextApiToGenerate;
    }

    public long getLastFailureEpoch() {
        return lastFailureEpoch;
    }

    public void setLastFailureEpoch(long lastFailureEpoch) {
        this.lastFailureEpoch = lastFailureEpoch;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TokenExecutionState{" +
                "executionId='" + executionId + '\'' +
                ", lastSuccessfulApi='" + lastSuccessfulApi + '\'' +
                ", nextApiToGenerate='" + nextApiToGenerate + '\'' +
                ", status=" + status +
                '}';
    }
}
