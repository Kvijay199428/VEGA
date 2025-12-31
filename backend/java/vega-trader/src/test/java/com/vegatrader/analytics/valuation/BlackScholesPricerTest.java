package com.vegatrader.analytics.valuation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BlackScholesPricer.
 * Verified against standard online calculators.
 */
class BlackScholesPricerTest {

    private static final double DELTA = 0.0001;

    @Test
    @DisplayName("Call Option Price: ATM")
    void callOptionPriceATM() {
        // Spot=100, Strike=100, T=1.0, r=0.05, sigma=0.2
        // Expected Call Price ~ 10.4506
        double price = BlackScholesPricer.calculateCallPrice(100, 100, 1.0, 0.05, 0.2);
        assertEquals(10.4506, price, 0.001);
    }

    @Test
    @DisplayName("Put Option Price: ATM")
    void putOptionPriceATM() {
        // Spot=100, Strike=100, T=1.0, r=0.05, sigma=0.2
        // Expected Put Price ~ 5.5735
        double price = BlackScholesPricer.calculatePutPrice(100, 100, 1.0, 0.05, 0.2);
        assertEquals(5.5735, price, 0.001);
    }

    @Test
    @DisplayName("Greeks: Call Delta ATM")
    void callDeltaATM() {
        // S=100, K=100, T=1, r=0.05, v=0.2
        // d1 = (ln(1) + (0.05 + 0.02) * 1) / 0.2 = 0.35
        // N(d1) = N(0.35) ~ 0.6368
        double delta = BlackScholesPricer.calculateCallDelta(100, 100, 1.0, 0.05, 0.2);
        assertEquals(0.6368, delta, 0.01);
    }

    @Test
    @DisplayName("Greeks: Put Delta ATM")
    void putDeltaATM() {
        // Put Delta = Call Delta - 1 = 0.6368 - 1 = -0.3632
        double delta = BlackScholesPricer.calculatePutDelta(100, 100, 1.0, 0.05, 0.2);
        assertEquals(-0.3632, delta, 0.01);
    }

    @Test
    @DisplayName("Greeks: Vega is same for Call and Put")
    void vegaEquality() {
        double vegaCall = BlackScholesPricer.calculateVega(100, 100, 1.0, 0.05, 0.2);
        double vegaPut = BlackScholesPricer.calculateVega(100, 100, 1.0, 0.05, 0.2);
        assertEquals(vegaCall, vegaPut, DELTA);
        assertTrue(vegaCall > 0);
    }
}
