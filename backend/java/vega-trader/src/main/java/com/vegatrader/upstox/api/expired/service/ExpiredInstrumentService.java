package com.vegatrader.upstox.api.expired.service;

import com.vegatrader.upstox.api.expired.model.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for expired instrument operations.
 * Expired instruments are derived at runtime, never stored in DB.
 * 
 * @since 4.4.0
 */
public interface ExpiredInstrumentService {

    /**
     * Fetch all expiry dates for an underlying instrument.
     * Includes up to 6 months of historical expiries.
     * 
     * @param underlyingKey e.g., "NSE_INDEX|Nifty 50"
     * @return List of expiry dates, sorted descending (latest first)
     */
    List<LocalDate> fetchExpiries(String underlyingKey);

    /**
     * Fetch expired option contracts for a given expiry.
     * 
     * @param underlyingKey e.g., "NSE_INDEX|Nifty 50"
     * @param expiry        Expiry date
     * @return List of expired option contracts (CE + PE)
     */
    List<ExpiredOptionContract> fetchExpiredOptions(String underlyingKey, LocalDate expiry);

    /**
     * Fetch expired future contracts for a given expiry.
     * 
     * @param underlyingKey e.g., "NSE_INDEX|Nifty 50"
     * @param expiry        Expiry date
     * @return List of expired future contracts
     */
    List<ExpiredFutureContract> fetchExpiredFutures(String underlyingKey, LocalDate expiry);

    /**
     * Construct the expired instrument key (runtime only).
     * Format: <instrument_key>|<DD-MM-YYYY>
     * 
     * @param instrumentKey Base instrument key
     * @param expiry        Expiry date
     * @return Expired instrument key for historical data fetch
     */
    String buildExpiredInstrumentKey(String instrumentKey, LocalDate expiry);

    /**
     * Validate underlying instrument key format.
     * 
     * @param underlyingKey e.g., "NSE_INDEX|Nifty 50"
     * @return true if valid format
     */
    boolean isValidUnderlyingKey(String underlyingKey);

    /**
     * Get supported intervals for historical candles.
     * 
     * @return List of interval codes
     */
    default List<String> getSupportedIntervals() {
        return List.of("1minute", "3minute", "5minute", "15minute", "30minute", "day");
    }
}
