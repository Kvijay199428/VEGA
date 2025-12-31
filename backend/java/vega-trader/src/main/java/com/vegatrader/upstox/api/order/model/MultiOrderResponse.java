package com.vegatrader.upstox.api.order.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Multi-order response for batch order placement.
 * Per order-mgmt/main1/a1.md and a2.md.
 * 
 * Handles success (200), partial success (207), and error (4XX).
 * 
 * @since 4.8.0
 */
public record MultiOrderResponse(
        String status, // success, partial_success, error
        List<OrderResult> data,
        List<OrderError> errors,
        Summary summary) {

    /**
     * Single order result.
     */
    public record OrderResult(
            String correlationId,
            String orderId) {
    }

    /**
     * Order error.
     */
    public record OrderError(
            String correlationId,
            String errorCode,
            String message) {
    }

    /**
     * Summary of batch operation.
     */
    public record Summary(
            int total,
            int payloadError,
            int success,
            int error) {
    }

    /**
     * Build success response.
     */
    public static MultiOrderResponse success(List<OrderResult> results) {
        return new MultiOrderResponse(
                "success",
                results,
                List.of(),
                new Summary(results.size(), 0, results.size(), 0));
    }

    /**
     * Build partial success response.
     */
    public static MultiOrderResponse partialSuccess(
            List<OrderResult> successResults,
            List<OrderError> errors) {
        return new MultiOrderResponse(
                "partial_success",
                successResults,
                errors,
                new Summary(
                        successResults.size() + errors.size(),
                        0,
                        successResults.size(),
                        errors.size()));
    }

    /**
     * Build error response.
     */
    public static MultiOrderResponse error(String errorCode, String message) {
        return new MultiOrderResponse(
                "error",
                List.of(),
                List.of(new OrderError(null, errorCode, message)),
                new Summary(0, 1, 0, 1));
    }

    /**
     * Builder for accumulating results.
     */
    public static class Builder {
        private final List<OrderResult> results = new ArrayList<>();
        private final List<OrderError> errors = new ArrayList<>();

        public Builder addSuccess(String correlationId, String orderId) {
            results.add(new OrderResult(correlationId, orderId));
            return this;
        }

        public Builder addError(String correlationId, String errorCode, String message) {
            errors.add(new OrderError(correlationId, errorCode, message));
            return this;
        }

        public MultiOrderResponse build() {
            if (errors.isEmpty()) {
                return success(results);
            } else if (results.isEmpty()) {
                return new MultiOrderResponse("error", results, errors,
                        new Summary(errors.size(), errors.size(), 0, errors.size()));
            } else {
                return partialSuccess(results, errors);
            }
        }
    }
}
