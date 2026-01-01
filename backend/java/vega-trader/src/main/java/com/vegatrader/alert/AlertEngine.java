package com.vegatrader.alert;

import com.vegatrader.alert.dto.AlertEvent;
import com.vegatrader.alert.entity.AlertRule;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.feed.MarketFeedListener;
import com.vegatrader.market.dto.OrderBookSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Alert evaluation engine.
 * Listens to market ticks and evaluates registered rules.
 */
@Service
public class AlertEngine implements MarketFeedListener {

    private static final Logger logger = LoggerFactory.getLogger(AlertEngine.class);

    private final Map<String, Set<AlertRule>> instrumentRules = new ConcurrentHashMap<>();

    // Map ruleId -> Rule? For management.

    public void addRule(AlertRule rule) {
        instrumentRules.computeIfAbsent(rule.getInstrumentKey(), k -> new CopyOnWriteArraySet<>())
                .add(rule);
        logger.info("Added alert rule: {} for {}", rule.getId(), rule.getInstrumentKey());
    }

    public void removeRule(String ruleId) {
        // Linear search removal (inefficient but simple for now)
        instrumentRules.values().forEach(set -> set.removeIf(r -> r.getId().equals(ruleId)));
    }

    @Override
    public void onTick(LiveMarketSnapshot tick) {
        if (tick == null)
            return;

        Set<AlertRule> rules = instrumentRules.get(tick.getInstrumentKey());
        if (rules == null || rules.isEmpty())
            return;

        rules.forEach(rule -> evaluate(rule, tick));
    }

    @Override
    public void onDepth(OrderBookSnapshot depth) {
        // No depth alerts yet
    }

    private void evaluate(AlertRule rule, LiveMarketSnapshot tick) {
        if (!rule.isActive())
            return;

        boolean triggered = false;
        double value = 0;

        switch (rule.getType()) {
            case PRICE_ABOVE:
                value = tick.getLtp();
                if (value > rule.getThreshold())
                    triggered = true;
                break;
            case PRICE_BELOW:
                value = tick.getLtp();
                if (value < rule.getThreshold())
                    triggered = true;
                break;
            case CHANGE_PERCENT_GREATER:
                value = tick.getChangePercent();
                if (value > rule.getThreshold())
                    triggered = true;
                break;
            case CHANGE_PERCENT_LESS:
                value = tick.getChangePercent();
                if (value < rule.getThreshold())
                    triggered = true;
                break;
            case VOLUME_GREATER:
                value = tick.getVolume();
                if (value > rule.getThreshold())
                    triggered = true;
                break;
        }

        if (triggered) {
            triggerAlert(rule, value);
        }
    }

    private void triggerAlert(AlertRule rule, double value) {
        logger.info("ALERT TRIGGERED: {} val={}", rule.getMessage(), value);

        AlertEvent event = AlertEvent.builder()
                .ruleId(rule.getId())
                .clientId(rule.getClientId())
                .instrumentKey(rule.getInstrumentKey())
                .message(rule.getMessage())
                .triggerValue(value)
                .timestamp(System.currentTimeMillis())
                .build();

        // TODO: Push to frontend via WebSocket (MarketBroadcaster)
        // For one-shot, disable or remove
        if (rule.isOneShot()) {
            rule.setActive(false);
            removeRule(rule.getId());
        }
    }
}
