package com.vegatrader.strategy;

import com.vegatrader.execution.ExecutionGateway;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import com.vegatrader.market.feed.FeedMode;
import com.vegatrader.market.service.MarketSubscriptionManager;
import com.vegatrader.market.websocket.MarketBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Engine to manage and run trading strategies.
 */
@Service
public class StrategyEngine {

    private static final Logger logger = LoggerFactory.getLogger(StrategyEngine.class);

    @Autowired
    private MarketSubscriptionManager subscriptionManager;

    @Autowired
    private ExecutionGateway executionGateway;

    @Autowired
    private MarketBroadcaster broadcaster;

    private final Map<String, Strategy> strategies = new ConcurrentHashMap<>();
    private final Map<String, Set<Strategy>> instrumentSubscribers = new ConcurrentHashMap<>();

    /**
     * Register and start a strategy.
     */
    public void registerStrategy(Strategy strategy) {
        if (strategies.containsKey(strategy.getId())) {
            throw new IllegalArgumentException("Strategy ID already exists: " + strategy.getId());
        }

        StrategyContext context = new StrategyContextImpl(strategy);
        try {
            strategy.onInit(context);
            strategies.put(strategy.getId(), strategy);
            logger.info("Strategy registered: {} ({})", strategy.getName(), strategy.getId());
        } catch (Exception e) {
            logger.error("Failed to initialize strategy " + strategy.getName(), e);
        }
    }

    /**
     * Stop and unregister a strategy.
     */
    public void unregisterStrategy(String strategyId) {
        Strategy strategy = strategies.remove(strategyId);
        if (strategy != null) {
            try {
                strategy.onDestroy();
                // Remove from subscribers
                instrumentSubscribers.values().forEach(set -> set.remove(strategy));
                logger.info("Strategy stopped: {}", strategy.getName());
            } catch (Exception e) {
                logger.error("Error stopping strategy " + strategy.getName(), e);
            }
        }
    }

    public void onTick(LiveMarketSnapshot tick) {
        Set<Strategy> subscribers = instrumentSubscribers.get(tick.getInstrumentKey());
        if (subscribers != null) {
            subscribers.forEach(s -> {
                try {
                    s.onTick(tick);
                } catch (Exception e) {
                    logger.error("Strategy execution error (Tick) in " + s.getName(), e);
                }
            });
        }
    }

    public void onDepth(OrderBookSnapshot depth) {
        Set<Strategy> subscribers = instrumentSubscribers.get(depth.getInstrumentKey());
        if (subscribers != null) {
            subscribers.forEach(s -> {
                try {
                    s.onDepth(depth);
                } catch (Exception e) {
                    logger.error("Strategy execution error (Depth) in " + s.getName(), e);
                }
            });
        }
    }

    // Inner class implementation of Context
    private class StrategyContextImpl implements StrategyContext {
        private final Strategy strategy;
        private final Logger strategyLogger;

        public StrategyContextImpl(Strategy strategy) {
            this.strategy = strategy;
            this.strategyLogger = LoggerFactory.getLogger("STRATEGY." + strategy.getName());
        }

        @Override
        public ExecutionGateway getExecutionGateway() {
            return executionGateway;
        }

        @Override
        public Logger getLogger() {
            return strategyLogger;
        }

        @Override
        public void subscribe(String instrumentKey) {
            instrumentSubscribers.computeIfAbsent(instrumentKey, k -> new CopyOnWriteArraySet<>())
                    .add(strategy);

            // Ensure system is subscribed to data feed
            subscriptionManager.subscribe("STRATEGY_ENGINE", Set.of(instrumentKey), FeedMode.FULL);
            // Using "STRATEGY_ENGINE" as special client ID
        }

        @Override
        public void emitSignal(String type, Object payload) {
            broadcaster.broadcastAll("STRATEGY_SIGNAL",
                    Map.of("strategyId", strategy.getId(), "type", type, "payload", payload));
        }
    }
}
