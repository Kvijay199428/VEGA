package com.vegatrader.upstox.api.order.retry;

import com.vegatrader.upstox.api.order.broker.BrokerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Order Retry Service with exponential backoff.
 * Per order-mgmt/a2.md, a3.md, a5.md, a6.md.
 * 
 * Handles transient failures with configurable retry policy.
 * 
 * @since 4.9.0
 */
@Service
public class OrderRetryService {

    private static final Logger logger = LoggerFactory.getLogger(OrderRetryService.class);

    // Retry configuration (configurable via AdminSettings)
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 1000; // 1 second
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long MAX_DELAY_MS = 30000; // 30 seconds

    // Retry tracking
    private final Map<String, RetryState> retryStates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * Execute with retry (Non-blocking).
     */
    public <T> CompletableFuture<RetryResult<T>> executeWithRetry(
            String operationId,
            Supplier<T> operation,
            Set<String> retryableErrors) {

        CompletableFuture<RetryResult<T>> future = new CompletableFuture<>();
        attempt(operationId, operation, retryableErrors, 1, future);
        return future;
    }

    private <T> void attempt(
            String operationId,
            Supplier<T> operation,
            Set<String> retryableErrors,
            int attempt,
            CompletableFuture<RetryResult<T>> future) {

        CompletableFuture.supplyAsync(operation)
                .thenAccept(result -> {
                    logger.debug("Operation {} succeeded on attempt {}", operationId, attempt);
                    future.complete(RetryResult.success(result, attempt));
                })
                .exceptionally(throwable -> {
                    Throwable e = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                    logger.warn("Operation {} failed (attempt {}): {}", operationId, attempt, e.getMessage());

                    if (attempt >= MAX_RETRIES || !isRetryable((Exception) e, retryableErrors)) {
                        future.complete(RetryResult.failure(e.getMessage(), attempt, true));
                    } else {
                        long delay = calculateDelay(attempt);
                        logger.info("Retrying operation {} in {}ms", operationId, delay);

                        scheduler.schedule(() -> attempt(operationId, operation, retryableErrors, attempt + 1, future),
                                delay, TimeUnit.MILLISECONDS);
                    }
                    return null;
                });
    }

    /**
     * Retry failed orders from multi-order batch.
     */
    public CompletableFuture<BrokerAdapter.MultiOrderResult> retryFailedOrders(
            List<BrokerAdapter.OrderRequest> failedOrders,
            BrokerAdapter broker,
            String batchId) {

        logger.info("Retrying {} failed orders from batch {}", failedOrders.size(), batchId);

        List<CompletableFuture<BrokerAdapter.OrderResult>> futures = failedOrders.stream()
                .map(order -> executeWithRetry(
                        batchId + "-" + order.correlationId(),
                        () -> broker.placeOrder(order),
                        Set.of("NETWORK_ERROR", "TIMEOUT", "RATE_LIMIT")).thenApply(result -> {
                            if (result.success()) {
                                return result.data();
                            } else {
                                return BrokerAdapter.OrderResult.error(
                                        order.correlationId(), "RETRY_FAILED", result.errorMessage());
                            }
                        }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<BrokerAdapter.OrderResult> results = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();

                    int successCount = (int) results.stream().filter(BrokerAdapter.OrderResult::success).count();
                    int errorCount = results.size() - successCount;
                    String status = errorCount == 0 ? "success" : (successCount == 0 ? "error" : "partial_success");

                    return new BrokerAdapter.MultiOrderResult(
                            status, results, results.size(), successCount, errorCount, 0);
                });
    }

    /**
     * Get retry state for operation.
     */
    public Optional<RetryState> getRetryState(String operationId) {
        return Optional.ofNullable(retryStates.get(operationId));
    }

    /**
     * Cancel pending retries.
     */
    public void cancelRetries(String operationId) {
        retryStates.remove(operationId);
        logger.info("Cancelled retries for operation: {}", operationId);
    }

    private boolean isRetryable(Exception e, Set<String> retryableErrors) {
        String message = e.getMessage();
        if (message == null)
            return false;

        return retryableErrors.stream().anyMatch(message::contains);
    }

    private long calculateDelay(int attempt) {
        double delay = BASE_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, attempt - 1);
        return Math.min((long) delay, MAX_DELAY_MS);
    }

    /**
     * Retry state tracking.
     */
    public static class RetryState {
        private int attempts = 0;
        private boolean success = false;
        private long firstAttemptTime = System.currentTimeMillis();

        public synchronized void incrementAttempts() {
            attempts++;
        }

        public synchronized int getAttempts() {
            return attempts;
        }

        public synchronized void setSuccess(boolean success) {
            this.success = success;
        }

        public synchronized boolean isSuccess() {
            return success;
        }

        public long getFirstAttemptTime() {
            return firstAttemptTime;
        }
    }

    /**
     * Retry result.
     */
    public record RetryResult<T>(
            boolean success,
            T data,
            String errorMessage,
            int attempts,
            boolean exhausted) {
        public static <T> RetryResult<T> success(T data, int attempts) {
            return new RetryResult<>(true, data, null, attempts, false);
        }

        public static <T> RetryResult<T> failure(String error, int attempts, boolean exhausted) {
            return new RetryResult<>(false, null, error, attempts, exhausted);
        }
    }
}
