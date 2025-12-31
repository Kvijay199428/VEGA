package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.response.common.ErrorResponse;

/**
 * Error handler for option chain specific errors.
 *
 * @since 2.0.0
 */
public class OptionChainErrorHandler extends BaseErrorHandler {

    public OptionChainErrorHandler() {
        registerResolutionHint("invalid_expiry_date",
                "Expiry date format should be YYYY-MM-DD and must be a valid expiry date");

        registerResolutionHint("expiry_not_found",
                "No expiry date found for the specified date. Check available expiries");

        registerResolutionHint("invalid_underlying",
                "Underlying instrument not found or does not have options");

        registerResolutionHint("option_chain_not_available",
                "Option chain data not available for this instrument/expiry combination");
    }

    @Override
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        ErrorResponse error = super.handleError(httpStatus, errorCode);
        error.addDetail("category", "OPTION_CHAIN_ERROR");

        if (errorCode.contains("expiry")) {
            error.addDetail("action_required", "VERIFY_EXPIRY_DATE");
        } else if (errorCode.contains("underlying")) {
            error.addDetail("action_required", "CHECK_INSTRUMENT");
        }

        return error;
    }
}
