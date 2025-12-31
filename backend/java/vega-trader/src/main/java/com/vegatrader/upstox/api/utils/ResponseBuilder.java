package com.vegatrader.upstox.api.utils;

import com.vegatrader.upstox.api.response.common.ApiResponse;
import com.vegatrader.upstox.api.response.common.ErrorResponse;
import com.vegatrader.upstox.api.response.common.PaginatedResponse;
import com.vegatrader.upstox.api.response.common.SuccessResponse;

import java.util.List;

/**
 * Utility class for building API responses.
 *
 * @since 2.0.0
 */
public final class ResponseBuilder {

    private ResponseBuilder() {
        // Utility class - no instantiation
    }

    /**
     * Creates a success response with data.
     *
     * @param data the response data
     * @param <T>  the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * Creates an error response.
     *
     * @param errorCode the error code
     * @param message   the error message
     * @param <T>       the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.error(errorCode, message);
    }

    /**
     * Creates an error response with HTTP status.
     *
     * @param errorCode  the error code
     * @param message    the error message
     * @param httpStatus the HTTP status code
     * @param <T>        the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, int httpStatus) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .httpStatus(httpStatus)
                .build();
        return ApiResponse.error(error);
    }

    /**
     * Creates a paginated response.
     *
     * @param data          the data list
     * @param pageNumber    the page number
     * @param pageSize      the page size
     * @param totalElements the total elements
     * @param <T>           the data type
     * @return ApiResponse with PaginatedResponse
     */
    public static <T> ApiResponse<PaginatedResponse<T>> paginated(
            List<T> data, int pageNumber, int pageSize, long totalElements) {

        PaginatedResponse<T> paginatedResponse = PaginatedResponse.<T>builder()
                .data(data)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .build();

        return ApiResponse.success(paginatedResponse);
    }

    /**
     * Creates a simple success response.
     *
     * @param message the success message
     * @return ApiResponse with SuccessResponse
     */
    public static ApiResponse<SuccessResponse> successMessage(String message) {
        SuccessResponse success = SuccessResponse.withMessage(message);
        return ApiResponse.success(success);
    }

    /**
     * Creates a success response with reference ID.
     *
     * @param message     the success message
     * @param referenceId the reference ID
     * @return ApiResponse with SuccessResponse
     */
    public static ApiResponse<SuccessResponse> successWithReference(String message, String referenceId) {
        SuccessResponse success = SuccessResponse.withReference(message, referenceId);
        return ApiResponse.success(success);
    }

    /**
     * Creates an unauthorized error response.
     *
     * @param <T> the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> unauthorized() {
        return error("unauthorized", "Access token is invalid or expired", 401);
    }

    /**
     * Creates a forbidden error response.
     *
     * @param <T> the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> forbidden() {
        return error("forbidden", "Access to resource is forbidden", 403);
    }

    /**
     * Creates a not found error response.
     *
     * @param resource the resource name
     * @param <T>      the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> notFound(String resource) {
        return error("not_found", resource + " not found", 404);
    }

    /**
     * Creates a rate limit exceeded error response.
     *
     * @param <T> the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> rateLimitExceeded() {
        return error("rate_limit_exceeded", "Rate limit exceeded. Please try again later", 429);
    }

    /**
     * Creates a validation error response.
     *
     * @param message the validation message
     * @param <T>     the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return error("validation_error", message, 422);
    }

    /**
     * Creates a server error response.
     *
     * @param <T> the data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> serverError() {
        return error("server_error", "Internal server error", 500);
    }
}
