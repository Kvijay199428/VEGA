package com.vegatrader.upstox.api.request.websocket;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Main request wrapper for Market Data Feed V3 WebSocket subscriptions.
 * 
 * <p>
 * Used to subscribe, change mode, or unsubscribe from market data feeds.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * MarketDataFeedV3Request request = MarketDataFeedV3Request.builder()
 *         .subscribe()
 *         .mode(MarketDataMode.FULL)
 *         .instrumentKeys("NSE_INDEX|Nifty Bank", "NSE_EQ|INE002A01018")
 *         .build();
 * }</pre>
 * 
 * @since 3.0.0
 */
public class MarketDataFeedV3Request {

    /**
     * Globally unique identifier for the request.
     */
    @SerializedName("guid")
    private String guid;

    /**
     * Method for the request (sub, change_mode, unsub).
     */
    @SerializedName("method")
    private String method;

    /**
     * Subscription data containing mode and instrument keys.
     */
    @SerializedName("data")
    private MarketDataFeedV3SubscriptionData data;

    public MarketDataFeedV3Request() {
    }

    public MarketDataFeedV3Request(String guid, String method, MarketDataFeedV3SubscriptionData data) {
        this.guid = guid;
        this.method = method;
        this.data = data;
    }

    public MarketDataFeedV3Request(String method, MarketDataFeedV3SubscriptionData data) {
        this.guid = generateGuid();
        this.method = method;
        this.data = data;
    }

    /**
     * Creates a new builder instance.
     * 
     * @return a new MarketDataFeedV3RequestBuilder
     */
    public static MarketDataFeedV3RequestBuilder builder() {
        return new MarketDataFeedV3RequestBuilder();
    }

    /**
     * Generates a GUID for the request.
     * 
     * @return a random GUID string
     */
    public static String generateGuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    // Getters and Setters

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setMethod(MarketDataMethod method) {
        this.method = method.getValue();
    }

    /**
     * Gets the method as an enum.
     * 
     * @return the MarketDataMethod enum, or null if method is invalid
     */
    public MarketDataMethod getMethodEnum() {
        if (method != null) {
            try {
                return MarketDataMethod.fromValue(method);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public MarketDataFeedV3SubscriptionData getData() {
        return data;
    }

    public void setData(MarketDataFeedV3SubscriptionData data) {
        this.data = data;
    }

    /**
     * Validates the request.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (guid == null || guid.isEmpty()) {
            throw new IllegalArgumentException("GUID is required");
        }

        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("Method is required");
        }

        if (getMethodEnum() == null) {
            throw new IllegalArgumentException("Invalid method: " + method);
        }

        if (data == null) {
            throw new IllegalArgumentException("Subscription data is required");
        }

        data.validate();
    }

    @Override
    public String toString() {
        return String.format("MarketDataFeedV3Request{guid='%s', method='%s', data=%s}",
                guid, method, data);
    }

    /**
     * Builder class for MarketDataFeedV3Request.
     */
    public static class MarketDataFeedV3RequestBuilder {
        private String guid;
        private String method;
        private String mode;
        private List<String> instrumentKeys;

        public MarketDataFeedV3RequestBuilder() {
            this.guid = generateGuid();
        }

        /**
         * Sets a custom GUID for the request.
         * 
         * @param guid the GUID
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder guid(String guid) {
            this.guid = guid;
            return this;
        }

        /**
         * Sets the method.
         * 
         * @param method the method string
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        /**
         * Sets the method.
         * 
         * @param method the MarketDataMethod enum
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder method(MarketDataMethod method) {
            this.method = method.getValue();
            return this;
        }

        /**
         * Sets the method to 'sub' (subscribe).
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder subscribe() {
            this.method = MarketDataMethod.SUB.getValue();
            return this;
        }

        /**
         * Sets the method to 'change_mode'.
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder changeMode() {
            this.method = MarketDataMethod.CHANGE_MODE.getValue();
            return this;
        }

        /**
         * Sets the method to 'unsub' (unsubscribe).
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder unsubscribe() {
            this.method = MarketDataMethod.UNSUB.getValue();
            return this;
        }

        /**
         * Sets the subscription mode.
         * 
         * @param mode the mode string
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the subscription mode.
         * 
         * @param mode the MarketDataMode enum
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder mode(MarketDataMode mode) {
            this.mode = mode.getValue();
            return this;
        }

        /**
         * Sets the mode to LTPC.
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder ltpcMode() {
            this.mode = MarketDataMode.LTPC.getValue();
            return this;
        }

        /**
         * Sets the mode to OPTION_GREEKS.
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder optionGreeksMode() {
            this.mode = MarketDataMode.OPTION_GREEKS.getValue();
            return this;
        }

        /**
         * Sets the mode to FULL.
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder fullMode() {
            this.mode = MarketDataMode.FULL.getValue();
            return this;
        }

        /**
         * Sets the mode to FULL_D30.
         * 
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder fullD30Mode() {
            this.mode = MarketDataMode.FULL_D30.getValue();
            return this;
        }

        /**
         * Sets the instrument keys.
         * 
         * @param instrumentKeys the list of instrument keys
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder instrumentKeys(List<String> instrumentKeys) {
            this.instrumentKeys = instrumentKeys;
            return this;
        }

        /**
         * Sets the instrument keys.
         * 
         * @param instrumentKeys the varargs instrument keys
         * @return this builder
         */
        public MarketDataFeedV3RequestBuilder instrumentKeys(String... instrumentKeys) {
            this.instrumentKeys = Arrays.asList(instrumentKeys);
            return this;
        }

        /**
         * Builds the MarketDataFeedV3Request.
         * 
         * @return the constructed request
         */
        public MarketDataFeedV3Request build() {
            MarketDataFeedV3SubscriptionData subscriptionData = new MarketDataFeedV3SubscriptionData(mode,
                    instrumentKeys);

            MarketDataFeedV3Request request = new MarketDataFeedV3Request();
            request.guid = this.guid;
            request.method = this.method;
            request.data = subscriptionData;

            return request;
        }
    }
}
