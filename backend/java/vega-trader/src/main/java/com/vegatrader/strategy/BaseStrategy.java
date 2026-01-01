package com.vegatrader.strategy;

import com.vegatrader.execution.ExecutionGateway;
import com.vegatrader.execution.dto.OrderRequest;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import org.slf4j.Logger;

/**
 * Base abstract strategy class to simplify implementation.
 */
public abstract class BaseStrategy implements Strategy {

    protected StrategyContext context;
    protected ExecutionGateway execution;
    protected Logger logger;

    @Override
    public void onInit(StrategyContext context) {
        this.context = context;
        this.execution = context.getExecutionGateway();
        this.logger = context.getLogger();
        logger.info("Initializing strategy: {}", getName());
        onStart();
    }

    /**
     * Override this for custom initialization logic.
     */
    protected abstract void onStart();

    @Override
    public void onDestroy() {
        logger.info("Stopping strategy: {}", getName());
        onStop();
    }

    /**
     * Override this for custom cleanup logic.
     */
    protected abstract void onStop();

    @Override
    public void onDepth(OrderBookSnapshot depth) {
        // Optional override
    }

    // Helpers

    protected void subscribe(String instrumentKey) {
        if (context != null)
            context.subscribe(instrumentKey);
    }

    protected void signal(String type, Object payload) {
        if (context != null)
            context.emitSignal(type, payload);
    }

    protected void buy(String instrument, int qty, double price) {
        // execution.placeOrder(...) wrapper
    }

    protected void sell(String instrument, int qty, double price) {
        // execution.placeOrder(...) wrapper
    }
}
