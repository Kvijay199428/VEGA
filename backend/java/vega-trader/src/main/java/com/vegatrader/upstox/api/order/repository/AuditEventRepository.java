package com.vegatrader.upstox.api.order.repository;

import com.vegatrader.upstox.api.order.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * JPA Repository for Audit Events.
 * Per order-mgmt/b2.md section 6.
 * 
 * @since 4.9.0
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    /**
     * Find events for an order (order history).
     */
    List<AuditEventEntity> findByOrderIdOrderByCreatedAtAsc(String orderId);

    /**
     * Find events for a user.
     */
    List<AuditEventEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find events by type.
     */
    List<AuditEventEntity> findByEventType(String eventType);

    /**
     * Find events in date range.
     */
    @Query("SELECT e FROM AuditEventEntity e WHERE e.orderId = :orderId " +
            "AND e.createdAt >= :start AND e.createdAt <= :end ORDER BY e.createdAt ASC")
    List<AuditEventEntity> findByOrderIdAndDateRange(
            @Param("orderId") String orderId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Find events for user with pagination (for audit export).
     */
    @Query("SELECT e FROM AuditEventEntity e WHERE e.userId = :userId " +
            "AND e.createdAt >= :start AND e.createdAt <= :end ORDER BY e.createdAt DESC")
    Page<AuditEventEntity> findForExport(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    /**
     * Count events for order.
     */
    long countByOrderId(String orderId);

    /**
     * Delete old events (retention policy).
     */
    @Query("DELETE FROM AuditEventEntity e WHERE e.createdAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") Instant cutoff);

    /**
     * Find events by request ID (for tracing).
     */
    List<AuditEventEntity> findByRequestId(String requestId);

    /**
     * Get state change history for order.
     */
    @Query("SELECT e FROM AuditEventEntity e WHERE e.orderId = :orderId " +
            "AND e.eventType = 'STATE_CHANGE' ORDER BY e.createdAt ASC")
    List<AuditEventEntity> getStateHistory(@Param("orderId") String orderId);
}
