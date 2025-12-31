package com.vegatrader.upstox.api.expired.service;

import com.vegatrader.upstox.api.expired.model.Candle;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for historical market data on expired instruments.
 * 
 * @since 4.4.0
 */
public interface HistoricalMarketDataService {

    /**
     * Fetch OHLC + OI historical data for expired instruments.
     * 
     * @param expiredInstrumentKey Format: <instrument_key>|<DD-MM-YYYY>
     * @param interval             Candle interval (1minute, 5minute, day, etc.)
     * @param fromDate             Start date
     * @param toDate               End date
     * @return List of candles, sorted by timestamp ascending
     */
    List<Candle> fetchExpiredHistoricalCandles(
            String expiredInstrumentKey,
            String interval,
            LocalDate fromDate,
            LocalDate toDate);

    /**
     * Validate interval parameter.
     * 
     * @param interval Interval code
     * @return true if valid
     */
    boolean isValidInterval(String interval);

    /**
     * Validate date range.
     * 
     * @param fromDate Start date
     * @param toDate   End date
     * @param maxDays  Maximum allowed days
     * @return true if valid range
     */
    default boolean isValidDateRange(LocalDate fromDate, LocalDate toDate, int maxDays) {
        if (fromDate == null || toDate == null)
            return false;
        if (fromDate.isAfter(toDate))
            return false;
        if (fromDate.plusDays(maxDays).isBefore(toDate))
            return false;
        return true;
    }
}
