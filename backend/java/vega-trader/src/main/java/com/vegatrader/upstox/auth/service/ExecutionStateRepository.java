package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.SqliteDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Execution State Repository - SQLite persistence for resume-from-failure.
 *
 * @since 2.4.0
 */
@Repository
public class ExecutionStateRepository {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionStateRepository.class);

    private final DataSource dataSource;

    public ExecutionStateRepository() {
        this.dataSource = SqliteDataSourceFactory.create();
        ensureTableExists();
    }

    public ExecutionStateRepository(DataSource ds) {
        this.dataSource = ds;
        ensureTableExists();
    }

    /**
     * Create table if not exists.
     */
    private void ensureTableExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS token_execution_state (
                    execution_id TEXT PRIMARY KEY,
                    last_success_api TEXT,
                    next_api TEXT,
                    last_failure_epoch INTEGER,
                    status TEXT,
                    created_at INTEGER DEFAULT (strftime('%s','now')),
                    updated_at INTEGER DEFAULT (strftime('%s','now'))
                )
                """;

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("✓ token_execution_state table ensured");
        } catch (SQLException e) {
            logger.warn("Could not ensure execution state table: {}", e.getMessage());
        }
    }

    /**
     * Save or update execution state.
     */
    public void save(TokenExecutionState state) {
        String sql = """
                INSERT OR REPLACE INTO token_execution_state
                (execution_id, last_success_api, next_api, last_failure_epoch, status, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, state.getExecutionId());
            ps.setString(2, state.getLastSuccessfulApi());
            ps.setString(3, state.getNextApiToGenerate());
            ps.setLong(4, state.getLastFailureEpoch());
            ps.setString(5, state.getStatus().name());
            ps.setLong(6, System.currentTimeMillis() / 1000);
            ps.executeUpdate();

            logger.info("✓ Execution state saved: {}", state.getExecutionId());

        } catch (SQLException e) {
            logger.error("Failed to save execution state: {}", e.getMessage());
        }
    }

    /**
     * Load execution state by ID.
     */
    public TokenExecutionState load(String executionId) {
        String sql = "SELECT * FROM token_execution_state WHERE execution_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, executionId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return null;
            }

            TokenExecutionState state = new TokenExecutionState();
            state.setExecutionId(executionId);
            state.setLastSuccessfulApi(rs.getString("last_success_api"));
            state.setNextApiToGenerate(rs.getString("next_api"));
            state.setLastFailureEpoch(rs.getLong("last_failure_epoch"));
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                state.setStatus(TokenExecutionState.ExecutionStatus.valueOf(statusStr));
            }

            return state;

        } catch (SQLException e) {
            logger.error("Failed to load execution state: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get latest execution state (for resume).
     */
    public TokenExecutionState getLatest() {
        String sql = "SELECT * FROM token_execution_state ORDER BY updated_at DESC LIMIT 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return null;
            }

            TokenExecutionState state = new TokenExecutionState();
            state.setExecutionId(rs.getString("execution_id"));
            state.setLastSuccessfulApi(rs.getString("last_success_api"));
            state.setNextApiToGenerate(rs.getString("next_api"));
            state.setLastFailureEpoch(rs.getLong("last_failure_epoch"));
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                state.setStatus(TokenExecutionState.ExecutionStatus.valueOf(statusStr));
            }

            return state;

        } catch (SQLException e) {
            logger.error("Failed to get latest execution state: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete execution state.
     */
    public void delete(String executionId) {
        String sql = "DELETE FROM token_execution_state WHERE execution_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, executionId);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Failed to delete execution state: {}", e.getMessage());
        }
    }
}
