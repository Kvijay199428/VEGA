package com.vegatrader.upstox.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Cooldown Manager - Zero retry, broker-safe 11-minute halt.
 * 
 * When broker throttling is detected:
 * 1. Stop Selenium immediately
 * 2. Wait 11 minutes (includes safety buffer)
 * 3. Resume from exact failure point
 *
 * @since 2.4.0
 */
public class CooldownManager {

    private static final Logger logger = LoggerFactory.getLogger(CooldownManager.class);
    private static final Logger cooldownLogger = LoggerFactory.getLogger("cooldown.events");

    // 11 minutes cooldown (10 min broker limit + 1 min buffer)
    public static final Duration COOLDOWN_DURATION = Duration.ofMinutes(11);
    public static final long COOLDOWN_MILLIS = COOLDOWN_DURATION.toMillis();

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Kolkata"));

    /**
     * Enforce cooldown period.
     * Blocks until cooldown is complete.
     */
    public void enforce(TokenExecutionState state) {
        long now = System.currentTimeMillis();
        long elapsedSinceFailure = now - state.getLastFailureEpoch();
        long remainingWait = COOLDOWN_MILLIS - elapsedSinceFailure;

        if (remainingWait <= 0) {
            logger.info("Cooldown period already elapsed. Ready to resume.");
            return;
        }

        Instant cooldownUntil = Instant.ofEpochMilli(now + remainingWait);
        String cooldownUntilStr = TIME_FORMAT.format(cooldownUntil);

        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║              BROKER COOLDOWN ACTIVE                           ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");
        logger.info("Last failed API: {}", state.getNextApiToGenerate());
        logger.info("Last success API: {}", state.getLastSuccessfulApi());
        logger.info("Cooldown until: {}", cooldownUntilStr);
        logger.info("Remaining wait: {} minutes {} seconds",
                remainingWait / 60000, (remainingWait % 60000) / 1000);

        // Log to cooldown events log
        cooldownLogger.info("[COOLDOWN_ENFORCED]");
        cooldownLogger.info("API_FAILED     : {}", state.getNextApiToGenerate());
        cooldownLogger.info("LAST_SUCCESS   : {}", state.getLastSuccessfulApi());
        cooldownLogger.info("COOLDOWN_UNTIL : {}", cooldownUntilStr);
        cooldownLogger.info("REMAINING_MS   : {}", remainingWait);

        try {
            // Log every minute during wait
            while (remainingWait > 0) {
                long sleepTime = Math.min(60000, remainingWait);
                Thread.sleep(sleepTime);
                remainingWait -= sleepTime;
                if (remainingWait > 0) {
                    logger.info("Cooldown: {} minutes remaining...", remainingWait / 60000 + 1);
                }
            }

            logger.info("╔═══════════════════════════════════════════════════════════════╗");
            logger.info("║              COOLDOWN COMPLETE - RESUMING                     ║");
            logger.info("╚═══════════════════════════════════════════════════════════════╝");

            cooldownLogger.info("[COOLDOWN_COMPLETE]");
            cooldownLogger.info("RESUME_FROM    : {}", state.getNextApiToGenerate());

        } catch (InterruptedException e) {
            logger.warn("Cooldown interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Start cooldown for a failed API.
     */
    public void startCooldown(TokenExecutionState state, String failedApi, String lastSuccessApi) {
        state.setStatus(TokenExecutionState.ExecutionStatus.COOLDOWN);
        state.setLastFailureEpoch(System.currentTimeMillis());
        state.setNextApiToGenerate(failedApi);
        state.setLastSuccessfulApi(lastSuccessApi);

        Instant cooldownUntil = Instant.now().plus(COOLDOWN_DURATION);
        String cooldownUntilStr = TIME_FORMAT.format(cooldownUntil);

        cooldownLogger.info("[COOLDOWN_TRIGGERED]");
        cooldownLogger.info("API_FAILED     : {}", failedApi);
        cooldownLogger.info("LAST_SUCCESS   : {}", lastSuccessApi);
        cooldownLogger.info("REASON         : Broker throttling detected");
        cooldownLogger.info("COOLDOWN_UNTIL : {}", cooldownUntilStr);

        logger.warn("⚠ Broker cooldown triggered for: {}. Waiting 11 minutes before resume.", failedApi);
    }

    /**
     * Check if state can resume now.
     */
    public boolean canResumeNow(TokenExecutionState state) {
        return state.canResume(COOLDOWN_MILLIS);
    }

    /**
     * Get remaining cooldown time in human-readable format.
     */
    public String getRemainingTimeFormatted(TokenExecutionState state) {
        long remaining = state.getRemainingCooldownMillis(COOLDOWN_MILLIS);
        if (remaining <= 0)
            return "Ready";
        long minutes = remaining / 60000;
        long seconds = (remaining % 60000) / 1000;
        return String.format("%d min %d sec", minutes, seconds);
    }
}
