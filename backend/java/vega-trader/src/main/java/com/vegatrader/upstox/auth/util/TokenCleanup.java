package com.vegatrader.upstox.auth.util;

import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Database cleanup utility for Upstox tokens.
 * Removes all existing tokens before multilogin execution.
 * 
 * Usage: java -cp "target/classes;target/test-classes"
 * com.vegatrader.upstox.auth.util.TokenCleanup
 */
public class TokenCleanup {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanup.class);

    public static void main(String[] args) {
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  UPSTOX TOKEN DATABASE CLEANUP                       ║");
        logger.info("╚═══════════════════════════════════════════════════════╝");
        logger.info("");

        try {
            TokenStorageService tokenStorage = new TokenStorageService(
                    new com.vegatrader.upstox.auth.repository.TokenRepository(),
                    new com.vegatrader.upstox.auth.service.TokenCacheService());
            List<UpstoxTokenEntity> existingTokens = tokenStorage.getAllActiveTokens();

            logger.info("Found {} existing tokens in database", existingTokens.size());

            if (existingTokens.isEmpty()) {
                logger.info("✓ No tokens to clean up");
                return;
            }

            logger.info("");
            logger.info("Deactivating all tokens:");

            int count = 0;
            for (UpstoxTokenEntity token : existingTokens) {
                logger.info("  [{}] Deactivating: {} (Primary: {}, Created: {})",
                        ++count,
                        token.getApiName(),
                        token.isPrimary(),
                        token.getCreatedAt());
                tokenStorage.deactivateToken(token.getApiName());
            }

            logger.info("");
            logger.info("╔═══════════════════════════════════════════════════════╗");
            logger.info("║  ✓ CLEANUP COMPLETE                                   ║");
            logger.info("║  Deactivated {} tokens                              ║", count);
            logger.info("╚═══════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            logger.error("✗ Cleanup failed", e);
            System.exit(1);
        }
    }
}
