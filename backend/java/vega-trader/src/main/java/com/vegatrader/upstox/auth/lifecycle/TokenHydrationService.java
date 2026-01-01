package com.vegatrader.upstox.auth.lifecycle;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Token Hydration Service
 * 
 * Loads and validates all persisted tokens when the application starts.
 * This eliminates false "0/6" states and prevents unnecessary Selenium flows.
 * 
 * Execution timing: Uses ApplicationReadyEvent (after Spring context
 * initialization)
 * 
 * @since Production Auth Architecture v2.0
 */
@Component
public class TokenHydrationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenHydrationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TokenRepository tokenRepository;
    private volatile boolean hydrationComplete = false;

    public TokenHydrationService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Hydrate tokens from database on application startup.
     * Runs after ApplicationContext is fully initialized.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void hydrateTokens() {
        LocalDateTime startTime = LocalDateTime.now();

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("[AUTH-HYDRATION] Starting token hydration at {}", startTime.format(DATE_FORMATTER));
        logger.info("═══════════════════════════════════════════════════════");

        List<UpstoxTokenEntity> validTokens = tokenRepository.findActiveAndValid();

        logger.info("[AUTH-HYDRATION] Found {} valid tokens in database", validTokens.size());

        validTokens.forEach(token -> {
            try {
                LocalDateTime validityAt = LocalDateTime.parse(token.getValidityAt(), DATE_FORMATTER);
                Duration remaining = Duration.between(LocalDateTime.now(), validityAt);

                long hours = remaining.toHours();
                long minutes = remaining.toMinutesPart();

                logger.info("[AUTH-HYDRATION] ✓ Token hydrated: {} | Valid for: {}h {}m",
                        token.getApiName(),
                        hours,
                        minutes);
            } catch (Exception e) {
                logger.warn("[AUTH-HYDRATION] ⚠ Could not parse validity for {}: {}",
                        token.getApiName(), e.getMessage());
            }
        });

        hydrationComplete = true;

        int missingCount = 6 - validTokens.size();

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("[AUTH-HYDRATION] Hydration complete | Valid={} | Missing={}",
                validTokens.size(),
                missingCount);
        if (missingCount > 0) {
            logger.warn("[AUTH-HYDRATION] ⚠ {} token(s) require generation", missingCount);
        } else {
            logger.info("[AUTH-HYDRATION] ✓ All tokens present and valid");
        }
        logger.info("═══════════════════════════════════════════════════════");
    }

    /**
     * Check if hydration has completed.
     * Used by controllers to ensure startup consistency.
     */
    public boolean isHydrationComplete() {
        return hydrationComplete;
    }
}
