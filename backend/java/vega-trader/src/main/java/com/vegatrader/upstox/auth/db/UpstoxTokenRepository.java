package com.vegatrader.upstox.auth.db;

import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Upstox token persistence.
 *
 * @since 2.2.0
 */
public interface UpstoxTokenRepository {

    /**
     * Find all tokens.
     */
    List<UpstoxTokenEntity> findAll();

    /**
     * Find token by API name.
     */
    Optional<UpstoxTokenEntity> findByApiName(ApiName apiName);

    /**
     * Find token by API name string.
     */
    Optional<UpstoxTokenEntity> findByApiName(String apiName);

    /**
     * Upsert token (DELETE + INSERT for atomicity).
     * CRITICAL: One row per api_name, replace immediately.
     */
    void upsertToken(UpstoxTokenEntity token);

    /**
     * Deactivate token (set is_active = 0).
     */
    void deactivateToken(ApiName apiName);

    /**
     * Delete token by API name.
     */
    void deleteByApiName(ApiName apiName);

    /**
     * Count total tokens.
     */
    int count();

    /**
     * Count active tokens.
     */
    int countActive();
}
