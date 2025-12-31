package com.vegatrader.upstox.api.websocket.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Specialized logger for Market Data Streamer V3 operations.
 * 
 * <p>
 * Provides structured logging with separate log files and performance metrics.
 * 
 * @since 3.0.0
 */
public class MarketDataStreamerV3Logger {

    private final Logger logger;
    private final boolean enabled;
    private final boolean logMarketUpdates;
    private final String logFilePath;

    // Performance metrics
    private long messagesReceived = 0;
    private long messagesProcessed = 0;
    private long connectionAttempts = 0;
    private long successfulConnections = 0;
    private long errors = 0;

    public MarketDataStreamerV3Logger(Class<?> clazz, String logFilePath, boolean enabled, boolean logMarketUpdates) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.logFilePath = logFilePath;
        this.enabled = enabled;
        this.logMarketUpdates = logMarketUpdates;

        ensureLogDirectory();
    }

    public MarketDataStreamerV3Logger(Class<?> clazz) {
        this(clazz, "logs/market-data-streamer-v3.log", true, false);
    }

    private void ensureLogDirectory() {
        if (logFilePath != null) {
            File logFile = new File(logFilePath);
            File logDir = logFile.getParentFile();
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs();
            }
        }
    }

    // Connection Logging

    public void logConnectionAttempt(int attemptNumber) {
        connectionAttempts++;
        if (enabled) {
            logger.info("[CONNECTION] Attempting connection #{}", attemptNumber);
        }
    }

    public void logConnectionSuccess() {
        successfulConnections++;
        if (enabled) {
            logger.info("[CONNECTION] ✓ WebSocket connection established");
        }
    }

    public void logConnectionClosed(int code, String reason) {
        if (enabled) {
            logger.info("[CONNECTION] Connection closed: code={}, reason={}", code, reason);
        }
    }

    public void logConnectionError(String message, Throwable error) {
        errors++;
        if (enabled) {
            logger.error("[CONNECTION] ✗ Connection error: {}", message, error);
        }
    }

    // Subscription Logging

    public void logSubscription(String method, String mode, int instrumentCount) {
        if (enabled) {
            logger.info("[SUBSCRIPTION] {} {} instruments in {} mode",
                    method.toUpperCase(), instrumentCount, mode);
        }
    }

    public void logSubscriptionSuccess(String method, int count) {
        if (enabled) {
            logger.debug("[SUBSCRIPTION] ✓ {} successful for {} instruments", method, count);
        }
    }

    public void logSubscriptionError(String method, String error) {
        errors++;
        if (enabled) {
            logger.error("[SUBSCRIPTION] ✗ {} failed: {}", method, error);
        }
    }

    public void logLimitValidation(String mode, int requested, int limit, boolean valid) {
        if (enabled) {
            if (valid) {
                logger.debug("[LIMIT] ✓ Mode {} validation passed: {}/{}", mode, requested, limit);
            } else {
                logger.warn("[LIMIT] ✗ Mode {} limit exceeded: {} > {}", mode, requested, limit);
            }
        }
    }

    // Message Logging

    public void logMessageReceived(String type, int instrumentCount) {
        messagesReceived++;
        if (enabled && logMarketUpdates) {
            logger.debug("[MESSAGE] Received {} message with {} instruments", type, instrumentCount);
        }
    }

    public void logMessageProcessed(String type, long processingTimeMs) {
        messagesProcessed++;
        if (enabled && logMarketUpdates) {
            logger.debug("[MESSAGE] Processed {} in {}ms", type, processingTimeMs);
        }
    }

    public void logParsingError(String message, Throwable error) {
        errors++;
        if (enabled) {
            logger.error("[MESSAGE] ✗ Parsing error: {}", message, error);
        }
    }

    // Reconnection Logging

    public void logReconnectAttempt(int attempt, int maxAttempts) {
        if (enabled) {
            logger.info("[RECONNECT] Attempt {}/{}", attempt, maxAttempts);
        }
    }

    public void logReconnectSuccess(int attempt) {
        if (enabled) {
            logger.info("[RECONNECT] ✓ Reconnected successfully on attempt {}", attempt);
        }
    }

    public void logReconnectStopped(String reason) {
        if (enabled) {
            logger.warn("[RECONNECT] ✗ Auto-reconnect stopped: {}", reason);
        }
    }

    // Performance Metrics

    public void logMetrics() {
        if (enabled) {
            double successRate = connectionAttempts > 0 ? (double) successfulConnections / connectionAttempts * 100 : 0;

            logger.info("[METRICS] Messages: received={}, processed={}, errors={}",
                    messagesReceived, messagesProcessed, errors);
            logger.info("[METRICS] Connections: attempts={}, successful={}, rate={:.2f}%",
                    connectionAttempts, successfulConnections, successRate);
        }
    }

    public void resetMetrics() {
        messagesReceived = 0;
        messagesProcessed = 0;
        connectionAttempts = 0;
        successfulConnections = 0;
        errors = 0;
    }

    // General Logging

    public void info(String message, Object... args) {
        if (enabled) {
            logger.info(message, args);
        }
    }

    public void debug(String message, Object... args) {
        if (enabled) {
            logger.debug(message, args);
        }
    }

    public void warn(String message, Object... args) {
        if (enabled) {
            logger.warn(message, args);
        }
    }

    public void error(String message, Object... args) {
        errors++;
        if (enabled) {
            logger.error(message, args);
        }
    }

    public void error(String message, Throwable throwable) {
        errors++;
        if (enabled) {
            logger.error(message, throwable);
        }
    }

    // Getters for metrics

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getMessagesProcessed() {
        return messagesProcessed;
    }

    public long getConnectionAttempts() {
        return connectionAttempts;
    }

    public long getSuccessfulConnections() {
        return successfulConnections;
    }

    public long getErrors() {
        return errors;
    }
}
