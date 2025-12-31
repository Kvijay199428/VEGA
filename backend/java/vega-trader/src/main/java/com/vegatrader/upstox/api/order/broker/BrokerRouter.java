package com.vegatrader.upstox.api.order.broker;

import com.vegatrader.upstox.api.settings.model.UserPrioritySettings;
import com.vegatrader.upstox.api.settings.service.SettingsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Broker Router for multi-broker request routing.
 * Per order-mgmt/b4.md section 5.1.
 * 
 * Resolution order:
 * 1. Explicit broker in request
 * 2. Strategy assigned broker
 * 3. User default broker
 * 4. Admin fallback broker
 * 
 * @since 4.9.0
 */
@Service
public class BrokerRouter {

    private static final Logger logger = LoggerFactory.getLogger(BrokerRouter.class);

    private final Map<String, BrokerAdapter> adapters = new HashMap<>();
    private final String defaultBroker = "UPSTOX";

    private final SettingsResolver settingsResolver;

    @Autowired
    public BrokerRouter(UpstoxBrokerAdapter upstoxAdapter, SettingsResolver settingsResolver) {
        adapters.put("UPSTOX", upstoxAdapter);
        this.settingsResolver = settingsResolver;
        logger.info("Broker router initialized with {} adapters", adapters.size());
    }

    /**
     * Get adapter for broker.
     */
    public BrokerAdapter getAdapter(String brokerName) {
        String name = brokerName != null ? brokerName.toUpperCase() : defaultBroker;
        BrokerAdapter adapter = adapters.get(name);

        if (adapter == null) {
            // Fallback to default if explicit fails? No, explicit should fail if invalid.
            // But for "User default" which might be invalid, we should fallback?
            // Current strict logic:
            throw new IllegalArgumentException("Broker not supported: " + name);
        }

        return adapter;
    }

    /**
     * Resolve broker for user.
     * Per b4.md section 5.1.
     */
    public BrokerAdapter resolveForUser(String userId, String explicitBroker, String strategyTag) {
        // 1. Explicit broker in request
        if (explicitBroker != null && !explicitBroker.isEmpty()) {
            logger.debug("Using explicit broker: {}", explicitBroker);
            return getAdapter(explicitBroker);
        }

        // 2. Strategy assigned broker (stub for StrategyConfigService)
        String strategyBroker = getStrategyBroker(strategyTag);
        if (strategyBroker != null) {
            logger.debug("Using strategy broker: {}", strategyBroker);
            return getAdapter(strategyBroker);
        }

        // 3. User default broker
        String userBroker = getUserDefaultBroker(userId);
        if (userBroker != null) {
            try {
                logger.debug("Using user default broker: {}", userBroker);
                return getAdapter(userBroker);
            } catch (IllegalArgumentException e) {
                logger.warn("User default broker {} not available, falling back", userBroker);
            }
        }

        // 4. Admin fallback broker
        logger.debug("Using default fallback broker: {}", defaultBroker);
        return getAdapter(defaultBroker);
    }

    /**
     * Check if broker supports capability.
     */
    public boolean supportsMultiOrder(String brokerName) {
        BrokerAdapter adapter = getAdapter(brokerName);
        return adapter.getCapabilities().supportsMultiOrder();
    }

    /**
     * Get available brokers.
     */
    public Set<String> getAvailableBrokers() {
        return adapters.keySet();
    }

    /**
     * Check broker availability.
     */
    public boolean isBrokerAvailable(String brokerName) {
        try {
            return getAdapter(brokerName).isAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Split orders by broker capability (for multi-order).
     * If broker doesn't support multi-order, returns individual order lists.
     */
    public List<List<BrokerAdapter.OrderRequest>> splitForBroker(
            List<BrokerAdapter.OrderRequest> orders,
            String brokerName) {

        BrokerAdapter adapter = getAdapter(brokerName);
        BrokerCapability caps = adapter.getCapabilities();

        if (!caps.supportsMultiOrder()) {
            // Split into individual orders
            List<List<BrokerAdapter.OrderRequest>> result = new ArrayList<>();
            for (BrokerAdapter.OrderRequest order : orders) {
                result.add(List.of(order));
            }
            return result;
        }

        // Split into batches of max size
        List<List<BrokerAdapter.OrderRequest>> batches = new ArrayList<>();
        for (int i = 0; i < orders.size(); i += caps.maxOrdersPerBatch()) {
            int end = Math.min(i + caps.maxOrdersPerBatch(), orders.size());
            batches.add(orders.subList(i, end));
        }

        return batches;
    }

    // ==================== Helper Methods ====================

    private String getUserDefaultBroker(String userId) {
        if (userId == null)
            return null;
        try {
            UserPrioritySettings settings = settingsResolver.resolveSettings(userId, null);
            if (settings != null && settings.brokerRoutingPriority() != null
                    && !settings.brokerRoutingPriority().isEmpty()) {
                return settings.brokerRoutingPriority().get(0);
            }
        } catch (Exception e) {
            logger.warn("Failed to resolve settings for user {}: {}", userId, e.getMessage());
        }
        return null; // Fallback handled in resolveForUser
    }

    private String getStrategyBroker(String strategyTag) {
        // Placeholder for StrategyConfigService
        // If we had a service: return strategyConfigService.getBroker(strategyTag);
        return null;
    }

    /**
     * Register a new broker adapter.
     */
    public void registerAdapter(BrokerAdapter adapter) {
        adapters.put(adapter.getBrokerName().toUpperCase(), adapter);
        logger.info("Registered broker adapter: {}", adapter.getBrokerName());
    }
}
