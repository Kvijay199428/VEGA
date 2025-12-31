package com.vegatrader.upstox.api.websocket.listener;

/**
 * Listener interface called when auto-reconnect has stopped after exhausting
 * all retry attempts.
 * 
 * @since 3.0.0
 */
@FunctionalInterface
public interface OnAutoReconnectStoppedListener {

    /**
     * Called when auto-reconnect has halted after maximum retries.
     * 
     * @param message informational message about why reconnection stopped
     */
    void onStopped(String message);
}
