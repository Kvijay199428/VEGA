package com.vegatrader.upstox.api.websocket.listener;

/**
 * Listener interface called when WebSocket connection is successfully
 * established.
 * 
 * @since 3.0.0
 */
@FunctionalInterface
public interface OnOpenListener {

    /**
     * Called when the WebSocket connection is opened.
     * This is the right place to send initial subscription requests.
     */
    void onOpen();
}
