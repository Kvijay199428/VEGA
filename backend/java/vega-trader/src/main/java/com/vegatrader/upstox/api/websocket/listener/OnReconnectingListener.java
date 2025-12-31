package com.vegatrader.upstox.api.websocket.listener;

/**
 * Listener interface called when a reconnection attempt is initiated.
 * 
 * @since 3.0.0
 */
@FunctionalInterface
public interface OnReconnectingListener {

    /**
     * Called when auto-reconnect is attempting to reconnect.
     * 
     * @param attemptNumber the current reconnection attempt number (1-based)
     */
    void onReconnecting(int attemptNumber);
}
