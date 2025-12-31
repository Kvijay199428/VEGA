package com.vegatrader.upstox.api.settings.repository;

import com.vegatrader.upstox.api.settings.entity.SettingsMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for settings_metadata table.
 * 
 * @since 4.3.0
 */
@Repository
public interface SettingsMetadataRepository extends JpaRepository<SettingsMetadataEntity, String> {

    List<SettingsMetadataEntity> findByCategoryOrderByDisplayOrderAsc(String category);

    List<SettingsMetadataEntity> findByEditableTrueOrderByDisplayOrderAsc();

    List<SettingsMetadataEntity> findByMinRoleOrderByDisplayOrderAsc(String roleMin);
}
