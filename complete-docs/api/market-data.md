# Market Data API

The Market Data module provides real-time and historical data access.

## Overview
Defined in `com.vegatrader.upstox.api.endpoints.MarketDataEndpoints`.

## LIVE QUOTES

### Full Quote
**Definition**: `FULL_QUOTE`
- **Method**: `GET`
- **Path**: `/market-quote/quotes`
- **Description**: Returns full market depth, OHLC, and IO info.

### OHLC
**Definition**: `OHLC_QUOTE`
- **Method**: `GET`
- **Path**: `/market-quote/ohlc`
- **Description**: Returns Open, High, Low, Close data for requested instruments.

### Last Traded Price (LTP)
**Definition**: `LTP_QUOTE`
- **Method**: `GET`
- **Path**: `/market-quote/ltp`
- **Description**: Lightweight endpoint for just the last traded price.

### Option Greeks
**Definition**: `OPTION_GREEKS`
- **Method**: `GET`
- **Path**: `/market-quote/option-greeks`
- **Description**: Real-time Delta, Gamma, Theta, Vega, and Rho values.

## HISTORICAL DATA

### Candlestick Data
**Definition**: `CANDLESTICK_DATA`
- **Method**: `GET`
- **Path**: `/market-quote/candlestick`
- **Description**: Historical OHLCV candles.

### Historical OHLC
**Definition**: `HISTORICAL_OHLC`
- **Method**: `GET`
- **Path**: `/market-quote/historical`

### Index Historical
**Definition**: `INDEX_HISTORICAL`
- **Method**: `GET`
- **Path**: `/market-quote/index-historical`

## METADATA & STATUS

### Instruments
- **BOD Instruments**: `GET /instruments` (`GET_INSTRUMENTS`)
- **Expired Contracts**: `GET /instruments/expired` (`GET_EXPIRED_INSTRUMENTS`)

### Market Status
- **Status**: `GET /market-information/market-status` (`MARKET_STATUS`)
- **Brokers**: `GET /market-information/brokers` (`GET_BROKERS`)

### Charges & Margins
- **Charges**: `GET /charges` (`GET_CHARGES`)
- **Margins**: `GET /margins` (`GET_MARGINS`)
