package com.vegatrader.upstox.api.admin.model;

import java.time.LocalDate;

/**
 * Request to disable a strike.
 * Per arch/a6.md section 2.1.
 */
public record StrikeDisableRequest(
        String exchange,
        String underlyingKey,
        LocalDate expiry,
        double strike,
        String optionType,
        String reason) {
}
