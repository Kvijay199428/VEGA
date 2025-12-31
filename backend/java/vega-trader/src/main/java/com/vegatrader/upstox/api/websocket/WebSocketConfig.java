package com.vegatrader.upstox.api.websocket;

/**
 * Configuration class for Market Data WebSocket V3 connection.
 * 
 * @since 3.0.0
 */
public class WebSocketConfig {

    /**
     * Default WebSocket URL for market data feed.
     */
    public static final String DEFAULT_WS_URL = "wss://api.upstox.com/v3/feed/market-data-feed";

    /**
     * Default auto-reconnect enabled flag.
     */
    public static final boolean DEFAULT_AUTO_RECONNECT = true;

    /**
     * Default reconnect interval in seconds.
     */
    public static final int DEFAULT_RECONNECT_INTERVAL = 5;

    /**
     * Default maximum reconnect attempts.
     */
    public static final int DEFAULT_MAX_RECONNECT_ATTEMPTS = 10;

    /**
     * Default read timeout in milliseconds.
     */
    public static final long DEFAULT_READ_TIMEOUT_MS = 30000;

    /**
     * Default write timeout in milliseconds.
     */
    public static final long DEFAULT_WRITE_TIMEOUT_MS = 30000;

    /**
     * Default ping interval in seconds.
     */
    public static final int DEFAULT_PING_INTERVAL = 30;

    private String wsUrl;
    private boolean autoReconnectEnabled;
    private int reconnectInterval;
    private int maxReconnectAttempts;
    private long readTimeout;
    private long writeTimeout;
    private int pingInterval;

    public WebSocketConfig() {
        this.wsUrl = DEFAULT_WS_URL;
        this.autoReconnectEnabled = DEFAULT_AUTO_RECONNECT;
        this.reconnectInterval = DEFAULT_RECONNECT_INTERVAL;
        this.maxReconnectAttempts = DEFAULT_MAX_RECONNECT_ATTEMPTS;
        this.readTimeout = DEFAULT_READ_TIMEOUT_MS;
        this.writeTimeout = DEFAULT_WRITE_TIMEOUT_MS;
        this.pingInterval = DEFAULT_PING_INTERVAL;
    }

    // Getters and Setters

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public boolean isAutoReconnectEnabled() {
        return autoReconnectEnabled;
    }

    public void setAutoReconnectEnabled(boolean autoReconnectEnabled) {
        this.autoReconnectEnabled = autoReconnectEnabled;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public void setMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    @Override
    public String toString() {
        return String.format("WebSocketConfig{url='%s', autoReconnect=%s, interval=%ds, maxAttempts=%d}",
                wsUrl, autoReconnectEnabled, reconnectInterval, maxReconnectAttempts);
    }
}
