package com.vegatrader.upstox.api.rms.enums;

/**
 * RMS Rejection Codes - Standardized per a1.md section 4.3.
 * 
 * @since 4.5.0
 */
public enum RmsRejectCode {

    // Expiry Related
    RMS_EXPIRY_INVALID("EXPIRY", "Contract has expired or invalid expiry date"),
    RMS_EXPIRY_NOT_FOUND("EXPIRY", "Expiry date not found for instrument"),
    RMS_EXPIRY_TOO_FAR("EXPIRY", "Expiry exceeds allowed range"),

    // Strike Related
    RMS_STRIKE_DISABLED("STRIKE", "Strike is disabled due to OI rules"),
    RMS_STRIKE_INVALID("STRIKE", "Invalid strike price for instrument"),
    RMS_STRIKE_OUT_OF_RANGE("STRIKE", "Strike outside allowed range"),

    // Price Related
    RMS_PRICE_BAND("PRICE", "Price outside circuit limits"),
    RMS_PRICE_INVALID("PRICE", "Invalid price format or value"),
    RMS_PRICE_TICK_SIZE("PRICE", "Price not in tick size"),

    // Quantity Related
    RMS_QTY_CAP("QUANTITY", "Quantity exceeds symbol cap"),
    RMS_QTY_LOT_SIZE("QUANTITY", "Quantity not in lot size"),
    RMS_QTY_FREEZE("QUANTITY", "Quantity exceeds freeze limit"),

    // BSE Specific
    RMS_BSE_T2T("BSE", "Trade-for-trade violation"),
    RMS_BSE_CNC_ONLY("BSE", "CNC/Delivery only allowed"),
    RMS_BSE_GROUP_BLOCKED("BSE", "BSE group blocked for trading"),

    // Client Risk
    RMS_CLIENT_LIMIT("CLIENT", "Client limit breached"),
    RMS_CLIENT_MARGIN("CLIENT", "Insufficient margin"),
    RMS_CLIENT_BLOCKED("CLIENT", "Client blocked for trading"),

    // Sector Related
    RMS_SECTOR_BLOCKED("SECTOR", "Sector blocked for trading"),
    RMS_SECTOR_EXPOSURE("SECTOR", "Sector exposure limit exceeded"),

    // General
    RMS_INSTRUMENT_INACTIVE("GENERAL", "Instrument not active"),
    RMS_SEGMENT_BLOCKED("GENERAL", "Segment blocked for trading"),
    RMS_SYSTEM_ERROR("GENERAL", "Internal RMS error");

    private final String category;
    private final String description;

    RmsRejectCode(String category, String description) {
        this.category = category;
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRetryAllowed() {
        return this == RMS_PRICE_BAND || this == RMS_PRICE_INVALID
                || this == RMS_PRICE_TICK_SIZE || this == RMS_QTY_CAP
                || this == RMS_SYSTEM_ERROR;
    }

    public boolean isBseSpecific() {
        return category.equals("BSE");
    }

    public boolean isClientRelated() {
        return category.equals("CLIENT");
    }
}
