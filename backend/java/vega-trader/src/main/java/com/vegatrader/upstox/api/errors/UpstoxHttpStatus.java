package com.vegatrader.upstox.api.errors;

/**
 * HTTP status codes used by Upstox API.
 * <p>
 * This enum provides comprehensive information about HTTP status codes
 * including:
 * <ul>
 * <li>Status code number</li>
 * <li>Status message</li>
 * <li>Description of when this status is used</li>
 * <li>Helper methods to identify success/error types</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
public enum UpstoxHttpStatus {

    // 2xx Success
    /**
     * 200 OK - Request successful, data returned.
     */
    OK(200, "OK", "Request successful, data returned"),

    /**
     * 201 Created - Resource created successfully.
     */
    CREATED(201, "Created", "Resource created successfully (e.g., order placed, GTT created)"),

    /**
     * 202 Accepted - Request accepted for processing.
     */
    ACCEPTED(202, "Accepted", "Request accepted for asynchronous processing"),

    /**
     * 204 No Content - Request successful, no content to return.
     */
    NO_CONTENT(204, "No Content", "Request successful, no content returned (e.g., successful DELETE)"),

    // 4xx Client Errors
    /**
     * 400 Bad Request - Invalid parameters or malformed request.
     */
    BAD_REQUEST(400, "Bad Request", "Invalid parameters, missing required fields, or malformed request"),

    /**
     * 401 Unauthorized - Missing or invalid authorization token.
     */
    UNAUTHORIZED(401, "Unauthorized", "Missing, expired, or invalid access token"),

    /**
     * 403 Forbidden - Insufficient permissions.
     */
    FORBIDDEN(403, "Forbidden", "User doesn't have required permissions for this operation"),

    /**
     * 404 Not Found - Resource not found.
     */
    NOT_FOUND(404, "Not Found", "Order ID doesn't exist, instrument not found, or resource unavailable"),

    /**
     * 409 Conflict - Conflict in request.
     */
    CONFLICT(409, "Conflict", "Duplicate order, conflicting state, or operation not allowed"),

    /**
     * 422 Unprocessable Entity - Validation error.
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity",
            "Semantic errors, insufficient funds, or business logic validation failure"),

    /**
     * 429 Too Many Requests - Rate limit exceeded.
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests", "API rate limit exceeded, retry after delay"),

    // 5xx Server Errors
    /**
     * 500 Internal Server Error - Server error.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", "Exchange error, system error, or unexpected server failure"),

    /**
     * 502 Bad Gateway - Gateway error.
     */
    BAD_GATEWAY(502, "Bad Gateway", "Temporary service unavailability or gateway timeout"),

    /**
     * 503 Service Unavailable - Service temporarily unavailable.
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable", "Maintenance mode, exchange closed, or service temporarily down");

    private final int code;
    private final String message;
    private final String description;

    UpstoxHttpStatus(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * Gets the HTTP status code number.
     *
     * @return the status code (e.g., 200, 404, 500)
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the HTTP status message.
     *
     * @return the status message (e.g., "OK", "Not Found")
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the description of when this status is used.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns true if this is a success status (2xx).
     *
     * @return true if status code is between 200-299
     */
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }

    /**
     * Returns true if this is a client error (4xx).
     *
     * @return true if status code is between 400-499
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * Returns true if this is a server error (5xx).
     *
     * @return true if status code is between 500-599
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }

    /**
     * Returns true if this is an error status (4xx or 5xx).
     *
     * @return true if error
     */
    public boolean isError() {
        return isClientError() || isServerError();
    }

    /**
     * Finds a status by its code number.
     *
     * @param code the HTTP status code
     * @return the corresponding UpstoxHttpStatus, or null if not found
     */
    public static UpstoxHttpStatus fromCode(int code) {
        for (UpstoxHttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%d %s", code, message);
    }
}
