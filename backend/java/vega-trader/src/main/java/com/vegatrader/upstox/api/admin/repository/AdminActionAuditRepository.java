package com.vegatrader.upstox.api.admin.repository;

import com.vegatrader.upstox.api.admin.entity.AdminActionAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for AdminActionAuditEntity.
 * Append-only audit trail for admin actions.
 * 
 * @since 5.0.0
 */
@Repository
public interface AdminActionAuditRepository extends JpaRepository<AdminActionAuditEntity, Long> {

    /**
     * Find all audit events by action type.
     */
    List<AdminActionAuditEntity> findByActionTypeOrderByPerformedAtDesc(String actionType);

    /**
     * Find audit events by performer.
     */
    List<AdminActionAuditEntity> findByPerformedByOrderByPerformedAtDesc(String performedBy);

    /**
     * Find audit events within a time range.
     */
    @Query("SELECT a FROM AdminActionAuditEntity a WHERE a.performedAt BETWEEN :start AND :end ORDER BY a.performedAt DESC")
    List<AdminActionAuditEntity> findByTimeRange(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Find recent admin actions (limit N).
     */
    @Query(value = "SELECT * FROM admin_actions_audit ORDER BY performed_at DESC LIMIT :limit", nativeQuery = true)
    List<AdminActionAuditEntity> findRecentActions(@Param("limit") int limit);

    /**
     * Find failed actions.
     */
    @Query("SELECT a FROM AdminActionAuditEntity a WHERE a.success = false ORDER BY a.performedAt DESC")
    List<AdminActionAuditEntity> findFailedActions();

    /**
     * Find by target entity.
     */
    List<AdminActionAuditEntity> findByTargetEntityOrderByPerformedAtDesc(String targetEntity);

    /**
     * Count actions by type.
     */
    long countByActionType(String actionType);

    /**
     * Count actions by performer.
     */
    long countByPerformedBy(String performedBy);
}
