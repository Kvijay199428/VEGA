package com.vegatrader.upstox.api.utils;

/**
 * Utility class for order-related validations.
 *
 * @since 2.0.0
 */
public final class OrderValidator {

    private OrderValidator() {
        // Utility class - no instantiation
    }

    /**
     * Valid order types.
     */
    public static final String[] VALID_ORDER_TYPES = {
            "MARKET", "LIMIT", "STOP_MARKET", "STOP_LIMIT"
    };

    /**
     * Valid product types.
     */
    public static final String[] VALID_PRODUCTS = {
            "D", "MIS", "BO", "CO"
    };

    /**
     * Valid validity types.
     */
    public static final String[] VALID_VALIDITIES = {
            "DAY", "IOC", "FOK", "GTT"
    };

    /**
     * Valid transaction types.
     */
    public static final String[] VALID_TRANSACTION_TYPES = {
            "BUY", "SELL"
    };

    /**
     * Validates order type.
     *
     * @param orderType the order type
     * @return true if valid
     */
    public static boolean isValidOrderType(String orderType) {
        if (orderType == null)
            return false;
        for (String valid : VALID_ORDER_TYPES) {
            if (valid.equalsIgnoreCase(orderType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates product type.
     *
     * @param product the product type
     * @return true if valid
     */
    public static boolean isValidProduct(String product) {
        if (product == null)
            return false;
        for (String valid : VALID_PRODUCTS) {
            if (valid.equalsIgnoreCase(product)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates validity type.
     *
     * @param validity the validity type
     * @return true if valid
     */
    public static boolean isValidValidity(String validity) {
        if (validity == null)
            return false;
        for (String valid : VALID_VALIDITIES) {
            if (valid.equalsIgnoreCase(validity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates transaction type.
     *
     * @param transactionType the transaction type
     * @return true if valid
     */
    public static boolean isValidTransactionType(String transactionType) {
        if (transactionType == null)
            return false;
        for (String valid : VALID_TRANSACTION_TYPES) {
            if (valid.equalsIgnoreCase(transactionType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates quantity.
     *
     * @param quantity the quantity
     * @return true if valid (> 0)
     */
    public static boolean isValidQuantity(Integer quantity) {
        return quantity != null && quantity > 0;
    }

    /**
     * Validates price for limit orders.
     *
     * @param price     the price
     * @param orderType the order type
     * @return true if valid
     */
    public static boolean isValidPrice(Double price, String orderType) {
        if ("MARKET".equalsIgnoreCase(orderType) || "STOP_MARKET".equalsIgnoreCase(orderType)) {
            return price == null || price == 0;
        }
        return price != null && price > 0;
    }

    /**
     * Validates trigger price for stop orders.
     *
     * @param triggerPrice the trigger price
     * @param orderType    the order type
     * @return true if valid
     */
    public static boolean isValidTriggerPrice(Double triggerPrice, String orderType) {
        if ("STOP_MARKET".equalsIgnoreCase(orderType) || "STOP_LIMIT".equalsIgnoreCase(orderType)) {
            return triggerPrice != null && triggerPrice > 0;
        }
        return triggerPrice == null || triggerPrice == 0;
    }

    /**
     * Validates tag length.
     *
     * @param tag the tag
     * @return true if valid (≤ 32 characters)
     */
    public static boolean isValidTag(String tag) {
        return tag == null || tag.length() <= 32;
    }

    /**
     * Comprehensive order validation.
     *
     * @param orderType       the order type
     * @param product         the product
     * @param validity        the validity
     * @param transactionType the transaction type
     * @param quantity        the quantity
     * @param price           the price
     * @param triggerPrice    the trigger price
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOrder(
            String orderType, String product, String validity, String transactionType,
            Integer quantity, Double price, Double triggerPrice) {

        if (!isValidOrderType(orderType)) {
            throw new IllegalArgumentException("Invalid order type: " + orderType);
        }
        if (!isValidProduct(product)) {
            throw new IllegalArgumentException("Invalid product: " + product);
        }
        if (!isValidValidity(validity)) {
            throw new IllegalArgumentException("Invalid validity: " + validity);
        }
        if (!isValidTransactionType(transactionType)) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
        if (!isValidQuantity(quantity)) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
        if (!isValidPrice(price, orderType)) {
            throw new IllegalArgumentException("Invalid price for order type: " + orderType);
        }
        if (!isValidTriggerPrice(triggerPrice, orderType)) {
            throw new IllegalArgumentException("Invalid trigger price for order type: " + orderType);
        }
    }
}
