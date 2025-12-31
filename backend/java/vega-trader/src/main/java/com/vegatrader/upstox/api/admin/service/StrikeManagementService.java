package com.vegatrader.upstox.api.admin.service;

import com.vegatrader.upstox.api.admin.entity.DisabledStrikeEntity;
import com.vegatrader.upstox.api.admin.model.StrikeDisableRequest;
import com.vegatrader.upstox.api.admin.model.StrikeEnableRequest;
import com.vegatrader.upstox.api.admin.repository.DisabledStrikeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Strike Management Service.
 * Handles disabling/enabling strikes for trading.
 * Per a1.md Section 9.
 * 
 * @since 5.0.0
 */
@Service
public class StrikeManagementService {

    private static final Logger logger = LoggerFactory.getLogger(StrikeManagementService.class);

    private final DisabledStrikeRepository disabledStrikeRepository;

    @Autowired
    public StrikeManagementService(DisabledStrikeRepository disabledStrikeRepository) {
        this.disabledStrikeRepository = disabledStrikeRepository;
    }

    /**
     * Disable a strike for trading.
     */
    @Transactional
    public DisabledStrikeEntity disableStrike(StrikeDisableRequest request, String adminUser) {
        logger.info("Disabling strike: {} @ {} {} {} - reason: {}",
                request.underlyingKey(), request.expiry(), request.strike(),
                request.optionType(), request.reason());

        // Check if already disabled
        Optional<DisabledStrikeEntity> existing = disabledStrikeRepository
                .findByUnderlyingKeyAndExpiryDateAndStrikePriceAndOptionTypeAndActiveTrue(
                        request.underlyingKey(), request.expiry(), request.strike(), request.optionType());

        if (existing.isPresent()) {
            logger.warn("Strike already disabled: {}", formatStrike(request));
            return existing.get();
        }

        DisabledStrikeEntity entity = DisabledStrikeEntity.disable(
                request.underlyingKey(),
                request.expiry(),
                request.strike(),
                request.optionType(),
                adminUser,
                request.reason());

        DisabledStrikeEntity saved = disabledStrikeRepository.save(entity);
        logger.info("Strike disabled: {} by {}", formatStrike(request), adminUser);
        return saved;
    }

    /**
     * Enable a previously disabled strike.
     */
    @Transactional
    public boolean enableStrike(StrikeEnableRequest request, String adminUser) {
        logger.info("Enabling strike: {} @ {} {} {}",
                request.underlyingKey(), request.expiry(), request.strike(), request.optionType());

        Optional<DisabledStrikeEntity> existing = disabledStrikeRepository
                .findByUnderlyingKeyAndExpiryDateAndStrikePriceAndOptionTypeAndActiveTrue(
                        request.underlyingKey(), request.expiry(), request.strike(), request.optionType());

        if (existing.isEmpty()) {
            logger.warn("Strike not found or not disabled: {}",
                    formatStrike(request.underlyingKey(), request.strike(), request.optionType()));
            return false;
        }

        DisabledStrikeEntity entity = existing.get();
        entity.enable(adminUser, request.reason());
        disabledStrikeRepository.save(entity);

        logger.info("Strike enabled: {} by {}",
                formatStrike(request.underlyingKey(), request.strike(), request.optionType()), adminUser);
        return true;
    }

    /**
     * Check if a strike is disabled.
     */
    public boolean isStrikeDisabled(String underlyingKey, LocalDate expiry, double strike, String optionType) {
        return disabledStrikeRepository.isStrikeDisabled(underlyingKey, expiry, strike, optionType);
    }

    /**
     * Get all disabled strikes.
     */
    public List<DisabledStrikeEntity> getAllDisabledStrikes() {
        return disabledStrikeRepository.findByActiveTrue();
    }

    /**
     * Get disabled strikes for an underlying.
     */
    public List<DisabledStrikeEntity> getDisabledStrikes(String underlyingKey) {
        return disabledStrikeRepository.findByUnderlyingKeyAndActiveTrue(underlyingKey);
    }

    /**
     * Get disabled strikes for an expiry date.
     */
    public List<DisabledStrikeEntity> getDisabledStrikesByExpiry(LocalDate expiryDate) {
        return disabledStrikeRepository.findByExpiryDateAndActiveTrue(expiryDate);
    }

    /**
     * Get recently disabled strikes.
     */
    public List<DisabledStrikeEntity> getRecentlyDisabled(int limit) {
        return disabledStrikeRepository.findRecentlyDisabled(limit);
    }

    /**
     * Count active disabled strikes.
     */
    public long countDisabledStrikes() {
        return disabledStrikeRepository.countByActiveTrue();
    }

    // === Private Helpers ===

    private String formatStrike(StrikeDisableRequest req) {
        return formatStrike(req.underlyingKey(), req.strike(), req.optionType());
    }

    private String formatStrike(String underlying, double strike, String optionType) {
        return String.format("%s/%s/%s", underlying, strike, optionType);
    }
}
