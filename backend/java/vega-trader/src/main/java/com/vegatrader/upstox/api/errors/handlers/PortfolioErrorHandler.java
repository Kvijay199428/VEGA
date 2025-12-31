package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.response.common.ErrorResponse;

/**
 * Error handler for portfolio specific errors.
 *
 * @since 2.0.0
 */
public class PortfolioErrorHandler extends BaseErrorHandler {

    public PortfolioErrorHandler() {
        registerResolutionHint("position_not_found",
                "No open position found for the specified instrument");

        registerResolutionHint("holding_not_found",
                "No holding found for the specified instrument");

        registerResolutionHint("insufficient_quantity",
                "Available quantity is less than requested quantity");

        registerResolutionHint("conversion_not_allowed",
                "Position conversion not allowed for this product type");

        registerResolutionHint("invalid_product_conversion",
                "Cannot convert between specified product types");
    }

    @Override
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        ErrorResponse error = super.handleError(httpStatus, errorCode);
        error.addDetail("category", "PORTFOLIO_ERROR");

        if (errorCode.contains("not_found")) {
            error.addDetail("action_required", "VERIFY_POSITION");
        } else if (errorCode.contains("conversion")) {
            error.addDetail("action_required", "CHECK_PRODUCT_RULES");
        }

        return error;
    }
}
