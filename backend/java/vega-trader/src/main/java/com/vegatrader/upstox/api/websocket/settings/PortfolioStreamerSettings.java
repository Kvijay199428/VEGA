package com.vegatrader.upstox.api.websocket.settings;

/**
 * Comprehensive settings for PortfolioDataStreamerV2.
 * 
 * <p>
 * Centralized configuration for portfolio WebSocket streaming including
 * connection settings, feature flags, timeouts, and performance tuning.
 * 
 * @since 2.0.0
 */
public class PortfolioStreamerSettings {

    // Connection settings
    private String wsUrl = "wss://api.upstox.com/v2/feed/portfolio-stream-feed";
    private String authorizeUrl = "https://api.upstox.com/v2/feed/portfolio-stream-feed/authorize";
    private boolean useAuthorizeEndpoint = true;

    // Feature flags - enable/disable specific update types
    private boolean enableOrders = true;
    private boolean enableHoldings = true;
    private boolean enablePositions = true;
    private boolean enableGtt = true;

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
    private String logFilePath = "logs/portfolio-data-streamer.log";
    private boolean logPortfolioUpdates = false; // Can be verbose

    // Caching settings
    private boolean enableCaching = true;
    private int cacheTTL = 3600; // seconds (1 hour)
    private int maxCacheSize = 10000;

    // Performance settings
    private int bufferCapacity = 16384; // Lower than market data (portfolio is low volume)
    private int bufferOfferTimeoutMs = 50; // Timeout for bounded blocking offer

    public PortfolioStreamerSettings() {
    }

    // Factory methods

    /**
     * Creates default settings with all features enabled.
     */
    public static PortfolioStreamerSettings createDefault() {
        return new PortfolioStreamerSettings();
    }

    /**
     * Creates settings with all update types enabled.
     */
    public static PortfolioStreamerSettings createWithAllUpdates() {
        PortfolioStreamerSettings settings = new PortfolioStreamerSettings();
        settings.setEnableOrders(true);
        settings.setEnableHoldings(true);
        settings.setEnablePositions(true);
        settings.setEnableGtt(true);
        return settings;
    }

    /**
     * Creates settings with only order updates enabled.
     */
    public static PortfolioStreamerSettings createOrdersOnly() {
        PortfolioStreamerSettings settings = new PortfolioStreamerSettings();
        settings.setEnableOrders(true);
        settings.setEnableHoldings(false);
        settings.setEnablePositions(false);
        settings.setEnableGtt(false);
        return settings;
    }

    // Getters and Setters

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

    public boolean isEnableOrders() {
        return enableOrders;
    }

    public void setEnableOrders(boolean enableOrders) {
        this.enableOrders = enableOrders;
    }

    public boolean isEnableHoldings() {
        return enableHoldings;
    }

    public void setEnableHoldings(boolean enableHoldings) {
        this.enableHoldings = enableHoldings;
    }

    public boolean isEnablePositions() {
        return enablePositions;
    }

    public void setEnablePositions(boolean enablePositions) {
        this.enablePositions = enablePositions;
    }

    public boolean isEnableGtt() {
        return enableGtt;
    }

    public void setEnableGtt(boolean enableGtt) {
        this.enableGtt = enableGtt;
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

    public boolean isLogPortfolioUpdates() {
        return logPortfolioUpdates;
    }

    public void setLogPortfolioUpdates(boolean logPortfolioUpdates) {
        this.logPortfolioUpdates = logPortfolioUpdates;
    }

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

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public int getBufferOfferTimeoutMs() {
        return bufferOfferTimeoutMs;
    }

    public void setBufferOfferTimeoutMs(int bufferOfferTimeoutMs) {
        this.bufferOfferTimeoutMs = bufferOfferTimeoutMs;
    }

    @Override
    public String toString() {
        return String.format(
                "PortfolioStreamerSettings{orders=%s, holdings=%s, positions=%s, gtt=%s, autoReconnect=%s, caching=%s}",
                enableOrders, enableHoldings, enablePositions, enableGtt, autoReconnectEnabled, enableCaching);
    }
}
