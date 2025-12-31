package com.vegatrader.upstox.api.order.risk;

import com.vegatrader.upstox.api.order.broker.BrokerAdapter;
import com.vegatrader.upstox.api.order.position.PositionAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Risk Engine for pre-order validation.
 * Per order-mgmt/b4.md section 4 and a1.md Section 8.
 * 
 * Validates:
 * - Max order value
 * - Position limits
 * - Delta exposure caps
 * - Symbol restrictions
 * - User-specific rules
 * - Soft vs Hard limits
 * 
 * @since 5.0.0
 */
@Service
public class RiskEngine {

    private static final Logger logger = LoggerFactory.getLogger(RiskEngine.class);

    // === Configurable Limits (should be loaded from database/config) ===
    private static final BigDecimal MAX_ORDER_VALUE = new BigDecimal("10000000"); // 1 Cr
    private static final int MAX_QUANTITY_PER_ORDER = 50000;
    private static final int MAX_OPEN_ORDERS_PER_USER = 100;
    private static final BigDecimal MAX_DAILY_TURNOVER = new BigDecimal("50000000"); // 5 Cr
    private static final BigDecimal MAX_EXPOSURE_PER_USER = new BigDecimal("20000000"); // 2 Cr
    private static final BigDecimal MAX_DELTA_EXPOSURE = new BigDecimal("100000"); // Delta cap
    private static final int MAX_POSITION_PER_INSTRUMENT = 100000;

    // Blocked symbols
    private final Set<String> blockedSymbols = new HashSet<>();

    private final PositionAggregationService positionService;

    @Autowired
    public RiskEngine(PositionAggregationService positionService) {
        this.positionService = positionService;
    }

    /**
     * Validate a single order with full risk checks.
     */
    public RiskValidationResult validate(BrokerAdapter.OrderRequest order, String userId) {
        List<String> hardViolations = new ArrayList<>();
        List<String> softWarnings = new ArrayList<>();

        // === HARD LIMITS (Must Block) ===

        // 1. Quantity check
        if (order.quantity() <= 0) {
            hardViolations.add("Quantity must be positive");
        }
        if (order.quantity() > MAX_QUANTITY_PER_ORDER) {
            hardViolations.add("Quantity exceeds maximum: " + MAX_QUANTITY_PER_ORDER);
        }

        // 2. Order value check
        BigDecimal orderValue = order.price() != null
                ? order.price().multiply(BigDecimal.valueOf(order.quantity()))
                : BigDecimal.ZERO;
        if (orderValue.compareTo(MAX_ORDER_VALUE) > 0) {
            hardViolations.add("Order value exceeds maximum: " + MAX_ORDER_VALUE);
        }

        // 3. Symbol restriction
        if (blockedSymbols.contains(order.instrumentToken())) {
            hardViolations.add("Symbol is blocked for trading: " + order.instrumentToken());
        }

        // 4. Price validation for LIMIT orders
        if ("LIMIT".equals(order.orderType()) &&
                (order.price() == null || order.price().compareTo(BigDecimal.ZERO) <= 0)) {
            hardViolations.add("Limit order requires a valid price");
        }

        // 5. Stop loss validation
        if (("SL".equals(order.orderType()) || "SL-M".equals(order.orderType())) &&
                (order.triggerPrice() == null || order.triggerPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            hardViolations.add("Stop loss order requires a valid trigger price");
        }

        // 6. Disclosed quantity validation
        if (order.disclosedQuantity() > order.quantity()) {
            hardViolations.add("Disclosed quantity cannot exceed order quantity");
        }

        // 7. Position limit check (integrated with PositionAggregationService)
        int currentPos = positionService.getNetQuantity(userId, order.instrumentToken());
        int signedQty = "BUY".equalsIgnoreCase(order.side()) ? order.quantity() : -order.quantity();
        int projectedPos = currentPos + signedQty;
        if (Math.abs(projectedPos) > MAX_POSITION_PER_INSTRUMENT) {
            hardViolations.add("Position limit exceeded for " + order.instrumentToken() +
                    ": current=" + currentPos + ", projected=" + projectedPos);
        }

        // 8. Exposure limit check
        BigDecimal currentExposure = positionService.getTotalExposure(userId);
        BigDecimal projectedExposure = currentExposure.add(orderValue);
        if (projectedExposure.compareTo(MAX_EXPOSURE_PER_USER) > 0) {
            hardViolations.add("Exposure limit exceeded: projected=" + projectedExposure);
        }

        // === SOFT LIMITS (Warn but Allow) ===

        // 1. High concentration warning
        if (Math.abs(projectedPos) > MAX_POSITION_PER_INSTRUMENT * 0.8) {
            softWarnings.add("Position approaching limit for " + order.instrumentToken());
        }

        // 2. High exposure warning
        if (projectedExposure.compareTo(MAX_EXPOSURE_PER_USER.multiply(BigDecimal.valueOf(0.8))) > 0) {
            softWarnings.add("Exposure approaching limit");
        }

        // === Result ===
        if (!hardViolations.isEmpty()) {
            logger.warn("Risk validation FAILED for {}: {}", order.correlationId(), hardViolations);
            return RiskValidationResult.failed(hardViolations);
        } else if (!softWarnings.isEmpty()) {
            logger.info("Risk validation PASSED with warnings for {}: {}", order.correlationId(), softWarnings);
            return RiskValidationResult.warning(softWarnings);
        } else {
            logger.debug("Risk validation PASSED for order: {}", order.correlationId());
            return RiskValidationResult.passed();
        }
    }

    /**
     * Validate multiple orders in a batch.
     */
    public RiskValidationResult validateBatch(List<BrokerAdapter.OrderRequest> orders, String userId) {
        List<String> allViolations = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();

        // Batch-level checks
        if (orders.size() > 25) {
            allViolations.add("Batch size exceeds maximum: 25");
        }

        // Total value check
        BigDecimal totalValue = orders.stream()
                .filter(o -> o.price() != null)
                .map(o -> o.price().multiply(BigDecimal.valueOf(o.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalValue.compareTo(MAX_DAILY_TURNOVER) > 0) {
            allViolations.add("Batch value exceeds daily turnover limit");
        }

        // Validate each order
        for (BrokerAdapter.OrderRequest order : orders) {
            RiskValidationResult result = validate(order, userId);
            if (!result.isPassed()) {
                allViolations.addAll(result.violations().stream()
                        .map(v -> order.correlationId() + ": " + v)
                        .toList());
            } else if (!result.violations().isEmpty()) {
                allWarnings.addAll(result.violations().stream()
                        .map(v -> order.correlationId() + ": " + v)
                        .toList());
            }
        }

        if (!allViolations.isEmpty()) {
            return RiskValidationResult.failed(allViolations);
        } else if (!allWarnings.isEmpty()) {
            return RiskValidationResult.warning(allWarnings);
        } else {
            return RiskValidationResult.passed();
        }
    }

    /**
     * Check if user can place more orders.
     */
    public boolean canPlaceOrder(String userId, int currentOpenOrders) {
        return currentOpenOrders < MAX_OPEN_ORDERS_PER_USER;
    }

    /**
     * Check position limit for symbol.
     */
    public boolean checkPositionLimit(String userId, String instrumentToken, int newQuantity) {
        int currentPosition = positionService.getNetQuantity(userId, instrumentToken);
        return Math.abs(currentPosition + newQuantity) <= MAX_POSITION_PER_INSTRUMENT;
    }

    /**
     * Get exchange freeze quantity.
     */
    public int getFreezeQuantity(String segment, String symbol) {
        return switch (segment) {
            case "NSE_FO", "BSE_FO" -> 1800; // NIFTY options
            case "MCX" -> 500;
            default -> 50000;
        };
    }

    /**
     * Check if order needs slicing.
     */
    public boolean needsSlicing(BrokerAdapter.OrderRequest order, String segment) {
        int freezeQty = getFreezeQuantity(segment, order.instrumentToken());
        return order.quantity() > freezeQty;
    }

    /**
     * Calculate slices for large order.
     */
    public List<Integer> calculateSlices(int quantity, int freezeQuantity) {
        List<Integer> slices = new ArrayList<>();
        int remaining = quantity;

        while (remaining > 0) {
            int sliceQty = Math.min(remaining, freezeQuantity);
            slices.add(sliceQty);
            remaining -= sliceQty;
        }

        return slices;
    }

    /**
     * Add symbol to blocked list.
     */
    public void blockSymbol(String instrumentToken) {
        blockedSymbols.add(instrumentToken);
        logger.info("Blocked symbol: {}", instrumentToken);
    }

    /**
     * Remove symbol from blocked list.
     */
    public void unblockSymbol(String instrumentToken) {
        blockedSymbols.remove(instrumentToken);
        logger.info("Unblocked symbol: {}", instrumentToken);
    }

    /**
     * Get blocked symbols.
     */
    public Set<String> getBlockedSymbols() {
        return Set.copyOf(blockedSymbols);
    }
}
