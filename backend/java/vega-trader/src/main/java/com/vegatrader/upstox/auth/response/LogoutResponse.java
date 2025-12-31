package com.vegatrader.upstox.auth.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for logout operation.
 *
 * @since 2.0.0
 */
public class LogoutResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public LogoutResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Checks if logout was successful.
     *
     * @return true if status is "success"
     */
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return String.format("LogoutResponse{status='%s', message='%s'}", status, message);
    }
}
