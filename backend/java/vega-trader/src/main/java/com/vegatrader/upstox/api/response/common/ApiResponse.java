package com.vegatrader.upstox.api.response.common;

import com.google.gson.annotations.SerializedName;

/**
 * Generic API response wrapper for all Upstox API calls.
 * <p>
 * This class provides a standardized structure for all API responses,
 * ensuring consistent handling of success and error cases.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Success response
 * ApiResponse<UserProfile> response = new ApiResponse<>("success", userProfile, null);
 * 
 * // Error response
 * ApiResponse<Void> errorResponse = new ApiResponse<>("error", null, errorDetails);
 * 
 * // Check status
 * if (response.isSuccess()) {
 *     UserProfile data = response.getData();
 * }
 * }</pre>
 * </p>
 *
 * @param <T> the type of data contained in the response
 * @since 2.0.0
 */
public class ApiResponse<T> {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private T data;

    @SerializedName("errors")
    private ErrorResponse errors;

    /**
     * Default constructor for JSON deserialization.
     */
    public ApiResponse() {
    }

    /**
     * Creates a new API response.
     *
     * @param status the response status ("success" or "error")
     * @param data   the response data (null for errors)
     * @param errors error details (null for success)
     */
    public ApiResponse(String status, T data, ErrorResponse errors) {
        this.status = status;
        this.data = data;
        this.errors = errors;
    }

    /**
     * Creates a successful response.
     *
     * @param data the response data
     * @param <T>  the data type
     * @return a success ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    /**
     * Creates an error response.
     *
     * @param errors the error details
     * @param <T>    the expected data type
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorResponse errors) {
        return new ApiResponse<>("error", null, errors);
    }

    /**
     * Creates an error response from error code and message.
     *
     * @param errorCode the error code
     * @param message   the error message
     * @param <T>       the expected data type
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message, null);
        return new ApiResponse<>("error", null, errorResponse);
    }

    /**
     * Returns true if this is a successful response.
     *
     * @return true if status is "success"
     */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    /**
     * Returns true if this is an error response.
     *
     * @return true if status is "error"
     */
    public boolean isError() {
        return "error".equalsIgnoreCase(status);
    }

    /**
     * Gets the response status.
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the response status.
     *
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the response data.
     * <p>
     * Returns null for error responses.
     * </p>
     *
     * @return the response data
     */
    public T getData() {
        return data;
    }

    /**
     * Sets the response data.
     *
     * @param data the data to set
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Gets the error details.
     * <p>
     * Returns null for successful responses.
     * </p>
     *
     * @return the error details
     */
    public ErrorResponse getErrors() {
        return errors;
    }

    /**
     * Sets the error details.
     *
     * @param errors the errors to set
     */
    public void setErrors(ErrorResponse errors) {
        this.errors = errors;
    }

    /**
     * Gets the data if successful, throws exception if error.
     *
     * @return the response data
     * @throws UpstoxApiException if response is an error
     */
    public T getDataOrThrow() throws UpstoxApiException {
        if (isError()) {
            throw new UpstoxApiException(errors);
        }
        return data;
    }

    @Override
    public String toString() {
        return String.format("ApiResponse{status='%s', data=%s, errors=%s}",
                status, data, errors);
    }

    /**
     * Exception thrown when attempting to get data from an error response.
     */
    public static class UpstoxApiException extends Exception {
        private final ErrorResponse errorResponse;

        public UpstoxApiException(ErrorResponse errorResponse) {
            super(errorResponse != null ? errorResponse.getMessage() : "Unknown error");
            this.errorResponse = errorResponse;
        }

        public ErrorResponse getErrorResponse() {
            return errorResponse;
        }
    }
}
