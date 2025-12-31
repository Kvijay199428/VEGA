# Portfolio API

The Portfolio module manages the user's holdings and positions.

## Overview
Defined in `com.vegatrader.upstox.api.endpoints.PortfolioEndpoints`.

## Endpoints

### 1. Long-Term Holdings
**Definition**: `GET_HOLDINGS`
- **Method**: `GET`
- **Path**: `/portfolio/long-term-holdings`
- **Description**: Fetches stocks held in CNC (Delivery) with quantity, price, and P&L.

### 2. Short-Term Positions
**Definition**: `GET_POSITIONS`
- **Method**: `GET`
- **Path**: `/portfolio/short-term-positions`
- **Description**: Fetches open intraday and F&O positions.

### 3. Net Positions
**Definition**: `GET_NET_POSITIONS`
- **Method**: `GET`
- **Path**: `/portfolio/net-positions`
- **Description**: Consolidated view of all positions across segments.

### 4. Convert Position
**Definition**: `CONVERT_POSITION`
- **Method**: `POST`
- **Path**: `/portfolio/convert-position`
- **Description**: Converts a position from one product type to another (e.g., MIS to CNC).
- **Usage**: Used to roll over intraday positions to delivery or vice versa.
