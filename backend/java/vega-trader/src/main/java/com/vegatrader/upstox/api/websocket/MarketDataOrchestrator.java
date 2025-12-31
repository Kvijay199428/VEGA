package com.vegatrader.upstox.api.websocket;

import com.vegatrader.upstox.auth.TokenCapability;
import com.vegatrader.service.UpstoxTokenProvider;
import com.vegatrader.upstox.api.websocket.settings.MarketDataStreamerSettings;
import com.vegatrader.upstox.api.websocket.settings.SubscriptionTier;
import com.vegatrader.upstox.api.websocket.listener.OnMarketUpdateV3Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Orchestrator for managing multiple MarketDataStreamerV3 connections.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Splits instrument keys across multiple tokens based on Upstox limits</li>
 * <li>Manages multiple WebSocket connections</li>
 * <li>Rotates tokens mid-stream via change_mode without disconnects</li>
 * <li>Respects category-specific subscription limits (LTPC, Full, etc.)</li>
 * </ul>
 */
public class MarketDataOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataOrchestrator.class);

    private final UpstoxTokenProvider tokenProvider;
    private final MarketDataStreamerSettings baseSettings;
    private final List<String> allInstrumentKeys;
    private final Mode mode;
    private final int maxKeysPerToken;

    private final List<MarketDataStreamerV3> connections = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService rotationScheduler;
    private OnMarketUpdateV3Listener globalListener;

    public MarketDataOrchestrator(
            UpstoxTokenProvider tokenProvider,
            MarketDataStreamerSettings baseSettings,
            List<String> allInstrumentKeys,
            Mode mode) {
        this.tokenProvider = tokenProvider;
        this.baseSettings = baseSettings;
        this.allInstrumentKeys = new ArrayList<>(allInstrumentKeys);
        this.mode = mode;
        this.maxKeysPerToken = determineMaxKeys(mode, baseSettings.getTier());
        this.rotationScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Token-Rotation-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    private int determineMaxKeys(Mode mode, SubscriptionTier tier) {
        boolean isPlus = tier == SubscriptionTier.PLUS;
        return switch (mode) {
            case LTPC -> isPlus ? 5000 : 2000;
            case FULL -> isPlus ? 2000 : 1500;
            case OPTION_GREEKS -> isPlus ? 3000 : 2000;
            case FULL_D30 -> 50; // Special limit for depth 30
            default -> 2000;
        };
    }

    public void setGlobalListener(OnMarketUpdateV3Listener listener) {
        this.globalListener = listener;
    }

    /**
     * Starts the orchestration by splitting keys and connecting streamers.
     */
    public void start() {
        logger.info("Starting MarketDataOrchestrator: instruments={}, mode={}, maxPerToken={}",
                allInstrumentKeys.size(), mode, maxKeysPerToken);

        List<List<String>> batches = splitIntoBatches(allInstrumentKeys, maxKeysPerToken);
        logger.info("Keys split into {} batches", batches.size());

        for (List<String> batch : batches) {
            MarketDataStreamerSettings batchSettings = cloneSettings(baseSettings);
            MarketDataStreamerV3 streamer = new MarketDataStreamerV3(
                    tokenProvider,
                    batchSettings,
                    new HashSet<>(batch),
                    mode);

            if (globalListener != null) {
                streamer.setOnMarketUpdateListener(globalListener);
            }

            streamer.connect();
            connections.add(streamer);
        }

        // Schedule rotation every 8 minutes (token freshness limit is 10 mins)
        rotationScheduler.scheduleAtFixedRate(this::rotateTokens, 8, 8, TimeUnit.MINUTES);
    }

    private void rotateTokens() {
        logger.info("AUDIT | ORCHESTRATOR | ROTATION | Starting scheduled rotation for {} connections",
                connections.size());
        for (MarketDataStreamerV3 streamer : connections) {
            try {
                String newToken = tokenProvider
                        .getAccessToken(TokenCapability.MARKET_DATA_WS);
                streamer.rotateToken(newToken);
            } catch (Exception e) {
                logger.error("AUDIT | ORCHESTRATOR | ROTATION | FAILED | Error rotating token: {}", e.getMessage());
            }
        }
    }

    private List<List<String>> splitIntoBatches(List<String> keys, int batchSize) {
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < keys.size(); i += batchSize) {
            batches.add(keys.subList(i, Math.min(i + batchSize, keys.size())));
        }
        return batches;
    }

    private MarketDataStreamerSettings cloneSettings(MarketDataStreamerSettings original) {
        MarketDataStreamerSettings clone = new MarketDataStreamerSettings(original.getTier());
        clone.setWsUrl(original.getWsUrl());
        clone.setAuthorizeUrl(original.getAuthorizeUrl());
        clone.setUseAuthorizeEndpoint(original.isUseAuthorizeEndpoint());
        clone.setEnableLogging(original.isEnableLogging());
        clone.setLogFilePath(original.getLogFilePath());
        clone.setLogMarketUpdates(original.isLogMarketUpdates());
        clone.setEnableCaching(original.isEnableCaching());
        clone.setMaxCacheSize(original.getMaxCacheSize());
        clone.setCacheTTL(original.getCacheTTL());
        clone.setBufferCapacity(original.getBufferCapacity());
        clone.setPingInterval(original.getPingInterval());
        clone.setConnectTimeout(original.getConnectTimeout());
        clone.setReadTimeout(original.getReadTimeout());
        clone.setWriteTimeout(original.getWriteTimeout());
        return clone;
    }

    public void stop() {
        logger.info("Stopping MarketDataOrchestrator");
        rotationScheduler.shutdownNow();
        for (MarketDataStreamerV3 streamer : connections) {
            streamer.disconnect();
        }
        connections.clear();
    }
}
