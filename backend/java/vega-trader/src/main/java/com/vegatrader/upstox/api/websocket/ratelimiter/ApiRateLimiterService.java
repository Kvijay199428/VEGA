package com.vegatrader.upstox.api.websocket.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * API rate limiter service using Guava RateLimiter.
 * 
 * <p>
 * Provides rate limiting for Upstox API calls:
 * <ul>
 * <li>Standard API: 50 requests/second</li>
 * <li>Multi-Order API: 4 requests/second</li>
 * </ul>
 * 
 * <p>
 * All API calls in MarketDataStreamerV3 and PortfolioDataStreamerV2
 * should first call acquireStandard() or acquireMultiOrder().
 * 
 * @since 2.0.0
 */
@Service
public class ApiRateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(ApiRateLimiterService.class);

    // Upstox API rate limits
    private static final double STANDARD_API_RATE = 50.0; // 50 req/sec
    private static final double MULTI_ORDER_RATE = 4.0; // 4 req/sec

    private final RateLimiter standardApiLimiter;
    private final RateLimiter multiOrderLimiter;
    private final MeterRegistry meterRegistry;

    // Metrics
    private Counter standardApiPermitsAcquired;
    private Counter standardApiPermitsRejected;
    private Counter multiOrderPermitsAcquired;
    private Counter multiOrderPermitsRejected;

    @SuppressWarnings("UnstableApiUsage")
    public ApiRateLimiterService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.standardApiLimiter = RateLimiter.create(STANDARD_API_RATE);
        this.multiOrderLimiter = RateLimiter.create(MULTI_ORDER_RATE);
        logger.info("ApiRateLimiterService initialized - Standard: {} req/s, MultiOrder: {} req/s",
                STANDARD_API_RATE, MULTI_ORDER_RATE);
    }

    @PostConstruct
    @SuppressWarnings("UnstableApiUsage")
    public void initMetrics() {
        // Gauges for rate limiter status
        Gauge.builder("api.ratelimiter.standard.rate", standardApiLimiter, RateLimiter::getRate)
                .description("Standard API rate limit (req/sec)")
                .register(meterRegistry);

        Gauge.builder("api.ratelimiter.multiorder.rate", multiOrderLimiter, RateLimiter::getRate)
                .description("Multi-order API rate limit (req/sec)")
                .register(meterRegistry);

        // Counters for permits
        standardApiPermitsAcquired = Counter.builder("api.ratelimiter.standard.acquired")
                .description("Standard API permits acquired")
                .register(meterRegistry);

        standardApiPermitsRejected = Counter.builder("api.ratelimiter.standard.rejected")
                .description("Standard API permits rejected")
                .register(meterRegistry);

        multiOrderPermitsAcquired = Counter.builder("api.ratelimiter.multiorder.acquired")
                .description("Multi-order API permits acquired")
                .register(meterRegistry);

        multiOrderPermitsRejected = Counter.builder("api.ratelimiter.multiorder.rejected")
                .description("Multi-order API permits rejected")
                .register(meterRegistry);

        logger.debug("API rate limiter metrics initialized");
    }

    /**
     * Acquires a permit for standard API call.
     * Blocks until permit is available.
     * 
     * @return time waited in seconds
     */
    @SuppressWarnings("UnstableApiUsage")
    public double acquireStandard() {
        double waited = standardApiLimiter.acquire();
        standardApiPermitsAcquired.increment();
        if (waited > 0.01) {
            logger.debug("Standard API rate limited, waited {}s", waited);
        }
        return waited;
    }

    /**
     * Tries to acquire a permit for standard API call without waiting.
     * 
     * @return true if permit was acquired
     */
    @SuppressWarnings("UnstableApiUsage")
    public boolean tryAcquireStandard() {
        boolean acquired = standardApiLimiter.tryAcquire();
        if (acquired) {
            standardApiPermitsAcquired.increment();
        } else {
            standardApiPermitsRejected.increment();
            logger.debug("Standard API permit rejected (rate limit)");
        }
        return acquired;
    }

    /**
     * Tries to acquire a permit with timeout.
     */
    @SuppressWarnings("UnstableApiUsage")
    public boolean tryAcquireStandard(long timeout, TimeUnit unit) {
        boolean acquired = standardApiLimiter.tryAcquire(timeout, unit);
        if (acquired) {
            standardApiPermitsAcquired.increment();
        } else {
            standardApiPermitsRejected.increment();
        }
        return acquired;
    }

    /**
     * Acquires a permit for multi-order API call.
     * Blocks until permit is available.
     */
    @SuppressWarnings("UnstableApiUsage")
    public double acquireMultiOrder() {
        double waited = multiOrderLimiter.acquire();
        multiOrderPermitsAcquired.increment();
        if (waited > 0.01) {
            logger.debug("Multi-order API rate limited, waited {}s", waited);
        }
        return waited;
    }

    /**
     * Tries to acquire a permit for multi-order API call without waiting.
     */
    @SuppressWarnings("UnstableApiUsage")
    public boolean tryAcquireMultiOrder() {
        boolean acquired = multiOrderLimiter.tryAcquire();
        if (acquired) {
            multiOrderPermitsAcquired.increment();
        } else {
            multiOrderPermitsRejected.increment();
            logger.debug("Multi-order API permit rejected (rate limit)");
        }
        return acquired;
    }

    /**
     * Tries to acquire a multi-order permit with timeout.
     */
    @SuppressWarnings("UnstableApiUsage")
    public boolean tryAcquireMultiOrder(long timeout, TimeUnit unit) {
        boolean acquired = multiOrderLimiter.tryAcquire(timeout, unit);
        if (acquired) {
            multiOrderPermitsAcquired.increment();
        } else {
            multiOrderPermitsRejected.increment();
        }
        return acquired;
    }

    /**
     * Gets the current standard API rate.
     */
    @SuppressWarnings("UnstableApiUsage")
    public double getStandardApiRate() {
        return standardApiLimiter.getRate();
    }

    /**
     * Gets the current multi-order API rate.
     */
    @SuppressWarnings("UnstableApiUsage")
    public double getMultiOrderApiRate() {
        return multiOrderLimiter.getRate();
    }

    /**
     * Sets the standard API rate (for testing or dynamic adjustment).
     */
    @SuppressWarnings("UnstableApiUsage")
    public void setStandardApiRate(double permitsPerSecond) {
        standardApiLimiter.setRate(permitsPerSecond);
        logger.info("Standard API rate updated to {} req/sec", permitsPerSecond);
    }

    /**
     * Sets the multi-order API rate.
     */
    @SuppressWarnings("UnstableApiUsage")
    public void setMultiOrderApiRate(double permitsPerSecond) {
        multiOrderLimiter.setRate(permitsPerSecond);
        logger.info("Multi-order API rate updated to {} req/sec", permitsPerSecond);
    }
}
