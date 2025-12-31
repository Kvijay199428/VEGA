package com.vegatrader.upstox.api.broker;

/**
 * Broker symbol mapping record.
 * 
 * @since 4.2.0
 */
public record BrokerSymbolMapping(
        String brokerId,
        String instrumentKey,
        String brokerSymbol,
        String brokerToken,
        boolean tradeable) {

    /**
     * Check if can trade.
     */
    public boolean canTrade() {
        return tradeable;
    }
}
