package com.vegatrader.upstox.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CooldownService - Manages API rate limit cooldown state.
 * 
 * When a 429 (rate limit) error is encountered, the system enters
 * an 11-minute cooldown period during which token generation is halted.
 * 
 * This is the single source of truth for cooldown state.
 * 
 * @since 2.0.0
 */
@Service
public class CooldownService {

    private static final Logger logger = LoggerFactory.getLogger(CooldownService.class);

    /**
     * Default cooldown duration: 11 minutes (660 seconds).
     */
    public static final long COOLDOWN_DURATION_MS = 11 * 60 * 1000; // 11 minutes

    private final AtomicBoolean cooldownActive = new AtomicBoolean(false);
    private final AtomicLong cooldownEndsAt = new AtomicLong(0);
    private final AtomicLong cooldownStartedAt = new AtomicLong(0);

    /**
     * Trigger cooldown (called when 429 rate limit is detected).
     */
    public void triggerCooldown() {
        long now = System.currentTimeMillis();
        cooldownStartedAt.set(now);
        cooldownEndsAt.set(now + COOLDOWN_DURATION_MS);
        cooldownActive.set(true);

        logger.warn("╔═══════════════════════════════════════════════════════╗");
        logger.warn("║  ⚠ RATE LIMIT DETECTED - COOLDOWN ACTIVATED          ║");
        logger.warn("║  Duration: 11 minutes                                 ║");
        logger.warn("║  Resume at: {}                                    ║", formatTime(cooldownEndsAt.get()));
        logger.warn("╚═══════════════════════════════════════════════════════╝");
    }

    /**
     * Trigger cooldown with custom duration.
     */
    public void triggerCooldown(long durationMs) {
        long now = System.currentTimeMillis();
        cooldownStartedAt.set(now);
        cooldownEndsAt.set(now + durationMs);
        cooldownActive.set(true);

        logger.warn("RATE LIMIT - Cooldown for {} seconds", durationMs / 1000);
    }

    /**
     * Check if cooldown is currently active.
     */
    public boolean isTokenGenerationCooldownActive() {
        if (!cooldownActive.get()) {
            return false;
        }

        // Check if cooldown has expired
        if (System.currentTimeMillis() >= cooldownEndsAt.get()) {
            cooldownActive.set(false);
            logger.info("✓ Cooldown expired - Token generation resumed");
            return false;
        }

        return true;
    }

    /**
     * Get remaining cooldown time in seconds.
     */
    public long remainingSeconds() {
        if (!isTokenGenerationCooldownActive()) {
            return 0;
        }
        long remaining = cooldownEndsAt.get() - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    /**
     * Get remaining cooldown time in milliseconds.
     */
    public long remainingMillis() {
        if (!isTokenGenerationCooldownActive()) {
            return 0;
        }
        return Math.max(0, cooldownEndsAt.get() - System.currentTimeMillis());
    }

    /**
     * Get cooldown end timestamp (epoch milliseconds).
     */
    public long getCooldownEndsAt() {
        return cooldownEndsAt.get();
    }

    /**
     * Get formatted cooldown message (e.g., "10m 32s").
     */
    public String getCooldownMessage() {
        long seconds = remainingSeconds();
        if (seconds <= 0) {
            return null;
        }
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%dm %02ds", minutes, secs);
    }

    /**
     * Clear cooldown (for testing or manual reset).
     */
    public void clearCooldown() {
        cooldownActive.set(false);
        cooldownEndsAt.set(0);
        cooldownStartedAt.set(0);
        logger.info("Cooldown cleared manually");
    }

    /**
     * Get current cooldown status.
     */
    public CooldownStatus getStatus() {
        return new CooldownStatus(
                isTokenGenerationCooldownActive(),
                cooldownEndsAt.get(),
                remainingSeconds(),
                getCooldownMessage());
    }

    private String formatTime(long epochMs) {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(epochMs));
    }

    /**
     * Cooldown status DTO.
     */
    public static class CooldownStatus {
        private final boolean active;
        private final long endsAt;
        private final long remainingSeconds;
        private final String message;

        public CooldownStatus(boolean active, long endsAt, long remainingSeconds, String message) {
            this.active = active;
            this.endsAt = endsAt;
            this.remainingSeconds = remainingSeconds;
            this.message = message;
        }

        public boolean isActive() {
            return active;
        }

        public long getEndsAt() {
            return endsAt;
        }

        public long getRemainingSeconds() {
            return remainingSeconds;
        }

        public String getMessage() {
            return message;
        }
    }
}
