package com.vegatrader.upstox.api.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vegatrader.upstox.api.response.websocket.*;
import com.vegatrader.upstox.api.websocket.buffer.PortfolioBufferConsumer;
import com.vegatrader.upstox.api.websocket.buffer.PortfolioDataBuffer;
import com.vegatrader.upstox.api.websocket.bus.EventBus;
import com.vegatrader.upstox.api.websocket.bus.InMemoryEventBus;
import com.vegatrader.upstox.api.websocket.cache.PortfolioDataCache;
import com.vegatrader.upstox.api.websocket.event.PortfolioUpdateEvent;
import com.vegatrader.upstox.api.websocket.listener.*;
import com.vegatrader.upstox.api.websocket.logging.PortfolioDataStreamerLogger;
import com.vegatrader.upstox.api.websocket.metrics.PortfolioMetricsCollector;
import com.vegatrader.upstox.api.websocket.protocol.PortfolioMessageParser;
import com.vegatrader.upstox.api.websocket.settings.PortfolioConnectionSettings;
import com.vegatrader.upstox.api.websocket.settings.PortfolioStreamerSettings;
import com.vegatrader.upstox.api.websocket.state.PortfolioFeedState;
import com.vegatrader.upstox.api.websocket.state.PortfolioStateTracker;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Enterprise-grade Portfolio WebSocket streamer V2.
 * 
 * <p>
 * ⚠️ CRITICAL ARCHITECTURE:
 * 
 * <pre>
 * WebSocket Thread → handleMessage()
 *                        ↓
 *               PortfolioMessageParser.parse()
 *                        ↓
 *               PortfolioDataBuffer.offerWithTimeout() [bounded blocking]
 *                        ↓ (if timeout)
 *               triggerControlledReconnect()
 *                        ↓
 *            SINGLE Consumer Thread (PortfolioBufferConsumer)
 *                        ↓
 *               EventBus.publish(PortfolioUpdateEvent) [ORDERED]
 *                        ↓
 *       Subscribers (cache, metrics, user listeners) [ISOLATED]
 * </pre>
 * 
 * <p>
 * Key guarantees:
 * <ul>
 * <li>✅ NO-DROP: Events never silently dropped</li>
 * <li>✅ STRICT ORDERING: Single-threaded sequential processing</li>
 * <li>✅ TIMESTAMP VALIDATION: Last-write-wins cache semantics</li>
 * <li>✅ STATE TRACKING: Feed health monitoring</li>
 * <li>✅ SUBSCRIBER ISOLATION: Failures don't cascade</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioDataStreamerV2 {

    // Core components
    private final PortfolioStreamerSettings settings;
    private final PortfolioConnectionSettings connectionSettings;
    private final EventBus eventBus;
    private final PortfolioDataBuffer buffer;
    private final PortfolioDataCache cache;
    private final PortfolioStateTracker stateTracker;
    private final PortfolioMetricsCollector metrics;
    private final PortfolioDataStreamerLogger logger;
    private final PortfolioMessageParser parser;

    // WebSocket
    private OkHttpClient client;
    private WebSocket webSocket;
    private String accessToken;

    // Threading
    private ExecutorService consumerExecutor;
    private ScheduledExecutorService reconnectExecutor;

    // State
    private volatile boolean connected = false;
    private volatile boolean autoReconnectEnabled = false;
    private volatile int reconnectAttempts = 0;

    // Listeners
    private OnOpenListener onOpenListener;
    private OnCloseListener onCloseListener;
    private OnErrorListener onErrorListener;
    private OnReconnectingListener onReconnectingListener;
    private OnAutoReconnectStoppedListener onAutoReconnectStoppedListener;
    private OnOrderUpdateListener onOrderUpdateListener;
    private OnHoldingUpdateListener onHoldingUpdateListener;
    private OnPositionUpdateListener onPositionUpdateListener;
    private OnGttUpdateListener onGttUpdateListener;

    /**
     * Creates portfolio streamer with default settings.
     */
    public PortfolioDataStreamerV2(String accessToken) {
        this(accessToken, PortfolioStreamerSettings.createDefault());
    }

    /**
     * Creates portfolio streamer with custom settings.
     */
    public PortfolioDataStreamerV2(String accessToken, PortfolioStreamerSettings settings) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("accessToken must not be null or empty");
        }
        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null");
        }

        this.accessToken = accessToken;
        this.settings = settings;
        this.connectionSettings = new PortfolioConnectionSettings();

        // Initialize components
        this.eventBus = new InMemoryEventBus();
        this.buffer = new PortfolioDataBuffer(settings.getBufferCapacity());
        this.cache = new PortfolioDataCache(
                settings.getCacheTTL(),
                settings.getMaxCacheSize(),
                settings.isEnableCaching());
        this.stateTracker = new PortfolioStateTracker();
        this.metrics = new PortfolioMetricsCollector();
        this.logger = new PortfolioDataStreamerLogger(
                getClass(),
                settings.getLogFilePath(),
                settings.isEnableLogging(),
                settings.isLogPortfolioUpdates());
        this.parser = new PortfolioMessageParser();

        // Setup event subscribers with isolation
        setupEventSubscribers();

        // Initialize OkHttp client
        this.client = new OkHttpClient.Builder()
                .readTimeout(settings.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(settings.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(settings.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .pingInterval(settings.getPingInterval(), TimeUnit.SECONDS)
                .followRedirects(true) // 302 Redirect handling
                .followSslRedirects(true)
                .build();

        logger.info("PortfolioDataStreamerV2 initialized with settings: {}", settings);
    }

    /**
     * Connects to portfolio WebSocket feed.
     * 
     * <p>
     * Per Documentation 1 & 2:
     * <ol>
     * <li>Call authorize endpoint to get single-use WSS URL</li>
     * <li>Connect to WebSocket with Authorization header</li>
     * <li>Server pushes updates automatically (no subscription needed)</li>
     * </ol>
     */
    public synchronized void connect() {
        if (connected) {
            logger.warn("Already connected");
            return;
        }

        try {
            connectionSettings.incrementConnections();
            stateTracker.transitionTo(PortfolioFeedState.CONNECTING, "connect() called");

            // Start single consumer thread
            startConsumer();

            // Step 1: Get authorized WebSocket URL (Doc 1)
            String wsUrl = getAuthorizedUrl();
            logger.info("Connecting to authorized URL");

            // Step 2: Build WebSocket request (Doc 2)
            Request request = new Request.Builder()
                    .url(wsUrl)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Accept", "*/*")
                    .build();

            // Connect
            webSocket = client.newWebSocket(request, new PortfolioWebSocketListener());
            logger.logConnectionAttempt(1);

        } catch (Exception e) {
            logger.logConnectionError("Failed to connect", e);
            stateTracker.transitionTo(PortfolioFeedState.DISCONNECTED, "Connection failed");
            connectionSettings.decrementConnections();
            throw new RuntimeException("Connection failed", e);
        }
    }

    /**
     * Disconnects from WebSocket feed.
     */
    public synchronized void disconnect() {
        if (!connected) {
            return;
        }

        logger.info("Disconnecting...");
        connected = false;
        autoReconnectEnabled = false;

        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
        }

        stopConsumer();
        stateTracker.transitionTo(PortfolioFeedState.DISCONNECTED, "disconnect() called");
        connectionSettings.decrementConnections();
    }

    /**
     * Triggers controlled reconnect on buffer saturation or errors.
     * 
     * <p>
     * ⚠️ CRITICAL: This prevents silent data loss by forcing resync.
     */
    public synchronized void triggerControlledReconnect(String reason) {
        logger.logControlledReconnect(reason);
        metrics.incrementReconnectAttempts();

        // Disconnect
        if (webSocket != null) {
            webSocket.close(1001, "Controlled reconnect: " + reason);
        }

        // Clear buffer to prevent further saturation
        buffer.clear();

        // Attempt reconnect
        if (settings.isAutoReconnectEnabled()) {
            scheduleReconnect();
        }
    }

    /**
     * Forces full resync - clears all state and reconnects.
     */
    public synchronized void forceResync() {
        logger.logForceResync("Manual resync requested");

        // Clear all state
        buffer.clear();
        cache.clear();
        metrics.reset();

        // Reconnect
        disconnect();
        try {
            Thread.sleep(1000); // Brief pause
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connect();
    }

    /**
     * Enables auto-reconnect with default settings.
     */
    public void autoReconnect(boolean enable) {
        this.autoReconnectEnabled = enable;
        if (enable) {
            logger.info("Auto-reconnect enabled");
        }
    }

    // Listener setters

    public void setOnOpenListener(OnOpenListener listener) {
        this.onOpenListener = listener;
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.onErrorListener = listener;
    }

    public void setOnReconnectingListener(OnReconnectingListener listener) {
        this.onReconnectingListener = listener;
    }

    public void setOnAutoReconnectStoppedListener(OnAutoReconnectStoppedListener listener) {
        this.onAutoReconnectStoppedListener = listener;
    }

    public void setOnOrderUpdateListener(OnOrderUpdateListener listener) {
        this.onOrderUpdateListener = listener;
    }

    public void setOnHoldingUpdateListener(OnHoldingUpdateListener listener) {
        this.onHoldingUpdateListener = listener;
    }

    public void setOnPositionUpdateListener(OnPositionUpdateListener listener) {
        this.onPositionUpdateListener = listener;
    }

    public void setOnGttUpdateListener(OnGttUpdateListener listener) {
        this.onGttUpdateListener = listener;
    }

    // State queries

    public boolean isConnected() {
        return connected;
    }

    public PortfolioFeedState getState() {
        return stateTracker.getState();
    }

    public PortfolioMetricsCollector.PortfolioMetrics getMetrics() {
        return metrics.getMetricsSnapshot();
    }

    public PortfolioDataBuffer.BufferStatistics getBufferStatistics() {
        return buffer.getStatistics();
    }

    public PortfolioDataCache.CacheStatistics getCacheStatistics() {
        return cache.getStatistics();
    }

    /**
     * Shuts down streamer and releases resources.
     */
    public synchronized void shutdown() {
        logger.info("Shutting down PortfolioDataStreamerV2");
        disconnect();
        stopConsumer();
        cache.shutdown();
        logger.close();

        if (reconnectExecutor != null && !reconnectExecutor.isShutdown()) {
            reconnectExecutor.shutdown();
        }

        // Cleanup OkHttp threads
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            try {
                if (client.cache() != null) {
                    client.cache().close();
                }
            } catch (Exception e) {
                logger.error("Error closing OkHttp cache", e);
            }
        }
    }

    // Private methods

    /**
     * Gets authorized WebSocket URL from Upstox authorize endpoint.
     * 
     * <p>
     * Per Documentation 1: Must call authorize endpoint first.
     * The returned URL is single-use.
     * 
     * @return The authorized WebSocket URL
     * @throws IOException if authorization fails
     */
    private String getAuthorizedUrl() throws IOException {
        if (!settings.isUseAuthorizeEndpoint()) {
            // Direct connection (for testing only)
            return buildWsUrlWithUpdateTypes();
        }

        // Build authorize URL with update_types query parameter
        String authorizeUrl = settings.getAuthorizeUrl();
        String updateTypesQuery = buildUpdateTypesQuery();
        if (!updateTypesQuery.isEmpty()) {
            authorizeUrl += "?" + updateTypesQuery;
        }

        logger.info("Calling authorize endpoint: {}", authorizeUrl);

        Request request = new Request.Builder()
                .url(authorizeUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Authorization failed with code: " + response.code());
            }

            String body = response.body() != null ? response.body().string() : "";
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(body, JsonObject.class);
            JsonObject data = root.getAsJsonObject("data");

            // Use snake_case field (non-deprecated per documentation)
            String wsUrl = data.get("authorized_redirect_uri").getAsString();
            logger.info("Authorized WebSocket URL obtained successfully");
            return wsUrl;
        }
    }

    /**
     * Builds update_types query parameter based on enabled settings.
     * 
     * @return Query string like "update_types=order,position,holding"
     */
    private String buildUpdateTypesQuery() {
        StringBuilder types = new StringBuilder();

        if (settings.isEnableOrders()) {
            types.append("order");
        }
        if (settings.isEnablePositions()) {
            if (types.length() > 0)
                types.append(",");
            types.append("position");
        }
        if (settings.isEnableHoldings()) {
            if (types.length() > 0)
                types.append(",");
            types.append("holding");
        }
        if (settings.isEnableGtt()) {
            if (types.length() > 0)
                types.append(",");
            types.append("gtt_order");
        }

        return types.length() > 0 ? "update_types=" + types : "";
    }

    /**
     * Builds WebSocket URL with update_types query parameter (for direct
     * connection).
     * 
     * @return WSS URL with query params
     */
    private String buildWsUrlWithUpdateTypes() {
        String baseUrl = settings.getWsUrl();
        String query = buildUpdateTypesQuery();
        return query.isEmpty() ? baseUrl : baseUrl + "?" + query;
    }

    private void startConsumer() {
        if (consumerExecutor == null || consumerExecutor.isShutdown()) {
            consumerExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("PortfolioConsumer-1");
                t.setDaemon(false);
                return t;
            });

            PortfolioBufferConsumer consumer = new PortfolioBufferConsumer(buffer, eventBus, "consumer-1");
            consumerExecutor.submit(consumer);
            logger.info("Started single consumer thread");
        }
    }

    private void stopConsumer() {
        if (consumerExecutor != null && !consumerExecutor.isShutdown()) {
            consumerExecutor.shutdownNow();
            try {
                consumerExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("Stopped consumer thread");
        }
    }

    private void scheduleReconnect() {
        if (reconnectAttempts >= settings.getMaxReconnectAttempts()) {
            logger.logReconnectStopped("Max attempts reached");
            if (onAutoReconnectStoppedListener != null) {
                onAutoReconnectStoppedListener.onStopped("Max attempts reached");
            }
            return;
        }

        if (reconnectExecutor == null || reconnectExecutor.isShutdown()) {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        reconnectAttempts++;
        int delay = settings.getReconnectInterval();

        logger.logReconnectAttempt(reconnectAttempts, settings.getMaxReconnectAttempts());

        reconnectExecutor.schedule(() -> {
            try {
                connect();
            } catch (Exception e) {
                logger.error("Reconnect failed", e);
                scheduleReconnect(); // Try again
            }
        }, delay, TimeUnit.SECONDS);
    }

    private void setupEventSubscribers() {
        // Subscribe to portfolio updates with isolation
        eventBus.subscribe(PortfolioUpdateEvent.class, event -> {
            try {
                handlePortfolioEvent(event);
            } catch (Exception e) {
                logger.error("Subscriber error: {}", e.getMessage(), e);
                metrics.incrementSubscriberErrors();
            }
        });
    }

    private void handlePortfolioEvent(PortfolioUpdateEvent event) {
        // Update cache
        if (event instanceof PortfolioUpdate) {
            PortfolioUpdate update = (PortfolioUpdate) event;
            updateCache(update);

            // Invoke user listeners only if LIVE
            if (stateTracker.isLive()) {
                invokeUserListeners(update);
            }
        }

        metrics.incrementUpdatesProcessed();
    }

    private void updateCache(PortfolioUpdate update) {
        if (update.isOrderUpdate() && update.getOrderUpdate() != null) {
            cache.putOrder(update.getOrderUpdate());
            metrics.incrementOrderUpdates();
        } else if (update.isHoldingUpdate() && update.getHoldingUpdate() != null) {
            cache.putHolding(update.getHoldingUpdate());
            metrics.incrementHoldingUpdates();
        } else if (update.isPositionUpdate() && update.getPositionUpdate() != null) {
            cache.putPosition(update.getPositionUpdate());
            metrics.incrementPositionUpdates();
        } else if (update.isGttUpdate() && update.getGttUpdate() != null) {
            cache.putGtt(update.getGttUpdate());
            metrics.incrementGttUpdates();
        }
    }

    private void invokeUserListeners(PortfolioUpdate update) {
        try {
            if (update.isOrderUpdate() && onOrderUpdateListener != null) {
                onOrderUpdateListener.onUpdate(update);
            } else if (update.isHoldingUpdate() && onHoldingUpdateListener != null) {
                onHoldingUpdateListener.onUpdate(update);
            } else if (update.isPositionUpdate() && onPositionUpdateListener != null) {
                onPositionUpdateListener.onUpdate(update);
            } else if (update.isGttUpdate() && onGttUpdateListener != null) {
                onGttUpdateListener.onUpdate(update);
            }
        } catch (Exception e) {
            logger.error("User listener error: {}", e.getMessage(), e);
        }
    }

    // WebSocket listener

    private class PortfolioWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            connected = true;
            reconnectAttempts = 0;
            stateTracker.transitionTo(PortfolioFeedState.SYNCING, "WebSocket opened");
            logger.logConnectionSuccess();

            if (onOpenListener != null) {
                onOpenListener.onOpen();
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            try {
                metrics.incrementUpdatesReceived();

                // Parse message
                PortfolioUpdateEvent event = parser.parse(text);

                // Transition to LIVE after first message
                if (stateTracker.getState() == PortfolioFeedState.SYNCING) {
                    stateTracker.transitionTo(PortfolioFeedState.LIVE, "Received first update");
                }

                // Offer to buffer with timeout
                boolean accepted = buffer.offerWithTimeout(
                        (PortfolioUpdate) event,
                        settings.getBufferOfferTimeoutMs(),
                        TimeUnit.MILLISECONDS);

                if (!accepted) {
                    // ⚠️ CRITICAL: Buffer saturated - trigger controlled reconnect
                    metrics.incrementBufferSaturations();
                    logger.logBufferSaturation(buffer.getUtilizationPercent());
                    triggerControlledReconnect("Buffer saturation");
                }

            } catch (Exception e) {
                logger.logParsingError("Message handling failed", e);
                metrics.incrementParseErrors();
                stateTracker.transitionTo(PortfolioFeedState.DEGRADED, "Parse errors");
            }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            logger.logConnectionClosed(code, reason);
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            connected = false;
            stateTracker.transitionTo(PortfolioFeedState.DISCONNECTED, "WebSocket closed");
            logger.logConnectionClosed(code, reason);

            if (onCloseListener != null) {
                onCloseListener.onClose(code, reason);
            }

            if (autoReconnectEnabled && settings.isAutoReconnectEnabled()) {
                scheduleReconnect();
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
            connected = false;

            // Check for 401 Unauthorized
            boolean isAuthFailure = t.getMessage() != null && t.getMessage().contains("401");
            if (response != null && response.code() == 401) {
                isAuthFailure = true;
            }

            if (isAuthFailure) {
                logger.error("Authentication failed: Invalid or expired Access Token. Auto-reconnect disabled.", t);
                stateTracker.transitionTo(PortfolioFeedState.DISCONNECTED, "Authentication failure");
                autoReconnectEnabled = false; // Disable auto-reconnect permanently
            } else {
                stateTracker.transitionTo(PortfolioFeedState.DISCONNECTED, "WebSocket failure");
                logger.logConnectionError("WebSocket failure",
                        t instanceof Exception ? (Exception) t : new Exception(t));
            }

            if (onErrorListener != null) {
                onErrorListener.onError(t instanceof Exception ? (Exception) t : new Exception(t));
            }

            // Only schedule reconnect if NOT an auth failure and enabled
            if (!isAuthFailure && autoReconnectEnabled && settings.isAutoReconnectEnabled()) {
                scheduleReconnect();
            }
        }
    }
}
