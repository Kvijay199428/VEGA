package com.vegatrader.upstox.api.websocket.listener;

/**
 * Listener interface called when an error occurs in the WebSocket connection or
 * data processing.
 * 
 * @since 3.0.0
 */
@FunctionalInterface
public interface OnErrorListener {

    /**
     * Called when an error occurs.
     * 
     * @param error the exception that occurred
     */
    void onError(Exception error);
}
