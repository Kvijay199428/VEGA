package com.vegatrader.upstox.api.websocket.bus;

/**
 * Functional interface for event subscribers.
 * 
 * <p>
 * Represents a callback that handles events of a specific type.
 * Can be implemented as a lambda or method reference.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * eventBus.subscribe(MarketUpdateEvent.class, event -> {
 *     System.out.println("Received: " + event.getInstrumentKey());
 * });
 * </pre>
 * 
 * @param <T> the type of event this subscriber handles
 * @since 3.1.0
 */
@FunctionalInterface
public interface EventSubscriber<T> {

    /**
     * Handles an event.
     * 
     * <p>
     * This method is called synchronously when an event is published.
     * Implementations should:
     * <ul>
     * <li>Execute quickly to avoid blocking the publisher</li>
     * <li>Not throw exceptions (catch and log internally)</li>
     * <li>Not modify the event object</li>
     * </ul>
     * 
     * @param event the event to handle (never null)
     */
    void handle(T event);
}
