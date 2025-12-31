package com.vegatrader.upstox.api.websocket.persistence;

import com.vegatrader.upstox.api.websocket.health.HealthFlags;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database snapshot handler for cold storage.
 * 
 * <p>
 * Responsibility:
 * <ul>
 * <li>Periodic persistence (not every tick)</li>
 * <li>Compliance / audit trail</li>
 * <li>End-of-day reconstruction</li>
 * </ul>
 * 
 * <p>
 * Uses SQLite for persistent storage with automatic table creation.
 * 
 * @since 3.1.0
 */
@Component
public class DBSnapshotHandler {

    private static final Logger logger = LoggerFactory.getLogger(DBSnapshotHandler.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBSnapshotHandler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates the market_snapshots table if it doesn't exist.
     */
    @PostConstruct
    public void initializeSchema() {
        try {
            String createTableSql = """
                    CREATE TABLE IF NOT EXISTS market_snapshots (
                        instrument_key TEXT PRIMARY KEY,
                        data BLOB NOT NULL,
                        ts BIGINT NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """;

            jdbcTemplate.execute(createTableSql);
            logger.info("market_snapshots table initialized");
            HealthFlags.setDbUp();

        } catch (Exception e) {
            logger.error("Failed to initialize market_snapshots table: {}", e.getMessage(), e);
            HealthFlags.setDbDown();
        }
    }

    /**
     * Stores a snapshot in the database.
     * 
     * <p>
     * Only called for snapshot events, not incremental updates.
     * Uses UPSERT for performance.
     * 
     * @param instrumentKey     the instrument key
     * @param payload           the serialized market data
     * @param exchangeTimestamp the exchange timestamp
     */
    public void upsertSnapshot(String instrumentKey, byte[] payload, long exchangeTimestamp) {
        try {
            String upsertSql = """
                    INSERT INTO market_snapshots (instrument_key, data, ts)
                    VALUES (?, ?, ?)
                    ON CONFLICT(instrument_key) DO UPDATE SET
                        data = excluded.data,
                        ts = excluded.ts,
                        updated_at = CURRENT_TIMESTAMP
                    """;

            jdbcTemplate.update(upsertSql, instrumentKey, payload, exchangeTimestamp);
            logger.trace("Stored snapshot for {} in DB (ts={})", instrumentKey, exchangeTimestamp);
            HealthFlags.setDbUp();

        } catch (Exception e) {
            logger.error("Failed to store snapshot in DB: {}", e.getMessage());
            HealthFlags.setDbDown();
        }
    }

    /**
     * Retrieves the latest snapshot from database.
     * 
     * @param instrumentKey the instrument key
     * @return the snapshot data, or null if not found
     */
    public byte[] getLatestSnapshot(String instrumentKey) {
        try {
            String querySql = """
                    SELECT data FROM market_snapshots
                    WHERE instrument_key = ?
                    ORDER BY ts DESC
                    LIMIT 1
                    """;

            byte[] data = jdbcTemplate.queryForObject(querySql, byte[].class, instrumentKey);
            logger.trace("Retrieved snapshot for {} from DB", instrumentKey);
            HealthFlags.setDbUp();
            return data;

        } catch (Exception e) {
            // EmptyResultDataAccessException is expected when no data exists
            if (!e.getClass().getSimpleName().equals("EmptyResultDataAccessException")) {
                logger.error("Failed to retrieve snapshot from DB: {}", e.getMessage());
                HealthFlags.setDbDown();
            } else {
                HealthFlags.setDbUp();
            }
            return null;
        }
    }
}
