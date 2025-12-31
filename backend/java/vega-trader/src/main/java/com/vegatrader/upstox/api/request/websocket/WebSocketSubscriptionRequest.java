package com.vegatrader.upstox.api.request.websocket;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for WebSocket market data subscription.
 *
 * @since 2.0.0
 */
public class WebSocketSubscriptionRequest {

    @SerializedName("action")
    private String action;

    @SerializedName("mode")
    private String mode;

    @SerializedName("instrument_keys")
    private List<String> instrumentKeys;

    public WebSocketSubscriptionRequest() {
    }

    public static WebSocketSubscriptionRequestBuilder builder() {
        return new WebSocketSubscriptionRequestBuilder();
    }

    // Getters/Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<String> getInstrumentKeys() {
        return instrumentKeys;
    }

    public void setInstrumentKeys(List<String> instrumentKeys) {
        this.instrumentKeys = instrumentKeys;
    }

    public void validate() {
        if (action == null || action.isEmpty()) {
            throw new IllegalArgumentException("Action is required");
        }
        if (!"subscribe".equalsIgnoreCase(action) && !"unsubscribe".equalsIgnoreCase(action)) {
            throw new IllegalArgumentException("Action must be 'subscribe' or 'unsubscribe'");
        }
        if (instrumentKeys == null || instrumentKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one instrument key is required");
        }
        if (instrumentKeys.size() > 100) {
            throw new IllegalArgumentException("Maximum 100 instruments per subscription");
        }
    }

    public static class WebSocketSubscriptionRequestBuilder {
        private String action, mode;
        private List<String> instrumentKeys;

        public WebSocketSubscriptionRequestBuilder action(String action) {
            this.action = action;
            return this;
        }

        public WebSocketSubscriptionRequestBuilder subscribe() {
            this.action = "subscribe";
            return this;
        }

        public WebSocketSubscriptionRequestBuilder unsubscribe() {
            this.action = "unsubscribe";
            return this;
        }

        public WebSocketSubscriptionRequestBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public WebSocketSubscriptionRequestBuilder ltpMode() {
            this.mode = "ltp";
            return this;
        }

        public WebSocketSubscriptionRequestBuilder fullMode() {
            this.mode = "full";
            return this;
        }

        public WebSocketSubscriptionRequestBuilder instrumentKeys(List<String> instrumentKeys) {
            this.instrumentKeys = instrumentKeys;
            return this;
        }

        public WebSocketSubscriptionRequestBuilder instrumentKeys(String... instrumentKeys) {
            this.instrumentKeys = Arrays.asList(instrumentKeys);
            return this;
        }

        public WebSocketSubscriptionRequest build() {
            WebSocketSubscriptionRequest request = new WebSocketSubscriptionRequest();
            request.action = this.action;
            request.mode = this.mode;
            request.instrumentKeys = this.instrumentKeys;
            return request;
        }
    }

    @Override
    public String toString() {
        return String.format("WebSocketSubscription{action='%s', mode='%s', instruments=%d}",
                action, mode, instrumentKeys != null ? instrumentKeys.size() : 0);
    }
}
