package com.vegatrader.upstox.api.websocket.logging;

import com.vegatrader.upstox.api.websocket.state.PortfolioFeedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Structured logger for PortfolioDataStreamerV2.
 * 
 * <p>
 * ⚠️ CRITICAL: Logging is NOT metrics. Separate concerns.
 * 
 * <p>
 * This logger handles:
 * <ul>
 * <li>Connection lifecycle events</li>
 * <li>State transitions</li>
 * <li>Error conditions</li>
 * <li>Reconnection attempts</li>
 * </ul>
 * 
 * <p>
 * Does NOT track:
 * <ul>
 * <li>Counters (that's PortfolioMetricsCollector's job)</li>
 * <li>Rates (that's metrics)</li>
 * <li>Aggregations (that's metrics)</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioDataStreamerLogger {

    private final Logger logger;
    private final String logFilePath;
    private final boolean enableLogging;
    private final boolean logUpdates;
    private PrintWriter fileWriter;

    public PortfolioDataStreamerLogger(Class<?> clazz, String logFilePath,
            boolean enableLogging, boolean logUpdates) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.logFilePath = logFilePath;
        this.enableLogging = enableLogging;
        this.logUpdates = logUpdates;

        if (enableLogging && logFilePath != null) {
            try {
                File logFile = new File(logFilePath);
                logFile.getParentFile().mkdirs();
                this.fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
            } catch (IOException e) {
                logger.error("Failed to initialize file logger: {}", e.getMessage());
            }
        }
    }

    // Connection lifecycle logging

    public void logConnectionAttempt(int attemptNumber) {
        info("Connection attempt #{}", attemptNumber);
    }

    public void logConnectionSuccess() {
        info("WebSocket connection established successfully");
    }

    public void logConnectionClosed(int code, String reason) {
        info("WebSocket connection closed: code={}, reason={}", code, reason);
    }

    public void logConnectionError(String message, Exception e) {
        if (e != null) {
            error("Connection error: {} - {}", message, e.getMessage(), e);
        } else {
            error("Connection error: {}", message);
        }
    }

    // State transition logging

    public void logStateTransition(PortfolioFeedState fromState, PortfolioFeedState toState, String reason) {
        info("State transition: {} → {} (reason: {})", fromState, toState, reason);
    }

    // Message processing logging

    public void logMessageReceived(String updateType) {
        if (logUpdates) {
            debug("Received {} update", updateType);
        }
    }

    public void logMessageProcessed(String updateType, long processingTimeMs) {
        if (logUpdates) {
            debug("Processed {} update in {}ms", updateType, processingTimeMs);
        }
    }

    public void logParsingError(String message, Exception e) {
        if (e != null) {
            error("Parsing error: {} - {}", message, e.getMessage(), e);
        } else {
            error("Parsing error: {}", message);
        }
    }

    // Buffer and backpressure logging

    public void logBufferSaturation(double utilizationPercent) {
        warn("Buffer saturation: {:.1f}% utilization", utilizationPercent);
    }

    public void logControlledReconnect(String reason) {
        warn("Triggering controlled reconnect: {}", reason);
    }

    // Resync logging

    public void logForceResync(String reason) {
        info("Force resync initiated: {}", reason);
    }

    // Cache logging

    public void logOutOfOrderRejection(String updateId, long incomingTs, long cachedTs) {
        debug("Rejected out-of-order update: id={}, incoming={}, cached={}",
                updateId, incomingTs, cachedTs);
    }

    // Reconnection logging

    public void logReconnectAttempt(int attempt, int maxAttempts) {
        info("Reconnect attempt {}/{}", attempt, maxAttempts);
    }

    public void logReconnectSuccess(int attempt) {
        info("Reconnection successful after {} attempt(s)", attempt);
    }

    public void logReconnectStopped(String reason) {
        warn("Auto-reconnect stopped: {}", reason);
    }

    // Generic logging methods

    public void info(String format, Object... args) {
        if (enableLogging) {
            logger.info(format, args);
            writeToFile("INFO", String.format(format.replace("{}", "%s"), args));
        }
    }

    public void warn(String format, Object... args) {
        if (enableLogging) {
            logger.warn(format, args);
            writeToFile("WARN", String.format(format.replace("{}", "%s"), args));
        }
    }

    public void error(String format, Object... args) {
        if (enableLogging) {
            logger.error(format, args);
            writeToFile("ERROR", String.format(format.replace("{}", "%s"), args));
        }
    }

    public void error(String message, Throwable t) {
        if (enableLogging) {
            logger.error(message, t);
            writeToFile("ERROR", message + " - " + t.getMessage());
        }
    }

    public void debug(String format, Object... args) {
        if (enableLogging && logUpdates) {
            logger.debug(format, args);
        }
    }

    private void writeToFile(String level, String message) {
        if (fileWriter != null) {
            try {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                fileWriter.println(String.format("[%s] [%s] %s", timestamp, level, message));
                fileWriter.flush();
            } catch (Exception e) {
                // Ignore file write errors
            }
        }
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}
