package com.vegatrader.upstox.api.websocket.listener;

/**
 * Listener interface called when WebSocket connection is closed.
 * 
 * @since 3.0.0
 */
@FunctionalInterface
public interface OnCloseListener {

    /**
     * Called when the WebSocket connection is closed.
     * 
     * @param code   the close code (e.g., 1000 for normal closure)
     * @param reason the reason for closure
     */
    void onClose(int code, String reason);
}
