package com.vegatrader.upstox.auth.repository;

import com.vegatrader.upstox.auth.entity.TokenAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for TokenAuditEntity.
 * Append-only audit trail for token lifecycle events.
 * 
 * @since 5.0.0
 */
@Repository
public interface TokenAuditRepository extends JpaRepository<TokenAuditEntity, Long> {

    /**
     * Find all audit events for an API name, ordered by timestamp descending.
     */
    List<TokenAuditEntity> findByApiNameOrderByEventTimestampDesc(String apiName);

    /**
     * Find audit events by event type.
     */
    List<TokenAuditEntity> findByEventTypeOrderByEventTimestampDesc(String eventType);

    /**
     * Find audit events within a time range.
     */
    @Query("SELECT t FROM TokenAuditEntity t WHERE t.eventTimestamp BETWEEN :start AND :end ORDER BY t.eventTimestamp DESC")
    List<TokenAuditEntity> findByTimeRange(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Find recent audit events for an API name (limit N).
     */
    @Query(value = "SELECT * FROM token_audit WHERE api_name = :apiName ORDER BY event_ts DESC LIMIT :limit", nativeQuery = true)
    List<TokenAuditEntity> findRecentByApiName(@Param("apiName") String apiName, @Param("limit") int limit);

    /**
     * Count events by type for an API.
     */
    long countByApiNameAndEventType(String apiName, String eventType);

    /**
     * Find all failed health checks.
     */
    @Query("SELECT t FROM TokenAuditEntity t WHERE t.eventType = 'HEALTH_CHECK_FAIL' ORDER BY t.eventTimestamp DESC")
    List<TokenAuditEntity> findHealthCheckFailures();
}
