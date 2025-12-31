# Backend OAuth Documentation

This document details the Authentication architecture within the `com.vegatrader.upstox.auth` package of the Vega Trader Backend.

## 1. REST Endpoints

The auth system exposes endpoints for both manual automation control and token lifecycle management.

### Login Automation Controller
**Base URL**: `/api/v1/auth/selenium`

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Initiates a single-API login flow via Selenium. | `LoginRequest` |
| `POST` | `/multi-login` | Initiates a sequential login flow for multiple APIs. | `MultiLoginRequest` |

### Token Generation Controller
**Base URL**: `/api/auth/upstox/tokens`

| Method | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/generate` | Orchestrates token generation with validity checks. | `GenerateRequest` (`mode`: `ALL`, `PARTIAL`, `INVALID_ONLY`) |
| `POST` | `/generate/{apiName}` | Forces generation for a specific API name. | N/A |

## 2. Database Schema

The authentication system persists tokens in the `upstox_tokens` table.

**Entity Class**: `com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity`

| Column Name | Type | Description |
| :--- | :--- | :--- |
| `id` | `INTEGER` | Primary Key. |
| `api_name` | `VARCHAR` | Unique identifier (e.g., `PRIMARY`, `WEBSOCKET1`). |
| `access_token` | `VARCHAR` | The active Upstox Bearer Token. |
| `client_id` | `VARCHAR` | OAuth Client ID. |
| `client_secret` | `VARCHAR` | OAuth Client Secret. |
| `redirect_uri` | `VARCHAR` | Registered Redirect URI. |
| `expires_in` | `BIGINT` | Token expiry duration in seconds. |
| `created_at` | `BIGINT` | Epoch timestamp of token creation. |
| `updated_at` | `BIGINT` | Epoch timestamp of last DB update. |
| `is_primary` | `BOOLEAN` | Flag indicating if this is the master trading account. |
| `is_active` | `INTEGER` | 1 if active, 0 if inactive. |
| `generated_at` | `VARCHAR` | Human-readable timestamp string. |
| `validity_at` | `VARCHAR` | Calculated expiry checking timestamp. |

## 3. Key Components

### Async Token Orchestrator
**Class**: `AsyncTokenOrchestrator`
- **Role**: High-level manager.
- **Logic**:
    1.  Loads all tokens.
    2.  Verifies them in parallel (using `CompletableFuture`).
    3.  If valid, returns immediately (Fast Path).
    4.  If invalid, triggers `TokenGenerationService`.

### Resume Orchestrator
**Class**: `ResumeOrchestrator`
- **Role**: Fault tolerance.
- **Logic**:
    - Saves state after each successful login.
    - If Broker Throttling triggers (e.g., waiting for PIN), enforces an **11-minute** cooldown.
    - Resumes execution from the next API in the list automatically.

### Selenium Integration
**Class**: `AuthenticationOrchestrator`
- **Role**: Browser Automation.
- **Tech**: Selenium WebDriver (Chrome).
- **Features**: Headless mode support, TOTP generation handling, and automatic consent approval.
