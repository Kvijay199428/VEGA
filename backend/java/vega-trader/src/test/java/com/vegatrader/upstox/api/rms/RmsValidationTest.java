package com.vegatrader.upstox.api.rms;

import com.vegatrader.upstox.api.rms.eligibility.*;
import com.vegatrader.upstox.api.rms.client.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RMS eligibility and client risk.
 * Tests cache, resolver, and evaluator logic.
 * 
 * Section 5 & 6: Eligibility Cache & RMS Validation Tests
 */
class RmsValidationTest {

    private ClientRiskEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ClientRiskEvaluator();
    }

    // === 5.1 ProductEligibility Tests ===

    @Test
    @DisplayName("ProductEligibility: normal allows all")
    void normalEligibilityAllowsAll() {
        ProductEligibility eligibility = ProductEligibility.normal();

        assertTrue(eligibility.misAllowed());
        assertTrue(eligibility.mtfAllowed());
        assertTrue(eligibility.cncAllowed());
        assertTrue(eligibility.isTradable());
    }

    @Test
    @DisplayName("ProductEligibility: CNC only blocks MIS/MTF")
    void cncOnlyBlocksMisMtf() {
        ProductEligibility eligibility = ProductEligibility.cncOnly("PCA");

        assertFalse(eligibility.misAllowed());
        assertFalse(eligibility.mtfAllowed());
        assertTrue(eligibility.cncAllowed());
        assertEquals("PCA", eligibility.reason());
    }

    @Test
    @DisplayName("ProductEligibility: blocked prevents all trading")
    void blockedPreventsTrading() {
        ProductEligibility eligibility = ProductEligibility.blocked("SUSPENDED");

        assertFalse(eligibility.misAllowed());
        assertFalse(eligibility.mtfAllowed());
        assertFalse(eligibility.cncAllowed());
        assertFalse(eligibility.isTradable());
    }

    // === 5.2 ClientRiskState Tests ===

    @Test
    @DisplayName("ClientRiskState: zero state initialization")
    void zeroStateInitialization() {
        ClientRiskState state = ClientRiskState.zero("CLIENT1");

        assertEquals("CLIENT1", state.clientId());
        assertEquals(0, state.grossExposure());
        assertEquals(0, state.netExposure());
        assertEquals(0, state.openPositions());
    }

    @Test
    @DisplayName("ClientRiskState: withOrder updates correctly")
    void withOrderUpdatesCorrectly() {
        ClientRiskState state = ClientRiskState.zero("CLIENT1");
        ClientRiskState updated = state.withOrder(100000.0, 1, true);

        assertEquals(100000.0, updated.grossExposure());
        assertEquals(100000.0, updated.netExposure());
        assertEquals(1, updated.openPositions());
    }

    @Test
    @DisplayName("ClientRiskState: intraday loss calculation")
    void intradayLossCalculation() {
        ClientRiskState profit = new ClientRiskState("C1", 0, 0, 0, 5000, 0);
        ClientRiskState loss = new ClientRiskState("C1", 0, 0, 0, -5000, 0);

        assertEquals(0, profit.intradayLoss());
        assertEquals(5000, loss.intradayLoss());
    }

    // === 6.1 ClientRiskEvaluator Tests ===

    @Test
    @DisplayName("ClientRiskEvaluator: passes valid order")
    void evaluatorPassesValidOrder() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 1_000_000, 500_000, 100_000, 5_000_000, 10, 50_000, true);
        ClientRiskState state = ClientRiskState.zero("C1");

        // Should not throw
        assertDoesNotThrow(() -> evaluator.validate(limit, state, 50000, 50000, 50000, 1));
    }

    @Test
    @DisplayName("ClientRiskEvaluator: rejects disabled client")
    void evaluatorRejectsDisabledClient() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 1_000_000, 500_000, 100_000, 5_000_000, 10, 50_000, false // disabled
        );
        ClientRiskState state = ClientRiskState.zero("C1");

        RiskRejectException ex = assertThrows(RiskRejectException.class,
                () -> evaluator.validate(limit, state, 50000, 50000, 50000, 1));
        assertEquals("CLIENT_DISABLED", ex.getCode());
    }

    @Test
    @DisplayName("ClientRiskEvaluator: rejects order value exceeded")
    void evaluatorRejectsOrderValueExceeded() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 1_000_000, 500_000, 100_000, 5_000_000, 10, 50_000, true);
        ClientRiskState state = ClientRiskState.zero("C1");

        RiskRejectException ex = assertThrows(RiskRejectException.class,
                () -> evaluator.validate(limit, state, 150000, 150000, 150000, 1) // exceeds 100k
        );
        assertEquals("ORDER_VALUE_LIMIT", ex.getCode());
    }

    @Test
    @DisplayName("ClientRiskEvaluator: rejects gross exposure exceeded")
    void evaluatorRejectsGrossExposureExceeded() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 500_000, 500_000, 100_000, 5_000_000, 10, 50_000, true);
        ClientRiskState state = new ClientRiskState("C1", 450_000, 0, 0, 0, 0);

        RiskRejectException ex = assertThrows(RiskRejectException.class,
                () -> evaluator.validate(limit, state, 80000, 530_000, 80000, 1) // projected > 500k
        );
        assertEquals("GROSS_EXPOSURE_LIMIT", ex.getCode());
    }

    @Test
    @DisplayName("ClientRiskEvaluator: rejects position count exceeded")
    void evaluatorRejectsPositionCountExceeded() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 10_000_000, 5_000_000, 500_000, 50_000_000, 5, 100_000, true);
        ClientRiskState state = new ClientRiskState("C1", 0, 0, 0, 0, 5);

        RiskRejectException ex = assertThrows(RiskRejectException.class,
                () -> evaluator.validate(limit, state, 50000, 50000, 50000, 6) // 6 > 5
        );
        assertEquals("POSITION_COUNT_LIMIT", ex.getCode());
    }

    @Test
    @DisplayName("ClientRiskEvaluator: rejects max loss hit")
    void evaluatorRejectsMaxLossHit() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 10_000_000, 5_000_000, 500_000, 50_000_000, 100, 20_000, true);
        ClientRiskState state = new ClientRiskState("C1", 200_000, 100_000, 1_000_000, -25_000, 3);

        RiskRejectException ex = assertThrows(RiskRejectException.class,
                () -> evaluator.validate(limit, state, 50000, 250_000, 150_000, 4));
        assertEquals("MAX_LOSS_HIT", ex.getCode());
    }

    // === 6.2 canTrade Tests ===

    @Test
    @DisplayName("ClientRiskEvaluator: canTrade returns false when disabled")
    void canTradeReturnsFalseWhenDisabled() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 1_000_000, 500_000, 100_000, 5_000_000, 10, 50_000, false);
        ClientRiskState state = ClientRiskState.zero("C1");

        assertFalse(evaluator.canTrade(limit, state));
    }

    @Test
    @DisplayName("ClientRiskEvaluator: canTrade returns false when max loss hit")
    void canTradeReturnsFalseWhenMaxLossHit() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 1_000_000, 500_000, 100_000, 5_000_000, 10, 10_000, true);
        ClientRiskState state = new ClientRiskState("C1", 0, 0, 0, -15_000, 0);

        assertFalse(evaluator.canTrade(limit, state));
    }

    @Test
    @DisplayName("ClientRiskEvaluator: canTrade returns true when valid")
    void canTradeReturnsTrueWhenValid() {
        ClientRiskLimit limit = new ClientRiskLimit(
                "C1", 1_000_000, 500_000, 100_000, 5_000_000, 10, 50_000, true);
        ClientRiskState state = ClientRiskState.zero("C1");

        assertTrue(evaluator.canTrade(limit, state));
    }
}
