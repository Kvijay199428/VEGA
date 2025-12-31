package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Background worker that retries persisting cached tokens to database.
 * Runs every 5 seconds when there are pending tokens in cache.
 *
 * @since 2.0.0
 */
@Service
public class DbRecoveryWorker {

    private static final Logger logger = LoggerFactory.getLogger(DbRecoveryWorker.class);

    private final TokenCacheService tokenCacheService;
    private final TokenRepository tokenRepository;

    public DbRecoveryWorker(TokenCacheService tokenCacheService, TokenRepository tokenRepository) {
        this.tokenCacheService = tokenCacheService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Scheduled task to retry persisting pending tokens.
     * Runs every 5 seconds.
     */
    @Scheduled(fixedDelay = 5000)
    public void retryPendingTokens() {
        if (tokenCacheService.getPendingCount() == 0) {
            return;
        }

        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  DB RECOVERY WORKER - RETRYING {} TOKENS  ║", tokenCacheService.getPendingCount());
        logger.info("╚═══════════════════════════════════════════════════════╝");

        tokenCacheService.setRecoveryInProgress(true);

        for (TokenCacheService.PendingToken pending : tokenCacheService.getPendingTokens()) {
            try {
                UpstoxTokenEntity entity = pending.getEntity();
                boolean saved = tokenRepository.save(entity);

                if (saved) {
                    tokenCacheService.removePendingToken(pending.getApiName());
                    logger.info("✓ Persisted cached token to DB: {}", pending.getApiName());
                } else {
                    logger.warn("✗ Failed to persist token: {}", pending.getApiName());
                }

            } catch (TokenRepository.DbLockException e) {
                logger.warn("Database still locked. Will retry in 5 seconds.");
                return; // Stop and wait for next cycle

            } catch (Exception e) {
                logger.error("Unexpected error during recovery for {}: {}",
                        pending.getApiName(), e.getMessage());
            }
        }

        if (tokenCacheService.getPendingCount() == 0) {
            tokenCacheService.setDbLocked(false);
            tokenCacheService.setRecoveryInProgress(false);
            logger.info("╔═══════════════════════════════════════════════════════╗");
            logger.info("║  ✓ DB RECOVERY COMPLETE - ALL TOKENS PERSISTED  ║");
            logger.info("╚═══════════════════════════════════════════════════════╝");
        }
    }
}
