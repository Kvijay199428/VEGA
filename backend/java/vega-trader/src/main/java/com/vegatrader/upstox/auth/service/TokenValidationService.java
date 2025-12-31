package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.utils.TokenExpiryCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for validating token expiry and status.
 *
 * @since 2.0.0
 */
public class TokenValidationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenValidationService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Validate if token is still valid (not expired).
     *
     * @param token token entity
     * @return true if valid
     */
    public boolean isTokenValid(UpstoxTokenEntity token) {
        if (token == null) {
            logger.warn("Token is null");
            return false;
        }

        if (!token.isActive()) {
            logger.warn("Token is inactive: {}", token.getApiName());
            return false;
        }

        if (isExpired(token)) {
            logger.warn("Token expired: {}", token.getApiName());
            return false;
        }

        return true;
    }

    /**
     * Check if token is expired based on validity_at.
     *
     * @param token token entity
     * @return true if expired
     */
    public boolean isExpired(UpstoxTokenEntity token) {
        if (token == null || token.getValidityAt() == null) {
            return true;
        }

        return TokenExpiryCalculator.isExpired(token.getValidityAt());
    }

    /**
     * Check if token needs refresh (within 1 hour of expiry).
     *
     * @param token token entity
     * @return true if needs refresh
     */
    public boolean needsRefresh(UpstoxTokenEntity token) {
        if (token == null || token.getValidityAt() == null) {
            return true;
        }

        try {
            LocalDateTime expiry = LocalDateTime.parse(token.getValidityAt(), FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourBeforeExpiry = expiry.minusHours(1);

            return now.isAfter(oneHourBeforeExpiry);
        } catch (Exception e) {
            logger.error("Error parsing validity_at: {}", token.getValidityAt(), e);
            return true;
        }
    }

    /**
     * Get seconds until token expires.
     *
     * @param token token entity
     * @return seconds until expiry, or 0 if expired
     */
    public long getSecondsUntilExpiry(UpstoxTokenEntity token) {
        if (token == null || token.getValidityAt() == null) {
            return 0;
        }

        try {
            LocalDateTime expiry = LocalDateTime.parse(token.getValidityAt(), FORMATTER);
            LocalDateTime now = LocalDateTime.now();

            long seconds = java.time.Duration.between(now, expiry).getSeconds();
            return Math.max(0, seconds);
        } catch (Exception e) {
            logger.error("Error calculating expiry time", e);
            return 0;
        }
    }

    /**
     * Get human-readable time until expiry.
     *
     * @param token token entity
     * @return formatted string like "2h 30m" or "Expired"
     */
    public String getTimeUntilExpiryString(UpstoxTokenEntity token) {
        if (isExpired(token)) {
            return "Expired";
        }

        long seconds = getSecondsUntilExpiry(token);
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
