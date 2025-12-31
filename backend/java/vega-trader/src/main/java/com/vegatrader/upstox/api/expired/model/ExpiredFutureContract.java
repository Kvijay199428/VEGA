package com.vegatrader.upstox.api.expired.model;

import java.time.LocalDate;

/**
 * Expired future contract from Upstox API.
 * 
 * @since 4.4.0
 */
public record ExpiredFutureContract(
        String instrumentKey,
        String tradingSymbol,
        int lotSize,
        String underlyingKey,
        LocalDate expiry) {

    /**
     * Construct the expired instrument key for historical candle fetch.
     */
    public String toExpiredInstrumentKey() {
        return instrumentKey + "|" + formatDate(expiry);
    }

    private String formatDate(LocalDate date) {
        return String.format("%02d-%02d-%04d",
                date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }
}
