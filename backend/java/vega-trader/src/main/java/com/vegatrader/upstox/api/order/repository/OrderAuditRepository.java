package com.vegatrader.upstox.api.order.repository;

import com.vegatrader.upstox.api.order.entity.OrderAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for OrderAuditEntity.
 * Append-only audit trail for order lifecycle events.
 * 
 * @since 5.0.0
 */
@Repository
public interface OrderAuditRepository extends JpaRepository<OrderAuditEntity, Long> {

    /**
     * Find all audit events for an order.
     */
    List<OrderAuditEntity> findByOrderIdOrderByEventTimestampDesc(String orderId);

    /**
     * Find audit events by event type.
     */
    List<OrderAuditEntity> findByEventTypeOrderByEventTimestampDesc(String eventType);

    /**
     * Find audit events within a time range.
     */
    @Query("SELECT o FROM OrderAuditEntity o WHERE o.eventTimestamp BETWEEN :start AND :end ORDER BY o.eventTimestamp DESC")
    List<OrderAuditEntity> findByTimeRange(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Find recent order audit events (limit N).
     */
    @Query(value = "SELECT * FROM order_audit ORDER BY event_ts DESC LIMIT :limit", nativeQuery = true)
    List<OrderAuditEntity> findRecentAuditEvents(@Param("limit") int limit);

    /**
     * Find by actor.
     */
    List<OrderAuditEntity> findByActorIdOrderByEventTimestampDesc(String actorId);

    /**
     * Find by broker.
     */
    List<OrderAuditEntity> findByBrokerCodeOrderByEventTimestampDesc(String brokerCode);

    /**
     * Count events by type for an order.
     */
    long countByOrderIdAndEventType(String orderId, String eventType);

    /**
     * Find rejected orders.
     */
    @Query("SELECT o FROM OrderAuditEntity o WHERE o.eventType = 'REJECT' ORDER BY o.eventTimestamp DESC")
    List<OrderAuditEntity> findRejectedOrders();
}
