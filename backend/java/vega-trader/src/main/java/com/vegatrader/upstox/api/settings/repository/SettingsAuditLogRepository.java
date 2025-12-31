package com.vegatrader.upstox.api.settings.repository;

import com.vegatrader.upstox.api.settings.entity.SettingsAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for settings_audit_log table.
 * 
 * @since 4.3.0
 */
@Repository
public interface SettingsAuditLogRepository extends JpaRepository<SettingsAuditLogEntity, Long> {

    List<SettingsAuditLogEntity> findByUserIdOrderByTimestampDesc(String userId);

    List<SettingsAuditLogEntity> findBySettingKeyOrderByTimestampDesc(String settingKey);

    List<SettingsAuditLogEntity> findByUserIdAndTimestampAfterOrderByTimestampDesc(
            String userId, LocalDateTime since);
}
