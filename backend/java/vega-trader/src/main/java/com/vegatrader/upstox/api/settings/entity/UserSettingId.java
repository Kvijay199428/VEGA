package com.vegatrader.upstox.api.settings.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for UserSettingEntity.
 */
public class UserSettingId implements Serializable {
    private String userId;
    private String settingKey;

    public UserSettingId() {
    }

    public UserSettingId(String userId, String settingKey) {
        this.userId = userId;
        this.settingKey = settingKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserSettingId that = (UserSettingId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(settingKey, that.settingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, settingKey);
    }
}
