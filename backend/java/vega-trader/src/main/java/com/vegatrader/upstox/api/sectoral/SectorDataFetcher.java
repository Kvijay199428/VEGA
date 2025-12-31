package com.vegatrader.upstox.api.sectoral;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fetches and parses NSE sectoral index data from CSV files.
 * <p>
 * This class downloads CSV files from NSE's public endpoints and parses them
 * into {@link SectorConstituent} objects. It handles network errors, parsing
 * errors,
 * and provides convenient methods for fetching sector data.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * SectorDataFetcher fetcher = new SectorDataFetcher();
 * 
 * // Fetch all constituents for Nifty Bank
 * List<SectorConstituent> bankStocks = fetcher.fetchSectorData(SectoralIndex.BANK);
 * 
 * // Get top 5 by weight
 * List<SectorConstituent> top5 = fetcher.getTopConstituents(SectoralIndex.BANK, 5);
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class SectorDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(SectorDataFetcher.class);
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int READ_TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient;

    /**
     * Creates a new sector data fetcher with default HTTP client.
     */
    public SectorDataFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Creates a new sector data fetcher with custom HTTP client.
     *
     * @param httpClient the HTTP client to use
     */
    public SectorDataFetcher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetches all constituents for the given sector.
     *
     * @param sector the sectoral index
     * @return list of sector constituents
     * @throws SectorDataException if fetching or parsing fails
     */
    public List<SectorConstituent> fetchSectorData(SectoralIndex sector) throws SectorDataException {
        logger.info("Fetching sector data for: {}", sector.getDisplayName());

        try {
            String csvContent = downloadCsv(sector.getFullUrl());
            List<SectorConstituent> constituents = parseCsv(csvContent, sector);

            logger.info("Successfully fetched {} constituents for {}",
                    constituents.size(), sector.getDisplayName());

            return constituents;
        } catch (Exception e) {
            logger.error("Failed to fetch sector data for {}", sector.getDisplayName(), e);
            throw new SectorDataException("Failed to fetch sector data: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches top N constituents by weight.
     *
     * @param sector the sectoral index
     * @param limit  the number of top constituents to return
     * @return list of top constituents sorted by weight (descending)
     * @throws SectorDataException if fetching fails
     */
    public List<SectorConstituent> getTopConstituents(SectoralIndex sector, int limit)
            throws SectorDataException {
        List<SectorConstituent> all = fetchSectorData(sector);

        return all.stream()
                .sorted((a, b) -> Double.compare(
                        b.getWeight() != null ? b.getWeight() : 0.0,
                        a.getWeight() != null ? a.getWeight() : 0.0))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Fetches constituents filtered by minimum weight.
     *
     * @param sector    the sectoral index
     * @param minWeight minimum weight percentage
     * @return list of constituents with weight >= minWeight
     * @throws SectorDataException if fetching fails
     */
    public List<SectorConstituent> getConstituentsByMinWeight(SectoralIndex sector, double minWeight)
            throws SectorDataException {
        List<SectorConstituent> all = fetchSectorData(sector);

        return all.stream()
                .filter(c -> c.getWeight() != null && c.getWeight() >= minWeight)
                .sorted((a, b) -> Double.compare(b.getWeight(), a.getWeight()))
                .collect(Collectors.toList());
    }

    /**
     * Downloads CSV content from URL.
     *
     * @param url the CSV file URL
     * @return the CSV content as string
     * @throws Exception if download fails
     */
    private String downloadCsv(String url) throws Exception {
        logger.debug("Downloading CSV from: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .timeout(Duration.ofSeconds(READ_TIMEOUT_SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new SectorDataException(
                    String.format("HTTP %d when downloading CSV from %s",
                            response.statusCode(), url));
        }

        return response.body();
    }

    /**
     * Parses CSV content into sector constituents.
     *
     * @param csvContent the CSV content
     * @param sector     the sector (for logging)
     * @return list of parsed constituents
     * @throws Exception if parsing fails
     */
    private List<SectorConstituent> parseCsv(String csvContent, SectoralIndex sector)
            throws Exception {
        List<SectorConstituent> constituents = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new java.io.StringReader(csvContent));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreHeaderCase(true)
                                .setTrim(true)
                                .build())) {

            for (CSVRecord record : csvParser) {
                try {
                    SectorConstituent constituent = parseRecord(record);
                    if (constituent != null) {
                        constituents.add(constituent);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse CSV record: {}", record, e);
                    // Continue parsing other records
                }
            }
        }

        return constituents;
    }

    /**
     * Parses a single CSV record into a SectorConstituent.
     *
     * @param record the CSV record
     * @return the parsed constituent, or null if invalid
     */
    private SectorConstituent parseRecord(CSVRecord record) {
        // Common CSV column names (NSE uses different variations)
        String symbol = getRecordValue(record, "Symbol", "Company", "Ticker");
        String companyName = getRecordValue(record, "Company Name", "Name", "Company");
        String industry = getRecordValue(record, "Industry", "Sector", "Sub-Sector");
        String series = getRecordValue(record, "Series");
        String isinCode = getRecordValue(record, "ISIN Code", "ISIN", "ISINCode");

        // Parse weight
        Double weight = null;
        String weightStr = getRecordValue(record, "Weight(%)", "Weight", "Weightage");
        if (weightStr != null && !weightStr.isEmpty()) {
            try {
                weight = Double.parseDouble(weightStr.replace("%", "").trim());
            } catch (NumberFormatException e) {
                logger.debug("Could not parse weight: {}", weightStr);
            }
        }

        // Validate required fields
        if (symbol == null || symbol.isEmpty()) {
            return null;
        }

        return SectorConstituent.builder()
                .symbol(symbol)
                .companyName(companyName)
                .industry(industry)
                .series(series)
                .isinCode(isinCode)
                .weight(weight)
                .build();
    }

    /**
     * Gets value from CSV record trying multiple column name variations.
     *
     * @param record      the CSV record
     * @param columnNames possible column names to try
     * @return the value, or null if not found
     */
    private String getRecordValue(CSVRecord record, String... columnNames) {
        for (String columnName : columnNames) {
            try {
                if (record.isMapped(columnName)) {
                    String value = record.get(columnName);
                    if (value != null && !value.trim().isEmpty()) {
                        return value.trim();
                    }
                }
            } catch (IllegalArgumentException e) {
                // Column doesn't exist, try next
            }
        }
        return null;
    }

    /**
     * Exception thrown when sector data fetching fails.
     */
    public static class SectorDataException extends Exception {
        public SectorDataException(String message) {
            super(message);
        }

        public SectorDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
