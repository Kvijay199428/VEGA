package com.vegatrader.upstox.auth.db;

import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository implementation with REPLACE semantics.
 * CRITICAL: Token is persisted immediately after generation.
 *
 * @since 2.2.0
 */
public class UpstoxTokenRepositoryImpl implements UpstoxTokenRepository {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxTokenRepositoryImpl.class);

    private final DataSource dataSource;

    public UpstoxTokenRepositoryImpl() {
        this.dataSource = SqliteDataSourceFactory.create();
        ensureTableExists();
    }

    public UpstoxTokenRepositoryImpl(DataSource ds) {
        this.dataSource = ds;
        ensureTableExists();
    }

    /**
     * Auto-create upstox_tokens table if it doesn't exist.
     */
    private void ensureTableExists() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS upstox_tokens (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    access_token VARCHAR(2000),
                    api_name VARCHAR(255) NOT NULL UNIQUE,
                    client_id VARCHAR(255),
                    client_secret VARCHAR(255),
                    created_at INTEGER,
                    expires_in INTEGER,
                    is_primary INTEGER,
                    last_refreshed INTEGER,
                    redirect_uri VARCHAR(255),
                    refresh_token VARCHAR(2000),
                    token_type VARCHAR(255),
                    api_index INTEGER,
                    generated_at VARCHAR(255),
                    is_active INTEGER DEFAULT 1,
                    purpose VARCHAR(255),
                    updated_at INTEGER,
                    user_id INTEGER,
                    validity_at VARCHAR(255)
                )
                """;

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            logger.info("✓ upstox_tokens table ensured");
        } catch (SQLException e) {
            logger.warn("Could not ensure table exists: {}", e.getMessage());
        }
    }

    @Override
    public List<UpstoxTokenEntity> findAll() {
        List<UpstoxTokenEntity> tokens = new ArrayList<>();
        String sql = "SELECT * FROM upstox_tokens ORDER BY api_index";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tokens.add(mapRow(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch all tokens", e);
        }

        return tokens;
    }

    @Override
    public Optional<UpstoxTokenEntity> findByApiName(ApiName apiName) {
        return findByApiName(apiName.name());
    }

    @Override
    public Optional<UpstoxTokenEntity> findByApiName(String apiName) {
        String sql = "SELECT * FROM upstox_tokens WHERE api_name = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apiName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch token for api_name: {}", apiName, e);
        }

        return Optional.empty();
    }

    @Override
    public void upsertToken(UpstoxTokenEntity t) {
        logger.info("Upserting token for api_name: {}", t.getApiName());

        // CRITICAL: DELETE + INSERT for atomicity (REPLACE semantics)
        String deleteSql = "DELETE FROM upstox_tokens WHERE api_name = ?";
        String insertSql = """
                INSERT INTO upstox_tokens (
                    access_token, api_name, client_id, client_secret,
                    created_at, expires_in, is_primary, redirect_uri,
                    token_type, api_index, generated_at, is_active,
                    purpose, updated_at, validity_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Delete existing
                try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                    delPs.setString(1, t.getApiName());
                    delPs.executeUpdate();
                }

                // Insert new
                try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                    insPs.setString(1, t.getAccessToken());
                    insPs.setString(2, t.getApiName());
                    insPs.setString(3, t.getClientId());
                    insPs.setString(4, t.getClientSecret());
                    insPs.setLong(5, t.getCreatedAt() != null ? t.getCreatedAt() : System.currentTimeMillis());
                    insPs.setLong(6, t.getExpiresIn() != null ? t.getExpiresIn() : 0);
                    insPs.setInt(7, Boolean.TRUE.equals(t.getIsPrimary()) ? 1 : 0);
                    insPs.setString(8, t.getRedirectUri());
                    insPs.setString(9, t.getTokenType() != null ? t.getTokenType() : "Bearer");
                    insPs.setInt(10, t.getApiIndex() != null ? t.getApiIndex() : 0);
                    insPs.setString(11, t.getGeneratedAt());
                    insPs.setString(12, t.getPurpose());
                    insPs.setLong(13, System.currentTimeMillis());
                    insPs.setString(14, t.getValidityAt());

                    insPs.executeUpdate();
                }

                conn.commit();
                logger.info("✓ Token upserted for: {}", t.getApiName());

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            logger.error("Failed to upsert token for: {}", t.getApiName(), e);
            throw new RuntimeException("Token upsert failed", e);
        }
    }

    @Override
    public void deactivateToken(ApiName apiName) {
        String sql = "UPDATE upstox_tokens SET is_active = 0, updated_at = ? WHERE api_name = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, apiName.name());
            ps.executeUpdate();

            logger.info("✓ Token deactivated: {}", apiName);

        } catch (SQLException e) {
            logger.error("Failed to deactivate token: {}", apiName, e);
        }
    }

    @Override
    public void deleteByApiName(ApiName apiName) {
        String sql = "DELETE FROM upstox_tokens WHERE api_name = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apiName.name());
            ps.executeUpdate();

            logger.info("✓ Token deleted: {}", apiName);

        } catch (SQLException e) {
            logger.error("Failed to delete token: {}", apiName, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM upstox_tokens";
        return countQuery(sql);
    }

    @Override
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM upstox_tokens WHERE is_active = 1";
        return countQuery(sql);
    }

    private int countQuery(String sql) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error("Count query failed", e);
        }
        return 0;
    }

    private UpstoxTokenEntity mapRow(ResultSet rs) throws SQLException {
        UpstoxTokenEntity e = new UpstoxTokenEntity();
        e.setId(rs.getInt("id"));
        e.setAccessToken(rs.getString("access_token"));
        e.setApiName(rs.getString("api_name"));
        e.setClientId(rs.getString("client_id"));
        e.setClientSecret(rs.getString("client_secret"));
        e.setCreatedAt(rs.getLong("created_at"));
        e.setExpiresIn(rs.getLong("expires_in"));
        e.setIsPrimary(rs.getInt("is_primary") == 1);
        e.setLastRefreshed(rs.getLong("last_refreshed"));
        e.setRedirectUri(rs.getString("redirect_uri"));
        e.setRefreshToken(rs.getString("refresh_token"));
        e.setTokenType(rs.getString("token_type"));
        e.setApiIndex(rs.getInt("api_index"));
        e.setGeneratedAt(rs.getString("generated_at"));
        e.setIsActive(rs.getInt("is_active"));
        e.setPurpose(rs.getString("purpose"));
        e.setUpdatedAt(rs.getLong("updated_at"));
        e.setUserId(rs.getInt("user_id"));
        e.setValidityAt(rs.getString("validity_at"));
        return e;
    }
}
