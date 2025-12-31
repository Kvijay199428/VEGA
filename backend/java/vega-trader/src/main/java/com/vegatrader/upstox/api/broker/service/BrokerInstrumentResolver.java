package com.vegatrader.upstox.api.broker.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Broker-agnostic instrument resolver interface.
 * Per arch/a2.md section 1.1.
 * 
 * @since 4.6.0
 */
public interface BrokerInstrumentResolver {

    /**
     * Resolve an option contract to broker-specific instrument.
     */
    MultiBrokerResolver.BrokerInstrument resolveOption(
            String underlyingKey,
            LocalDate expiry,
            BigDecimal strike,
            String optionType);

    /**
     * Resolve a futures contract.
     */
    default MultiBrokerResolver.BrokerInstrument resolveFuture(
            String underlyingKey,
            LocalDate expiry) {
        throw new UnsupportedOperationException("Future resolution not implemented");
    }

    /**
     * Get broker identifier.
     */
    String getBrokerCode();

    /**
     * Check if broker is healthy.
     */
    boolean isHealthy();
}
