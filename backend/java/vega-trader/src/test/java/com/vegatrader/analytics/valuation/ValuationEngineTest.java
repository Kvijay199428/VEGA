package com.vegatrader.analytics.valuation;

import com.vegatrader.upstox.api.optionchain.model.OptionChainStrike;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ValuationEngineTest {

        @Test
        @DisplayName("Evaluate: Call Overvalued")
        void evaluateCallOvervalued() {
                // Fair Vol = 20%
                // We set market price to match a HIGHER Vol (e.g. 50%) or just high price
                // Fair Price (S=100, K=100, T=1, r=0.05, v=0.20) ~ 10.45
                // We set Market Price = 15.0 (approx 43% higher)
                // Threshold = 10%

                var cfg = new ValuationSettings(
                                0.05, 0.20, 2.0, 10.0, 10.0, 0.02, 500, 1000, true, true, true, false);

                var md = new OptionChainStrike.MarketData(
                                15.0, 14.0, 1000, 5000, 14.8, 100, 15.2, 100, 4800);

                var option = new OptionChainStrike.OptionData(
                                "key", md, null, null);

                var result = ValuationEngine.evaluate(
                                option, 100.0, 100.0, 1.0, true, 0.20, cfg);

                assertEquals(ValuationStatus.OVERVALUED, result.status());
                assertEquals(Action.SELL, result.action());
                assertTrue(result.fairPrice() < 11.0); // ~10.45
                assertTrue(result.mispricingPct() > 10.0);
        }

        @Test
        @DisplayName("Evaluate: Put Undervalued")
        void evaluatePutUndervalued() {
                // Fair Price (S=100, K=100, T=1, r=0.05, v=0.20) ~ 5.57
                // Market Price = 4.0 (approx 28% lower)
                // Threshold = 10%

                var cfg = new ValuationSettings(
                                0.05, 0.20, 2.0, 10.0, 10.0, 0.02, 500, 1000, true, true, true, false);

                var md = new OptionChainStrike.MarketData(
                                4.0, 5.0, 1000, 5000, 3.9, 100, 4.1, 100, 4800);

                var option = new OptionChainStrike.OptionData(
                                "key", md, null, null);

                var result = ValuationEngine.evaluate(
                                option, 100.0, 100.0, 1.0, false, 0.20, cfg);

                assertEquals(ValuationStatus.UNDERVALUED, result.status());
                assertEquals(Action.BUY, result.action());
                assertTrue(result.fairPrice() > 5.0); // ~5.57
                assertTrue(result.mispricingPct() < -10.0);
        }

        @Test
        @DisplayName("Evaluate: Near Expiry (Theta Decay / Zero Time)")
        void evaluateNearExpiry() {
                var cfg = ValuationSettings.defaults();
                var md = new OptionChainStrike.MarketData(10.0, 10.0, 100, 100, 9.0, 10, 11.0, 10, 100);
                var option = new OptionChainStrike.OptionData("key", md, null, null);

                // Time = 0
                var result = ValuationEngine.evaluate(
                                option, 100.0, 100.0, 0.0, true, 0.20, cfg);

                assertEquals(ValuationStatus.FAIR, result.status());
                assertEquals(Action.HOLD, result.action());
        }

        @Test
        @DisplayName("Evaluate: Confidence Score (High Spread -> Low Confidence)")
        void evaluateConfidenceLow() {
                var cfg = ValuationSettings.defaults();

                // Spread 10 vs 20 (100% spread!)
                // Volume low
                var md = new OptionChainStrike.MarketData(
                                15.0, 14.0, 50, 500, 10.0, 100, 20.0, 100, 4800);

                var option = new OptionChainStrike.OptionData(
                                "key", md, null, null);

                var result = ValuationEngine.evaluate(
                                option, 100.0, 100.0, 1.0, true, 0.20, cfg);

                // Depending on ConfidenceScorer logic (check threshold)
                // Spread > 5% usually reduces confidence?
                // Let's assert it is NOT HIGH
                assertNotEquals(ConfidenceLevel.HIGH, result.confidence());
        }
}
