package com.vegatrader.upstox.api.rms.validation;

/**
 * RMS validation exception.
 * 
 * @since 4.1.0
 */
public class RmsException extends RuntimeException {

    private final String code;

    public RmsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public RmsException(String code) {
        super(code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // Common exceptions
    public static RmsException instrumentNotFound(String key) {
        return new RmsException("INSTRUMENT_NOT_FOUND", "Instrument not found: " + key);
    }

    public static RmsException productNotAllowed(String product, String reason) {
        return new RmsException("PRODUCT_NOT_ALLOWED", product + " not allowed: " + reason);
    }

    public static RmsException priceBandViolation(double price, double lower, double upper) {
        return new RmsException("PRICE_BAND_VIOLATION",
                String.format("Price %.2f outside band [%.2f - %.2f]", price, lower, upper));
    }

    public static RmsException quantityCapExceeded(int qty, int maxQty) {
        return new RmsException("QTY_CAP_EXCEEDED",
                String.format("Quantity %d exceeds cap %d", qty, maxQty));
    }

    public static RmsException valueCapExceeded(double value, double maxValue) {
        return new RmsException("VALUE_CAP_EXCEEDED",
                String.format("Value %.2f exceeds cap %.2f", value, maxValue));
    }

    public static RmsException t2tNettingBlocked() {
        return new RmsException("T2T_NO_NETTING", "Square-off not allowed for Trade-for-Trade instruments");
    }

    public static RmsException contractExpired(String key) {
        return new RmsException("CONTRACT_EXPIRED", "F&O contract expired: " + key);
    }
}
