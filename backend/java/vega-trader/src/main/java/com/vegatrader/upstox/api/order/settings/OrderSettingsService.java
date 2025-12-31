package com.vegatrader.upstox.api.order.settings;

import com.vegatrader.upstox.api.settings.service.AdminSettingsService;
import com.vegatrader.upstox.api.settings.service.UserSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Order Settings Service for dynamic configuration.
 * Per order-mgmt/b2.md section 3 and b4.md section 10.
 * 
 * Integrates with AdminSettingsService for configurable behavior.
 * 
 * @since 4.9.0
 */
@Service
public class OrderSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(OrderSettingsService.class);

    private final AdminSettingsService adminSettingsService;
    private final UserSettingsService userSettingsService;

    // Default values (if AdminSettings not available)
    private static final int DEFAULT_MAX_BATCH_SIZE = 25;
    private static final int DEFAULT_MAX_CANCEL_BATCH = 50;
    private static final int DEFAULT_IDEMPOTENCY_WINDOW_SEC = 300;
    private static final int DEFAULT_ORDER_BOOK_CACHE_TTL_SEC = 2;
    private static final int DEFAULT_TRADE_CACHE_TTL_SEC = 5;
    private static final int DEFAULT_RATE_LIMIT_PER_MINUTE = 120;

    public OrderSettingsService(AdminSettingsService adminSettingsService, UserSettingsService userSettingsService) {
        this.adminSettingsService = adminSettingsService;
        this.userSettingsService = userSettingsService;
    }

    // ==================== Order Placement Settings ====================

    /**
     * Get max orders per batch for multi-order.
     */
    public int getMaxOrdersPerBatch() {
        return getIntSetting("order.multi.max_batch_size", DEFAULT_MAX_BATCH_SIZE);
    }

    /**
     * Get max orders for cancel multi.
     */
    public int getMaxCancelBatch() {
        return getIntSetting("order.cancel.max_batch_size", DEFAULT_MAX_CANCEL_BATCH);
    }

    /**
     * Check if maintenance window is enabled.
     */
    public boolean isMaintenanceWindowEnabled() {
        return getBooleanSetting("order.maintenance_window.enabled", true);
    }

    /**
     * Get maintenance window start hour (IST).
     */
    public int getMaintenanceWindowStartHour() {
        return getIntSetting("order.maintenance_window.start_hour", 0);
    }

    /**
     * Get maintenance window end hour (IST).
     */
    public int getMaintenanceWindowEndHour() {
        return getIntSetting("order.maintenance_window.end_hour", 5);
    }

    // ==================== Coordinator Settings ====================

    /**
     * Get idempotency window in seconds.
     */
    public int getIdempotencyWindowSec() {
        return getIntSetting("coordinator.idempotency_window_sec", DEFAULT_IDEMPOTENCY_WINDOW_SEC);
    }

    /**
     * Check if broker failover is enabled.
     */
    public boolean isBrokerFailoverEnabled() {
        return getBooleanSetting("coordinator.broker_failover_enabled", false);
    }

    /**
     * Get audit level.
     */
    public String getAuditLevel() {
        return getStringSetting("coordinator.audit_level", "FULL");
    }

    // ==================== Cache Settings ====================

    /**
     * Get order book cache TTL in seconds.
     */
    public int getOrderBookCacheTTL() {
        return getIntSetting("cache.order_book_ttl_sec", DEFAULT_ORDER_BOOK_CACHE_TTL_SEC);
    }

    /**
     * Get trade cache TTL in seconds.
     */
    public int getTradeCacheTTL() {
        return getIntSetting("cache.trade_ttl_sec", DEFAULT_TRADE_CACHE_TTL_SEC);
    }

    /**
     * Check if cache is enabled.
     */
    public boolean isCacheEnabled() {
        return getBooleanSetting("cache.enabled", true);
    }

    // ==================== Rate Limit Settings ====================

    /**
     * Get rate limit per user per minute.
     */
    public int getRateLimitPerMinute() {
        return getIntSetting("ratelimit.per_minute", DEFAULT_RATE_LIMIT_PER_MINUTE);
    }

    /**
     * Check if rate limiting is enabled.
     */
    public boolean isRateLimitEnabled() {
        return getBooleanSetting("ratelimit.enabled", true);
    }

    // ==================== Risk Settings ====================

    /**
     * Get max order value.
     */
    public long getMaxOrderValue() {
        return getLongSetting("risk.max_order_value", 10000000L);
    }

    /**
     * Get max position per symbol.
     */
    public int getMaxPositionPerSymbol() {
        return getIntSetting("risk.max_position_per_symbol", 50000);
    }

    // ==================== User-level Settings ====================

    /**
     * Get user's default broker.
     */
    public String getUserDefaultBroker(String userId) {
        return userSettingsService.getSetting(userId, "user.default_broker");
    }

    /**
     * Check if user prefers cache.
     */
    public boolean userPrefersCache(String userId) {
        return userSettingsService.getBoolean(userId, "user.performance.prefer_cache", true);
    }

    /**
     * Get user's default time range for order history.
     */
    public int getUserDefaultTimeRangeDays(String userId) {
        return userSettingsService.getInt(userId, "user.ui.order_history_days", 1);
    }

    // ==================== Helpers ====================

    private int getIntSetting(String key, int defaultValue) {
        try {
            String value = adminSettingsService.getSettingValue(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (Exception e) {
            logger.debug("Failed to get setting {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    private long getLongSetting(String key, long defaultValue) {
        try {
            String value = adminSettingsService.getSettingValue(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getBooleanSetting(String key, boolean defaultValue) {
        try {
            String value = adminSettingsService.getSettingValue(key);
            return value != null ? Boolean.parseBoolean(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getStringSetting(String key, String defaultValue) {
        try {
            String value = adminSettingsService.getSettingValue(key);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
