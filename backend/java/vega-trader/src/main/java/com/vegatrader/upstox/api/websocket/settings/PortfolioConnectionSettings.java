package com.vegatrader.upstox.api.websocket.settings;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages portfolio WebSocket connection lifecycle.
 * 
 * <p>
 * Tracks active connections. Unlike market data, portfolio streams
 * don't have subscription limits per tier.
 * 
 * @since 2.0.0
 */
public class PortfolioConnectionSettings {

    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final int maxConnections;

    /**
     * Creates connection settings with default max connections (5).
     */
    public PortfolioConnectionSettings() {
        this(5);
    }

    /**
     * Creates connection settings with specified max connections.
     * 
     * @param maxConnections maximum allowed simultaneous connections
     */
    public PortfolioConnectionSettings(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Increments active connection count.
     * 
     * @throws IllegalStateException if max connections exceeded
     */
    public synchronized void incrementConnections() {
        int current = activeConnections.get();
        if (current >= maxConnections) {
            throw new IllegalStateException(
                    String.format("Maximum connections (%d) reached", maxConnections));
        }
        activeConnections.incrementAndGet();
    }

    /**
     * Decrements active connection count.
     */
    public synchronized void decrementConnections() {
        activeConnections.updateAndGet(current -> Math.max(0, current - 1));
    }

    /**
     * Gets current active connection count.
     * 
     * @return number of active connections
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * Gets maximum allowed connections.
     * 
     * @return max connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Checks if a new connection can be added.
     * 
     * @return true if under max connections
     */
    public boolean canAddConnection() {
        return activeConnections.get() < maxConnections;
    }

    @Override
    public String toString() {
        return String.format("PortfolioConnectionSettings{active=%d, max=%d}",
                activeConnections.get(), maxConnections);
    }
}
