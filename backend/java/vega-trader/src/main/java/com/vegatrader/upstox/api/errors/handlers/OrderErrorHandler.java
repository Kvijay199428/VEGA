package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.response.common.ErrorResponse;

/**
 * Error handler for order-specific errors.
 * <p>
 * Provides detailed resolution hints for common order placement,
 * modification, and cancellation errors.
 * </p>
 *
 * @since 2.0.0
 */
public class OrderErrorHandler extends BaseErrorHandler {

    public OrderErrorHandler() {
        // Register order-specific resolution hints
        registerResolutionHint("insufficient_funds",
                "Add funds to your account or reduce order quantity");

        registerResolutionHint("invalid_order_type",
                "Use valid order types: MARKET, LIMIT, STOP_MARKET, STOP_LIMIT");

        registerResolutionHint("invalid_quantity",
                "Ensure quantity is a positive integer and matches lot size");

        registerResolutionHint("invalid_price",
                "Ensure price is within tick size and circuit limits");

        registerResolutionHint("order_not_found",
                "Verify order ID is correct and order exists");

        registerResolutionHint("order_already_executed",
                "Cannot modify/cancel executed orders");

        registerResolutionHint("order_already_cancelled",
                "Order is already cancelled");

        registerResolutionHint("exchange_not_open",
                "Place order during market hours");

        registerResolutionHint("invalid_product_type",
                "Use valid product types: D, MIS, BO");

        registerResolutionHint("position_not_found",
                "Ensure you have an open position for the instrument");
    }

    @Override
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        ErrorResponse error = super.handleError(httpStatus, errorCode);

        // Add order-specific details based on error type
        if ("insufficient_funds".equals(errorCode)) {
            error.addDetail("category", "FUND_ERROR");
            error.addDetail("action_required", "ADD_FUNDS");
        } else if ("exchange_not_open".equals(errorCode)) {
            error.addDetail("category", "TIMING_ERROR");
            error.addDetail("action_required", "RETRY_LATER");
        } else if (errorCode.contains("invalid")) {
            error.addDetail("category", "VALIDATION_ERROR");
            error.addDetail("action_required", "FIX_REQUEST");
        }

        return error;
    }
}
