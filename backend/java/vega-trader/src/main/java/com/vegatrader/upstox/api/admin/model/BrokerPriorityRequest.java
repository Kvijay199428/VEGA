package com.vegatrader.upstox.api.admin.model;

import java.util.List;

/**
 * Request to update broker priority.
 * Per arch/a6.md section 2.2.
 */
public record BrokerPriorityRequest(
        String instrumentType,
        String exchange,
        List<String> priority) {
}
