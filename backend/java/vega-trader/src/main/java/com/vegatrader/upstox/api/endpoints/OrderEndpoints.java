package com.vegatrader.upstox.api.endpoints;

/**
 * Upstox Order Management endpoints.
 * <p>
 * Comprehensive set of endpoints for:
 * <ul>
 * <li>Regular orders (place, modify, cancel)</li>
 * <li>After-Market Orders (AMO)</li>
 * <li>Order Stop Loss (OSL) orders</li>
 * <li>Good-Till-Triggered (GTT) orders</li>
 * <li>Order status and history</li>
 * <li>Trade execution details</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 */
public enum OrderEndpoints implements UpstoxEndpoint {

    // Regular Order Operations
    /**
     * Place a new order.
     * <p>
     * POST /order/place
     * </p>
     */
    PLACE_ORDER("/order/place", HttpMethod.POST, "Place a new order"),

    /**
     * Modify an existing order.
     * <p>
     * PUT /order/modify
     * </p>
     */
    MODIFY_ORDER("/order/modify", HttpMethod.PUT, "Modify an existing pending order"),

    /**
     * Cancel an active order.
     * <p>
     * DELETE /order/cancel
     * </p>
     */
    CANCEL_ORDER("/order/cancel", HttpMethod.DELETE, "Cancel an active or pending order"),

    /**
     * Get details of a specific order.
     * <p>
     * GET /order/{order_id}
     * </p>
     */
    GET_ORDER_DETAILS("/order/{order_id}", HttpMethod.GET, "Get details of a specific order by ID"),

    /**
     * Get all orders for the day.
     * <p>
     * GET /order/orders
     * </p>
     */
    GET_ALL_ORDERS("/order/orders", HttpMethod.GET, "Get all orders for the current trading day"),

    /**
     * Get all executed trades.
     * <p>
     * GET /order/trades
     * </p>
     */
    GET_TRADES("/order/trades", HttpMethod.GET, "Get all executed trades for the day"),

    /**
     * Get order book (pending orders).
     * <p>
     * GET /order/order-book
     * </p>
     */
    GET_ORDER_BOOK("/order/order-book", HttpMethod.GET, "Get order book with pending orders"),

    /**
     * Get trade book (executed trades).
     * <p>
     * GET /order/trade-book
     * </p>
     */
    GET_TRADE_BOOK("/order/trade-book", HttpMethod.GET, "Get trade book with all executed trades"),

    // After Market Orders (AMO)
    /**
     * Place an After Market Order.
     * <p>
     * POST /order/place-after-market-order
     * </p>
     */
    PLACE_AMO("/order/place-after-market-order", HttpMethod.POST, "Place an After Market Order (AMO)"),

    /**
     * Modify an After Market Order.
     * <p>
     * PUT /order/modify-after-market-order
     * </p>
     */
    MODIFY_AMO("/order/modify-after-market-order", HttpMethod.PUT, "Modify an existing AMO"),

    /**
     * Cancel an After Market Order.
     * <p>
     * DELETE /order/cancel-after-market-order
     * </p>
     */
    CANCEL_AMO("/order/cancel-after-market-order", HttpMethod.DELETE, "Cancel an After Market Order"),

    /**
     * Get After Market Order book.
     * <p>
     * GET /order/after-market-order-book
     * </p>
     */
    GET_AMO_BOOK("/order/after-market-order-book", HttpMethod.GET, "Get After Market Order book"),

    /**
     * Get After Market executed trades.
     * <p>
     * GET /order/after-market-trades
     * </p>
     */
    GET_AMO_TRADES("/order/after-market-trades", HttpMethod.GET, "Get AMO executed trades"),

    // Order Stop Loss (OSL) Orders
    /**
     * Place an Order Stop Loss order.
     * <p>
     * POST /order/place-osl-order
     * </p>
     */
    PLACE_OSL("/order/place-osl-order", HttpMethod.POST, "Place an Order Stop Loss (OSL) order"),

    /**
     * Cancel an Order Stop Loss order.
     * <p>
     * DELETE /order/cancel-osl-order
     * </p>
     */
    CANCEL_OSL("/order/cancel-osl-order", HttpMethod.DELETE, "Cancel an OSL order"),

    // GTT (Good Till Triggered) Orders
    /**
     * Create a GTT order.
     * <p>
     * POST /order/create-gtt
     * </p>
     */
    CREATE_GTT("/order/create-gtt", HttpMethod.POST, "Create a Good-Till-Triggered (GTT) order"),

    /**
     * Get all GTT orders.
     * <p>
     * GET /order/gtt/orders
     * </p>
     */
    GET_GTT_ORDERS("/order/gtt/orders", HttpMethod.GET, "Get all GTT orders"),

    /**
     * Modify a GTT order.
     * <p>
     * PUT /order/modify-gtt
     * </p>
     */
    MODIFY_GTT("/order/modify-gtt", HttpMethod.PUT, "Modify an existing GTT order"),

    /**
     * Cancel a GTT order.
     * <p>
     * DELETE /order/cancel-gtt
     * </p>
     */
    CANCEL_GTT("/order/cancel-gtt", HttpMethod.DELETE, "Cancel a GTT order"),

    // Trade P&L
    /**
     * Get trade profit and loss details.
     * <p>
     * GET /order/trade-profit-loss
     * </p>
     */
    GET_TRADE_PNL("/order/trade-profit-loss", HttpMethod.GET, "Get trade-wise profit and loss details"),

    /**
     * Get P&L breakdown by symbol.
     * <p>
     * GET /order/trade-profit-loss-by-symbol
     * </p>
     */
    GET_PNL_BY_SYMBOL("/order/trade-profit-loss-by-symbol", HttpMethod.GET, "Get P&L breakdown by trading symbol"),

    /**
     * Get P&L calculation metadata.
     * <p>
     * GET /order/trade-profit-loss-metadata
     * </p>
     */
    GET_PNL_METADATA("/order/trade-profit-loss-metadata", HttpMethod.GET, "Get metadata for P&L calculations");

    private final String path;
    private final HttpMethod method;
    private final String description;
    private static final String CATEGORY = "Orders";

    OrderEndpoints(String path, HttpMethod method, String description) {
        this.path = path;
        this.method = method;
        this.description = description;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
