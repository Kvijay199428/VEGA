package com.vegatrader.upstox.api.websocket.settings;

/**
 * Comprehensive settings for MarketDataStreamerV3.
 * 
 * <p>
 * Centralizes all configuration including connection, subscription limits,
 * timeouts, and feature flags.
 * 
 * @since 3.0.0
 */
public class MarketDataStreamerSettings {

    // Connection settings
    private String wsUrl = "wss://api.upstox.com/v3/feed/market-data-feed";
    private String authorizeUrl = "https://api.upstox.com/v3/feed/market-data-feed/authorize";
    private boolean useAuthorizeEndpoint = true;

    // Subscription settings
    private final ConnectionSettings connectionSettings;
    private com.vegatrader.upstox.api.websocket.Mode subscriptionMode = com.vegatrader.upstox.api.websocket.Mode.FULL;

    // Timeout settings (milliseconds)
    private long readTimeout = 30000;
    private long writeTimeout = 30000;
    private long connectTimeout = 10000;
    private int pingInterval = 30; // seconds

    // Auto-reconnect settings
    private boolean autoReconnectEnabled = true;
    private int reconnectInterval = 5; // seconds
    private int maxReconnectAttempts = 10;

    // Logging settings
    private boolean enableLogging = true;
    private String logFilePath = "logs/market-data-streamer.log";
    private boolean logMarketUpdates = false; // Can be verbose

    // Caching settings
    private boolean enableCaching = true;
    private int cacheTTL = 60; // seconds
    private int maxCacheSize = 10000;

    // Performance settings
    private int messageQueueSize = 1000;
    private int workerThreads = 8;
    private int bufferCapacity = 524288; // Enterprise upgrade: 512K capacity

    public MarketDataStreamerSettings() {
        this.connectionSettings = new ConnectionSettings(SubscriptionTier.NORMAL);
    }

    public MarketDataStreamerSettings(SubscriptionTier tier) {
        this.connectionSettings = new ConnectionSettings(tier);
    }

    // Connection Settings

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public boolean isUseAuthorizeEndpoint() {
        return useAuthorizeEndpoint;
    }

    public void setUseAuthorizeEndpoint(boolean useAuthorizeEndpoint) {
        this.useAuthorizeEndpoint = useAuthorizeEndpoint;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public SubscriptionTier getTier() {
        return connectionSettings.getTier();
    }

    // Timeout Settings

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

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    // Subscription Mode Settings

    public com.vegatrader.upstox.api.websocket.Mode getSubscriptionMode() {
        return subscriptionMode;
    }

    public void setSubscriptionMode(com.vegatrader.upstox.api.websocket.Mode mode) {
        this.subscriptionMode = mode;
    }

    // Auto-Reconnect Settings

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

    // Logging Settings

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public boolean isLogMarketUpdates() {
        return logMarketUpdates;
    }

    public void setLogMarketUpdates(boolean logMarketUpdates) {
        this.logMarketUpdates = logMarketUpdates;
    }

    // Caching Settings

    public boolean isEnableCaching() {
        return enableCaching;
    }

    public void setEnableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }

    public int getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    // Performance Settings

    public int getMessageQueueSize() {
        return messageQueueSize;
    }

    public void setMessageQueueSize(int messageQueueSize) {
        this.messageQueueSize = messageQueueSize;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    /**
     * Creates settings for Normal tier.
     * 
     * @return settings instance
     */
    public static MarketDataStreamerSettings createNormalTier() {
        return new MarketDataStreamerSettings(SubscriptionTier.NORMAL);
    }

    /**
     * Creates settings for Plus tier.
     * 
     * @return settings instance
     */
    public static MarketDataStreamerSettings createPlusTier() {
        return new MarketDataStreamerSettings(SubscriptionTier.PLUS);
    }

    @Override
    public String toString() {
        return String.format("MarketDataStreamerSettings{tier=%s, autoReconnect=%s, logging=%s, caching=%s}",
                getTier(), autoReconnectEnabled, enableLogging, enableCaching);
    }
}
