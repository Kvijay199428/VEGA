package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.response.common.ErrorResponse;

/**
 * Error handler for market data specific errors.
 *
 * @since 2.0.0
 */
public class MarketDataErrorHandler extends BaseErrorHandler {

    public MarketDataErrorHandler() {
        registerResolutionHint("invalid_instrument_key",
                "Verify instrument key format: EXCHANGE|ISIN (e.g., NSE_EQ|INE528G01035)");

        registerResolutionHint("instrument_not_found",
                "Check if instrument is listed and trading");

        registerResolutionHint("invalid_interval",
                "Use valid intervals: 1minute, 5minute, 15minute, 30minute, 60minute, 1day, 1week, 1month");

        registerResolutionHint("invalid_date_range",
                "Ensure from_date is before to_date and within allowed historical data range");

        registerResolutionHint("data_not_available",
                "Historical data may not be available for this instrument/date range");
    }

    @Override
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        ErrorResponse error = super.handleError(httpStatus, errorCode);

        if (errorCode.contains("instrument")) {
            error.addDetail("category", "INSTRUMENT_ERROR");
        } else if (errorCode.contains("date") || errorCode.contains("interval")) {
            error.addDetail("category", "PARAMETER_ERROR");
        }

        return error;
    }
}
