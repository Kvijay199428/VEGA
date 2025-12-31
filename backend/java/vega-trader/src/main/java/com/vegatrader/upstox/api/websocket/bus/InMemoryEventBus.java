package com.vegatrader.upstox.api.websocket.bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/**
 * Thread-safe in-memory event bus implementation.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Thread-safe for concurrent publish and subscribe</li>
 * <li>Type-safe event routing</li>
 * <li>Zero external dependencies</li>
 * <li>O(1) lookup for event types</li>
 * </ul>
 * 
 * <p>
 * Thread-safety implementation:
 * <ul>
 * <li>ConcurrentHashMap for event type → subscriber list mapping</li>
 * <li>CopyOnWriteArrayList for subscriber lists (optimized for reads)</li>
 * </ul>
 * 
 * <p>
 * Performance characteristics:
 * <ul>
 * <li>Publish: O(n) where n = number of subscribers for event type</li>
 * <li>Subscribe: O(1) amortized</li>
 * <li>Memory: Bounded by number of event types × subscribers</li>
 * </ul>
 * 
 * @since 3.1.0
 */
@Component
public class InMemoryEventBus implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEventBus.class);

    /**
     * Map of event type -> list of subscribers.
     * ConcurrentHashMap allows concurrent reads/writes to the map itself.
     * CopyOnWriteArrayList allows concurrent iteration during publish.
     */
    private final Map<Class<?>, List<EventSubscriber<?>>> subscribers = new ConcurrentHashMap<>();

    /**
     * Subscribes to events of a specific type.
     * 
     * <p>
     * Thread-safe: Can be called concurrently with publish operations.
     * 
     * @param <T>        the event type
     * @param eventType  the class of events to subscribe to
     * @param subscriber the subscriber callback
     * @throws NullPointerException if eventType or subscriber is null
     */
    @Override
    public <T> void subscribe(Class<T> eventType, EventSubscriber<T> subscriber) {
        if (eventType == null) {
            throw new NullPointerException("eventType must not be null");
        }
        if (subscriber == null) {
            throw new NullPointerException("subscriber must not be null");
        }

        subscribers
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(subscriber);

        logger.debug("Subscriber registered for event type: {}", eventType.getSimpleName());
    }

    /**
     * Publishes an event to all subscribers of its type.
     * 
     * <p>
     * Thread-safe: Can be called concurrently from multiple threads.
     * 
     * <p>
     * Subscribers are notified synchronously on the calling thread.
     * If a subscriber throws an exception, it is logged and other subscribers
     * continue to receive the event.
     * 
     * @param <T>   the event type
     * @param event the event to publish
     * @throws NullPointerException if event is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        if (event == null) {
            throw new NullPointerException("event must not be null");
        }

        Class<?> eventType = event.getClass();
        List<EventSubscriber<?>> subs = subscribers.get(eventType);

        if (subs != null && !subs.isEmpty()) {
            for (EventSubscriber<?> sub : subs) {
                try {
                    ((EventSubscriber<T>) sub).handle(event);
                } catch (Exception e) {
                    logger.error("Subscriber failed to handle event of type {}: {}",
                            eventType.getSimpleName(), e.getMessage(), e);
                    // Continue notifying other subscribers
                }
            }
        } else {
            logger.trace("No subscribers for event type: {}", eventType.getSimpleName());
        }
    }

    /**
     * Gets the number of subscribers for a specific event type.
     * 
     * @param eventType the event type
     * @return the number of subscribers, or 0 if no subscribers
     */
    public int getSubscriberCount(Class<?> eventType) {
        List<EventSubscriber<?>> subs = subscribers.get(eventType);
        return subs != null ? subs.size() : 0;
    }

    /**
     * Gets the total number of distinct event types with subscribers.
     * 
     * @return the number of registered event types
     */
    public int getEventTypeCount() {
        return subscribers.size();
    }

    /**
     * Clears all subscribers (for testing purposes).
     */
    public void clear() {
        subscribers.clear();
        logger.debug("All subscribers cleared");
    }
}
