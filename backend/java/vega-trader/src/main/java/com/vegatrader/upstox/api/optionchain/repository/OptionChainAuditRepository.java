package com.vegatrader.upstox.api.optionchain.repository;

import com.vegatrader.upstox.api.optionchain.entity.OptionChainAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for OptionChainAuditEntity.
 * Append-only audit trail for option chain fetches.
 * 
 * @since 5.0.0
 */
@Repository
public interface OptionChainAuditRepository extends JpaRepository<OptionChainAuditEntity, Long> {

    /**
     * Find all audit events for an instrument key.
     */
    List<OptionChainAuditEntity> findByInstrumentKeyOrderByFetchTimestampDesc(String instrumentKey);

    /**
     * Find audit events by instrument and expiry.
     */
    List<OptionChainAuditEntity> findByInstrumentKeyAndExpiryDateOrderByFetchTimestampDesc(
            String instrumentKey, LocalDate expiryDate);

    /**
     * Find audit events within a time range.
     */
    @Query("SELECT o FROM OptionChainAuditEntity o WHERE o.fetchTimestamp BETWEEN :start AND :end ORDER BY o.fetchTimestamp DESC")
    List<OptionChainAuditEntity> findByTimeRange(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Find failed fetches.
     */
    @Query("SELECT o FROM OptionChainAuditEntity o WHERE o.statusCode != 200 ORDER BY o.fetchTimestamp DESC")
    List<OptionChainAuditEntity> findFailedFetches();

    /**
     * Count fetches by source for an instrument.
     */
    long countByInstrumentKeyAndFetchSource(String instrumentKey, String fetchSource);

    /**
     * Find recent fetches (limit N).
     */
    @Query(value = "SELECT * FROM option_chain_audit ORDER BY fetch_ts DESC LIMIT :limit", nativeQuery = true)
    List<OptionChainAuditEntity> findRecentFetches(@Param("limit") int limit);

    /**
     * Find by user.
     */
    List<OptionChainAuditEntity> findByUserIdOrderByFetchTimestampDesc(String userId);
}
