package com.vegatrader.upstox.api.websocket.settings;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized limits configuration for MarketDataStreamerV3.
 * 
 * <p>
 * Defines connection and subscription limits per user type:
 * 
 * <h3>Normal Users</h3>
 * <ul>
 * <li>Connections: 2 max</li>
 * <li>LTPC: 5000 individual / 2000 combined</li>
 * <li>Option Greeks: 3000 individual / 2000 combined</li>
 * <li>Full: 2000 individual / 1500 combined</li>
 * </ul>
 * 
 * <h3>Upstox Plus Users</h3>
 * <ul>
 * <li>Connections: 5 max</li>
 * <li>Full D30: 50 individual / 1500 combined</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public final class LimitsConfig {

    // Connection limits
    public static final int NORMAL_CONNECTIONS = 2;
    public static final int PLUS_CONNECTIONS = 5;

    // Subscription limits by user type
    public static final Map<SubscriptionCategory, SubscriptionLimits> NORMAL_LIMITS = new EnumMap<>(
            SubscriptionCategory.class);
    public static final Map<SubscriptionCategory, SubscriptionLimits> PLUS_LIMITS = new EnumMap<>(
            SubscriptionCategory.class);

    static {
        // Normal Users limits
        NORMAL_LIMITS.put(SubscriptionCategory.LTPC, new SubscriptionLimits(5000, 2000));
        NORMAL_LIMITS.put(SubscriptionCategory.OPTION_GREEKS, new SubscriptionLimits(3000, 2000));
        NORMAL_LIMITS.put(SubscriptionCategory.FULL, new SubscriptionLimits(2000, 1500));

        // Upstox Plus limits
        PLUS_LIMITS.put(SubscriptionCategory.FULL_D30, new SubscriptionLimits(50, 1500));
        // Plus users can also use normal categories with enhanced limits
        PLUS_LIMITS.put(SubscriptionCategory.LTPC, new SubscriptionLimits(5000, 2000));
        PLUS_LIMITS.put(SubscriptionCategory.OPTION_GREEKS, new SubscriptionLimits(3000, 2000));
        PLUS_LIMITS.put(SubscriptionCategory.FULL, new SubscriptionLimits(2000, 1500));
    }

    private LimitsConfig() {
        // Utility class
    }

    /**
     * Gets subscription limits for the specified user type.
     */
    public static Map<SubscriptionCategory, SubscriptionLimits> getLimits(UserType userType) {
        return userType == UserType.PLUS ? PLUS_LIMITS : NORMAL_LIMITS;
    }

    /**
     * Gets connection limit for the specified user type.
     */
    public static int getConnectionLimit(UserType userType) {
        return userType == UserType.PLUS ? PLUS_CONNECTIONS : NORMAL_CONNECTIONS;
    }

    /**
     * Gets subscription limit for a specific category and user type.
     */
    public static SubscriptionLimits getSubscriptionLimit(UserType userType, SubscriptionCategory category) {
        Map<SubscriptionCategory, SubscriptionLimits> limits = getLimits(userType);
        SubscriptionLimits limit = limits.get(category);
        if (limit == null) {
            throw new IllegalArgumentException(
                    String.format("Category %s not supported for user type %s", category, userType));
        }
        return limit;
    }

    /**
     * Checks if a category is available for the specified user type.
     */
    public static boolean isCategoryAvailable(UserType userType, SubscriptionCategory category) {
        return getLimits(userType).containsKey(category);
    }
}
