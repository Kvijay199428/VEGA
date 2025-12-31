package com.vegatrader.upstox.api.admin.model;

/**
 * Request to rollback a contract version.
 * Per arch/a6.md section 2.3.
 */
public record ContractRollbackRequest(
        String broker,
        int contractVersion,
        String reason) {
}
