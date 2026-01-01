package com.vegatrader.risk;

import com.vegatrader.execution.dto.OrderRequest;

/**
 * Exception thrown when a risk check fails.
 */
public class RiskException extends RuntimeException {
    public RiskException(String message) {
        super(message);
    }
}
