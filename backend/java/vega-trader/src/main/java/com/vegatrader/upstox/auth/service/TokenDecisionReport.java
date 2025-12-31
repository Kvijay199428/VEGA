package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;

import java.util.ArrayList;
import java.util.List;

/**
 * Report from token decision engine.
 *
 * @since 2.2.0
 */
public class TokenDecisionReport {

    private final List<ApiName> valid;
    private final List<ApiName> invalid;
    private final List<ApiName> missing;

    public TokenDecisionReport(List<ApiName> valid, List<ApiName> invalid, List<ApiName> missing) {
        this.valid = valid != null ? valid : new ArrayList<>();
        this.invalid = invalid != null ? invalid : new ArrayList<>();
        this.missing = missing != null ? missing : new ArrayList<>();
    }

    public List<ApiName> getValid() {
        return valid;
    }

    public List<ApiName> getInvalid() {
        return invalid;
    }

    public List<ApiName> getMissing() {
        return missing;
    }

    public int getValidCount() {
        return valid.size();
    }

    public int getInvalidCount() {
        return invalid.size();
    }

    public int getMissingCount() {
        return missing.size();
    }

    public int getTotalConfigured() {
        return ApiName.count();
    }

    /**
     * Get all APIs that need regeneration (invalid + missing).
     */
    public List<ApiName> getNeedRegeneration() {
        List<ApiName> result = new ArrayList<>(invalid);
        result.addAll(missing);
        return result;
    }

    /**
     * Check if all tokens are valid.
     */
    public boolean allValid() {
        return invalid.isEmpty() && missing.isEmpty();
    }

    /**
     * Pretty print for CLI.
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("        TOKEN STATUS SUMMARY\n");
        sb.append("═══════════════════════════════════════════════\n\n");

        for (ApiName api : ApiName.values()) {
            String status;
            if (valid.contains(api)) {
                status = "✓ VALID";
            } else if (invalid.contains(api)) {
                status = "✗ INVALID";
            } else if (missing.contains(api)) {
                status = "○ MISSING";
            } else {
                status = "? UNKNOWN";
            }
            sb.append(String.format("  %-15s : %s%n", api.name(), status));
        }

        sb.append("\n───────────────────────────────────────────────\n");
        sb.append(String.format("  Valid Tokens  : %d%n", valid.size()));
        sb.append(String.format("  Invalid Tokens: %d%n", invalid.size()));
        sb.append(String.format("  Missing Tokens: %d%n", missing.size()));
        sb.append("═══════════════════════════════════════════════\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "TokenDecisionReport{valid=" + valid.size() +
                ", invalid=" + invalid.size() +
                ", missing=" + missing.size() + "}";
    }
}
