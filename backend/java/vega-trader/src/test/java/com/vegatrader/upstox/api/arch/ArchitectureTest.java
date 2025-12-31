package com.vegatrader.upstox.api.arch;

import com.vegatrader.upstox.api.broker.service.*;
import com.vegatrader.upstox.api.admin.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Architecture features per arch/a1-a6.md.
 */
class ArchitectureTest {

    // === BrokerInstrumentPrewarmJob Tests ===

    @Test
    @DisplayName("OptionDescriptor: record creation")
    void optionDescriptorCreation() {
        var descriptor = new BrokerInstrumentPrewarmJob.OptionDescriptor(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                BigDecimal.valueOf(24000),
                "CE");

        assertEquals("NSE_INDEX|Nifty 50", descriptor.underlyingKey());
        assertEquals(LocalDate.of(2025, 1, 30), descriptor.expiry());
        assertEquals(BigDecimal.valueOf(24000), descriptor.strike());
        assertEquals("CE", descriptor.optionType());
    }

    // === MultiBrokerResolver Tests ===

    @Test
    @DisplayName("MultiBrokerResolver: enabled brokers list")
    void multiBrokerResolverEnabledBrokers() {
        MultiBrokerResolver resolver = new MultiBrokerResolver();
        List<String> brokers = resolver.getEnabledBrokers();

        assertNotNull(brokers);
        assertTrue(brokers.contains("UPSTOX"));
    }

    @Test
    @DisplayName("MultiBrokerResolver: broker availability check")
    void multiBrokerResolverAvailability() {
        MultiBrokerResolver resolver = new MultiBrokerResolver();

        assertFalse(resolver.isBrokerAvailable("UPSTOX")); // Not registered yet

        // Register a mock resolver
        resolver.registerResolver("UPSTOX", new MockResolver());
        assertTrue(resolver.isBrokerAvailable("UPSTOX"));
    }

    @Test
    @DisplayName("BrokerInstrument: record creation")
    void brokerInstrumentRecordCreation() {
        var instrument = new MultiBrokerResolver.BrokerInstrument(
                "NSE_FO|37590",
                "37590",
                "NIFTY25JAN24000CE",
                50,
                BigDecimal.valueOf(0.05),
                1800,
                true);

        assertEquals("NSE_FO|37590", instrument.brokerInstrumentKey());
        assertEquals(50, instrument.lotSize());
        assertTrue(instrument.weekly());
    }

    // === Admin DTOs Tests ===

    @Test
    @DisplayName("StrikeDisableRequest: record creation")
    void strikeDisableRequest() {
        var request = new StrikeDisableRequest(
                "NSE",
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                24000.0,
                "CE",
                "Illiquid");

        assertEquals("NSE", request.exchange());
        assertEquals(24000.0, request.strike());
        assertEquals("CE", request.optionType());
    }

    @Test
    @DisplayName("BrokerPriorityRequest: record creation")
    void brokerPriorityRequest() {
        var request = new BrokerPriorityRequest(
                "OPTION",
                "NSE",
                List.of("UPSTOX", "ZERODHA"));

        assertEquals("OPTION", request.instrumentType());
        assertEquals(2, request.priority().size());
        assertEquals("UPSTOX", request.priority().get(0));
    }

    @Test
    @DisplayName("ContractRollbackRequest: record creation")
    void contractRollbackRequest() {
        var request = new ContractRollbackRequest(
                "UPSTOX",
                17,
                "Incorrect expiry mapping");

        assertEquals("UPSTOX", request.broker());
        assertEquals(17, request.contractVersion());
    }

    // === Mock Resolver for Testing ===

    static class MockResolver implements BrokerInstrumentResolver {
        @Override
        public MultiBrokerResolver.BrokerInstrument resolveOption(
                String underlyingKey, LocalDate expiry, BigDecimal strike, String optionType) {
            return new MultiBrokerResolver.BrokerInstrument(
                    "NSE_FO|12345", "12345", "MOCK", 50, BigDecimal.valueOf(0.05), 1800, false);
        }

        @Override
        public String getBrokerCode() {
            return "MOCK";
        }

        @Override
        public boolean isHealthy() {
            return true;
        }
    }
}
