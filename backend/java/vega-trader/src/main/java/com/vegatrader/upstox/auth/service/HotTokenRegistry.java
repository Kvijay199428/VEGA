package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory HOT token registry for zero-delay token availability.
 * 
 * Trading systems read from: HOT_TOKENS → fallback → DB
 * 
 * This guarantees:
 * - Zero blocking
 * - No trading delay
 * - Persistence failures do not stop trading
 *
 * @since 2.3.0
 */
@org.springframework.stereotype.Component
public final class HotTokenRegistry {

    private static final Logger logger = LoggerFactory.getLogger(HotTokenRegistry.class);

    private final ConcurrentHashMap<ApiName, HotToken> HOT_TOKENS = new ConcurrentHashMap<>();
    private final com.vegatrader.util.time.TimeProvider timeProvider;

    public HotTokenRegistry(com.vegatrader.util.time.TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        logger.info("HotTokenRegistry initialized with TimeProvider");
    }

    /**
     * Store token in hot registry.
     */
    public void put(ApiName apiName, String accessToken, Instant validUntil) {
        HOT_TOKENS.put(apiName, new HotToken(accessToken, validUntil, timeProvider.now()));
        logger.info("✓ HOT token registered: {} (valid until: {})", apiName, validUntil);
    }

    /**
     * Get token from hot registry.
     */
    public Optional<String> get(ApiName apiName) {
        HotToken token = HOT_TOKENS.get(apiName);
        if (token == null) {
            return Optional.empty();
        }
        if (timeProvider.now().isAfter(token.validUntil())) {
            logger.debug("HOT token expired: {}", apiName);
            HOT_TOKENS.remove(apiName);
            return Optional.empty();
        }
        return Optional.of(token.accessToken());
    }

    /**
     * Check if token exists and is valid.
     */
    public boolean isValid(ApiName apiName) {
        return get(apiName).isPresent();
    }

    /**
     * Remove token from hot registry.
     */
    public void remove(ApiName apiName) {
        HOT_TOKENS.remove(apiName);
        logger.debug("HOT token removed: {}", apiName);
    }

    /**
     * Clear all tokens.
     */
    public void clear() {
        HOT_TOKENS.clear();
        logger.info("HOT token registry cleared");
    }

    /**
     * Get all hot token count.
     */
    public int count() {
        return HOT_TOKENS.size();
    }

    /**
     * Get valid token count.
     */
    public int validCount() {
        return (int) HOT_TOKENS.values().stream()
                .filter(t -> timeProvider.now().isBefore(t.validUntil()))
                .count();
    }

    /**
     * Hot token record.
     */
    public record HotToken(String accessToken, Instant validUntil, Instant storedAt) {
    }
}
