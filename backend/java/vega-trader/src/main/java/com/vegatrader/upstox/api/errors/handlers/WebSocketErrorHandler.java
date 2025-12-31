package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.response.common.ErrorResponse;

/**
 * Error handler for WebSocket specific errors.
 *
 * @since 2.0.0
 */
public class WebSocketErrorHandler extends BaseErrorHandler {

    public WebSocketErrorHandler() {
        registerResolutionHint("connection_failed",
                "Failed to establish WebSocket connection. Check network and credentials");

        registerResolutionHint("subscription_failed",
                "Failed to subscribe to data feed. Verify instrument keys");

        registerResolutionHint("invalid_message_format",
                "WebSocket message format is invalid");

        registerResolutionHint("subscription_limit_exceeded",
                "Maximum subscription limit reached. Unsubscribe from unused instruments");

        registerResolutionHint("disconnected",
                "WebSocket connection lost. Attempting auto-reconnect");
    }

    @Override
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        ErrorResponse error = super.handleError(httpStatus, errorCode);
        error.addDetail("category", "WEBSOCKET_ERROR");

        if ("connection_failed".equals(errorCode) || "disconnected".equals(errorCode)) {
            error.addDetail("action_required", "RECONNECT");
        } else if (errorCode.contains("subscription")) {
            error.addDetail("action_required", "REVIEW_SUBSCRIPTIONS");
        }

        return error;
    }
}
