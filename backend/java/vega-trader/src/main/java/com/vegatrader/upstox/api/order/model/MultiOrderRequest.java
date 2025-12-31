package com.vegatrader.upstox.api.order.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Multi-order request for batch order placement.
 * Per order-mgmt/main1/a1.md and a2.md - Place Multi Order V2.
 * 
 * Supports up to 25 orders per request.
 * BUY orders execute before SELL orders.
 * Auto-slicing for quantities exceeding freeze limits.
 * 
 * @since 4.8.0
 */
public record MultiOrderRequest(
        List<OrderLine> orders) {

    /**
     * Single order line in a multi-order batch.
     */
    public record OrderLine(
            String correlationId, // Unique ID per order (max 20 chars)
            int quantity,
            String product, // D, I, CO, MTF
            String validity, // DAY, IOC
            BigDecimal price,
            String orderType, // MARKET, LIMIT, SL, SL-M
            String transactionType, // BUY, SELL
            String instrumentToken,
            String tag, // Optional (max 40 chars)
            int disclosedQuantity,
            BigDecimal triggerPrice,
            boolean isAmo,
            boolean slice // Enable auto-slicing for large orders
    ) {

        /**
         * Validate order line.
         */
        public ValidationResult validate() {
            if (correlationId == null || correlationId.length() > 20) {
                return ValidationResult.error("correlationId must be <= 20 chars");
            }
            if (quantity <= 0) {
                return ValidationResult.error("quantity must be positive");
            }
            if (instrumentToken == null || instrumentToken.isBlank()) {
                return ValidationResult.error("instrumentToken is required");
            }
            if (tag != null && tag.length() > 40) {
                return ValidationResult.error("tag must be <= 40 chars");
            }
            return ValidationResult.ok();
        }
    }

    /**
     * Validation result.
     */
    public record ValidationResult(boolean valid, String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String msg) {
            return new ValidationResult(false, msg);
        }
    }
}
