package com.vegatrader.upstox.api.settings.service;

import com.vegatrader.upstox.api.settings.model.UserPrioritySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Settings Resolver per final-settings.md Annexure C.
 * Resolves settings in hierarchy: Regulatory > Exchange > System > User >
 * Session.
 * 
 * @since 4.6.0
 */
@Service
public class SettingsResolver {

    private static final Logger logger = LoggerFactory.getLogger(SettingsResolver.class);

    // In-memory cache for user settings (would use DB in production)
    private final ConcurrentHashMap<String, UserPrioritySettings> userSettingsCache = new ConcurrentHashMap<>();

    /**
     * Resolve final settings for a user.
     * Hierarchy: Regulatory > Exchange > System Defaults > User Preferences >
     * Session
     */
    public UserPrioritySettings resolveSettings(String userId, UserPrioritySettings sessionOverride) {
        logger.debug("Resolving settings for user: {}", userId);

        // 1. Start with system defaults
        UserPrioritySettings resolved = UserPrioritySettings.defaults();

        // 2. Apply user preferences (from DB/cache)
        UserPrioritySettings userPrefs = getUserSettings(userId);
        if (userPrefs != null) {
            resolved = mergeSettings(resolved, userPrefs);
        }

        // 3. Apply session overrides (if enabled)
        if (sessionOverride != null && sessionOverride.isValid()) {
            resolved = mergeSettings(resolved, sessionOverride);
        }

        // 4. Regulatory/Exchange rules are NOT overridable - enforced in RMS

        logger.debug("Resolved settings for {}: {}", userId, resolved);
        return resolved;
    }

    /**
     * Get user's stored settings.
     */
    public UserPrioritySettings getUserSettings(String userId) {
        return userSettingsCache.get(userId);
    }

    /**
     * Save user settings with validation.
     */
    public void saveUserSettings(String userId, UserPrioritySettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null");
        }

        if (!settings.isValid()) {
            throw new IllegalArgumentException("Invalid settings: validation failed");
        }

        // Merge with defaults to ensure all fields populated
        UserPrioritySettings merged = settings.mergeWithDefaults();

        userSettingsCache.put(userId, merged);
        logger.info("Saved settings for user: {}", userId);

        // TODO: Persist to database
        // TODO: Log to audit table
    }

    /**
     * Get system defaults.
     */
    public UserPrioritySettings getSystemDefaults() {
        return UserPrioritySettings.defaults();
    }

    /**
     * Merge base settings with overrides (override takes precedence).
     */
    private UserPrioritySettings mergeSettings(UserPrioritySettings base, UserPrioritySettings override) {
        return new UserPrioritySettings(
                override.instrumentLoadPriority() != null && !override.instrumentLoadPriority().isEmpty()
                        ? override.instrumentLoadPriority()
                        : base.instrumentLoadPriority(),
                override.preferredSectors() != null
                        ? override.preferredSectors()
                        : base.preferredSectors(),
                override.validationPriority() != null && !override.validationPriority().isEmpty()
                        ? override.validationPriority()
                        : base.validationPriority(),
                override.brokerRoutingPriority() != null && !override.brokerRoutingPriority().isEmpty()
                        ? override.brokerRoutingPriority()
                        : base.brokerRoutingPriority(),
                override.defaultProductType() != null
                        ? override.defaultProductType()
                        : base.defaultProductType(),
                override.defaultExchange() != null
                        ? override.defaultExchange()
                        : base.defaultExchange(),
                override.confirmBeforePlace());
    }
}
