package com.vegatrader.upstox.api.optionchain.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Feed multicast dispatcher - single diff, many consumers.
 * Per websocket/b1.md section 4.
 * 
 * 1000 users subscribing to the same (symbol, expiry)
 * must NOT cause 1000 recomputations.
 * 
 * @since 4.8.0
 */
@Component
public class FeedMulticastDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(FeedMulticastDispatcher.class);

    // streamKey → set of subscriber callbacks
    private final Map<String, Set<DeltaSubscriber>> subscribers = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Subscribe to a stream.
     */
    public void subscribe(String streamKey, DeltaSubscriber subscriber) {
        subscribers.computeIfAbsent(streamKey, k -> new CopyOnWriteArraySet<>()).add(subscriber);
        logger.debug("Subscribed to {}, total {} subscribers", streamKey, getSubscriberCount(streamKey));
    }

    /**
     * Unsubscribe from a stream.
     */
    public void unsubscribe(String streamKey, DeltaSubscriber subscriber) {
        var subs = subscribers.get(streamKey);
        if (subs != null) {
            subs.remove(subscriber);
            if (subs.isEmpty()) {
                subscribers.remove(streamKey);
            }
        }
        logger.debug("Unsubscribed from {}", streamKey);
    }

    /**
     * Publish deltas to all subscribers of a stream.
     * Single diff → many consumers.
     */
    public void publish(String streamKey, List<WsMessage.Delta> deltas) {
        var subs = subscribers.get(streamKey);
        if (subs == null || subs.isEmpty()) {
            return;
        }

        for (DeltaSubscriber subscriber : subs) {
            try {
                subscriber.onDelta(deltas);
            } catch (Exception e) {
                logger.error("Error publishing to subscriber", e);
            }
        }

        logger.debug("Published {} deltas to {} subscribers of {}",
                deltas.size(), subs.size(), streamKey);
    }

    /**
     * Publish snapshot to all subscribers.
     */
    public void publishSnapshot(String streamKey, OptionChainFeedStreamV3 snapshot) {
        var subs = subscribers.get(streamKey);
        if (subs == null || subs.isEmpty()) {
            return;
        }

        for (DeltaSubscriber subscriber : subs) {
            try {
                subscriber.onSnapshot(snapshot);
            } catch (Exception e) {
                logger.error("Error publishing snapshot to subscriber", e);
            }
        }
    }

    /**
     * Get subscriber count for a stream.
     */
    public int getSubscriberCount(String streamKey) {
        var subs = subscribers.get(streamKey);
        return subs == null ? 0 : subs.size();
    }

    /**
     * Get total subscriber count.
     */
    public int getTotalSubscriberCount() {
        return subscribers.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Get all active stream keys.
     */
    public Set<String> getActiveStreams() {
        return Set.copyOf(subscribers.keySet());
    }

    /**
     * Subscriber callback interface.
     */
    public interface DeltaSubscriber {
        void onDelta(List<WsMessage.Delta> deltas);

        void onSnapshot(OptionChainFeedStreamV3 snapshot);
    }
}
