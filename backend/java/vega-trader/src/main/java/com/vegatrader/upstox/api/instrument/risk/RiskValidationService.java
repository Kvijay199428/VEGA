package com.vegatrader.upstox.api.instrument.risk;

import com.vegatrader.upstox.api.instrument.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for validating orders against risk rules.
 * 
 * <p>
 * Implements the risk validation flow:
 * <ol>
 * <li>Check if instrument exists</li>
 * <li>Check if suspended</li>
 * <li>Check product-specific rules (MIS/MTF overlays)</li>
 * <li>Calculate margin</li>
 * </ol>
 * 
 * @since 4.0.0
 */
@Service
public class RiskValidationService {

    private static final Logger logger = LoggerFactory.getLogger(RiskValidationService.class);

    private final InstrumentMasterRepository masterRepository;
    private final InstrumentMisRepository misRepository;
    private final InstrumentMtfRepository mtfRepository;
    private final InstrumentSuspensionRepository suspensionRepository;

    public RiskValidationService(
            InstrumentMasterRepository masterRepository,
            InstrumentMisRepository misRepository,
            InstrumentMtfRepository mtfRepository,
            InstrumentSuspensionRepository suspensionRepository) {
        this.masterRepository = masterRepository;
        this.misRepository = misRepository;
        this.mtfRepository = mtfRepository;
        this.suspensionRepository = suspensionRepository;
    }

    /**
     * Validates an order for risk compliance.
     * 
     * @param instrumentKey the instrument key
     * @param productType   the product type (CNC, MIS, MTF)
     * @param qty           the quantity
     * @param ltp           the last traded price
     * @return validation result
     */
    public RiskValidationResult validate(String instrumentKey, ProductType productType,
            int qty, double ltp) {
        logger.debug("Validating order: {} {} qty={} ltp={}",
                instrumentKey, productType, qty, ltp);

        // 1. Check instrument exists
        if (!masterRepository.existsById(instrumentKey)) {
            return RiskValidationResult.reject("Instrument not found: " + instrumentKey);
        }

        // 2. Check suspended
        if (suspensionRepository.existsByInstrumentKey(instrumentKey)) {
            return RiskValidationResult.reject("Instrument is suspended: " + instrumentKey);
        }

        // 3. Check product-specific rules
        switch (productType) {
            case MIS:
                if (!misRepository.existsByInstrumentKey(instrumentKey)) {
                    return RiskValidationResult.reject("MIS not allowed for: " + instrumentKey);
                }
                break;

            case MTF:
                if (!mtfRepository.isMtfEnabled(instrumentKey)) {
                    return RiskValidationResult.reject("MTF not enabled for: " + instrumentKey);
                }
                break;

            case CNC:
                // CNC is always allowed for active instruments
                break;
        }

        // 4. Calculate margin
        double requiredMargin = productType.calculateMargin(ltp, qty);

        logger.debug("Order validated: margin={}", requiredMargin);
        return RiskValidationResult.approve(requiredMargin);
    }

    /**
     * Checks if instrument is tradable (not suspended).
     */
    public boolean isTradable(String instrumentKey) {
        return masterRepository.existsById(instrumentKey)
                && !suspensionRepository.existsByInstrumentKey(instrumentKey);
    }

    /**
     * Gets trading eligibility for all product types.
     */
    public TradingEligibility getEligibility(String instrumentKey) {
        TradingEligibility eligibility = new TradingEligibility();
        eligibility.setInstrumentKey(instrumentKey);
        eligibility.setExists(masterRepository.existsById(instrumentKey));
        eligibility.setSuspended(suspensionRepository.existsByInstrumentKey(instrumentKey));
        eligibility.setMisAllowed(misRepository.existsByInstrumentKey(instrumentKey));
        eligibility.setMtfEnabled(mtfRepository.isMtfEnabled(instrumentKey));
        eligibility.setCncAllowed(eligibility.isExists() && !eligibility.isSuspended());
        return eligibility;
    }

    /**
     * Risk validation result.
     */
    public static class RiskValidationResult {
        private final boolean approved;
        private final String reason;
        private final Double requiredMargin;

        private RiskValidationResult(boolean approved, String reason, Double requiredMargin) {
            this.approved = approved;
            this.reason = reason;
            this.requiredMargin = requiredMargin;
        }

        public static RiskValidationResult approve(double margin) {
            return new RiskValidationResult(true, null, margin);
        }

        public static RiskValidationResult reject(String reason) {
            return new RiskValidationResult(false, reason, null);
        }

        public boolean isApproved() {
            return approved;
        }

        public String getReason() {
            return reason;
        }

        public Double getRequiredMargin() {
            return requiredMargin;
        }
    }

    /**
     * Trading eligibility for an instrument.
     */
    public static class TradingEligibility {
        private String instrumentKey;
        private boolean exists;
        private boolean suspended;
        private boolean misAllowed;
        private boolean mtfEnabled;
        private boolean cncAllowed;

        public String getInstrumentKey() {
            return instrumentKey;
        }

        public void setInstrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }

        public boolean isSuspended() {
            return suspended;
        }

        public void setSuspended(boolean suspended) {
            this.suspended = suspended;
        }

        public boolean isMisAllowed() {
            return misAllowed;
        }

        public void setMisAllowed(boolean misAllowed) {
            this.misAllowed = misAllowed;
        }

        public boolean isMtfEnabled() {
            return mtfEnabled;
        }

        public void setMtfEnabled(boolean mtfEnabled) {
            this.mtfEnabled = mtfEnabled;
        }

        public boolean isCncAllowed() {
            return cncAllowed;
        }

        public void setCncAllowed(boolean cncAllowed) {
            this.cncAllowed = cncAllowed;
        }
    }
}
