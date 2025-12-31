package com.vegatrader.upstox.api.admin.service;

import com.vegatrader.upstox.api.admin.entity.AdminActionAuditEntity;
import com.vegatrader.upstox.api.admin.entity.DisabledStrikeEntity;
import com.vegatrader.upstox.api.admin.entity.BrokerRegistryEntity;
import com.vegatrader.upstox.api.admin.repository.AdminActionAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Admin Dashboard Service.
 * Provides data for admin dashboard queries.
 * Per a1.md Section 9.
 * 
 * @since 5.0.0
 */
@Service
public class AdminDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);

    private final StrikeManagementService strikeService;
    private final BrokerPriorityService brokerService;
    private final AdminActionAuditRepository auditRepository;

    @Autowired
    public AdminDashboardService(StrikeManagementService strikeService,
            BrokerPriorityService brokerService,
            AdminActionAuditRepository auditRepository) {
        this.strikeService = strikeService;
        this.brokerService = brokerService;
        this.auditRepository = auditRepository;
    }

    /**
     * Get dashboard summary.
     */
    public DashboardSummary getDashboardSummary() {
        return new DashboardSummary(
                strikeService.countDisabledStrikes(),
                brokerService.countActiveBrokers(),
                auditRepository.count(),
                getRecentActionsCount(24), // Last 24 hours
                getFailedActionsCount(24));
    }

    /**
     * Get recent audit actions.
     */
    public List<AdminActionAuditEntity> getRecentActions(int limit) {
        return auditRepository.findRecentActions(limit);
    }

    /**
     * Get actions by type.
     */
    public List<AdminActionAuditEntity> getActionsByType(String actionType) {
        return auditRepository.findByActionTypeOrderByPerformedAtDesc(actionType);
    }

    /**
     * Get actions within time range.
     */
    public List<AdminActionAuditEntity> getActionsByTimeRange(Instant start, Instant end) {
        return auditRepository.findByTimeRange(start, end);
    }

    /**
     * Get disabled strikes summary.
     */
    public List<DisabledStrikeEntity> getDisabledStrikes() {
        return strikeService.getAllDisabledStrikes();
    }

    /**
     * Get broker status.
     */
    public List<BrokerRegistryEntity> getBrokerStatus() {
        return brokerService.getAllActiveBrokers();
    }

    /**
     * Get activity metrics.
     */
    public ActivityMetrics getActivityMetrics(int hoursBack) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        Instant now = Instant.now();

        List<AdminActionAuditEntity> recentActions = auditRepository.findByTimeRange(since, now);

        Map<String, Long> byType = new HashMap<>();
        long successCount = 0;
        long failCount = 0;

        for (AdminActionAuditEntity action : recentActions) {
            byType.merge(action.getActionType(), 1L, Long::sum);
            if (Boolean.TRUE.equals(action.getSuccess())) {
                successCount++;
            } else {
                failCount++;
            }
        }

        return new ActivityMetrics(
                recentActions.size(),
                byType,
                successCount,
                failCount,
                hoursBack);
    }

    /**
     * Get top performers.
     */
    public List<PerformerStats> getTopPerformers(int limit) {
        List<AdminActionAuditEntity> allActions = auditRepository.findAll();
        Map<String, Long> byPerformer = new HashMap<>();

        for (AdminActionAuditEntity action : allActions) {
            byPerformer.merge(action.getPerformedBy(), 1L, Long::sum);
        }

        return byPerformer.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> new PerformerStats(e.getKey(), e.getValue()))
                .toList();
    }

    // === Private Helpers ===

    private long getRecentActionsCount(int hoursBack) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        return auditRepository.findByTimeRange(since, Instant.now()).size();
    }

    private long getFailedActionsCount(int hoursBack) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);
        return auditRepository.findByTimeRange(since, Instant.now()).stream()
                .filter(a -> Boolean.FALSE.equals(a.getSuccess()))
                .count();
    }

    // === DTOs ===

    public record DashboardSummary(
            long disabledStrikes,
            long activeBrokers,
            long totalAuditActions,
            long recentActions24h,
            long failedActions24h) {
    }

    public record ActivityMetrics(
            long totalActions,
            Map<String, Long> byActionType,
            long successCount,
            long failCount,
            int hoursBack) {
    }

    public record PerformerStats(
            String performer,
            long actionCount) {
    }
}
