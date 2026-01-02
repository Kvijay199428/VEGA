package com.vegatrader.market.journal;

import com.vegatrader.market.depth.model.L30OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Canonical Snapshot Writers per instrument.
 * Handles directory creation and file rotation (hourly).
 * 
 * Directory Structure:
 * database/marketdata_raw/{SEGMENT}/{INSTRUMENT}/{YYYY-MM-DD}_{HH}.bin
 */
@Component
public class JournalManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(JournalManager.class);
    private static final String BASE_DIR = "database/marketdata_raw";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH");

    // Map: InstrumentKey -> Writer
    private final Map<String, CanonicalSnapshotWriter> writers = new ConcurrentHashMap<>();

    // Cache current hour to detect rotation need (simplification for now)
    // Real implementation checks per write or uses a scheduled task to rotate.
    // For low latency, we check sparingly or assume single writer thread.

    public void writePromise(L30OrderBook book) {
        if (book == null)
            return;

        try {
            String key = book.getInstrumentKey();
            CanonicalSnapshotWriter writer = writers.computeIfAbsent(key, this::createWriter);

            if (writer != null) {
                // Parse instrument ID from key or book (assuming key is SEGMENT|TOKEN)
                int instrumentId = parseInstrumentId(key);
                long now = System.currentTimeMillis();
                long exchangeTs = book.getExchangeTs();

                writer.write(book, exchangeTs, now, instrumentId);
            }
        } catch (Exception e) {
            logger.error("Failed to journal snapshot for {}", book.getInstrumentKey(), e);
        }
    }

    private CanonicalSnapshotWriter createWriter(String instrumentKey) {
        try {
            // Key format: SEGMENT|TOKEN e.g. NSE_FO|12345
            String[] parts = instrumentKey.split("\\|");
            String segment = parts.length > 0 ? parts[0] : "UNKNOWN";
            String token = parts.length > 1 ? parts[1] : "UNKNOWN";

            LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
            String dateStr = now.format(DATE_FORMAT);
            String hourStr = now.format(HOUR_FORMAT);

            Path dir = Paths.get(BASE_DIR, segment, token);
            Files.createDirectories(dir);

            String filename = String.format("%s_%s.bin", dateStr, hourStr);
            Path file = dir.resolve(filename);

            logger.info("Opening journal: {}", file);

            FileChannel channel = FileChannel.open(file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);

            return new CanonicalSnapshotWriter(channel);

        } catch (IOException e) {
            logger.error("Failed to create writer for {}", instrumentKey, e);
            return null;
        }
    }

    // Naive parsing - in real app use Instrument Cache
    private int parseInstrumentId(String key) {
        try {
            String[] parts = key.split("\\|");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException ignored) {
        }
        return 0; // hash or 0
    }

    @Override
    public void close() {
        writers.values().forEach(w -> {
            try {
                w.close();
            } catch (IOException e) {
                logger.warn("Error closing writer", e);
            }
        });
        writers.clear();
    }
}
