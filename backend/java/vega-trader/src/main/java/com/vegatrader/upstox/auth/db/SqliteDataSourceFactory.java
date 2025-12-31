package com.vegatrader.upstox.auth.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SQLite DataSource factory with auto-detection.
 * Database location: <backend-root>/vega_trade.db
 *
 * @since 2.2.0
 */
public final class SqliteDataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SqliteDataSourceFactory.class);
    // Updated path: backend/java/vega-trader/database/vega_trade.db
    private static final String DB_SUBDIR = "database";
    private static final String DB_FILENAME = "vega_trade.db";

    private static DataSource instance;

    private SqliteDataSourceFactory() {
    }

    /**
     * Create or get singleton DataSource.
     * Auto-detects database location: working-dir/database/vega_trade.db
     */
    public static synchronized DataSource create() {
        if (instance != null) {
            return instance;
        }

        try {
            Path dbDir = Paths.get(System.getProperty("user.dir"), DB_SUBDIR);
            // Create directory if not exists
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
            }
            Path dbPath = dbDir.resolve(DB_FILENAME);

            SQLiteDataSource ds = new SQLiteDataSource();
            ds.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());

            logger.info("✓ SQLite DataSource initialized: {}", dbPath.toAbsolutePath());
            instance = ds;
            return ds;

        } catch (Exception e) {
            logger.error("Failed to initialize SQLite datasource", e);
            throw new IllegalStateException("Failed to initialize SQLite datasource", e);
        }
    }

    /**
     * Create DataSource with custom path.
     */
    public static DataSource create(String dbPath) {
        try {
            SQLiteDataSource ds = new SQLiteDataSource();
            ds.setUrl("jdbc:sqlite:" + dbPath);

            logger.info("✓ SQLite DataSource initialized: {}", dbPath);
            return ds;

        } catch (Exception e) {
            logger.error("Failed to initialize SQLite datasource: {}", dbPath, e);
            throw new IllegalStateException("Failed to initialize SQLite datasource", e);
        }
    }

    /**
     * Get default database path.
     */
    public static Path getDefaultDbPath() {
        return Paths.get(System.getProperty("user.dir"), DB_SUBDIR, DB_FILENAME);
    }
}
