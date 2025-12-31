package com.vegatrader.upstox.api.websocket.persistence;

import com.vegatrader.upstox.api.websocket.health.HealthFlags;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Filesystem archive handler for fallback storage.
 * 
 * <p>
 * Activated when both Redis AND DB are unavailable.
 * Ensures zero data loss by writing to rotated files.
 * 
 * <p>
 * Directory structure:
 * 
 * <pre>
 * /archive/MarketDataStreamerV3/data/
 * └── 2025-12-27/
 *     ├── market_data_2025-12-27_09.log
 *     ├── market_data_2025-12-27_10.log
 *     └── ...
 * </pre>
 * 
 * @since 3.1.0
 */
@Component
public class FileArchiveHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileArchiveHandler.class);
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH");

    private final Path baseDir;
    private final ExecutorService asyncWriter;

    /**
     * Creates a file archive handler with configurable base directory.
     */
    public FileArchiveHandler(
            @Value("${market.archive.basedir:./archive/MarketDataStreamerV3/data}") String baseDirPath) {
        this.baseDir = Paths.get(baseDirPath);
        this.asyncWriter = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("FileArchiveWriter");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Initializes the base directory.
     */
    @PostConstruct
    public void initializeDirectory() {
        try {
            Files.createDirectories(baseDir);
            logger.info("File archive initialized at: {}", baseDir.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to initialize archive directory: {}", e.getMessage(), e);
        }
    }

    /**
     * Archives a market data update asynchronously.
     * 
     * <p>
     * Only writes if both Redis and DB are down.
     * Appends to hourly-rotated file.
     * 
     * @param instrumentKey the instrument key
     * @param payload       the serialized market data
     */
    public void archive(String instrumentKey, byte[] payload) {
        // Only write if both primary stores are down
        if (HealthFlags.redisUp() || HealthFlags.dbUp()) {
            return;
        }

        // Async write to avoid blocking WebSocket thread
        asyncWriter.submit(() -> writeToFile(instrumentKey, payload));
    }

    /**
     * Writes data to the appropriate hourly file.
     */
    private void writeToFile(String instrumentKey, byte[] payload) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Path dateDir = baseDir.resolve(now.toLocalDate().toString());
            Files.createDirectories(dateDir);

            // Hourly rotation: market_data_2025-12-27_09.log
            String fileName = "market_data_" + now.format(HOUR_FORMATTER) + ".log";
            Path file = dateDir.resolve(fileName);

            // Append newline-delimited JSON
            try (BufferedWriter writer = Files.newBufferedWriter(file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                writer.write(new String(payload));
                writer.newLine();
            }

            logger.trace("Archived {} to filesystem: {}", instrumentKey, file);

        } catch (IOException e) {
            // Last resort: log and drop
            logger.error("Failed to archive to filesystem: {}", e.getMessage());
        }
    }

    /**
     * Shuts down async writer gracefully.
     */
    @PreDestroy
    public void shutdown() {
        try {
            asyncWriter.shutdown();
            if (!asyncWriter.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncWriter.shutdownNow();
            }
            logger.info("FileArchiveHandler shutdown complete");
        } catch (InterruptedException e) {
            asyncWriter.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the base directory path.
     * 
     * @return the base directory
     */
    public Path getBaseDir() {
        return baseDir;
    }
}
