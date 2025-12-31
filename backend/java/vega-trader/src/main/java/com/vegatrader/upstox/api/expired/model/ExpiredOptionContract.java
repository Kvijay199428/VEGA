package com.vegatrader.upstox.api.expired.model;

import java.time.LocalDate;

/**
 * Expired option contract from Upstox API.
 * 
 * @since 4.4.0
 */
public record ExpiredOptionContract(
        String instrumentKey,
        String tradingSymbol,
        String instrumentType, // CE / PE
        double strikePrice,
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

    public boolean isCall() {
        return "CE".equalsIgnoreCase(instrumentType);
    }

    public boolean isPut() {
        return "PE".equalsIgnoreCase(instrumentType);
    }
}
