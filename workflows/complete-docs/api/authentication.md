# Authentication API

The Authentication module handles user login, token generation, and session management using OAuth 2.0 flow.

## Overview
All authentication endpoints are part of the `com.vegatrader.upstox.api.endpoints.AuthenticationEndpoints` enum.

- **Base URL**: `https://api.upstox.com/v2`
- **Redirect URI**: `http://localhost:28021/api/v1/auth/upstox/callback`

## Endpoints

### 1. Authorization Dialog
**Definition**: `LOGIN_DIALOG`
- **Method**: `GET`
- **Path**: `/login/authorization/dialog`
- **Description**: Redirects the user to the Upstox login page to initiate the authorization process.
- **Flow**: User logs in at Upstox -> Redirected back to `redirect_uri` with `code`.

### 2. Get Access Token
**Definition**: `GET_TOKEN`
- **Method**: `POST`
- **Path**: `/login/authorization/token`
- **Description**: Exchanges the authorization code for an access token.
- **Params**:
  - `code`: Authorization code from callback.
  - `client_id`: App Key.
  - `client_secret`: App Secret.
  - `redirect_uri`: Configured callback URL.
  - `grant_type`: `authorization_code`.

### 3. Renew Token
**Definition**: `RENEW_TOKEN`
- **Method**: `POST`
- **Path**: `/login/authorization/token`
- **Description**: Refreshes an expired access token.
- **Params**:
  - `refresh_token`: The refresh token received during initial login.
  - `grant_type`: `refresh_token`.

### 4. Logout
**Definition**: `LOGOUT`
- **Method**: `POST`
- **Path**: `/logout`
- **Description**: Revokes the current access token and logs the user out.

## Integration Notes
- Tokens are typically valid for 24 hours.
- Implementing automatic token renewal using the `RENEW_TOKEN` endpoint is recommended for long-running sessions.
