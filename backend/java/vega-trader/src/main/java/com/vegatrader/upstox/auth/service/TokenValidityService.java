package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.util.format.TextFormatter;
import com.vegatrader.util.time.LocaleConstants;
import com.vegatrader.util.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Token validity service - Layer 1 (time-based check).
 * 
 * <p>
 * Uses TimeProvider for deterministic time operations.
 *
 * @since 2.2.0
 */
@Service
public class TokenValidityService {

    private static final Logger logger = LoggerFactory.getLogger(TokenValidityService.class);
    private static final DateTimeFormatter VALIDITY_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss", LocaleConstants.DEFAULT_LOCALE);
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final TimeProvider timeProvider;
    private final TextFormatter formatter;

    public TokenValidityService(TimeProvider timeProvider, TextFormatter formatter) {
        this.timeProvider = timeProvider;
        this.formatter = formatter;
    }

    /**
     * Check if token is valid based on validity_at timestamp.
     */
    public boolean isTimeValid(UpstoxTokenEntity token) {
        if (token == null || token.getValidityAt() == null || token.getValidityAt().isEmpty()) {
            return false;
        }

        try {
            Instant now = timeProvider.now();
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
            Instant now = timeProvider.now();
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
            return String.format(Locale.ROOT, "%dh %dm", hours, minutes);
        } else {
            return String.format(Locale.ROOT, "%dm", minutes);
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
        LocalDateTime now = timeProvider.dateTime(IST);
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
