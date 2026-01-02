package com.vegatrader.upstox.api.instrument;

import com.vegatrader.upstox.api.instrument.entity.*;
import com.vegatrader.upstox.api.instrument.repository.*;
import com.vegatrader.upstox.api.instrument.search.InstrumentSearchService;
import com.vegatrader.upstox.api.instrument.validation.InstrumentKeyPattern;
import com.vegatrader.upstox.api.instrument.risk.ProductType;
import com.vegatrader.upstox.api.instrument.risk.RiskValidationService;
import com.vegatrader.upstox.api.utils.InstrumentKeyValidator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.vegatrader.service.UpstoxTokenProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Instrument Module.
 * 
 * <p>
 * Tests repositories, search, and risk validation.
 * 
 * @since 4.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class InstrumentModuleIntegrationTest {

    // Mock external dependencies not available in test context
    @MockBean
    private UpstoxTokenProvider tokenProvider;

    @Autowired
    private InstrumentMasterRepository masterRepository;

    @Autowired
    private InstrumentMisRepository misRepository;

    @Autowired
    private InstrumentMtfRepository mtfRepository;

    @Autowired
    private InstrumentSuspensionRepository suspensionRepository;

    @Autowired
    private InstrumentSearchService searchService;

    @Autowired
    private RiskValidationService riskValidationService;

    private static final String TEST_KEY = "NSE_EQ|INE002A01018";
    private static final String TEST_SYMBOL = "RELIANCE";
    private static final LocalDate TODAY = LocalDate.now();

    @BeforeEach
    void setUp() {
        // Create test instrument
        InstrumentMasterEntity instrument = new InstrumentMasterEntity();
        instrument.setInstrumentKey(TEST_KEY);
        instrument.setSegment("NSE_EQ");
        instrument.setExchange("NSE");
        instrument.setInstrumentType("EQ");
        instrument.setTradingSymbol(TEST_SYMBOL);
        instrument.setName("Reliance Industries Ltd");
        instrument.setIsin("INE002A01018");
        instrument.setLotSize(1);
        instrument.setTickSize(0.05);
        instrument.setTradingDate(TODAY);
        instrument.setIsActive(true);
        masterRepository.save(instrument);
    }

    // ==================== Pattern Validation Tests ====================

    @Test
    @Order(1)
    @DisplayName("Validate single instrument key pattern")
    void testSingleKeyPattern() {
        assertTrue(InstrumentKeyPattern.isValidSingleKey("NSE_EQ|INE002A01018"));
        assertTrue(InstrumentKeyPattern.isValidSingleKey("NSE_FO|NIFTY25JANFUT"));
        assertTrue(InstrumentKeyPattern.isValidSingleKey("NSE_INDEX|Nifty 50"));

        assertFalse(InstrumentKeyPattern.isValidSingleKey("INVALID"));
        assertFalse(InstrumentKeyPattern.isValidSingleKey("NSE|RELIANCE"));
        assertFalse(InstrumentKeyPattern.isValidSingleKey(null));
    }

    @Test
    @Order(2)
    @DisplayName("Validate multi-key pattern")
    void testMultiKeyPattern() {
        assertTrue(InstrumentKeyPattern.isValidMultiKey("NSE_EQ|REL,NSE_EQ|TCS"));
        assertFalse(InstrumentKeyPattern.isValidMultiKey("INVALID,KEYS"));
    }

    @Test
    @Order(3)
    @DisplayName("Validate expired key pattern")
    void testExpiredKeyPattern() {
        assertTrue(InstrumentKeyPattern.isValidExpiredKey("NSE_FO|RELIANCE|27-06-2024"));
        assertFalse(InstrumentKeyPattern.isValidExpiredKey("NSE_FO|RELIANCE|2024-06-27"));
    }

    // ==================== InstrumentKeyValidator Tests ====================

    @Test
    @Order(4)
    @DisplayName("Test InstrumentKeyValidator.isValid()")
    void testInstrumentKeyValidatorIsValid() {
        assertTrue(InstrumentKeyValidator.isValid("NSE_EQ|RELIANCE"));
        assertTrue(InstrumentKeyValidator.isValid("BSE_FO|NIFTY"));
        assertFalse(InstrumentKeyValidator.isValid("INVALID"));
    }

    @Test
    @Order(5)
    @DisplayName("Test InstrumentKeyValidator segment checks")
    void testSegmentChecks() {
        assertTrue(InstrumentKeyValidator.isNSEEquity("NSE_EQ|REL"));
        assertTrue(InstrumentKeyValidator.isNSEFO("NSE_FO|NIFTY"));
        assertTrue(InstrumentKeyValidator.isBSEEquity("BSE_EQ|TCS"));
        assertTrue(InstrumentKeyValidator.isIndex("NSE_INDEX|Nifty 50"));
        assertTrue(InstrumentKeyValidator.isDerivative("NSE_FO|RELIANCE"));
    }

    // ==================== Repository Tests ====================

    @Test
    @Order(10)
    @DisplayName("Find instrument by key")
    void testFindByKey() {
        Optional<InstrumentMasterEntity> result = masterRepository.findById(TEST_KEY);

        assertTrue(result.isPresent());
        assertEquals(TEST_SYMBOL, result.get().getTradingSymbol());
        assertEquals("NSE_EQ", result.get().getSegment());
    }

    @Test
    @Order(11)
    @DisplayName("Search by symbol prefix")
    void testSearchBySymbolPrefix() {
        List<InstrumentMasterEntity> results = masterRepository.searchBySymbolPrefix("REL");

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(i -> i.getTradingSymbol().equals(TEST_SYMBOL)));
    }

    @Test
    @Order(12)
    @DisplayName("Search by segment and type")
    void testSearchBySegmentAndType() {
        List<InstrumentMasterEntity> results = masterRepository
                .findByTradingSymbolIgnoreCaseAndSegmentAndInstrumentType(TEST_SYMBOL, "NSE_EQ", "EQ");

        assertEquals(1, results.size());
        assertEquals(TEST_KEY, results.get(0).getInstrumentKey());
    }

    // ==================== Overlay Tests ====================

    @Test
    @Order(20)
    @DisplayName("MIS overlay check")
    void testMisOverlay() {
        // Add MIS overlay
        InstrumentMisEntity mis = new InstrumentMisEntity();
        mis.setInstrumentKey(TEST_KEY);
        mis.setIntradayMargin(20.0);
        mis.setIntradayLeverage(5.0);
        mis.setTradingDate(TODAY);
        misRepository.save(mis);

        assertTrue(misRepository.existsByInstrumentKey(TEST_KEY));
    }

    @Test
    @Order(21)
    @DisplayName("MTF overlay check")
    void testMtfOverlay() {
        // Add MTF overlay
        InstrumentMtfEntity mtf = new InstrumentMtfEntity();
        mtf.setInstrumentKey(TEST_KEY);
        mtf.setMtfEnabled(true);
        mtf.setMtfBracket(3.0);
        mtf.setTradingDate(TODAY);
        mtfRepository.save(mtf);

        assertTrue(mtfRepository.isMtfEnabled(TEST_KEY));
    }

    @Test
    @Order(22)
    @DisplayName("Suspension overlay check")
    void testSuspensionOverlay() {
        // Instrument should not be suspended initially
        assertFalse(suspensionRepository.existsByInstrumentKey(TEST_KEY));

        // Add suspension
        InstrumentSuspensionEntity suspension = new InstrumentSuspensionEntity();
        suspension.setInstrumentKey(TEST_KEY);
        suspension.setTradingDate(TODAY);
        suspension.setReason("Corporate Action");
        suspensionRepository.save(suspension);

        assertTrue(suspensionRepository.existsByInstrumentKey(TEST_KEY));
    }

    // ==================== Search Service Tests ====================

    @Test
    @Order(30)
    @DisplayName("Resolve instrument key")
    void testResolveInstrumentKey() {
        Optional<String> key = searchService.resolveInstrumentKey(TEST_SYMBOL, "NSE_EQ", "EQ");

        assertTrue(key.isPresent());
        assertEquals(TEST_KEY, key.get());
    }

    @Test
    @Order(31)
    @DisplayName("Autocomplete search")
    void testAutocomplete() {
        var results = searchService.autocomplete("REL");

        assertFalse(results.isEmpty());
    }

    @Test
    @Order(32)
    @DisplayName("Find by key with overlays")
    void testFindByKeyWithOverlays() {
        var result = searchService.findByKey(TEST_KEY);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getMisAllowed());
        assertNotNull(result.get().getMtfEnabled());
        assertNotNull(result.get().getSuspended());
    }

    // ==================== Risk Validation Tests ====================

    @Test
    @Order(40)
    @DisplayName("Risk validation - CNC always allowed")
    void testRiskValidationCNC() {
        var result = riskValidationService.validate(TEST_KEY, ProductType.CNC, 10, 2500.0);

        assertTrue(result.isApproved());
        assertNotNull(result.getRequiredMargin());
        assertEquals(25000.0, result.getRequiredMargin()); // 10 * 2500 * 100%
    }

    @Test
    @Order(41)
    @DisplayName("Risk validation - MIS requires overlay")
    void testRiskValidationMIS() {
        // Without MIS overlay, should be rejected
        var result = riskValidationService.validate(TEST_KEY, ProductType.MIS, 10, 2500.0);

        assertFalse(result.isApproved());
        assertNotNull(result.getReason());
    }

    @Test
    @Order(42)
    @DisplayName("Risk validation - suspended instrument rejected")
    void testRiskValidationSuspended() {
        // Add suspension
        InstrumentSuspensionEntity suspension = new InstrumentSuspensionEntity();
        suspension.setInstrumentKey(TEST_KEY);
        suspension.setTradingDate(TODAY);
        suspensionRepository.save(suspension);

        var result = riskValidationService.validate(TEST_KEY, ProductType.CNC, 10, 2500.0);

        assertFalse(result.isApproved());
        assertTrue(result.getReason().contains("suspended"));
    }

    @Test
    @Order(43)
    @DisplayName("Trading eligibility check")
    void testTradingEligibility() {
        var eligibility = riskValidationService.getEligibility(TEST_KEY);

        assertTrue(eligibility.isExists());
        assertFalse(eligibility.isSuspended());
        assertTrue(eligibility.isCncAllowed());
    }
}
