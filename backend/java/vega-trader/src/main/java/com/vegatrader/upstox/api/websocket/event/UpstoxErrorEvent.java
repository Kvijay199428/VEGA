package com.vegatrader.upstox.api.websocket.event;

/**
 * Error event from Upstox WebSocket.
 * 
 * <p>
 * Sent when subscription fails, limits exceeded, or other errors occur.
 * 
 * @since 3.1.0
 */
public class UpstoxErrorEvent implements MarketUpdateEvent {

    private final String code;
    private final String message;
    private final long timestamp;

    public UpstoxErrorEvent(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getInstrumentKey() {
        return null; // Errors don't have instruments
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return "error";
    }

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public String toString() {
        return "UpstoxErrorEvent{code='" + code + "', message='" + message + "'}";
    }
}
