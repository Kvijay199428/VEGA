package com.vegatrader.upstox.api.websocket.event;

/**
 * Unknown event type from Upstox WebSocket.
 * 
 * <p>
 * Handles schema drift gracefully by capturing unknown message types
 * for logging and monitoring without breaking the stream.
 * 
 * <p>
 * When Upstox adds new message types in V4 or V3 updates, this prevents
 * application crashes and allows monitoring of new schema additions.
 * 
 * @since 3.1.0
 */
public class UnknownEvent implements MarketUpdateEvent {

    private final String type;
    private final String rawJson;
    private final long timestamp;

    /**
     * Creates an unknown event.
     * 
     * @param type    the unknown message type from JSON
     * @param rawJson the raw JSON message (for debugging)
     */
    public UnknownEvent(String type, String rawJson) {
        this.type = type;
        this.rawJson = rawJson;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the unknown message type.
     * 
     * @return the type field from the JSON message
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the raw JSON message.
     * 
     * @return the complete JSON string
     */
    public String getRawJson() {
        return rawJson;
    }

    @Override
    public String getInstrumentKey() {
        return null; // Unknown events don't have instrument keys
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return "unknown:" + type;
    }

    @Override
    public Object getData() {
        return this;
    }

    @Override
    public String toString() {
        return "UnknownEvent{type='" + type + "', json='" +
                (rawJson.length() > 100 ? rawJson.substring(0, 100) + "..." : rawJson) + "'}";
    }
}
