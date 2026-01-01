package com.vegatrader.execution.dto;

/**
 * Normalized Order Status.
 */
public enum OrderStatus {
    OPEN,
    TRIGGER_PENDING,
    COMPLETE, // Fully filled
    CANCELLED,
    REJECTED,
    PARTIALLY_FILLED,
    VALIDATION_PENDING,
    UNKNOWN;
}
