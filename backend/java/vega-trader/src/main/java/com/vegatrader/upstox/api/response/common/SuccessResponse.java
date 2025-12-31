package com.vegatrader.upstox.api.response.common;

import com.google.gson.annotations.SerializedName;

/**
 * Simple success response for operations that don't return data.
 * <p>
 * This class is used for API operations like DELETE, logout, or other
 * operations that only return a success confirmation without additional data.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Order cancellation
 * SuccessResponse cancelResponse = new SuccessResponse(
 *         "Order cancelled successfully",
 *         "240101000000001");
 * 
 * // Logout
 * SuccessResponse logoutResponse = SuccessResponse.withMessage("Logged out successfully");
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class SuccessResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("reference_id")
    private String referenceId;

    @SerializedName("timestamp")
    private Long timestamp;

    @SerializedName("status")
    private String status;

    /**
     * Default constructor for JSON deserialization.
     */
    public SuccessResponse() {
        this.status = "success";
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Creates a success response with a message.
     *
     * @param message the success message
     */
    public SuccessResponse(String message) {
        this();
        this.message = message;
    }

    /**
     * Creates a success response with message and reference ID.
     *
     * @param message     the success message
     * @param referenceId the reference ID (e.g., order_id, transaction_id)
     */
    public SuccessResponse(String message, String referenceId) {
        this(message);
        this.referenceId = referenceId;
    }

    /**
     * Creates a success response with only a message.
     *
     * @param message the success message
     * @return a new SuccessResponse
     */
    public static SuccessResponse withMessage(String message) {
        return new SuccessResponse(message);
    }

    /**
     * Creates a success response with message and reference.
     *
     * @param message     the success message
     * @param referenceId the reference ID
     * @return a new SuccessResponse
     */
    public static SuccessResponse withReference(String message, String referenceId) {
        return new SuccessResponse(message, referenceId);
    }

    /**
     * Gets the success message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the success message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the reference ID.
     *
     * @return the reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the reference ID.
     *
     * @param referenceId the reference ID to set
     */
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * Gets the timestamp when the operation succeeded.
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
     * Gets the status.
     *
     * @return the status (always "success")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("SuccessResponse{message='%s', referenceId='%s', timestamp=%d}",
                message, referenceId, timestamp);
    }
}
