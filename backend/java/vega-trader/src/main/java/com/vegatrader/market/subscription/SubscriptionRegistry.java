package com.vegatrader.market.subscription;

import com.vegatrader.market.feed.FeedMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscription registry tracking client-to-instrument mappings.
 * Uses reference counting to manage shared Upstox subscriptions.
 * 
 * Multiple clients watching same instrument = 1 Upstox subscription.
 */
@Component
public class SubscriptionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRegistry.class);

    /** Client ID -> Set of subscribed instruments */
    private final Map<String, Set<String>> clientToInstruments = new ConcurrentHashMap<>();

    /** Instrument -> Reference count (number of clients watching) */
    private final Map<String, AtomicInteger> instrumentRefCount = new ConcurrentHashMap<>();

    /** Currently active Upstox subscriptions */
    private final Set<String> activeUpstoxSubscriptions = ConcurrentHashMap.newKeySet();

    /** Instrument -> Feed mode */
    private final Map<String, FeedMode> instrumentModes = new ConcurrentHashMap<>();

    /**
     * Register client subscription to instruments.
     * 
     * @param clientId    Client/session ID
     * @param instruments Set of instrument keys
     * @param mode        Feed mode
     * @return Set of NEW instruments that need Upstox subscription
     */
    public Set<String> subscribe(String clientId, Set<String> instruments, FeedMode mode) {
        Set<String> newSubscriptions = new HashSet<>();

        clientToInstruments.computeIfAbsent(clientId, k -> ConcurrentHashMap.newKeySet());
        Set<String> clientInstruments = clientToInstruments.get(clientId);

        for (String inst : instruments) {
            // Add to client's subscriptions
            if (clientInstruments.add(inst)) {
                // Increment ref count
                AtomicInteger refCount = instrumentRefCount.computeIfAbsent(inst, k -> new AtomicInteger(0));
                int count = refCount.incrementAndGet();

                // Track mode (upgrade if higher)
                instrumentModes.merge(inst, mode, (old, newMode) -> newMode.ordinal() > old.ordinal() ? newMode : old);

                // If first subscriber, need Upstox subscription
                if (count == 1 && !activeUpstoxSubscriptions.contains(inst)) {
                    newSubscriptions.add(inst);
                    activeUpstoxSubscriptions.add(inst);
                    logger.info("New subscription: {} (mode={})", inst, mode);
                }
            }
        }

        return newSubscriptions;
    }

    /**
     * Unsubscribe client from instruments.
     * 
     * @param clientId    Client/session ID
     * @param instruments Set of instrument keys (null = all)
     * @return Set of instruments to unsubscribe from Upstox (ref count = 0)
     */
    public Set<String> unsubscribe(String clientId, Set<String> instruments) {
        Set<String> toUnsubscribe = new HashSet<>();

        Set<String> clientInstruments = clientToInstruments.get(clientId);
        if (clientInstruments == null)
            return toUnsubscribe;

        Set<String> toRemove = instruments != null ? instruments : new HashSet<>(clientInstruments);

        for (String inst : toRemove) {
            if (clientInstruments.remove(inst)) {
                AtomicInteger refCount = instrumentRefCount.get(inst);
                if (refCount != null && refCount.decrementAndGet() <= 0) {
                    activeUpstoxSubscriptions.remove(inst);
                    instrumentRefCount.remove(inst);
                    instrumentModes.remove(inst);
                    toUnsubscribe.add(inst);
                    logger.info("Unsubscribed (no clients): {}", inst);
                }
            }
        }

        // Remove client if no subscriptions left
        if (clientInstruments.isEmpty()) {
            clientToInstruments.remove(clientId);
        }

        return toUnsubscribe;
    }

    /**
     * Remove all subscriptions for a client (on disconnect).
     */
    public Set<String> removeClient(String clientId) {
        return unsubscribe(clientId, null);
    }

    /**
     * Get all active Upstox subscriptions.
     */
    public Set<String> getActiveSubscriptions() {
        return Collections.unmodifiableSet(activeUpstoxSubscriptions);
    }

    /**
     * Get clients subscribed to an instrument.
     */
    public Set<String> getClientsForInstrument(String instrumentKey) {
        Set<String> clients = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : clientToInstruments.entrySet()) {
            if (entry.getValue().contains(instrumentKey)) {
                clients.add(entry.getKey());
            }
        }
        return clients;
    }

    /**
     * Get instruments for a client.
     */
    public Set<String> getInstrumentsForClient(String clientId) {
        Set<String> instruments = clientToInstruments.get(clientId);
        return instruments != null ? Collections.unmodifiableSet(instruments) : Set.of();
    }

    /**
     * Get feed mode for instrument.
     */
    public FeedMode getModeForInstrument(String instrumentKey) {
        return instrumentModes.getOrDefault(instrumentKey, FeedMode.FULL);
    }

    /**
     * Check if instrument has any subscribers.
     */
    public boolean hasSubscribers(String instrumentKey) {
        return activeUpstoxSubscriptions.contains(instrumentKey);
    }

    /**
     * Get total subscription count.
     */
    public int getTotalSubscriptionCount() {
        return activeUpstoxSubscriptions.size();
    }

    /**
     * Get total client count.
     */
    public int getClientCount() {
        return clientToInstruments.size();
    }
}
