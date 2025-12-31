# Order Service Module

The Order Service is the central nervous system for all trading operations, handling orchestration, batching, and resilience.

## 1. Coordinator Service
**Class**: `com.vegatrader.upstox.api.order.service.CoordinatorService`

The single authoritative entry point for all Order Management System (OMS) operations.

### Responsibilities
- **Orchestration**: Routes commands to `MultiOrderService` or persistence layers.
- **Idempotency**: Prevents duplicate order execution using cached `idempotencyKey` (Window: 300s).
- **Read-Side Caching**:
  - **Order Book**: Cached for 2 seconds (High frequency access).
  - **Trades**: Cached for 5 seconds.
  - **History**: Cached for 60 seconds.

## 2. Multi-Order Service
**Class**: `com.vegatrader.upstox.api.order.service.MultiOrderService`

Manages batch execution of orders, essential for complex strategies (e.g., Iron Condor deployment).

### Features
- **Batching**: Supports up to 25 orders per request.
- **Execution Order**: **BUY** orders are always executed before **SELL** orders to ensure margin benefits.
- **Maintenance Window**: Rejects orders between 00:00 - 05:30 IST.
- **Bulk Operations**:
  - `cancelMultiOrder`: Cancel multiple orders by ID.
  - `cancelByFilter`: Cancel by Segment or Tag.
  - `exitAllPositions`: Emergency square-off for all open positions.

## 3. Resilience (Retry Service)
**Class**: `com.vegatrader.upstox.api.order.retry.OrderRetryService`

Provides resilience against transient failures (Network, Timeout, Rate Limits).

- **Strategy**: Exponential Backoff.
- **Config**: Max 3 retries, Base Delay 1s, Max Delay 30s.
- **Async**: Uses `CompletableFuture` for non-blocking retries.
