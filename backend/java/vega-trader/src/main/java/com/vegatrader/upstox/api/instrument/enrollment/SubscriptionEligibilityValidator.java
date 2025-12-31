package com.vegatrader.upstox.api.instrument.enrollment;

import com.vegatrader.upstox.api.websocket.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates subscription eligibility based on mode limits.
 * 
 * <p>
 * This is a hard guardrail that prevents subscription requests
 * from exceeding Upstox API limits, which would result in
 * WebSocket rejection at runtime.
 * 
 * <p>
 * Limits are mode-specific:
 * <ul>
 * <li>LTPC: 5000 instruments</li>
 * <li>OPTION_GREEKS: 2000 instruments</li>
 * <li>FULL: 2000 instruments</li>
 * <li>FULL_D30: 1000 instruments</li>
 * </ul>
 * 
 * @since 3.1.0
 */
public final class SubscriptionEligibilityValidator {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionEligibilityValidator.class);

    /**
     * Validates that the subscription count does not exceed mode limit.
     * 
     * @param count number of instruments to subscribe
     * @param mode  subscription mode with individual limit
     * @throws IllegalStateException if count exceeds limit
     */
    public void validate(int count, Mode mode) {
        if (count > mode.getIndividualLimit()) {
            String message = String.format(
                    "Subscription limit exceeded for mode %s: %d > %d",
                    mode, count, mode.getIndividualLimit());
            logger.error(message);
            throw new IllegalStateException(message);
        }

        logger.debug("Subscription validated: {} instruments for mode {} (limit: {})",
                count, mode, mode.getIndividualLimit());
    }

    /**
     * Checks if subscription is valid without throwing exception.
     * 
     * @param count number of instruments
     * @param mode  subscription mode
     * @return true if valid, false if exceeds limit
     */
    public boolean isValid(int count, Mode mode) {
        return count <= mode.getIndividualLimit();
    }
}
