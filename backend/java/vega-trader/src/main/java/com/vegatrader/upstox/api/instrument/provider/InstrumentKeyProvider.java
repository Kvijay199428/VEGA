package com.vegatrader.upstox.api.instrument.provider;

import java.util.Set;

/**
 * Provides subscription-ready instrument keys for MarketDataStreamerV3.
 * 
 * <p>
 * This is the mandatory contract that MarketDataStreamerV3 depends on.
 * Implementations must ensure returned keys are:
 * <ul>
 * <li>Validated</li>
 * <li>De-duplicated</li>
 * <li>Limit-safe (below subscription limits)</li>
 * <li>Ready for immediate WebSocket subscription</li>
 * </ul>
 * 
 * <p>
 * MarketDataStreamerV3 should NEVER:
 * <ul>
 * <li>Access instrument files directly</li>
 * <li>Perform filtering logic</li>
 * <li>Discover instruments</li>
 * <li>Validate subscription limits</li>
 * </ul>
 * 
 * <p>
 * All instrument enrollment concerns are handled by implementations
 * of this interface, maintaining clean bounded contexts.
 * 
 * @since 3.1.0
 * @see FileBackedInstrumentKeyProvider
 */
public interface InstrumentKeyProvider {

    /**
     * Returns subscription-ready instrument keys.
     * 
     * @return immutable set of instrument keys in format "EXCHANGE|SYMBOL"
     *         (e.g., "NSE_INDEX|Nifty 50", "NSE_FO|45450")
     */
    Set<String> getInstrumentKeys();
}
