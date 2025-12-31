# Orders API

The Orders module is the core of the trading functionality, handling order placement, modification, and cancellation.

## Overview
Defined in `com.vegatrader.upstox.api.endpoints.OrderEndpoints`.

## REGULAR ORDERS

### Place Order
**Definition**: `PLACE_ORDER`
- **Method**: `POST`
- **Path**: `/order/place`
- **Description**: Place a new standard order (Limit, Market, SL, SL-M).

### Modify Order
**Definition**: `MODIFY_ORDER`
- **Method**: `PUT`
- **Path**: `/order/modify`
- **Description**: Modify quantity, price, or type of a pending order.

### Cancel Order
**Definition**: `CANCEL_ORDER`
- **Method**: `DELETE`
- **Path**: `/order/cancel`
- **Description**: Cancel a pending order.

### Order Details & History
- **Specific Order**: `GET /order/{order_id}` (`GET_ORDER_DETAILS`)
- **All Orders**: `GET /order/orders` (`GET_ALL_ORDERS`) - Fetch for current day.
- **Trades**: `GET /order/trades` (`GET_TRADES`) - Executed trades for current day.
- **Order Book**: `GET /order/order-book` (`GET_ORDER_BOOK`) - Pending orders.
- **Trade Book**: `GET /order/trade-book` (`GET_TRADE_BOOK`) - Executed trades.

## ADVANCED ORDERS

### After Market Orders (AMO)
- **Place**: `POST /order/place-after-market-order` (`PLACE_AMO`)
- **Modify**: `PUT /order/modify-after-market-order` (`MODIFY_AMO`)
- **Cancel**: `DELETE /order/cancel-after-market-order` (`CANCEL_AMO`)
- **Book**: `GET /order/after-market-order-book` (`GET_AMO_BOOK`)

### Order Stop Loss (OSL)
- **Place**: `POST /order/place-osl-order` (`PLACE_OSL`)
- **Cancel**: `DELETE /order/cancel-osl-order` (`CANCEL_OSL`)

### Good-Till-Triggered (GTT)
- **Create**: `POST /order/create-gtt` (`CREATE_GTT`)
- **Modify**: `PUT /order/modify-gtt` (`MODIFY_GTT`)
- **Cancel**: `DELETE /order/cancel-gtt` (`CANCEL_GTT`)
- **List**: `GET /order/gtt/orders` (`GET_GTT_ORDERS`)

## PROFIT & LOSS (P&L)

### Trade P&L
**Definition**: `GET_TRADE_PNL`
- **Method**: `GET`
- **Path**: `/order/trade-profit-loss`
- **Description**: Trade-wise P&L details.

### Symbol P&L
**Definition**: `GET_PNL_BY_SYMBOL`
- **Method**: `GET`
- **Path**: `/order/trade-profit-loss-by-symbol`
- **Description**: Aggregated P&L by trading symbol.

### Metadata
**Definition**: `GET_PNL_METADATA`
- **Method**: `GET`
- **Path**: `/order/trade-profit-loss-metadata`
