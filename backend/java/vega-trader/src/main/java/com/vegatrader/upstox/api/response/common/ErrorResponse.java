package com.vegatrader.upstox.api.response.common;

import com.google.gson.annotations.SerializedName;
import com.vegatrader.upstox.api.errors.UpstoxHttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents error details in API responses.
 * <p>
 * This class provides detailed information about API errors including:
 * <ul>
 * <li>Error code (e.g., "insufficient_funds", "invalid_order_type")</li>
 * <li>Human-readable error message</li>
 * <li>HTTP status code</li>
 * <li>Additional error details (optional)</li>
 * <li>Resolution hints (optional)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * ErrorResponse error = new ErrorResponse(
 *         "insufficient_funds",
 *         "Insufficient balance to place order",
 *         422);
 * error.addDetail("available_balance", "10000.00");
 * error.addDetail("required_balance", "15000.00");
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class ErrorResponse {

    @SerializedName("errorCode")
    private String errorCode;

    @SerializedName("message")
    private String message;

    @SerializedName("httpStatus")
    private Integer httpStatus;

    @SerializedName("details")
    private Map<String, Object> details;

    @SerializedName("resolution")
    private String resolution;

    @SerializedName("timestamp")
    private Long timestamp;

    /**
     * Default constructor for JSON deserialization.
     */
    public ErrorResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Creates a new error response.
     *
     * @param errorCode  the error code
     * @param message    the error message
     * @param httpStatus the HTTP status code
     */
    public ErrorResponse(String errorCode, String message, Integer httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
        this.timestamp = System.currentTimeMillis();
        this.details = new HashMap<>();
    }

    /**
     * Creates a new error response with resolution hint.
     *
     * @param errorCode  the error code
     * @param message    the error message
     * @param httpStatus the HTTP status code
     * @param resolution the resolution hint
     */
    public ErrorResponse(String errorCode, String message, Integer httpStatus, String resolution) {
        this(errorCode, message, httpStatus);
        this.resolution = resolution;
    }

    /**
     * Builder for creating error responses.
     *
     * @return a new ErrorResponseBuilder
     */
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    /**
     * Adds a detail entry to the error.
     *
     * @param key   the detail key
     * @param value the detail value
     * @return this ErrorResponse for chaining
     */
    public ErrorResponse addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code.
     *
     * @param errorCode the error code to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the HTTP status code.
     *
     * @return the HTTP status code
     */
    public Integer getHttpStatus() {
        return httpStatus;
    }

    /**
     * Sets the HTTP status code.
     *
     * @param httpStatus the status code to set
     */
    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    /**
     * Gets the error details map.
     *
     * @return the details map
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Sets the error details map.
     *
     * @param details the details to set
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    /**
     * Gets the resolution hint.
     *
     * @return the resolution hint
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution hint.
     *
     * @param resolution the resolution to set
     */
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * Gets the timestamp when the error occurred.
     *
     * @return the timestamp in milliseconds
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns true if this is a client error (4xx).
     *
     * @return true if client error
     */
    public boolean isClientError() {
        return httpStatus != null && httpStatus >= 400 && httpStatus < 500;
    }

    /**
     * Returns true if this is a server error (5xx).
     *
     * @return true if server error
     */
    public boolean isServerError() {
        return httpStatus != null && httpStatus >= 500 && httpStatus < 600;
    }

    /**
     * Returns true if this is a rate limit error (429).
     *
     * @return true if rate limited
     */
    public boolean isRateLimitError() {
        return httpStatus != null && httpStatus == 429;
    }

    @Override
    public String toString() {
        return String.format("ErrorResponse{errorCode='%s', message='%s', httpStatus=%d, resolution='%s'}",
                errorCode, message, httpStatus, resolution);
    }

    /**
     * Builder class for ErrorResponse.
     */
    public static class ErrorResponseBuilder {
        private String errorCode;
        private String message;
        private Integer httpStatus;
        private String resolution;
        private Map<String, Object> details = new HashMap<>();

        public ErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder httpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public ErrorResponseBuilder httpStatus(UpstoxHttpStatus status) {
            this.httpStatus = status.getCode();
            return this;
        }

        public ErrorResponseBuilder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public ErrorResponseBuilder addDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse error = new ErrorResponse(errorCode, message, httpStatus, resolution);
            error.setDetails(details);
            return error;
        }
    }
}
