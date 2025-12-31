# Documentation Comparison Analysis

This document provides a detailed comparison between the two primary documentation sets within the project:
1. **Application Documentation**: `workflows/complete-docs` (Vega Trader Backend)
2. **SDK Documentation**: `upstox-java-sdk/sdk-doc` (Upstox Java Client)

## 1. High-Level Overview

| Feature | Application Documentation (`workflows/complete-docs`) | SDK Documentation (`upstox-java-sdk/sdk-doc`) |
| :--- | :--- | :--- |
| **Primary Focus** | **Business Logic & Architecture**. Describes *how* the application trades, manages risk, and orchestrates services. | **Library Reference**. Describes the *tools and components* available to interact with the Upstox API. |
| **Target Audience** | Backend Developers maintaining the trading system, Architects, and DevOps. | Developers integrating the Upstox API into any Java application. |
| **Organization** | By **Business Domain** (Orders, Auth, Instruments, Analytics). | By **Software Component** (API Clients, Feeder, Models, Auth). |
| **Abstraction Level** | **High**. Focuses on flows, strategies (Retry, Cooldown), and service interactions. | **Low**. Focuses on classes, methods, signatures, and data types. |

## 2. Detailed Structure Comparison

### A. Authentication
| Aspect | Application Docs (`api/auth/`) | SDK Docs (`auth-module.md`) |
| :--- | :--- | :--- |
| **Content** | Describes the **Automated Login System**: Selenium workflows, `ResumeOrchestrator` for fault tolerance, `CooldownManager` for rate limits, and `AsyncTokenOrchestrator`. | Describes the **Auth Schemes**: `OAuth`, `ApiKeyAuth`, and `OAuthFlow` classes used to inject headers. |
| **Relationship** | The App's `AsyncTokenOrchestrator` *uses* the SDK's `LoginApi` and `OAuth` classes to execute the login flow. | The SDK provides the building blocks (Tokens, APIs) that the App orchestrates. |

### B. Market Data (Feeders)
| Aspect | Application Docs (`api/market-data.md`) | SDK Docs (`feeder-module.md` & `proto-rpc.md`) |
| :--- | :--- | :--- |
| **Content** | Focuses on **Consumption & Storage**: How the app manages `MarketDataBuffer`, persists ticks to Redis/SQLite, and handles backpressure. | Focuses on **Transport & Decoding**: `MarketDataStreamerV3`, WebSocket connection details, and Protobuf (`MarketDataFeedV3`) definitions. |
| **Relationship** | The App wraps the SDK's `MarketDataStreamerV3` to build a resilient, buffering data pipeline suitable for trading. | The SDK handles the raw socket connection and binary decoding, delivering POJOs (`MarketUpdateV3`) to the App. |

### C. Order Management
| Aspect | Application Docs (`api/orders/`) | SDK Docs (`api-clients.md` & `models.md`) |
| :--- | :--- | :--- |
| **Content** | **Strategic Execution**: `CoordinatorService` for routing, `MultiOrderService` for batching, `RMS` for pre-trade checks, and `OrderRetryService` for resilience. | **API Wrapper**: `OrderApi` client methods (`placeOrder`, `cancelOrder`) and DTOs (`PlaceOrderRequest`, `OrderData`). |
| **Relationship** | The App's `CoordinatorService` determines *what* to trade, then calls the SDK's `OrderApi` to *execute* the trade. | The SDK provides the raw interface to send HTTP requests to Upstox; the App adds intelligence (retries, safety checks). |

## 3. Key Differences in Scope

### 1. Error Handling
- **SDK Docs**: Mentions `ApiException` and standard error responses. It is passive (throws errors).
- **App Docs**: Details active recovery strategies like **Exponential Backoff**, **Token Regeneration**, and **Circuit Breakers**.

### 2. Data Models
- **SDK Docs**: Exhaustive list of all ~120 DTOs (`PutCallOptionChainData`, `LTPC`, etc.) mirroring the Upstox JSON schema.
- **App Docs**: Discusses domain entities like `Instrument`, `OptionChain`, and database schemas (`upstox_tokens`).

### 3. Usage
- **SDK Docs**: "How do I call endpoint X?"
- **App Docs**: "How does the system ensure orders are placed safely and tokens remain valid 24/7?"

## 4. Conclusion
The two documentation sets are complementary:
- Use **SDK Docs** when you need to know the specific parameters of an API call or the structure of a response object.
- Use **App Docs** when you need to understand the system's architecture, trading flows, or how to modify the core business logic.
