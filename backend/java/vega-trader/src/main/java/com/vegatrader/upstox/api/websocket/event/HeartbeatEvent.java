package com.vegatrader.upstox.api.websocket.event;

/**
 * Heartbeat event from Upstox WebSocket.
 * 
 * <p>
 * Sent periodically to maintain connection and detect network issues.
 * 
 * @since 3.1.0
 */
public class HeartbeatEvent implements MarketUpdateEvent {

    private final long timestamp;

    public HeartbeatEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public HeartbeatEvent(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getInstrumentKey() {
        return null; // Heartbeats don't have instruments
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return "heartbeat";
    }

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public String toString() {
        return "HeartbeatEvent{timestamp=" + timestamp + "}";
    }
}
