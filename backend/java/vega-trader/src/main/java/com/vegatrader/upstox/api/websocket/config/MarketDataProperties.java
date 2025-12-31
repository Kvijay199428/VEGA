package com.vegatrader.upstox.api.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot configuration properties for market data settings.
 * 
 * <p>
 * Maps from application.yml/properties:
 * 
 * <pre>
 * marketdata:
 *   connection-limits:
 *     normal: 2
 *     plus: 5
 *   subscription-limits:
 *     LTPC:
 *       individual: 5000
 *       combined: 2000
 * </pre>
 * 
 * @since 2.0.0
 */
@Component
@ConfigurationProperties(prefix = "marketdata")
public class MarketDataProperties {

    private ConnectionLimits connectionLimits = new ConnectionLimits();
    private Map<String, SubscriptionLimit> subscriptionLimits = new HashMap<>();
    private boolean enabled = true;
    private boolean autoStart = false;
    private int reconnectDelayMs = 5000;
    private int maxBatchSize = 1000;
    private int bufferCapacity = 50000;
    private boolean metricsEnabled = true;

    // Getters and Setters

    public ConnectionLimits getConnectionLimits() {
        return connectionLimits;
    }

    public void setConnectionLimits(ConnectionLimits connectionLimits) {
        this.connectionLimits = connectionLimits;
    }

    public Map<String, SubscriptionLimit> getSubscriptionLimits() {
        return subscriptionLimits;
    }

    public void setSubscriptionLimits(Map<String, SubscriptionLimit> subscriptionLimits) {
        this.subscriptionLimits = subscriptionLimits;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public int getReconnectDelayMs() {
        return reconnectDelayMs;
    }

    public void setReconnectDelayMs(int reconnectDelayMs) {
        this.reconnectDelayMs = reconnectDelayMs;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    /**
     * Connection limits per user type.
     */
    public static class ConnectionLimits {
        private int normal = 2;
        private int plus = 5;

        public int getNormal() {
            return normal;
        }

        public void setNormal(int normal) {
            this.normal = normal;
        }

        public int getPlus() {
            return plus;
        }

        public void setPlus(int plus) {
            this.plus = plus;
        }
    }

    /**
     * Per-category subscription limits.
     */
    public static class SubscriptionLimit {
        private int individual;
        private int combined;

        public int getIndividual() {
            return individual;
        }

        public void setIndividual(int individual) {
            this.individual = individual;
        }

        public int getCombined() {
            return combined;
        }

        public void setCombined(int combined) {
            this.combined = combined;
        }
    }
}
