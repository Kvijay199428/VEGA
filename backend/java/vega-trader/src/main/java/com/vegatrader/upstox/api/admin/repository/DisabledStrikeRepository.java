package com.vegatrader.upstox.api.admin.repository;

import com.vegatrader.upstox.api.admin.entity.DisabledStrikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DisabledStrikeEntity.
 * 
 * @since 5.0.0
 */
@Repository
public interface DisabledStrikeRepository extends JpaRepository<DisabledStrikeEntity, Long> {

    /**
     * Find all actively disabled strikes.
     */
    List<DisabledStrikeEntity> findByActiveTrue();

    /**
     * Find disabled strikes for an underlying.
     */
    List<DisabledStrikeEntity> findByUnderlyingKeyAndActiveTrue(String underlyingKey);

    /**
     * Find specific disabled strike.
     */
    Optional<DisabledStrikeEntity> findByUnderlyingKeyAndExpiryDateAndStrikePriceAndOptionTypeAndActiveTrue(
            String underlyingKey, LocalDate expiryDate, Double strikePrice, String optionType);

    /**
     * Check if a strike is disabled.
     */
    @Query("SELECT COUNT(d) > 0 FROM DisabledStrikeEntity d WHERE d.underlyingKey = :underlying " +
            "AND d.expiryDate = :expiry AND d.strikePrice = :strike AND d.optionType = :optionType AND d.active = true")
    boolean isStrikeDisabled(@Param("underlying") String underlyingKey,
            @Param("expiry") LocalDate expiryDate,
            @Param("strike") Double strikePrice,
            @Param("optionType") String optionType);

    /**
     * Find all disabled strikes for a date.
     */
    List<DisabledStrikeEntity> findByExpiryDateAndActiveTrue(LocalDate expiryDate);

    /**
     * Count active disabled strikes.
     */
    long countByActiveTrue();

    /**
     * Find recently disabled strikes.
     */
    @Query(value = "SELECT * FROM disabled_strikes WHERE active = true ORDER BY disabled_at DESC LIMIT :limit", nativeQuery = true)
    List<DisabledStrikeEntity> findRecentlyDisabled(@Param("limit") int limit);
}
