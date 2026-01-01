package com.vegatrader.strategy.sample;

import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.strategy.BaseStrategy;
import org.springframework.stereotype.Component;

/**
 * Simple Momentum Strategy for testing.
 */
@Component
public class MomentumStrategy extends BaseStrategy {

    private static final String ID = "MOMENTUM_V1";
    private static final String TARGET_INSTRUMENT = "NSE_EQ|RELIANCE";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Simple Momentum Test";
    }

    @Override
    protected void onStart() {
        logger.info("Starting Momentum Strategy...");
        // subscribe(TARGET_INSTRUMENT); // Auto-subscribe on start?
        // Let's wait for user to configure usually, but for test we can hardcode
    }

    @Override
    protected void onStop() {
        logger.info("Momentum Strategy Stopped");
    }

    @Override
    public void onTick(LiveMarketSnapshot tick) {
        if (tick.getInstrumentKey().equals(TARGET_INSTRUMENT)) {
            // Simple logic: if change > 1%, emit signal
            if (tick.getChangePercent() > 1.0) {
                logger.info("Momentum signal! Change: {}%", tick.getChangePercent());
                signal("BUY_SIGNAL", "Strong momentum detected: " + tick.getLtp());
            }
        }
    }
}
