package com.vegatrader.upstox.api.errors;

/**
 * Comprehensive enum of Upstox API error codes.
 * <p>
 * This enum contains all documented error codes from the Upstox API,
 * organized by category with descriptions and resolution hints.
 * </p>
 * <p>
 * Each error code includes:
 * <ul>
 * <li>Error code string</li>
 * <li>Typical HTTP status</li>
 * <li>Human-readable description</li>
 * <li>Resolution hint</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxHttpStatus
 */
public enum UpstoxErrorCode {

    // Authentication Errors
    INVALID_GRANT("invalid_grant", UpstoxHttpStatus.BAD_REQUEST, "Invalid or expired authorization code",
            "Re-authenticate user to get new authorization code"),
    INVALID_CLIENT("invalid_client", UpstoxHttpStatus.UNAUTHORIZED, "Invalid API key or client ID",
            "Verify API key in app settings"),
    UNAUTHORIZED_CLIENT("unauthorized_client", UpstoxHttpStatus.FORBIDDEN, "Client not authorized for this grant type",
            "Check app permissions with Upstox"),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type", UpstoxHttpStatus.BAD_REQUEST, "Invalid grant_type parameter",
            "Use 'authorization_code' as grant_type"),
    INVALID_SCOPE("invalid_scope", UpstoxHttpStatus.BAD_REQUEST, "Requested scope not available",
            "Check available scopes in documentation"),
    INVALID_REDIRECT_URI("invalid_redirect_uri", UpstoxHttpStatus.BAD_REQUEST,
            "Redirect URI doesn't match registered URI", "Use exact redirect URI from app settings"),
    INVALID_RESPONSE_TYPE("invalid_response_type", UpstoxHttpStatus.BAD_REQUEST,
            "Invalid response_type (must be 'code')", "Ensure response_type=code"),
    UNAUTHORIZED("unauthorized", UpstoxHttpStatus.UNAUTHORIZED, "User not authenticated or token expired",
            "Provide valid access token or re-authenticate"),
    ACCESS_DENIED("access_denied", UpstoxHttpStatus.FORBIDDEN, "User denied permission",
            "User must approve app permissions"),

    // Order Placement Errors
    INSUFFICIENT_FUNDS("insufficient_funds", UpstoxHttpStatus.UNPROCESSABLE_ENTITY, "Not enough margin/funds",
            "Check account balance or reduce order quantity"),
    INVALID_ORDER_TYPE("invalid_order_type", UpstoxHttpStatus.BAD_REQUEST, "Invalid order type",
            "Use: MARKET, LIMIT, STOP_MARKET, STOP_LIMIT"),
    INVALID_QUANTITY("invalid_quantity", UpstoxHttpStatus.BAD_REQUEST, "Invalid or zero quantity",
            "Quantity must be > 0 and valid lot size"),
    INVALID_PRICE("invalid_price", UpstoxHttpStatus.BAD_REQUEST, "Invalid price",
            "Price must be > 0 and within min/max limits"),
    INVALID_INSTRUMENT("invalid_instrument", UpstoxHttpStatus.NOT_FOUND, "Instrument not found or invalid",
            "Check instrument_key format"),
    INVALID_ORDER_DURATION("invalid_order_duration", UpstoxHttpStatus.BAD_REQUEST, "Invalid duration",
            "Use: DAY, IOC, FOK, or valid GTT"),
    TRADING_SYMBOL_NOT_FOUND("trading_symbol_not_found", UpstoxHttpStatus.NOT_FOUND,
            "Trading symbol not found in order book", "Verify instrument is available for trading"),
    EXCHANGE_NOT_OPEN("exchange_not_open", UpstoxHttpStatus.SERVICE_UNAVAILABLE, "Exchange not open for trading",
            "Place order only during market hours or use AMO"),
    EXCEEDS_CIRCUIT_LIMIT("exceeds_circuit_limit", UpstoxHttpStatus.UNPROCESSABLE_ENTITY,
            "Order price exceeds circuit limit", "Adjust price within upper/lower circuit limits"),
    ORDER_VALIDATION_FAILED("order_validation_failed", UpstoxHttpStatus.BAD_REQUEST, "Multiple validation errors",
            "Check all order parameters"),
    GTC_ORDER_LIMIT_EXCEEDED("gtc_order_limit_exceeded", UpstoxHttpStatus.UNPROCESSABLE_ENTITY,
            "Too many active GTT orders", "Cancel some GTT orders first"),
    DUPLICATE_ORDER("duplicate_order", UpstoxHttpStatus.CONFLICT, "Duplicate order detected",
            "Avoid placing identical orders quickly"),
    INSUFFICIENT_QUANTITY("insufficient_quantity", UpstoxHttpStatus.UNPROCESSABLE_ENTITY,
            "Insufficient position for short sell", "Check available holdings"),
    PRICE_PRECISION_ERROR("price_precision_error", UpstoxHttpStatus.BAD_REQUEST, "Price precision incorrect",
            "Check price tick size for instrument"),
    PERMISSION_DENIED("permission_denied", UpstoxHttpStatus.FORBIDDEN, "User doesn't have permission",
            "Check trading limits/restrictions"),

    // Order Modification/Cancellation Errors
    ORDER_NOT_FOUND("order_not_found", UpstoxHttpStatus.NOT_FOUND, "Order ID doesn't exist or already executed",
            "Verify order_id is correct"),
    ORDER_ALREADY_EXECUTED("order_already_executed", UpstoxHttpStatus.CONFLICT, "Cannot modify/cancel executed order",
            "Order already completed/filled"),
    ORDER_ALREADY_CANCELLED("order_already_cancelled", UpstoxHttpStatus.CONFLICT, "Order already cancelled",
            "Order was previously cancelled"),
    INVALID_MODIFIED_QUANTITY("invalid_modified_quantity", UpstoxHttpStatus.BAD_REQUEST, "Modified quantity is invalid",
            "Quantity must be > 0"),
    INVALID_MODIFIED_PRICE("invalid_modified_price", UpstoxHttpStatus.BAD_REQUEST, "Modified price is invalid",
            "Price must be > 0"),
    INSUFFICIENT_FUNDS_FOR_MODIFICATION("insufficient_funds_for_modification", UpstoxHttpStatus.UNPROCESSABLE_ENTITY,
            "Not enough funds for modified order", "Check available margin"),
    MODIFICATION_NOT_ALLOWED("modification_not_allowed", UpstoxHttpStatus.FORBIDDEN,
            "Order type doesn't allow modification", "Market orders cannot be modified"),
    ORDER_EXPIRED("order_expired", UpstoxHttpStatus.CONFLICT, "Order expired (DAY order after market close)",
            "Place new order instead"),
    CANNOT_CANCEL_EXECUTED_ORDER("cannot_cancel_executed_order", UpstoxHttpStatus.CONFLICT,
            "Cannot cancel order with executions", "Partially filled orders cannot be cancelled"),
    CANCELLATION_NOT_ALLOWED("cancellation_not_allowed", UpstoxHttpStatus.FORBIDDEN,
            "Cancellation not allowed for this order type", "Some GTT orders may have restrictions"),
    EXCHANGE_ERROR("exchange_error", UpstoxHttpStatus.INTERNAL_SERVER_ERROR, "Exchange rejected the operation",
            "Retry or contact support"),

    // Portfolio Errors
    NO_HOLDINGS("no_holdings", UpstoxHttpStatus.OK, "User has no holdings (empty result)",
            "Start trading to build holdings"),
    POSITION_NOT_FOUND("position_not_found", UpstoxHttpStatus.NOT_FOUND, "Position doesn't exist",
            "Verify position details"),
    INVALID_CONVERSION_TYPE("invalid_conversion_type", UpstoxHttpStatus.BAD_REQUEST, "Invalid conversion type",
            "Use valid conversion type (MIS->CNC, etc.)"),
    POSITION_CANNOT_BE_CONVERTED("position_cannot_be_converted", UpstoxHttpStatus.CONFLICT,
            "Position type doesn't allow conversion", "Some positions are non-convertible"),

    // Market Data Errors
    INVALID_INSTRUMENT_KEY("invalid_instrument_key", UpstoxHttpStatus.BAD_REQUEST, "Invalid instrument_key format",
            "Use format: NSE_EQ|INE848E01016"),
    INSTRUMENT_NOT_FOUND("instrument_not_found", UpstoxHttpStatus.NOT_FOUND, "Instrument not found",
            "Verify instrument exists"),
    INVALID_REQUEST("invalid_request", UpstoxHttpStatus.BAD_REQUEST, "Missing required parameters",
            "Provide all required parameters"),
    MARKET_DATA_UNAVAILABLE("market_data_unavailable", UpstoxHttpStatus.SERVICE_UNAVAILABLE,
            "Market data service unavailable", "Retry after some time"),
    MAX_INSTRUMENTS_EXCEEDED("max_instruments_exceeded", UpstoxHttpStatus.BAD_REQUEST,
            "Exceeded max instruments limit (500)", "Request fewer instruments"),
    RATE_LIMIT_EXCEEDED("rate_limit_exceeded", UpstoxHttpStatus.TOO_MANY_REQUESTS, "Too many requests",
            "Wait before retrying, implement exponential backoff"),

    // Historical Data Errors
    INVALID_INTERVAL("invalid_interval", UpstoxHttpStatus.BAD_REQUEST, "Invalid time interval",
            "Use: 1minute, 5minute, 15minute, 30minute, 60minute, 1day, 1week, 1month"),
    INVALID_DATE_RANGE("invalid_date_range", UpstoxHttpStatus.BAD_REQUEST, "Invalid from_date or to_date",
            "from_date must be <= to_date"),
    DATE_RANGE_TOO_LARGE("date_range_too_large", UpstoxHttpStatus.UNPROCESSABLE_ENTITY,
            "Date range exceeds max allowed (7 days)", "Request smaller date range"),
    NO_DATA_FOR_PERIOD("no_data_for_period", UpstoxHttpStatus.NOT_FOUND, "No data available for requested period",
            "Check if instrument had trading activity"),
    MARKET_CLOSED_PERIOD("market_closed_period", UpstoxHttpStatus.NO_CONTENT, "No trading on requested date",
            "Check market holiday calendar"),

    // Option Chain Errors
    INVALID_EXPIRY_DATE("invalid_expiry_date", UpstoxHttpStatus.BAD_REQUEST, "Invalid or malformed expiry date",
            "Use format: YYYY-MM-DD"),
    EXPIRY_NOT_FOUND("expiry_not_found", UpstoxHttpStatus.NOT_FOUND, "Expiry date not available",
            "Use available expiry dates"),
    NO_CHAIN_DATA("no_chain_data", UpstoxHttpStatus.NO_CONTENT, "No chain data for underlying",
            "Check if options are available"),

    // WebSocket Errors
    INVALID_INSTRUMENT_WS("invalid_instrument_ws", UpstoxHttpStatus.BAD_REQUEST,
            "Invalid instrument_key in WebSocket subscribe", "Check instrument_key format"),
    SUBSCRIPTION_LIMIT_EXCEEDED("subscription_limit_exceeded", UpstoxHttpStatus.BAD_REQUEST,
            "Too many WebSocket subscriptions", "Unsubscribe from some instruments first"),
    MAX_SUBSCRIPTIONS_EXCEEDED("max_subscriptions_exceeded", UpstoxHttpStatus.BAD_REQUEST,
            "Exceeded max concurrent WebSocket subscriptions", "Limit typically 100-200 symbols"),
    INVALID_MODE("invalid_mode", UpstoxHttpStatus.BAD_REQUEST, "Invalid subscription mode", "Use: ltp, full, snapshot"),
    CONNECTION_TIMEOUT("connection_timeout", UpstoxHttpStatus.INTERNAL_SERVER_ERROR, "WebSocket connection timed out",
            "Reconnect with backoff strategy"),

    // Generic Errors
    SERVER_ERROR("server_error", UpstoxHttpStatus.INTERNAL_SERVER_ERROR, "Server-side error",
            "Retry request or contact support"),
    DATA_UNAVAILABLE("data_unavailable", UpstoxHttpStatus.SERVICE_UNAVAILABLE, "Data temporarily unavailable",
            "Retry after some time");

    private final String code;
    private final UpstoxHttpStatus httpStatus;
    private final String description;
    private final String resolution;

    UpstoxErrorCode(String code, UpstoxHttpStatus httpStatus, String description, String resolution) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
        this.resolution = resolution;
    }

    public String getCode() {
        return code;
    }

    public UpstoxHttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDescription() {
        return description;
    }

    public String getResolution() {
        return resolution;
    }

    public static UpstoxErrorCode fromCode(String code) {
        for (UpstoxErrorCode error : values()) {
            if (error.code.equals(code)) {
                return error;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s (%d): %s", code, httpStatus.getCode(), description);
    }
}
