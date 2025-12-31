package com.vegatrader.upstox.api.websocket.bus;

/**
 * Event bus interface for publishing and subscribing to typed events.
 * 
 * <p>
 * Provides type-safe event publication and subscription without requiring
 * publishers to know about subscribers. This pattern is used in:
 * <ul>
 * <li>Enterprise messaging systems</li>
 * <li>FIX gateways</li>
 * <li>Market data distributors</li>
 * </ul>
 * 
 * <p>
 * Thread-safety: Implementations must be thread-safe for concurrent
 * publish and subscribe operations.
 * 
 * @since 3.1.0
 */
public interface EventBus {

    /**
     * Publishes an event to all subscribers of its type.
     * 
     * <p>
     * This operation should be non-blocking and fail-fast. Subscribers
     * are notified synchronously on the calling thread.
     * 
     * @param <T>   the event type
     * @param event the event to publish (must not be null)
     * @throws NullPointerException if event is null
     */
    <T> void publish(T event);

    /**
     * Subscribes to events of a specific type.
     * 
     * <p>
     * The subscriber will receive all future events of the specified type
     * until explicitly unsubscribed (if supported by implementation).
     * 
     * <p>
     * Multiple subscriptions for the same event type are allowed.
     * Each subscriber will be notified independently.
     * 
     * @param <T>        the event type
     * @param eventType  the class of events to subscribe to
     * @param subscriber the subscriber callback
     * @throws NullPointerException if eventType or subscriber is null
     */
    <T> void subscribe(Class<T> eventType, EventSubscriber<T> subscriber);
}
