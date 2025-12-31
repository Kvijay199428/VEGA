package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.errors.UpstoxErrorCode;
import com.vegatrader.upstox.api.errors.UpstoxHttpStatus;
import com.vegatrader.upstox.api.response.common.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Base error handler providing common error handling functionality.
 * <p>
 * All endpoint-specific error handlers extend this class.
 * </p>
 *
 * @since 2.0.0
 */
public abstract class BaseErrorHandler {

    protected final Map<String, String> resolutionHints = new HashMap<>();

    /**
     * Handles an error based on HTTP status and error code.
     *
     * @param httpStatus the HTTP status code
     * @param errorCode  the Upstox error code
     * @return ErrorResponse with details and resolution
     */
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        UpstoxHttpStatus status = UpstoxHttpStatus.fromCode(httpStatus);

        // Find matching Upstox error code
        UpstoxErrorCode upstoxError = findErrorCode(errorCode);

        String message = upstoxError != null ? upstoxError.getDescription()
                : (status != null ? status.getDescription() : "Unknown error occurred");

        String resolution = getResolutionHint(errorCode);

        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .httpStatus(httpStatus)
                .resolution(resolution)
                .build();
    }

    /**
     * Gets resolution hint for an error code.
     *
     * @param errorCode the error code
     * @return resolution hint
     */
    protected String getResolutionHint(String errorCode) {
        return resolutionHints.getOrDefault(errorCode,
                "Please check API documentation or contact support");
    }

    /**
     * Finds Upstox error code enum by code string.
     *
     * @param code the error code
     * @return UpstoxErrorCode or null
     */
    protected UpstoxErrorCode findErrorCode(String code) {
        for (UpstoxErrorCode error : UpstoxErrorCode.values()) {
            if (error.getCode().equalsIgnoreCase(code)) {
                return error;
            }
        }
        return null;
    }

    /**
     * Registers custom resolution hints.
     *
     * @param errorCode the error code
     * @param hint      the resolution hint
     */
    protected void registerResolutionHint(String errorCode, String hint) {
        resolutionHints.put(errorCode, hint);
    }
}
