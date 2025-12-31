package com.vegatrader.upstox.api.websocket;

import com.vegatrader.upstox.api.request.websocket.MarketDataFeedV3Request;
import com.vegatrader.upstox.api.request.websocket.MarketDataMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper utility to construct MarketDataFeedV3Request objects for Upstox V3.
 * Note: Requests are JSON-based but sent as binary bytes in V3.
 */
public class MarketDataRequestHelper {

    /**
     * Creates a subscription request.
     */
    public static MarketDataFeedV3Request buildSubscribeRequest(Collection<String> instrumentKeys, String mode) {
        return MarketDataFeedV3Request.builder()
                .subscribe()
                .mode(parseMode(mode))
                .instrumentKeys(new ArrayList<>(instrumentKeys))
                .build();
    }

    /**
     * Creates an unsubscription request.
     */
    public static MarketDataFeedV3Request buildUnsubscribeRequest(Collection<String> instrumentKeys, String mode) {
        return MarketDataFeedV3Request.builder()
                .unsubscribe()
                .mode(parseMode(mode))
                .instrumentKeys(new ArrayList<>(instrumentKeys))
                .build();
    }

    /**
     * Creates a change_mode request (used for token rotation).
     */
    public static MarketDataFeedV3Request buildChangeModeRequest(Collection<String> instrumentKeys, String mode) {
        return MarketDataFeedV3Request.builder()
                .changeMode()
                .mode(parseMode(mode))
                .instrumentKeys(new ArrayList<>(instrumentKeys))
                .build();
    }

    private static MarketDataMode parseMode(String mode) {
        if (mode == null)
            return MarketDataMode.LTPC;
        try {
            return MarketDataMode.fromValue(mode.toLowerCase());
        } catch (IllegalArgumentException e) {
            return MarketDataMode.LTPC;
        }
    }
}
