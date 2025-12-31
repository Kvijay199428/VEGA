package com.vegatrader.upstox.api.rms.validation;

import com.vegatrader.upstox.api.instrument.risk.ProductType;
import com.vegatrader.upstox.api.rms.eligibility.EligibilityCache;
import com.vegatrader.upstox.api.rms.eligibility.ProductEligibility;
import com.vegatrader.upstox.api.rms.entity.*;
import com.vegatrader.upstox.api.rms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Enterprise RMS validation service.
 * 
 * <p>
 * Validates:
 * <ul>
 * <li>Product eligibility (MIS/MTF/CNC)</li>
 * <li>Price bands</li>
 * <li>Quantity caps</li>
 * <li>T2T netting</li>
 * <li>F&O contract status</li>
 * </ul>
 * 
 * @since 4.1.0
 */
@Service
public class RmsValidationService {

    private static final Logger logger = LoggerFactory.getLogger(RmsValidationService.class);

    private final EligibilityCache eligibilityCache;
    private final PriceBandRepository priceBandRepo;
    private final QuantityCapRepository quantityCapRepo;
    private final ExchangeSeriesRepository seriesRepo;
    private final FoContractLifecycleRepository foRepo;

    public RmsValidationService(
            EligibilityCache eligibilityCache,
            PriceBandRepository priceBandRepo,
            QuantityCapRepository quantityCapRepo,
            ExchangeSeriesRepository seriesRepo,
            FoContractLifecycleRepository foRepo) {
        this.eligibilityCache = eligibilityCache;
        this.priceBandRepo = priceBandRepo;
        this.quantityCapRepo = quantityCapRepo;
        this.seriesRepo = seriesRepo;
        this.foRepo = foRepo;
    }

    /**
     * Validates an order against all RMS rules.
     * 
     * @param instrumentKey the instrument key
     * @param product       the product type (CNC/MIS/MTF)
     * @param qty           order quantity
     * @param price         order price
     * @return validation result
     */
    public RmsValidationResult validate(String instrumentKey, ProductType product, int qty, double price) {
        logger.debug("Validating order: {} {} qty={} price={}", instrumentKey, product, qty, price);

        try {
            // 1. Get eligibility from cache
            ProductEligibility eligibility = eligibilityCache.getEligibility(instrumentKey);

            // 2. Validate product
            validateProduct(instrumentKey, product, eligibility);

            // 3. Validate price band
            validatePriceBand(instrumentKey, price);

            // 4. Validate quantity cap
            validateQuantityCap(instrumentKey, qty, price);

            // 5. Check F&O contract status
            if (instrumentKey.contains("_FO|")) {
                validateFoContract(instrumentKey);
            }

            // 6. Calculate margin
            double marginPct = eligibility.marginPct() != null ? eligibility.marginPct() : getMarginPct(product);
            double requiredMargin = price * qty * (marginPct / 100.0);

            logger.debug("Order validated: margin={}", requiredMargin);
            return RmsValidationResult.approved(requiredMargin, eligibility);

        } catch (RmsException e) {
            logger.warn("Order rejected: {}", e.getMessage());
            return RmsValidationResult.rejected(e.getCode(), e.getMessage());
        }
    }

    private void validateProduct(String instrumentKey, ProductType product, ProductEligibility eligibility) {
        switch (product) {
            case MIS:
                if (!eligibility.misAllowed()) {
                    throw RmsException.productNotAllowed("MIS", eligibility.reason());
                }
                break;
            case MTF:
                if (!eligibility.mtfAllowed()) {
                    throw RmsException.productNotAllowed("MTF", eligibility.reason());
                }
                break;
            case CNC:
                if (!eligibility.cncAllowed()) {
                    throw RmsException.productNotAllowed("CNC", eligibility.reason());
                }
                break;
        }
    }

    private void validatePriceBand(String instrumentKey, double price) {
        Optional<PriceBandEntity> bandOpt = priceBandRepo.findTodayBandForKey(instrumentKey);
        if (bandOpt.isEmpty()) {
            bandOpt = priceBandRepo.findLatestForKey(instrumentKey);
        }

        if (bandOpt.isPresent()) {
            PriceBandEntity band = bandOpt.get();
            if (band.isOutsideBand(price)) {
                throw RmsException.priceBandViolation(price, band.getLowerPrice(), band.getUpperPrice());
            }
        }
    }

    private void validateQuantityCap(String instrumentKey, int qty, double price) {
        Optional<QuantityCapEntity> capOpt = quantityCapRepo.findActiveCapForKey(instrumentKey);

        if (capOpt.isPresent()) {
            QuantityCapEntity cap = capOpt.get();

            if (qty > cap.getMaxQty()) {
                throw RmsException.quantityCapExceeded(qty, cap.getMaxQty());
            }

            double value = qty * price;
            if (cap.getMaxValue() != null && cap.getMaxValue() > 0 && value > cap.getMaxValue()) {
                throw RmsException.valueCapExceeded(value, cap.getMaxValue());
            }
        }
    }

    private void validateFoContract(String instrumentKey) {
        if (!foRepo.isActiveContract(instrumentKey)) {
            throw RmsException.contractExpired(instrumentKey);
        }
    }

    private double getMarginPct(ProductType product) {
        return switch (product) {
            case MIS -> 20.0;
            case MTF -> 33.33;
            case CNC -> 100.0;
        };
    }

    /**
     * Checks if T2T netting is blocked for an instrument.
     */
    public boolean isT2TNettingBlocked(String instrumentKey, String exchange, String series) {
        return seriesRepo.findByExchangeAndSeriesCode(exchange, series)
                .map(s -> Boolean.TRUE.equals(s.getTradeForTrade()))
                .orElse(false);
    }

    /**
     * Validates T2T netting for square-off orders.
     */
    public void validateT2TSquareOff(String instrumentKey, String exchange, String series, boolean isClosingTrade) {
        if (isClosingTrade && isT2TNettingBlocked(instrumentKey, exchange, series)) {
            throw RmsException.t2tNettingBlocked();
        }
    }

    /**
     * Gets eligibility for frontend display.
     */
    public ProductEligibility getEligibility(String instrumentKey) {
        return eligibilityCache.getEligibility(instrumentKey);
    }

    /**
     * Invalidates eligibility cache for an instrument.
     */
    public void refreshEligibility(String instrumentKey) {
        eligibilityCache.invalidate(instrumentKey);
    }
}
