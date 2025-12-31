package com.vegatrader.upstox.api.strike.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Strike Rule Scheduler per a1.md section 2.4.
 * Enforces strike scheme rules daily.
 * 
 * @since 4.5.0
 */
@Service
public class StrikeRuleScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StrikeRuleScheduler.class);

    private final StrikeStatusRepository strikeStatusRepo;
    private final StrikeSchemeRepository schemeRepo;

    public StrikeRuleScheduler(
            StrikeStatusRepository strikeStatusRepo,
            StrikeSchemeRepository schemeRepo) {
        this.strikeStatusRepo = strikeStatusRepo;
        this.schemeRepo = schemeRepo;
    }

    /**
     * Daily strike rule enforcement at 6:30 PM IST.
     * Per a1.md section 2.4:
     * - IF strike NOT in current strike_scheme AND OI == 0 → DISABLE
     * - IF strike NOT in scheme AND OI > 0 → KEEP ENABLED
     */
    @Scheduled(cron = "0 30 18 * * ?", zone = "Asia/Kolkata")
    @Transactional
    public void enforceStrikeRules() {
        logger.info("Starting daily strike rule enforcement...");

        int disabled = 0;
        int kept = 0;

        var allStrikes = strikeStatusRepo.findByEnabledTrue();

        for (var strike : allStrikes) {
            boolean inScheme = isInCurrentScheme(
                    strike.getExchange(),
                    strike.getUnderlying(),
                    strike.getStrikePrice());

            if (!inScheme && strike.getOpenInterest() == 0) {
                strike.setEnabled(false);
                strike.setDisabledReason("NOT_IN_SCHEME_ZERO_OI");
                strike.setDisabledAt(java.time.LocalDateTime.now());
                strikeStatusRepo.save(strike);
                disabled++;
            } else {
                kept++;
            }
        }

        logger.info("Strike enforcement complete: disabled={}, kept={}", disabled, kept);
    }

    /**
     * Re-enable strikes that now have OI.
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Kolkata")
    @Transactional
    public void reenableStrikesWithOI() {
        logger.info("Checking for strikes to re-enable...");

        var disabledStrikes = strikeStatusRepo.findByEnabledFalse();
        int reenabled = 0;

        for (var strike : disabledStrikes) {
            if (strike.getOpenInterest() > 0) {
                strike.setEnabled(true);
                strike.setDisabledReason(null);
                strike.setDisabledAt(null);
                strikeStatusRepo.save(strike);
                reenabled++;
            }
        }

        logger.info("Re-enabled {} strikes with OI", reenabled);
    }

    /**
     * Check if strike is in current scheme.
     */
    public boolean isInCurrentScheme(String exchange, String underlying, double strikePrice) {
        return schemeRepo.findByExchangeAndUnderlying(exchange, underlying)
                .map(scheme -> {
                    int interval = scheme.getStrikeInterval();
                    return strikePrice % interval == 0;
                })
                .orElse(true); // If no scheme, allow all
    }

    /**
     * Manual enforcement trigger.
     */
    public void triggerManualEnforcement() {
        logger.info("Manual strike enforcement triggered");
        enforceStrikeRules();
    }
}
