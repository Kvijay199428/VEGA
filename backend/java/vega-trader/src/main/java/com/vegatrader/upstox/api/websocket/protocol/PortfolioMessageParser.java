package com.vegatrader.upstox.api.websocket.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.vegatrader.upstox.api.response.websocket.*;
import com.vegatrader.upstox.api.websocket.PortfolioUpdate;
import com.vegatrader.upstox.api.websocket.event.PortfolioUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defensive parser for portfolio WebSocket messages.
 * 
 * <p>
 * ⚠️ CRITICAL: Extracts and validates timestamps for cache semantics.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Defensive parsing (never throws)</li>
 * <li>Timestamp extraction and validation</li>
 * <li>Schema drift detection</li>
 * <li>Type-based routing</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioMessageParser.class);

    private final ObjectMapper objectMapper;
    private final Gson gson;

    public PortfolioMessageParser() {
        this.objectMapper = new ObjectMapper();
        this.gson = new Gson();
    }

    /**
     * Parses JSON message to PortfolioUpdateEvent.
     * 
     * <p>
     * ⚠️ CRITICAL: Extracts timestamp for cache validation.
     * 
     * @param json the raw JSON message
     * @return PortfolioUpdateEvent or error event if parsing fails
     */
    public PortfolioUpdateEvent parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // Extract type
            String type = root.path("type").asText(null);
            if (type == null || type.isEmpty()) {
                logger.warn("Missing 'type' field in portfolio message");
                return createErrorEvent("Missing type field");
            }

            // CRITICAL: Extract timestamp
            long timestamp = root.path("timestamp").asLong(0);
            if (timestamp == 0) {
                logger.warn("Missing timestamp in portfolio update, using current time");
                timestamp = System.currentTimeMillis();
            }

            // Route based on type
            switch (type.toLowerCase()) {
                case "order":
                    return parseOrderUpdate(json, timestamp);

                case "holding":
                    return parseHoldingUpdate(json, timestamp);

                case "position":
                    return parsePositionUpdate(json, timestamp);

                case "gtt":
                    return parseGttUpdate(json, timestamp);

                default:
                    logger.warn("Unknown portfolio update type: {}", type);
                    return createUnknownEvent(type, json);
            }

        } catch (Exception e) {
            logger.error("Failed to parse portfolio message: {}", e.getMessage());
            return createErrorEvent(e.getMessage());
        }
    }

    private PortfolioUpdateEvent parseOrderUpdate(String json, long timestamp) {
        try {
            OrderUpdate order = gson.fromJson(json, OrderUpdate.class);

            // Ensure timestamp is set
            if (order.getTimestamp() == null) {
                order.setTimestamp(timestamp);
            }

            return new PortfolioUpdate(order);

        } catch (Exception e) {
            logger.error("Failed to parse order update: {}", e.getMessage());
            return createErrorEvent("Order parse error: " + e.getMessage());
        }
    }

    private PortfolioUpdateEvent parseHoldingUpdate(String json, long timestamp) {
        try {
            HoldingUpdate holding = gson.fromJson(json, HoldingUpdate.class);

            if (holding.getTimestamp() == null) {
                holding.setTimestamp(timestamp);
            }

            return new PortfolioUpdate(holding);

        } catch (Exception e) {
            logger.error("Failed to parse holding update: {}", e.getMessage());
            return createErrorEvent("Holding parse error: " + e.getMessage());
        }
    }

    private PortfolioUpdateEvent parsePositionUpdate(String json, long timestamp) {
        try {
            PositionUpdate position = gson.fromJson(json, PositionUpdate.class);

            if (position.getTimestamp() == null) {
                position.setTimestamp(timestamp);
            }

            return new PortfolioUpdate(position);

        } catch (Exception e) {
            logger.error("Failed to parse position update: {}", e.getMessage());
            return createErrorEvent("Position parse error: " + e.getMessage());
        }
    }

    private PortfolioUpdateEvent parseGttUpdate(String json, long timestamp) {
        try {
            GttUpdate gtt = gson.fromJson(json, GttUpdate.class);

            if (gtt.getTimestamp() == null) {
                gtt.setTimestamp(timestamp);
            }

            return new PortfolioUpdate(gtt);

        } catch (Exception e) {
            logger.error("Failed to parse GTT update: {}", e.getMessage());
            return createErrorEvent("GTT parse error: " + e.getMessage());
        }
    }

    private PortfolioUpdateEvent createErrorEvent(String message) {
        return new ErrorEvent(message);
    }

    private PortfolioUpdateEvent createUnknownEvent(String type, String json) {
        return new UnknownEvent(type, json);
    }

    // Error event for parse failures
    private static class ErrorEvent implements PortfolioUpdateEvent {
        private final String message;
        private final long timestamp;

        ErrorEvent(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String getUpdateType() {
            return "ERROR";
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
            return message;
        }

        @Override
        public String toString() {
            return "ErrorEvent{message='" + message + "'}";
        }
    }

    // Unknown event for schema drift detection
    private static class UnknownEvent implements PortfolioUpdateEvent {
        private final String type;
        private final String json;
        private final long timestamp;

        UnknownEvent(String type, String json) {
            this.type = type;
            this.json = json;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String getUpdateType() {
            return type;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String getEventType() {
            return "unknown";
        }

        @Override
        public Object getData() {
            return json;
        }

        @Override
        public String toString() {
            return "UnknownEvent{type='" + type + "', json='" +
                    (json.length() > 100 ? json.substring(0, 100) + "..." : json) + "'}";
        }
    }
}
