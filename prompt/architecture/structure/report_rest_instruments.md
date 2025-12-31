# REST API & Instruments Report

This report helps you navigate the extensive **REST API Wrapper** and the **Master Data (Instruments)** services.

## 1. REST API Wrapper & DTOs

### 🏗️ Module Overview
The backend includes a comprehensive set of **Data Transfer Objects (DTOs)** that map 1:1 with Upstox's JSON responses. This ensures type safety throughout the application.

### 📂 File Structure
```text
src/main/java/com/vegatrader/upstox/api/
├── request/                         # Request Models
│   ├── auth/                        # Token requests
│   ├── order/                       # Order placement models
│   ├── market/                      # Historical/Option chain requests
│   └── portfolio/                   # Position conversion models
├── response/                        # Response Models
│   ├── auth/
│   ├── order/
│   ├── market/
│   └── common/                      # Generic wrappers (ApiResponse<T>)
├── endpoints/                       # API Route Definitions
└── ratelimit/                       # Rate Limiting Logic
```

### 🧠 Functional Breakdown

#### A. Generic Response Wrappers (`response/common`)
*   **`ApiResponse<T>`**: Wraps the standard Upstox envelope (`status`, `data`, `errors`).
*   **`PaginatedResponse<T>`**: Handles paged data (like Order Books).

#### B. Rate Limiting (`ratelimit/`)
*   **`RateLimiter`**: An interface for implementing client-side throttling.
*   **`StandardAPIRateLimiter`**: Implements the logic to pause requests if the quota (e.g., 10 requests/sec) is exceeded, complying with Upstox's fair usage policy.

---

## 2. Instrument & Master Data

### 🏗️ Module Overview
Handling 100,000+ trading instruments (Scripts) is a challenge. This module manages **Instrument Enrollment** (loading master lists) and **Filtering** (searching for specific scripts).

### 📂 File Structure
```text
src/main/java/com/vegatrader/upstox/api/instrument/
├── service/
│   └── InstrumentEnrollmentService.java   # Downloads Master CSV/JSON
└── filter/
    └── InstrumentFilterService.java       # Search & Filter Logic
```

### 🧠 Functional Breakdown

#### A. Enrollment Service (`InstrumentEnrollmentService`)
*   **Function**: Connects to Upstox's public instrument CDN.
*   **Capabilities**:
    *   Fetches the "complete" list or segmented lists (NSE Equity, NFO Futures).
    *   Parses the CSV/JSON data into Java Objects (`Instrument`).
    *   Used by the Live Test to dynamically fetch valid `instrument_keys` (e.g., finding the key for "RELIANCE").

#### B. Filter Service (`InstrumentFilterService`)
*   **Function**: Allows searching the loaded instrument list.
*   **Features**:
    *   Find by Symbol (e.g., "NIFTY 50").
    *   Find by Exchange (NSE, NFO).
    *   Find by Token.

---

## 3. Implementation Status

| Component | Status | Verification Notes |
| :--- | :--- | :--- |
| **DTO Models** | ✅ **COMPLETED** | Over 60 files implementing the schema. |
| **Rate Limiter** | 🏗️ **IMPLEMENTED** | Logic exists, but integration into a global `RestClient` is pending. |
| **Enrollment** | ✅ **TESTED** | Verified in `InstrumentEnrollmentServiceTest` to fetch live keys. |
| **Filtering** | ✅ **TESTED** | Search logic verified. |
