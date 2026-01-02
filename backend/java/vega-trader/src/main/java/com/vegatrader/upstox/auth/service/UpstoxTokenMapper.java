package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.selenium.v2.LoginConfigV2;
import com.vegatrader.upstox.auth.selenium.v2.LoginResultV2;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Maps login results to token entities.
 *
 * @since 2.2.0
 */
public class UpstoxTokenMapper {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /**
     * Create entity from login result and config.
     */
    public static UpstoxTokenEntity from(LoginResultV2 result, LoginConfigV2 config) {
        LocalDateTime now = LocalDateTime.now(IST);
        LocalDateTime expiryTime = calculateExpiry(now);

        UpstoxTokenEntity entity = new UpstoxTokenEntity();
        entity.setAccessToken(result.getAccessToken());
        entity.setApiName(config.getApiName());
        entity.setClientId(config.getClientId());
        entity.setClientSecret(config.getClientSecret());
        entity.setCreatedAt(now);
        entity.setExpiresIn(result.getExpiresIn() > 0 ? result.getExpiresIn() : 86400L);
        entity.setIsPrimary(config.isPrimary());
        entity.setRedirectUri(config.getRedirectUri());
        entity.setTokenType("Bearer");
        entity.setApiIndex(getApiIndex(config.getApiName()));
        entity.setGeneratedAt(now.format(DATETIME_FORMAT));
        entity.setIsActive(1);
        entity.setPurpose(getPurpose(config.getApiName()));
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setValidityAt(expiryTime.format(DATETIME_FORMAT));

        return entity;
    }

    /**
     * Create entity from access token string (minimal).
     */
    public static UpstoxTokenEntity fromToken(String accessToken, String apiName,
            String clientId, String clientSecret,
            String redirectUri) {
        LocalDateTime now = LocalDateTime.now(IST);
        LocalDateTime expiryTime = calculateExpiry(now);

        UpstoxTokenEntity entity = new UpstoxTokenEntity();
        entity.setAccessToken(accessToken);
        entity.setApiName(apiName);
        entity.setClientId(clientId);
        entity.setClientSecret(clientSecret);
        entity.setCreatedAt(now);
        entity.setExpiresIn(86400L);
        entity.setIsPrimary(ApiName.PRIMARY.name().equals(apiName));
        entity.setRedirectUri(redirectUri);
        entity.setTokenType("Bearer");
        entity.setApiIndex(getApiIndex(apiName));
        entity.setGeneratedAt(now.format(DATETIME_FORMAT));
        entity.setIsActive(1);
        entity.setPurpose(getPurpose(apiName));
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setValidityAt(expiryTime.format(DATETIME_FORMAT));

        return entity;
    }

    /**
     * Calculate token expiry (3:30 AM IST rule).
     */
    private static LocalDateTime calculateExpiry(LocalDateTime now) {
        LocalDateTime todayExpiry = now.toLocalDate().atTime(3, 30);

        if (now.isBefore(todayExpiry)) {
            return todayExpiry;
        } else {
            return todayExpiry.plusDays(1);
        }
    }

    /**
     * Get API index from name.
     */
    private static int getApiIndex(String apiName) {
        try {
            return ApiName.fromString(apiName).getIndex();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get purpose from API name.
     */
    private static String getPurpose(String apiName) {
        if (apiName == null)
            return "GENERAL";

        if (apiName.startsWith("WEBSOCKET")) {
            return "WEBSOCKET_STREAMING";
        } else if (apiName.startsWith("OPTIONCHAIN")) {
            return "OPTION_CHAIN_DATA";
        } else if (apiName.equals("PRIMARY")) {
            return "PRIMARY_TRADING";
        }
        return "GENERAL";
    }
}
