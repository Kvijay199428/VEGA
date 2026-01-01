package com.vegatrader.upstox.auth.event;

import java.time.Instant;

/**
 * Auth Progress Event
 * 
 * Represents a single token generation event during batch authentication.
 * Emitted via SSE to provide real-time progress feedback to frontend.
 * 
 * @since Production Auth Architecture v2.0
 */
public class AuthProgressEvent {

    private String api; // "PRIMARY", "WEBSOCKET_1", etc.
    private String status; // "STARTED", "SUCCESS", "FAILED"
    private int completed; // Number of tokens completed
    private int total; // Total APIs (always 6)
    private Instant timestamp;
    private String message; // Optional error message

    public AuthProgressEvent() {
    }

    public AuthProgressEvent(String api, String status, int completed, int total, Instant timestamp, String message) {
        this.api = api;
        this.status = status;
        this.completed = completed;
        this.total = total;
        this.timestamp = timestamp;
        this.message = message;
    }

    // Getters and Setters
    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AuthProgressEvent{" +
                "api='" + api + '\'' +
                ", status='" + status + '\'' +
                ", completed=" + completed +
                ", total=" + total +
                ", message='" + message + '\'' +
                '}';
    }
}
