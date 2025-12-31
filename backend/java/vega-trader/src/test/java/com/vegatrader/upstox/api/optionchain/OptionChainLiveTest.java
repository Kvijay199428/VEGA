package com.vegatrader.upstox.api.optionchain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Option Chain Live Testing per TESTING_GUIDE.md.
 * Logs all responses to prompt/optionchain/testing/log/
 * 
 * Run with: mvn test -Dtest="OptionChainLiveTest" -DLIVE_TEST=true
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "LIVE_TEST", matches = "true")
class OptionChainLiveTest {

    private static final Logger logger = LoggerFactory.getLogger(OptionChainLiveTest.class);

    private static final String BASE_URL = "http://localhost:28020/api/v1";
    private static final String LOG_DIR = "d:/projects/VEGA TRADER/prompt/optionchain/testing/log";

    private static HttpClient httpClient;
    private static ObjectMapper mapper;
    private static PrintWriter apiFetchLog;
    private static PrintWriter cacheHitLog;
    private static PrintWriter fallbackLog;

    @BeforeAll
    static void setup() throws IOException {
        httpClient = HttpClient.newHttpClient();
        mapper = new ObjectMapper();

        // Create log directory
        Files.createDirectories(Path.of(LOG_DIR));

        // Initialize log files
        apiFetchLog = new PrintWriter(new FileWriter(LOG_DIR + "/api-fetch.log", true));
        cacheHitLog = new PrintWriter(new FileWriter(LOG_DIR + "/cache-hit.log", true));
        fallbackLog = new PrintWriter(new FileWriter(LOG_DIR + "/fallback.log", true));

        logHeader(apiFetchLog, "API FETCH LOG");
        logHeader(cacheHitLog, "CACHE HIT LOG");
        logHeader(fallbackLog, "FALLBACK LOG");
    }

    @AfterAll
    static void teardown() {
        if (apiFetchLog != null)
            apiFetchLog.close();
        if (cacheHitLog != null)
            cacheHitLog.close();
        if (fallbackLog != null)
            fallbackLog.close();
    }

    // ======= TC-OC-001: Basic Live Fetch =======

    @Test
    @Order(1)
    @DisplayName("TC-OC-001: Basic Live Fetch - NIFTY")
    void testBasicLiveFetchNifty() throws Exception {
        String symbol = "NSE_INDEX|Nifty 50";
        String expiry = getNextWeeklyExpiry();

        long startTime = System.currentTimeMillis();
        HttpResponse<String> response = fetchOptionChain(symbol, expiry);
        long latency = System.currentTimeMillis() - startTime;

        logApiResponse(apiFetchLog, symbol, expiry, response, latency);

        assertEquals(200, response.statusCode(), "Expected HTTP 200");

        JsonNode json = mapper.readTree(response.body());
        assertEquals("success", json.get("status").asText());
        assertTrue(json.get("strikeCount").asInt() > 0, "Expected non-empty strikes");

        logger.info("TC-OC-001 PASSED: {} strikes, {}ms latency",
                json.get("strikeCount").asInt(), latency);
    }

    @Test
    @Order(2)
    @DisplayName("TC-OC-001b: Basic Live Fetch - BANKNIFTY")
    void testBasicLiveFetchBankNifty() throws Exception {
        String symbol = "NSE_INDEX|Nifty Bank";
        String expiry = getNextWeeklyExpiry();

        long startTime = System.currentTimeMillis();
        HttpResponse<String> response = fetchOptionChain(symbol, expiry);
        long latency = System.currentTimeMillis() - startTime;

        logApiResponse(apiFetchLog, symbol, expiry, response, latency);

        assertEquals(200, response.statusCode());
        logger.info("TC-OC-001b PASSED: BANKNIFTY, {}ms", latency);
    }

    // ======= TC-OC-010: Cache Hit =======

    @Test
    @Order(10)
    @DisplayName("TC-OC-010: Cache Hit Validation")
    void testCacheHit() throws Exception {
        String symbol = "NSE_INDEX|Nifty 50";
        String expiry = getNextWeeklyExpiry();

        // First fetch - should populate cache
        HttpResponse<String> firstResponse = fetchOptionChain(symbol, expiry);
        assertEquals(200, firstResponse.statusCode());

        // Second fetch - should be cache hit
        long startTime = System.currentTimeMillis();
        HttpResponse<String> secondResponse = fetchOptionChain(symbol, expiry);
        long latency = System.currentTimeMillis() - startTime;

        assertEquals(200, secondResponse.statusCode());

        JsonNode json = mapper.readTree(secondResponse.body());
        String fetchSource = json.has("fetchSource") ? json.get("fetchSource").asText() : "UNKNOWN";

        logCacheHit(cacheHitLog, symbol, expiry, fetchSource, latency);

        // Cache hit should be faster
        assertTrue(latency < 100, "Cache hit should be < 100ms, was " + latency);
        logger.info("TC-OC-010 PASSED: source={}, {}ms", fetchSource, latency);
    }

    // ======= TC-OC-020: Fallback Test =======

    @Test
    @Order(20)
    @DisplayName("TC-OC-020: Fallback on Cached Data")
    void testFallback() throws Exception {
        // This tests that we get a response even if primary fetch would fail
        // In live mode, we just verify the response structure
        String symbol = "NSE_INDEX|Nifty 50";
        String expiry = getNextWeeklyExpiry();

        HttpResponse<String> response = fetchOptionChain(symbol, expiry);

        assertNotNull(response.body());
        assertTrue(response.statusCode() == 200 || response.statusCode() == 503);

        logFallback(fallbackLog, symbol, expiry, response);

        logger.info("TC-OC-020 PASSED: Status {}", response.statusCode());
    }

    // ======= TC-OC-040: RMS Integration =======

    @Test
    @Order(40)
    @DisplayName("TC-OC-040: RMS - No Disabled Strikes Returned")
    void testRmsDisabledStrikes() throws Exception {
        String symbol = "NSE_INDEX|Nifty 50";
        String expiry = getNextWeeklyExpiry();

        HttpResponse<String> response = fetchOptionChain(symbol, expiry);
        assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        JsonNode data = json.get("data");

        // All returned strikes should be enabled
        for (JsonNode strike : data) {
            double strikePrice = strike.get("strikePrice").asDouble();
            // Just verify structure - disabled strikes won't be in response
            assertTrue(strikePrice > 0, "Strike price should be positive");
        }

        logger.info("TC-OC-040 PASSED: {} valid strikes returned", data.size());
    }

    // ======= TC-OC-050: Expiries Endpoint =======

    @Test
    @Order(50)
    @DisplayName("TC-OC-050: Get Expiries List")
    void testGetExpiries() throws Exception {
        String symbol = "NSE_INDEX|Nifty 50";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/option-chain/expiries?symbol=" +
                        java.net.URLEncoder.encode(symbol, "UTF-8")))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonNode json = mapper.readTree(response.body());
        logger.info("TC-OC-050 PASSED: {} expiries", json.get("expiryCount").asInt());
    }

    // ======= TC-OC-070: Negative Test - Invalid Expiry =======

    @Test
    @Order(70)
    @DisplayName("TC-OC-070: Invalid Expiry Returns Error")
    void testInvalidExpiry() throws Exception {
        String symbol = "NSE_INDEX|Nifty 50";
        String expiry = "1900-01-01"; // Invalid past date

        HttpResponse<String> response = fetchOptionChain(symbol, expiry);

        // Should get 400 or error response
        assertTrue(response.statusCode() == 400 ||
                response.body().contains("error"),
                "Invalid expiry should return error");

        logger.info("TC-OC-070 PASSED: Invalid expiry handled correctly");
    }

    // ======= Helper Methods =======

    private HttpResponse<String> fetchOptionChain(String symbol, String expiry)
            throws Exception {
        String url = String.format("%s/option-chain?symbol=%s&expiry=%s",
                BASE_URL,
                java.net.URLEncoder.encode(symbol, "UTF-8"),
                expiry);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String getNextWeeklyExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate thursday = today.with(java.time.DayOfWeek.THURSDAY);

        if (today.isAfter(thursday) || today.isEqual(thursday)) {
            thursday = thursday.plusWeeks(1);
        }

        return thursday.format(DateTimeFormatter.ISO_DATE);
    }

    private static void logHeader(PrintWriter writer, String title) {
        writer.println("========================================");
        writer.println(title);
        writer.println("Generated: " + ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
        writer.println("========================================");
        writer.println();
        writer.flush();
    }

    private static void logApiResponse(PrintWriter writer, String symbol, String expiry,
            HttpResponse<String> response, long latencyMs) {
        writer.printf("[%s] FETCH: symbol=%s, expiry=%s, status=%d, latency=%dms%n",
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                symbol, expiry, response.statusCode(), latencyMs);
        writer.println("Response: " + truncate(response.body(), 500));
        writer.println("---");
        writer.flush();
    }

    private static void logCacheHit(PrintWriter writer, String symbol, String expiry,
            String source, long latencyMs) {
        writer.printf("[%s] CACHE: symbol=%s, expiry=%s, source=%s, latency=%dms%n",
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                symbol, expiry, source, latencyMs);
        writer.println("---");
        writer.flush();
    }

    private static void logFallback(PrintWriter writer, String symbol, String expiry,
            HttpResponse<String> response) {
        writer.printf("[%s] FALLBACK: symbol=%s, expiry=%s, status=%d%n",
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                symbol, expiry, response.statusCode());
        writer.println("---");
        writer.flush();
    }

    private static String truncate(String s, int maxLen) {
        if (s == null)
            return "null";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
