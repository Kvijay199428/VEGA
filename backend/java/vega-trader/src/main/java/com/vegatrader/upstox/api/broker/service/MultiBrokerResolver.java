package com.vegatrader.upstox.api.broker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Multi-Broker Resolver per arch/a4.md section 2.
 * Resolves instruments across all enabled brokers.
 * 
 * @since 4.6.0
 */
@Service
public class MultiBrokerResolver {

    private static final Logger logger = LoggerFactory.getLogger(MultiBrokerResolver.class);

    private final Map<String, BrokerInstrumentResolver> resolvers = new HashMap<>();

    public MultiBrokerResolver() {
        // Resolvers will be injected via Spring or registered dynamically
    }

    /**
     * Register a broker-specific resolver.
     */
    public void registerResolver(String broker, BrokerInstrumentResolver resolver) {
        resolvers.put(broker.toUpperCase(), resolver);
        logger.info("Registered resolver for broker: {}", broker);
    }

    /**
     * Resolve instrument across all enabled brokers.
     */
    public void resolveAcrossBrokers(BrokerInstrumentPrewarmJob.OptionDescriptor descriptor) {
        for (String broker : getEnabledBrokers()) {
            try {
                BrokerInstrumentResolver resolver = resolvers.get(broker);
                if (resolver != null) {
                    resolver.resolveOption(
                            descriptor.underlyingKey(),
                            descriptor.expiry(),
                            descriptor.strike(),
                            descriptor.optionType());
                    logger.debug("Resolved {} for broker {}", descriptor, broker);
                }
            } catch (Exception e) {
                logger.warn("Resolution failed for broker {}: {}", broker, e.getMessage());
            }
        }
    }

    /**
     * Resolve for a specific broker.
     */
    public BrokerInstrument resolveForBroker(
            String broker,
            String underlyingKey,
            LocalDate expiry,
            BigDecimal strike,
            String optionType) {

        BrokerInstrumentResolver resolver = resolvers.get(broker.toUpperCase());
        if (resolver == null) {
            throw new IllegalArgumentException("No resolver for broker: " + broker);
        }

        return resolver.resolveOption(underlyingKey, expiry, strike, optionType);
    }

    /**
     * Get list of enabled brokers.
     */
    public List<String> getEnabledBrokers() {
        // TODO: Read from broker_registry table
        return List.of("UPSTOX"); // Default for now
    }

    /**
     * Check if broker is available.
     */
    public boolean isBrokerAvailable(String broker) {
        return resolvers.containsKey(broker.toUpperCase());
    }

    /**
     * Broker instrument result.
     */
    public record BrokerInstrument(
            String brokerInstrumentKey,
            String exchangeToken,
            String tradingSymbol,
            int lotSize,
            BigDecimal tickSize,
            Integer freezeQuantity,
            boolean weekly) {
    }
}
