package com.vegatrader.upstox.api.rms.entity;

/**
 * Equity Security Type classification.
 * Determines product eligibility based on security classification.
 * 
 * @since 4.1.0
 */
public enum EquitySecurityType {

    /**
     * Standard equity - all products allowed.
     */
    NORMAL("Standard equity", true, true, true),

    /**
     * Small and Medium Enterprise - CNC only.
     */
    SME("Small and Medium Enterprise equity", false, false, true),

    /**
     * Initial Public Offering - CNC only.
     */
    IPO("Initial Public Offering", false, false, true),

    /**
     * Prompt Corrective Action - CNC only.
     */
    PCA("Under regulatory watch", false, false, true),

    /**
     * Relisted equity - CNC only.
     */
    RELIST("Relisted equity", false, false, true);

    private final String description;
    private final boolean misAllowed;
    private final boolean mtfAllowed;
    private final boolean cncAllowed;

    EquitySecurityType(String description, boolean misAllowed, boolean mtfAllowed, boolean cncAllowed) {
        this.description = description;
        this.misAllowed = misAllowed;
        this.mtfAllowed = mtfAllowed;
        this.cncAllowed = cncAllowed;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMisAllowed() {
        return misAllowed;
    }

    public boolean isMtfAllowed() {
        return mtfAllowed;
    }

    public boolean isCncAllowed() {
        return cncAllowed;
    }

    /**
     * Safe parse from string.
     */
    public static EquitySecurityType fromCode(String code) {
        if (code == null)
            return NORMAL;
        try {
            return valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}
