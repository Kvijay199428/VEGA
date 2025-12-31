package com.vegatrader.upstox.api.websocket.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * State tracker for portfolio feed health monitoring.
 * 
 * <p>
 * Tracks feed state transitions and provides state-based gating for listeners.
 * 
 * <p>
 * State transitions:
 * <ul>
 * <li>DISCONNECTED → CONNECTING (on connect())</li>
 * <li>CONNECTING → SYNCING (on WebSocket open)</li>
 * <li>SYNCING → LIVE (after receiving initial updates)</li>
 * <li>LIVE → DEGRADED (on parse errors, buffer saturation)</li>
 * <li>DEGRADED → LIVE (error rate drops)</li>
 * <li>* → DISCONNECTED (on disconnect)</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioStateTracker {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioStateTracker.class);

    private final AtomicReference<PortfolioFeedState> currentState;
    private final List<StateTransition> history;
    private volatile long degradedSince = 0;

    /**
     * Creates a state tracker with initial DISCONNECTED state.
     */
    public PortfolioStateTracker() {
        this.currentState = new AtomicReference<>(PortfolioFeedState.DISCONNECTED);
        this.history = Collections.synchronizedList(new ArrayList<>());
        recordTransition(null, PortfolioFeedState.DISCONNECTED, "Initial state");
    }

    /**
     * Gets the current feed state.
     * 
     * @return current state
     */
    public PortfolioFeedState getState() {
        return currentState.get();
    }

    /**
     * Transitions to a new state.
     * 
     * @param newState the target state
     * @param reason   reason for transition
     */
    public synchronized void transitionTo(PortfolioFeedState newState, String reason) {
        PortfolioFeedState oldState = currentState.get();

        if (oldState == newState) {
            return; // No-op if already in target state
        }

        currentState.set(newState);
        recordTransition(oldState, newState, reason);

        // Track degraded duration
        if (newState == PortfolioFeedState.DEGRADED) {
            degradedSince = System.currentTimeMillis();
        } else if (oldState == PortfolioFeedState.DEGRADED) {
            degradedSince = 0;
        }

        logger.info("State transition: {} → {} (reason: {})", oldState, newState, reason);
    }

    /**
     * Checks if feed is in LIVE state.
     * 
     * @return true if state is LIVE
     */
    public boolean isLive() {
        return currentState.get() == PortfolioFeedState.LIVE;
    }

    /**
     * Checks if feed is connected (SYNCING, LIVE, or DEGRADED).
     * 
     * @return true if connected
     */
    public boolean isConnected() {
        PortfolioFeedState state = currentState.get();
        return state == PortfolioFeedState.SYNCING
                || state == PortfolioFeedState.LIVE
                || state == PortfolioFeedState.DEGRADED;
    }

    /**
     * Gets duration in DEGRADED state.
     * 
     * @return milliseconds in degraded state, or 0 if not degraded
     */
    public long getDegradedDuration() {
        if (currentState.get() == PortfolioFeedState.DEGRADED && degradedSince > 0) {
            return System.currentTimeMillis() - degradedSince;
        }
        return 0;
    }

    /**
     * Gets state transition history.
     * 
     * @return unmodifiable list of state transitions
     */
    public List<StateTransition> getStateHistory() {
        synchronized (history) {
            return Collections.unmodifiableList(new ArrayList<>(history));
        }
    }

    /**
     * Gets the last N state transitions.
     * 
     * @param count number of recent transitions to retrieve
     * @return list of recent transitions
     */
    public List<StateTransition> getRecentTransitions(int count) {
        synchronized (history) {
            int size = history.size();
            int fromIndex = Math.max(0, size - count);
            return Collections.unmodifiableList(new ArrayList<>(history.subList(fromIndex, size)));
        }
    }

    private void recordTransition(PortfolioFeedState from, PortfolioFeedState to, String reason) {
        history.add(new StateTransition(from, to, reason, System.currentTimeMillis()));
    }

    /**
     * Immutable state transition record.
     */
    public static class StateTransition {
        public final PortfolioFeedState fromState;
        public final PortfolioFeedState toState;
        public final String reason;
        public final long timestamp;

        StateTransition(PortfolioFeedState fromState, PortfolioFeedState toState,
                String reason, long timestamp) {
            this.fromState = fromState;
            this.toState = toState;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("%s → %s (%s) at %d",
                    fromState, toState, reason, timestamp);
        }
    }
}
