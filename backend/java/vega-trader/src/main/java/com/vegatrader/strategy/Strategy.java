package com.vegatrader.strategy;

import com.vegatrader.execution.ExecutionGateway;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;

/**
 * Strategy Interface.
 * Defines the lifecycle and callbacks for a trading strategy.
 */
public interface Strategy {

    /**
     * Unique ID of the strategy instance.
     */
    String getId();

    /**
     * Human-readable name.
     */
    String getName();

    /**
     * Initialize strategy resources.
     * 
     * @param context The strategy execution context (access to execution, history,
     *                generic services)
     */
    void onInit(StrategyContext context);

    /**
     * Called on each market tick for subscribed instruments.
     */
    void onTick(LiveMarketSnapshot tick);

    /**
     * Called on depth updates.
     */
    void onDepth(OrderBookSnapshot depth);

    /**
     * Called when strategy is stopped.
     */
    void onDestroy();
}
