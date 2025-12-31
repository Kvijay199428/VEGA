package com.vegatrader.upstox.auth.repository;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing upstox_tokens table.
 * Provides CRUD operations for token management.
 *
 * @since 2.0.0
 */
@Repository
public class TokenRepository {

    private static final Logger logger = LoggerFactory.getLogger(TokenRepository.class);

    private static final String DB_URL = "jdbc:sqlite:database/vega_trade.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    /**
     * Constructor initializes the database table if needed.
     */
    public TokenRepository() {
        createTableIfNotExists();
    }

    /**
     * Create table if it doesn't exist.
     */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS upstox_tokens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "access_token TEXT, " +
                "api_name TEXT, " +
                "client_id TEXT, " +
                "client_secret TEXT, " +
                "created_at TIMESTAMP, " +
                "expires_in INTEGER, " +
                "is_primary INTEGER DEFAULT 0, " +
                "refresh_token TEXT, " +
                "token_type TEXT, " +
                "api_index INTEGER DEFAULT 0, " +
                "generated_at TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "purpose TEXT, " +
                "updated_at INTEGER, " +
                "user_id INTEGER, " +
                "validity_at TEXT, " +
                "redirect_uri TEXT, " +
                "last_refreshed TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("✓ Database table 'upstox_tokens' verified/created.");
        } catch (SQLException e) {
            logger.error("Error creating table", e);
        }
    }

    /**
     * Find token by API name (with primary preference).
     */
    public Optional<UpstoxTokenEntity> findByApiName(String apiName) {
        String sql = "SELECT * FROM upstox_tokens WHERE api_name = ? AND is_active = 1 " +
                "ORDER BY is_primary DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, apiName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding token by apiName: {}", apiName, e);
        }

        return Optional.empty();
    }

    /**
     * Find all active tokens.
     */
    public List<UpstoxTokenEntity> findAllActive() {
        String sql = "SELECT * FROM upstox_tokens WHERE is_active = 1 " +
                "ORDER BY api_name, is_primary DESC";

        List<UpstoxTokenEntity> tokens = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tokens.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all active tokens", e);
        }

        return tokens;
    }

    /**
     * Find all active tokens by category prefix.
     */
    public List<UpstoxTokenEntity> findByApiNamePrefix(String prefix) {
        String sql = "SELECT * FROM upstox_tokens WHERE api_name LIKE ? AND is_active = 1 " +
                "ORDER BY api_name";

        List<UpstoxTokenEntity> tokens = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tokens.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding tokens by prefix: {}", prefix, e);
        }

        return tokens;
    }

    /**
     * Exception for database lock errors.
     */
    public static class DbLockException extends RuntimeException {
        public DbLockException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Save or update token.
     * Uses upsert behavior - deletes existing token with same api_name before
     * inserting.
     *
     * @return true if saved successfully, false if DB error (non-lock)
     * @throws DbLockException if database is locked (SQLITE_BUSY)
     */
    public boolean save(UpstoxTokenEntity token) throws DbLockException {
        // Delete existing token with same api_name (upsert behavior)
        if (!deleteByApiName(token.getApiName())) {
            return false;
        }
        return insert(token);
    }

    /**
     * Delete token by api_name.
     *
     * @return true if successful
     * @throws DbLockException if database is locked
     */
    public boolean deleteByApiName(String apiName) throws DbLockException {
        String sql = "DELETE FROM upstox_tokens WHERE api_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apiName);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                logger.debug("Deleted {} existing token(s) for apiName: {}", deleted, apiName);
            }
            return true;
        } catch (SQLException e) {
            if (isSqliteBusy(e)) {
                logger.error("╔═══════════════════════════════════════════════════════╗");
                logger.error("║  DATABASE LOCKED (SQLITE_BUSY)  ║");
                logger.error("╚═══════════════════════════════════════════════════════╝");
                throw new DbLockException("Database is locked", e);
            }
            logger.warn("Error deleting token by apiName: {}", apiName, e);
            return false;
        }
    }

    /**
     * Insert new token.
     *
     * @return true if successful
     * @throws DbLockException if database is locked
     */
    private boolean insert(UpstoxTokenEntity token) throws DbLockException {
        String sql = "INSERT INTO upstox_tokens (access_token, api_name, client_id, client_secret, " +
                "created_at, expires_in, is_primary, refresh_token, token_type, api_index, " +
                "generated_at, is_active, purpose, updated_at, user_id, validity_at, redirect_uri) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            setStatementParameters(stmt, token);
            stmt.executeUpdate();

            logger.info("✓ Inserted token for apiName: {}", token.getApiName());
            return true;
        } catch (SQLException e) {
            if (isSqliteBusy(e)) {
                logger.error("╔═══════════════════════════════════════════════════════╗");
                logger.error("║  DATABASE LOCKED (SQLITE_BUSY)  ║");
                logger.error("╚═══════════════════════════════════════════════════════╝");
                throw new DbLockException("Database is locked", e);
            }
            logger.error("Error inserting token", e);
            return false;
        }
    }

    /**
     * Check if SQLException is SQLITE_BUSY.
     */
    private boolean isSqliteBusy(SQLException e) {
        return e.getMessage() != null &&
                (e.getMessage().contains("SQLITE_BUSY") ||
                        e.getMessage().contains("database is locked"));
    }

    /**
     * Update existing token.
     */
    private void update(UpstoxTokenEntity token) {
        String sql = "UPDATE upstox_tokens SET access_token = ?, refresh_token = ?, " +
                "last_refreshed = ?, is_active = ?, updated_at = ?, validity_at = ? " +
                "WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token.getAccessToken());
            stmt.setString(2, token.getRefreshToken());
            stmt.setTimestamp(3, token.getLastRefreshed() != null ? Timestamp.valueOf(token.getLastRefreshed()) : null);
            stmt.setInt(4, token.getIsActive());
            stmt.setLong(5, System.currentTimeMillis());
            stmt.setString(6, token.getValidityAt());
            stmt.setInt(7, token.getId());

            stmt.executeUpdate();

            logger.info("Updated token id: {}, apiName: {}", token.getId(), token.getApiName());
        } catch (SQLException e) {
            logger.error("Error updating token", e);
        }
    }

    /**
     * Deactivate token by API name.
     */
    public void deactivateByApiName(String apiName) {
        String sql = "UPDATE upstox_tokens SET is_active = 0, updated_at = ? WHERE api_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, apiName);
            stmt.executeUpdate();

            logger.info("Deactivated tokens for apiName: {}", apiName);
        } catch (SQLException e) {
            logger.error("Error deactivating token", e);
        }
    }

    /**
     * Count active tokens.
     */
    public int countActive() {
        String sql = "SELECT COUNT(*) as count FROM upstox_tokens WHERE is_active = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.error("Error counting active tokens", e);
        }

        return 0;
    }

    /**
     * Map ResultSet to UpstoxTokenEntity.
     */
    private UpstoxTokenEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        UpstoxTokenEntity entity = new UpstoxTokenEntity();
        entity.setId(rs.getInt("id"));
        entity.setAccessToken(rs.getString("access_token"));
        entity.setApiName(rs.getString("api_name"));
        entity.setClientId(rs.getString("client_id"));
        entity.setClientSecret(rs.getString("client_secret"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            entity.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        entity.setExpiresIn(rs.getLong("expires_in"));
        entity.setIsPrimary(rs.getBoolean("is_primary"));

        Timestamp refreshedTimestamp = rs.getTimestamp("last_refreshed");
        if (refreshedTimestamp != null) {
            entity.setLastRefreshed(refreshedTimestamp.toLocalDateTime());
        }

        entity.setRedirectUri(rs.getString("redirect_uri"));
        entity.setRefreshToken(rs.getString("refresh_token"));
        entity.setTokenType(rs.getString("token_type"));
        entity.setApiIndex(rs.getInt("api_index"));
        entity.setGeneratedAt(rs.getString("generated_at"));
        entity.setIsActive(rs.getInt("is_active"));
        entity.setPurpose(rs.getString("purpose"));
        entity.setUpdatedAt(rs.getLong("updated_at"));
        entity.setUserId(rs.getInt("user_id"));
        entity.setValidityAt(rs.getString("validity_at"));

        return entity;
    }

    /**
     * Set prepared statement parameters for insert.
     */
    private void setStatementParameters(PreparedStatement stmt, UpstoxTokenEntity token)
            throws SQLException {
        stmt.setString(1, token.getAccessToken());
        stmt.setString(2, token.getApiName());
        stmt.setString(3, token.getClientId());
        stmt.setString(4, token.getClientSecret());
        stmt.setTimestamp(5, token.getCreatedAt() != null ? Timestamp.valueOf(token.getCreatedAt())
                : Timestamp.valueOf(LocalDateTime.now()));
        stmt.setLong(6, token.getExpiresIn() != null ? token.getExpiresIn() : 86400);
        stmt.setBoolean(7, token.getIsPrimary() != null ? token.getIsPrimary() : false);
        stmt.setString(8, token.getRefreshToken());
        stmt.setString(9, token.getTokenType());
        stmt.setInt(10, token.getApiIndex() != null ? token.getApiIndex() : 0);
        stmt.setString(11, token.getGeneratedAt());
        stmt.setInt(12, token.getIsActive() != null ? token.getIsActive() : 1);
        stmt.setString(13, token.getPurpose());
        stmt.setLong(14, System.currentTimeMillis());
        stmt.setInt(15, token.getUserId() != null ? token.getUserId() : 0);
        stmt.setString(16, token.getValidityAt());
        stmt.setString(17, token.getRedirectUri());
    }
}
