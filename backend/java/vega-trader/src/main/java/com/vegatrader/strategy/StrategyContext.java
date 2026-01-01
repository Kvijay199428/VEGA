package com.vegatrader.strategy;

import com.vegatrader.execution.ExecutionGateway;
import org.slf4j.Logger;

/**
 * Context provided to strategies for interaction with the system.
 * Facade pattern to control what strategies can access.
 */
public interface StrategyContext {

    /**
     * Get the execution gateway to place orders.
     */
    ExecutionGateway getExecutionGateway();

    /**
     * Get logger for the strategy.
     */
    Logger getLogger();

    /**
     * Subscribe to an instrument for this strategy.
     */
    void subscribe(String instrumentKey);

    /**
     * Broadcast a signal/event to UI.
     */
    void emitSignal(String type, Object payload);
}
