package com.vegatrader.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for market data subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    /** Single instrument key (e.g., NSE_EQ|RELIANCE) */
    private String instrumentKey;

    /** Multiple instrument keys */
    private Set<String> instrumentKeys;

    /** Sector code for sector-based subscription */
    private String sector;

    /** Feed mode: LTPC, FULL, FULL_D30 */
    private String mode;

    /**
     * Get all instrument keys (combines single + multiple).
     */
    public Set<String> getAllInstrumentKeys() {
        if (instrumentKeys != null && !instrumentKeys.isEmpty()) {
            return instrumentKeys;
        }
        if (instrumentKey != null && !instrumentKey.isBlank()) {
            return Set.of(instrumentKey);
        }
        return Set.of();
    }
}
