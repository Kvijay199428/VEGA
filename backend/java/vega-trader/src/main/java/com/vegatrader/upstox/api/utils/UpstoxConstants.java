package com.vegatrader.upstox.api.utils;

/**
 * Constants for Upstox API.
 *
 * @since 2.0.0
 */
public final class UpstoxConstants {

    private UpstoxConstants() {
        // Utility class - no instantiation
    }

    // API URLs
    public static final String PRODUCTION_BASE_URL = "https://api.upstox.com/v2";
    public static final String HFT_BASE_URL = "https://api-hft.upstox.com/v3";
    public static final String SANDBOX_BASE_URL = "https://api-sandbox.upstox.com/v3";
    public static final String WEBSOCKET_URL = "wss://api.upstox.com/v2/feed/market-data-feed";

    // Order Types
    public static final String ORDER_TYPE_MARKET = "MARKET";
    public static final String ORDER_TYPE_LIMIT = "LIMIT";
    public static final String ORDER_TYPE_STOP_MARKET = "STOP_MARKET";
    public static final String ORDER_TYPE_STOP_LIMIT = "STOP_LIMIT";

    // Product Types
    public static final String PRODUCT_DELIVERY = "D";
    public static final String PRODUCT_INTRADAY = "MIS";
    public static final String PRODUCT_BRACKET_ORDER = "BO";
    public static final String PRODUCT_COVER_ORDER = "CO";

    // Validity Types
    public static final String VALIDITY_DAY = "DAY";
    public static final String VALIDITY_IOC = "IOC";
    public static final String VALIDITY_FOK = "FOK";
    public static final String VALIDITY_GTT = "GTT";

    // Transaction Types
    public static final String TRANSACTION_BUY = "BUY";
    public static final String TRANSACTION_SELL = "SELL";

    // Order States
    public static final String ORDER_STATE_PENDING = "PENDING";
    public static final String ORDER_STATE_OPEN = "OPEN";
    public static final String ORDER_STATE_COMPLETE = "COMPLETE";
    public static final String ORDER_STATE_EXECUTED = "EXECUTED";
    public static final String ORDER_STATE_CANCELLED = "CANCELLED";
    public static final String ORDER_STATE_REJECTED = "REJECTED";

    // Exchanges
    public static final String EXCHANGE_NSE = "NSE";
    public static final String EXCHANGE_BSE = "BSE";
    public static final String EXCHANGE_MCX = "MCX";
    public static final String EXCHANGE_NFO = "NFO";
    public static final String EXCHANGE_BFO = "BFO";
    public static final String EXCHANGE_CDS = "CDS";

    // Segments
    public static final String SEGMENT_NSE_EQ = "NSE_EQ";
    public static final String SEGMENT_NSE_FO = "NSE_FO";
    public static final String SEGMENT_NSE_INDEX = "NSE_INDEX";
    public static final String SEGMENT_BSE_EQ = "BSE_EQ";
    public static final String SEGMENT_MCX_FO = "MCX_FO";

    // Rate Limits (Standard API)
    public static final int STANDARD_RATE_LIMIT_PER_SECOND = 50;
    public static final int STANDARD_RATE_LIMIT_PER_MINUTE = 500;
    public static final int STANDARD_RATE_LIMIT_PER_30_MINUTE = 2000;

    // Rate Limits (Multi-Order API)
    public static final int MULTI_ORDER_RATE_LIMIT_PER_SECOND = 4;
    public static final int MULTI_ORDER_RATE_LIMIT_PER_MINUTE = 40;
    public static final int MULTI_ORDER_RATE_LIMIT_PER_30_MINUTE = 160;
    public static final int MAX_ORDERS_PER_BATCH = 10;

    // WebSocket Limits
    public static final int MAX_WEBSOCKET_SUBSCRIPTIONS = 100;

    // Cache TTL
    public static final long SECTOR_CACHE_TTL_HOURS = 24;

    // Timeouts
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int WEBSOCKET_CONNECT_TIMEOUT_SECONDS = 10;

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Market Hours (IST)
    public static final int MARKET_OPEN_HOUR = 9;
    public static final int MARKET_OPEN_MINUTE = 15;
    public static final int MARKET_CLOSE_HOUR = 15;
    public static final int MARKET_CLOSE_MINUTE = 30;

    // Nifty Indices
    public static final String NIFTY_50_KEY = "NSE_INDEX|Nifty 50";
    public static final String BANK_NIFTY_KEY = "NSE_INDEX|Nifty Bank";
    public static final String FIN_NIFTY_KEY = "NSE_INDEX|Nifty Fin Service";
    public static final String MIDCAP_NIFTY_KEY = "NSE_INDEX|Nifty Midcap 50";

    // Option Types
    public static final String OPTION_TYPE_CALL = "CE";
    public static final String OPTION_TYPE_PUT = "PE";

    // Instrument Types
    public static final String INSTRUMENT_TYPE_EQUITY = "EQUITY";
    public static final String INSTRUMENT_TYPE_OPTION = "OPTION";
    public static final String INSTRUMENT_TYPE_FUTURE = "FUTURE";
    public static final String INSTRUMENT_TYPE_INDEX = "INDEX";

    // Error Messages
    public static final String ERROR_INVALID_INSTRUMENT_KEY = "Invalid instrument key format";
    public static final String ERROR_INVALID_QUANTITY = "Quantity must be greater than 0";
    public static final String ERROR_INVALID_PRICE = "Invalid price for order type";
    public static final String ERROR_RATE_LIMIT_EXCEEDED = "Rate limit exceeded";
    public static final String ERROR_UNAUTHORIZED = "Unauthorized access";
}
