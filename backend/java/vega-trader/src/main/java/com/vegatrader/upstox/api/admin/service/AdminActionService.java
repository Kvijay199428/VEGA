package com.vegatrader.upstox.api.admin.service;

import com.vegatrader.upstox.api.admin.entity.AdminActionAuditEntity;
import com.vegatrader.upstox.api.admin.model.*;
import com.vegatrader.upstox.api.admin.repository.AdminActionAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Admin Action Service per arch/a6.md.
 * Handles strike disablement, broker priority, and contract rollback.
 * Integrated with AdminActionAuditRepository for persistence.
 * 
 * @since 5.0.0
 */
@Service
public class AdminActionService {

    private static final Logger logger = LoggerFactory.getLogger(AdminActionService.class);

    private final AdminActionAuditRepository auditRepository;

    @Autowired
    public AdminActionService(AdminActionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Disable a strike for trading.
     */
    public void disableStrike(StrikeDisableRequest request, String adminUser, String ipAddress) {
        logger.info("Disabling strike: {} @ {} {} {} - reason: {}",
                request.underlyingKey(), request.expiry(), request.strike(),
                request.optionType(), request.reason());

        // TODO: Insert into disabled_strikes table (Phase 4)

        // Log to admin_actions_audit
        logAuditAction("STRIKE_DISABLE",
                "STRIKE",
                formatStrikeTarget(request),
                null,
                request.toString(),
                request.reason(),
                adminUser,
                "ADMIN",
                ipAddress,
                true,
                null);
    }

    /**
     * Re-enable a strike.
     */
    public void enableStrike(StrikeEnableRequest request, String adminUser) {
        logger.info("Enabling strike: {} @ {} {} {}",
                request.underlyingKey(), request.expiry(), request.strike(), request.optionType());

        // TODO: Update disabled_strikes set active = false (Phase 4)

        logAuditAction("STRIKE_ENABLE",
                "STRIKE",
                formatStrikeTarget(request.underlyingKey(), request.strike(), request.optionType()),
                null,
                request.toString(),
                request.reason(),
                adminUser,
                "ADMIN",
                null,
                true,
                null);
    }

    /**
     * Update broker priority.
     */
    public void updateBrokerPriority(BrokerPriorityRequest request, String adminUser) {
        logger.info("Updating broker priority for {} / {}: {}",
                request.instrumentType(), request.exchange(), request.priority());

        // TODO: Update broker_registry table (Phase 4)

        logAuditAction("BROKER_PRIORITY",
                "BROKER",
                request.exchange() + "/" + request.instrumentType(),
                null,
                String.join(",", request.priority()),
                null,
                adminUser,
                "ADMIN",
                null,
                true,
                null);
    }

    /**
     * Rollback to a previous contract version.
     */
    public void rollbackContract(ContractRollbackRequest request, String adminUser) {
        logger.info("Rolling back {} to version {}", request.broker(), request.contractVersion());

        // TODO: Update broker_symbol_mapping active flags (Phase 4)
        // TODO: Log to contract_version_history (Phase 4)

        logAuditAction("CONTRACT_ROLLBACK",
                "CONTRACT",
                request.broker() + " v" + request.contractVersion(),
                null,
                request.toString(),
                request.reason(),
                adminUser,
                "ADMIN",
                null,
                true,
                null);
    }

    /**
     * Get recent admin actions.
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
     * Get actions by performer.
     */
    public List<AdminActionAuditEntity> getActionsByPerformer(String performedBy) {
        return auditRepository.findByPerformedByOrderByPerformedAtDesc(performedBy);
    }

    /**
     * Get actions within a time range.
     */
    public List<AdminActionAuditEntity> getActionsByTimeRange(Instant start, Instant end) {
        return auditRepository.findByTimeRange(start, end);
    }

    /**
     * Get failed actions.
     */
    public List<AdminActionAuditEntity> getFailedActions() {
        return auditRepository.findFailedActions();
    }

    // === Private Helpers ===

    private void logAuditAction(String actionType, String targetEntity, String targetId,
            String oldValue, String newValue, String reason,
            String performedBy, String performerRole, String ipAddress,
            boolean success, String errorMessage) {
        try {
            AdminActionAuditEntity audit = new AdminActionAuditEntity();
            audit.setActionType(actionType);
            audit.setTargetEntity(targetEntity);
            audit.setTargetId(targetId);
            audit.setOldValue(oldValue);
            audit.setNewValue(newValue);
            audit.setReason(reason);
            audit.setPerformedBy(performedBy);
            audit.setPerformerRole(performerRole);
            audit.setIpAddress(ipAddress);
            audit.setPerformedAt(Instant.now());
            audit.setSuccess(success);
            audit.setErrorMessage(errorMessage);
            audit.setIntegrityHash(computeIntegrityHash(audit));

            auditRepository.save(audit);
            logger.info("Admin audit logged: {} on {} by {}", actionType, targetId, performedBy);
        } catch (Exception e) {
            logger.error("Failed to log admin audit: {}", e.getMessage());
        }
    }

    private String computeIntegrityHash(AdminActionAuditEntity audit) {
        String data = String.format("%s|%s|%s|%s|%s",
                audit.getActionType(), audit.getTargetId(), audit.getPerformedBy(),
                audit.getPerformedAt(), audit.getSuccess());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String formatStrikeTarget(StrikeDisableRequest req) {
        return formatStrikeTarget(req.underlyingKey(), req.strike(), req.optionType());
    }

    private String formatStrikeTarget(String underlying, double strike, String optionType) {
        return String.format("%s/%s/%s", underlying, strike, optionType);
    }
}
