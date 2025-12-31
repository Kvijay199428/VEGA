# Order Management Enterprise Implementation Plan

## Overview
Implement all P0-P3 features from gap analysis to make Order Management production-ready.

---

## Phase 1: P0 - Critical (JPA, Broker, Risk, Charges)

### 1.1 JPA Repositories
Replace in-memory stores with Spring Data JPA.

#### [NEW] `repository/OrderRepository.java`
- JPA repository for [Order](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/model/Order.java#12-224) entity
- Custom queries: findByUserId, findByStatus, findByTag

#### [NEW] `repository/TradeRepository.java`
- JPA repository for [Trade](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/model/Trade.java#14-213) entity
- Custom queries: findByOrderId, findByUserId, findByDateRange

#### [NEW] `repository/OrderChargesRepository.java`
- JPA repository for [OrderCharges](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/model/OrderCharges.java#14-143) entity

#### [NEW] `repository/AuditEventRepository.java`
- JPA repository for audit events

#### [MODIFY] [model/Order.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/model/Order.java)
- Add JPA annotations (@Entity, @Table, @Id)

#### [MODIFY] [model/Trade.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/model/Trade.java)
- Add JPA annotations

#### [MODIFY] [service/OrderPersistenceOrchestrator.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/service/OrderPersistenceOrchestrator.java)
- Inject repositories, replace ConcurrentHashMap

---

### 1.2 Broker Adapter Interface
Create abstraction for multi-broker support.

#### [NEW] `broker/BrokerAdapter.java`
- Interface: placeOrder, modifyOrder, cancelOrder, getOrderStatus

#### [NEW] `broker/BrokerCapability.java`
- Enum: supportsMultiOrder, supportsModify, rateLimit

#### [NEW] `broker/upstox/UpstoxBrokerAdapter.java`
- Upstox API implementation using existing SDK

#### [NEW] `broker/BrokerRouter.java`
- Routes requests to appropriate broker based on user settings

---

### 1.3 Risk/Margin Engine
Pre-order validation per b4.md section 4.

#### [NEW] `risk/RiskEngine.java`
- Validates: max order value, position limits, symbol restrictions

#### [NEW] `risk/MarginService.java`
- Checks available margin before order placement

#### [NEW] `risk/RiskValidationResult.java`
- Result record with pass/fail and reasons

---

### 1.4 Charge Calculation
Actual STT, GST, brokerage per b2.md section 5.

#### [NEW] `charges/ChargeCalculator.java`
- Calculates all charge types per segment

#### [NEW] `charges/ChargeBreakdown.java`
- Record with brokerage, STT, GST, SEBI, stampDuty, exchangeFees

---

## Phase 2: P1 - High (Rate Limiting, Retry, Audit, P&L, Cache)

### 2.1 Rate Limiting
#### [NEW] `ratelimit/RateLimitService.java`
- Per-user and per-IP rate limiting
- Configurable via AdminSettings

### 2.2 Retry Mechanism
#### [NEW] `retry/OrderRetryService.java`
- Exponential backoff for failed orders
- Configurable max retries

### 2.3 Audit Export
#### [NEW] `audit/AuditExportService.java`
- CSV, PDF, Regulator Pack (ZIP) export
- Per b2.md section 6

### 2.4 P&L Engine
#### [NEW] `pnl/PnLService.java`
- Realized, Unrealized, Net P&L calculation
- Per b2.md section 5

### 2.5 Distributed Cache
#### [NEW] `cache/OrderCacheService.java`
- Redis/Caffeine integration
- TTL configuration per b2.md

---

## Phase 3: P2 - Medium (Slicing, Settlement, CLI, Settings)

### 3.1 Auto Slicing
#### [MODIFY] [service/MultiOrderService.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/service/MultiOrderService.java)
- Add slicing logic for large orders

### 3.2 Settlement Tracking
#### [NEW] `settlement/SettlementService.java`
- T+0/T+1 settlement tracking

### 3.3 CLI Integration
#### [NEW] `cli/TraderCLI.java`
- Spring Shell commands for order operations

### 3.4 Settings Integration
#### [MODIFY] [service/CoordinatorService.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/order/service/CoordinatorService.java)
- Wire AdminSettingsService for dynamic config

---

## Phase 4: P3 - Low (Frontend, Sandbox, Extended Audit)

### 4.1 Frontend Components (Skip - Backend Focus)
### 4.2 Sandbox Mode
#### [NEW] `config/EnvironmentConfig.java`
- Sandbox/Live endpoint switching

### 4.3 Extended Audit
#### [MODIFY] `audit/AuditExportService.java`
- 8-year retention support

---

## Verification Plan

### Automated Tests
1. JPA Repository tests with H2
2. Broker adapter tests with WireMock
3. Risk engine validation tests
4. Charge calculation accuracy tests
5. Rate limiting tests
6. Retry mechanism tests

### Manual Verification
1. Place order and verify DB persistence
2. Multi-order batch with actual charges
3. Rate limit enforcement
4. Audit export in all formats

---

## File Summary

| Priority | New Files | Modified Files |
|----------|-----------|----------------|
| P0 | 10 | 4 |
| P1 | 5 | 0 |
| P2 | 3 | 2 |
| P3 | 1 | 1 |
| **Total** | **19** | **7** |
