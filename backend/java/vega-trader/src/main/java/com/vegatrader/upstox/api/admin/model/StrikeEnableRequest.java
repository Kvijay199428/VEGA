package com.vegatrader.upstox.api.admin.model;

import java.time.LocalDate;

/**
 * Request to enable a strike.
 */
public record StrikeEnableRequest(
        String exchange,
        String underlyingKey,
        LocalDate expiry,
        double strike,
        String optionType,
        String reason) {
}
