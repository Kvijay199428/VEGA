package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-memory token cache for database lock resilience.
 * Stores tokens when SQLite is locked, auto-persists when unlocked.
 *
 * @since 2.0.0
 */
@Service
public class TokenCacheService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCacheService.class);

    private final ConcurrentHashMap<String, PendingToken> pendingTokens = new ConcurrentHashMap<>();
    private final AtomicBoolean dbLocked = new AtomicBoolean(false);
    private final AtomicBoolean recoveryInProgress = new AtomicBoolean(false);

    /**
     * Pending token structure for cache storage.
     */
    public static class PendingToken {
        private final String apiName;
        private final UpstoxTokenEntity entity;
        private final Instant cachedAt;

        public PendingToken(String apiName, UpstoxTokenEntity entity) {
            this.apiName = apiName;
            this.entity = entity;
            this.cachedAt = Instant.now();
        }

        public String getApiName() {
            return apiName;
        }

        public UpstoxTokenEntity getEntity() {
            return entity;
        }

        public Instant getCachedAt() {
            return cachedAt;
        }
    }

    /**
     * Cache a token when database is locked.
     */
    public void cachePendingToken(UpstoxTokenEntity entity) {
        String apiName = entity.getApiName();
        pendingTokens.put(apiName, new PendingToken(apiName, entity));
        dbLocked.set(true);

        logger.warn("╔═══════════════════════════════════════════════════════╗");
        logger.warn("║  TOKEN CACHED IN MEMORY (DB LOCKED)  ║");
        logger.warn("╚═══════════════════════════════════════════════════════╝");
        logger.warn("API: {} | Cached at: {}", apiName, Instant.now());
        logger.warn("Pending tokens in cache: {}", pendingTokens.size());
    }

    /**
     * Get all pending tokens for recovery.
     */
    public Collection<PendingToken> getPendingTokens() {
        return pendingTokens.values();
    }

    /**
     * Remove a token from cache after successful DB persist.
     */
    public void removePendingToken(String apiName) {
        pendingTokens.remove(apiName);
        logger.info("✓ Token removed from cache: {} | Remaining: {}", apiName, pendingTokens.size());

        if (pendingTokens.isEmpty()) {
            dbLocked.set(false);
            recoveryInProgress.set(false);
            logger.info("✓ All pending tokens persisted. DB lock cleared.");
        }
    }

    /**
     * Check if a token exists in cache.
     */
    public boolean hasPendingToken(String apiName) {
        return pendingTokens.containsKey(apiName);
    }

    /**
     * Get pending token count.
     */
    public int getPendingCount() {
        return pendingTokens.size();
    }

    /**
     * Check if database is currently locked.
     */
    public boolean isDbLocked() {
        return dbLocked.get();
    }

    /**
     * Set database lock status.
     */
    public void setDbLocked(boolean locked) {
        dbLocked.set(locked);
    }

    /**
     * Check if recovery is in progress.
     */
    public boolean isRecoveryInProgress() {
        return recoveryInProgress.get();
    }

    /**
     * Set recovery in progress flag.
     */
    public void setRecoveryInProgress(boolean inProgress) {
        recoveryInProgress.set(inProgress);
    }

    /**
     * Get cache status for API response.
     */
    public CacheStatus getStatus() {
        return new CacheStatus(
                dbLocked.get(),
                pendingTokens.size(),
                recoveryInProgress.get());
    }

    /**
     * Cache status DTO.
     */
    public static class CacheStatus {
        private final boolean dbLocked;
        private final int pendingInCache;
        private final boolean recoveryInProgress;

        public CacheStatus(boolean dbLocked, int pendingInCache, boolean recoveryInProgress) {
            this.dbLocked = dbLocked;
            this.pendingInCache = pendingInCache;
            this.recoveryInProgress = recoveryInProgress;
        }

        public boolean isDbLocked() {
            return dbLocked;
        }

        public int getPendingInCache() {
            return pendingInCache;
        }

        public boolean isRecoveryInProgress() {
            return recoveryInProgress;
        }
    }
}
