package com.vegatrader.upstox.api.settings.repository;

import com.vegatrader.upstox.api.settings.entity.UserSettingEntity;
import com.vegatrader.upstox.api.settings.entity.UserSettingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for user_settings table.
 * 
 * @since 4.3.0
 */
@Repository
public interface UserSettingRepository extends JpaRepository<UserSettingEntity, UserSettingId> {

    List<UserSettingEntity> findByUserId(String userId);

    Optional<UserSettingEntity> findByUserIdAndSettingKey(String userId, String settingKey);

    @Query("SELECT s FROM UserSettingEntity s WHERE s.settingKey LIKE :prefix%")
    List<UserSettingEntity> findByKeyPrefix(@Param("prefix") String prefix);

    @Query("SELECT s FROM UserSettingEntity s WHERE s.userId = :userId AND s.settingKey LIKE :category%")
    List<UserSettingEntity> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);

    @Modifying
    @Query("UPDATE UserSettingEntity s SET s.settingValue = :value WHERE s.userId = :userId AND s.settingKey = :key")
    int updateValue(@Param("userId") String userId, @Param("key") String key, @Param("value") String value);

    @Modifying
    @Query("DELETE FROM UserSettingEntity s WHERE s.userId = :userId AND s.settingKey = :key")
    int deleteByUserIdAndKey(@Param("userId") String userId, @Param("key") String key);
}
