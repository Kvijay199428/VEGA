package com.vegatrader.service;

// Unused import removed (UpstoxTokenEntity)
import com.vegatrader.upstox.auth.service.TokenStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token lease manager to prevent multiple connections using the same token.
 * 
 * <p>
 * Uses SHA-256 hash of the access token as the lease key to support
 * token rotation and rolling upgrades.
 * 
 * @since 3.1.0
 */
public class TokenLeaseManager {

    private static final Logger logger = LoggerFactory.getLogger(TokenLeaseManager.class);

    private static final long DEFAULT_LEASE_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    private final long leaseTimeoutMs;
    private final Map<String, TokenLease> activeLeases;

    public TokenLeaseManager(TokenStorageService tokenStorage) {
        this(tokenStorage, DEFAULT_LEASE_TIMEOUT_MS);
    }

    public TokenLeaseManager(TokenStorageService tokenStorage, long leaseTimeoutMs) {
        this.leaseTimeoutMs = leaseTimeoutMs;
        this.activeLeases = new ConcurrentHashMap<>();
        logger.info("TokenLeaseManager initialized with timeout: {}ms", leaseTimeoutMs);
    }

    /**
     * Acquires a token lease for the given access token.
     * 
     * @param apiName     the logical API name (for logging)
     * @param accessToken the actual token string
     * @return token lease if successful, null if already leased
     */
    public synchronized TokenLease acquireLease(String apiName, String accessToken) {
        if (accessToken == null || accessToken.isEmpty())
            return null;

        String tokenHash = hashToken(accessToken);
        cleanupExpiredLeases();

        if (activeLeases.containsKey(tokenHash)) {
            TokenLease existingLease = activeLeases.get(tokenHash);
            if (!existingLease.isExpired()) {
                logger.warn("Token for {} is already leased", apiName);
                return null;
            }
            activeLeases.remove(tokenHash);
        }

        long expiryTime = System.currentTimeMillis() + leaseTimeoutMs;
        TokenLease lease = new TokenLease(apiName, tokenHash, accessToken, expiryTime);
        activeLeases.put(tokenHash, lease);

        logger.info("✓ Acquired lease for {} (hash: {})", apiName, tokenHash.substring(0, 8));
        return lease;
    }

    public synchronized void releaseLease(TokenLease lease) {
        if (lease == null)
            return;
        activeLeases.remove(lease.getTokenHash());
        logger.info("✓ Released lease for {}", lease.getApiName());
    }

    private TokenLease findExpiredLease(long now) {
        for (TokenLease lease : activeLeases.values()) {
            if (lease.isExpired(now)) {
                return lease;
            }
        }
        return null;
    }

    public synchronized void cleanupExpiredLeases() {
        long now = System.currentTimeMillis();
        int removed = 0;

        TokenLease expired;
        while ((expired = findExpiredLease(now)) != null) {
            activeLeases.remove(expired.getTokenHash());
            logger.warn("Expired lease cleaned up for token hash={}", expired.getTokenHash().substring(0, 8));
            removed++;
        }

        if (removed > 0) {
            logger.info("Total cleaned up {} expired leases", removed);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static class TokenLease {
        private final String apiName;
        private final String tokenHash;
        private final String token;
        private final long expiryTime;

        public TokenLease(String apiName, String tokenHash, String token, long expiryTime) {
            this.apiName = apiName;
            this.tokenHash = tokenHash;
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public String getApiName() {
            return apiName;
        }

        public String getTokenHash() {
            return tokenHash;
        }

        public String getToken() {
            return token;
        }

        public boolean isExpired(long now) {
            return now > expiryTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
