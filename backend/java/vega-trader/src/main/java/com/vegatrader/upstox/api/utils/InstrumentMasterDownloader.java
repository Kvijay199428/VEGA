package com.vegatrader.upstox.api.utils;

import com.vegatrader.upstox.api.response.instrument.InstrumentResponse;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for downloading and parsing instrument master files.
 *
 * @since 2.0.0
 */
public final class InstrumentMasterDownloader {

    private InstrumentMasterDownloader() {
        // Utility class - no instantiation
    }

    /**
     * Base URL for instrument master files.
     */
    public static final String BASE_URL = "https://assets.upstox.com/market-quote/instruments/exchange/";

    /**
     * Available exchanges for instrument download.
     */
    public enum Exchange {
        COMPLETE("complete.csv.gz"),
        NSE("NSE.json.gz"),
        BSE("BSE.json.gz"),
        MCX("MCX.json.gz"),
        SUSPENDED("suspended-instrument.json.gz"),
        MTF("MTF.json.gz"),
        NSE_MIS("NSE_MIS.json.gz"),
        BSE_MIS("BSE_MIS.json.gz");

        private final String filename;

        Exchange(String filename) {
            this.filename = filename;
        }

        public String getUrl() {
            return BASE_URL + filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    /**
     * Downloads instrument master file for specified exchange.
     *
     * @param exchange   the exchange
     * @param outputPath the output file path
     * @throws IOException if download fails
     */
    public static void download(Exchange exchange, String outputPath) throws IOException {
        URL url = java.net.URI.create(exchange.getUrl()).toURL();

        try (InputStream in = url.openStream();
                FileOutputStream out = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Downloads and decompresses instrument master file.
     *
     * @param exchange   the exchange
     * @param outputPath the decompressed output path
     * @throws IOException if download or decompression fails
     */
    public static void downloadAndDecompress(Exchange exchange, String outputPath) throws IOException {
        URL url = java.net.URI.create(exchange.getUrl()).toURL();

        try (InputStream in = url.openStream();
                GZIPInputStream gzipIn = new GZIPInputStream(in);
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIn, StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Parses CSV instrument master file.
     *
     * @param filePath the CSV file path
     * @return list of instruments
     * @throws IOException if file reading fails
     */
    public static List<InstrumentResponse> parseCSV(String filePath) throws IOException {
        List<InstrumentResponse> instruments = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            String header = reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    InstrumentResponse instrument = new InstrumentResponse();
                    instrument.setExchangeToken(parts[0]);
                    instrument.setTradingSymbol(parts[1]);
                    instrument.setName(parts[2]);
                    instrument.setSegment(parts[3]);
                    instrument.setExpiry(parts.length > 4 ? parts[4] : null);
                    instrument.setStrike(parts.length > 5 && !parts[5].isEmpty() ? Double.parseDouble(parts[5]) : null);
                    instrument.setLotSize(parts.length > 6 && !parts[6].isEmpty() ? Integer.parseInt(parts[6]) : null);
                    instrument.setInstrumentType(parts.length > 7 ? parts[7] : null);

                    instruments.add(instrument);
                }
            }
        }

        return instruments;
    }

    /**
     * Gets the recommended file path for saving instrument master.
     *
     * @param exchange the exchange
     * @param baseDir  the base directory
     * @return recommended file path
     */
    public static String getRecommendedPath(Exchange exchange, String baseDir) {
        String filename = exchange.getFilename().replace(".gz", "");
        return baseDir + File.separator + filename;
    }
}
