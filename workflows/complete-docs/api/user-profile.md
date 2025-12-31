# User Profile API

The User Profile module provides access to account details and fund information.

## Overview
Defined in `com.vegatrader.upstox.api.endpoints.UserProfileEndpoints`.

## Endpoints

### 1. User Profile
**Definition**: `USER_PROFILE`
- **Method**: `GET`
- **Path**: `/user/profile`
- **Description**: Returns detailed profile info:
  - Name, Email, PAN
  - Enabled Exchanges & Products
  - User ID

### 2. Funds & Margin
**Definition**: `USER_FUNDS`
- **Method**: `GET`
- **Path**: `/user/get-funds-and-active-orders`
- **Description**: Returns fund availability and utilization:
  - Available Margin
  - Used Margin
  - Active Orders count
