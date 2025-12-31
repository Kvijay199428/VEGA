package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Token validity service - Layer 1 (time-based check).
 *
 * @since 2.2.0
 */
public class TokenValidityService {

    private static final Logger logger = LoggerFactory.getLogger(TokenValidityService.class);
    private static final DateTimeFormatter VALIDITY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /**
     * Check if token is valid based on validity_at timestamp.
     */
    public boolean isTimeValid(UpstoxTokenEntity token) {
        if (token == null || token.getValidityAt() == null || token.getValidityAt().isEmpty()) {
            return false;
        }

        try {
            Instant now = Instant.now();
            Instant validity = LocalDateTime
                    .parse(token.getValidityAt(), VALIDITY_FORMAT)
                    .atZone(IST)
                    .toInstant();

            boolean valid = now.isBefore(validity);
            logger.debug("Token {} time validity: {}, expires at: {}",
                    token.getApiName(), valid, token.getValidityAt());
            return valid;

        } catch (Exception e) {
            logger.warn("Failed to parse validity_at for {}: {}",
                    token.getApiName(), token.getValidityAt());
            return false;
        }
    }

    /**
     * Get remaining validity in seconds.
     */
    public long getRemainingSeconds(UpstoxTokenEntity token) {
        if (token == null || token.getValidityAt() == null) {
            return 0;
        }

        try {
            Instant now = Instant.now();
            Instant validity = LocalDateTime
                    .parse(token.getValidityAt(), VALIDITY_FORMAT)
                    .atZone(IST)
                    .toInstant();

            long remaining = validity.getEpochSecond() - now.getEpochSecond();
            return Math.max(0, remaining);

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get human-readable time remaining.
     */
    public String getTimeRemaining(UpstoxTokenEntity token) {
        long seconds = getRemainingSeconds(token);
        if (seconds <= 0)
            return "EXPIRED";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    /**
     * Check if token is active in database.
     */
    public boolean isActive(UpstoxTokenEntity token) {
        return token != null && Integer.valueOf(1).equals(token.getIsActive());
    }

    /**
     * Calculate validity_at for new token (3:30 AM IST next day).
     */
    public String calculateValidityAt() {
        LocalDateTime now = LocalDateTime.now(IST);
        LocalDateTime expiryTime;

        // If before 3:30 AM, expires today at 3:30 AM
        // If after 3:30 AM, expires tomorrow at 3:30 AM
        LocalDateTime todayExpiry = now.toLocalDate().atTime(3, 30);

        if (now.isBefore(todayExpiry)) {
            expiryTime = todayExpiry;
        } else {
            expiryTime = todayExpiry.plusDays(1);
        }

        return expiryTime.format(VALIDITY_FORMAT);
    }
}
