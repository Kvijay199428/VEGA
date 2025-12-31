# Vega Trader Backend Documentation

Welcome to the comprehensive documentation for the Vega Trader Java Backend.

## Overview
This documentation covers the entire `backend/java/vega-trader` project, including configuration, API endpoints, and internal mechanisms.

## Documentation Index

### 1. Resources & Configuration
- **[Configuration Guide](resources/configuration.md)**: `application.properties` and environment setup.
- **[Database Schema](resources/database.md)**: SQLite schema and Flyway migrations.

### 2. Core Modules
- **[Admin & Governance](api/core/admin.md)**: Dashboard services and audit logging.
- **[Settings Management](api/core/settings.md)**: Dynamic configuration updates.
- **[Broker Adapter](api/core/broker.md)**: Multi-broker connection management.

### 3. Market Data & Instruments
- **[Instrument Master](api/market/instruments.md)**: Search, enrollment, and lookup services.
- **[Option Chain](api/market/option-chain.md)**: Real-time chain streaming and Greeks.
- **[Market Data API](api/market-data.md)**: WebSocket feed handlers (V3).

### 4. Order Management
- **[Order Service](api/orders/order-service.md)**: Orchestration, Batching, and Retry logic.
- **[Risk Management (RMS)](api/orders/rms.md)**: Pre-trade validation rules.
- **[Settlement](api/orders/settlement.md)**: Trade settlement cycles and funds logic.

### 5. Authentication & Security
- **[Auth Service](api/auth/auth-service.md)**: Async orchestration and Cooldown management.
- **[Selenium Automation](api/auth/selenium-automation.md)**: Browser automation workflows.
- **[Auth Database](api/auth/auth-db.md)**: Token storage and schema.

### 6. Internal Architecture
- **[Base Architecture](internal/base/architecture.md)**: Token pooling and Lease management.
- **[Analytics Engine](internal/base/analytics.md)**: Black-Scholes valuation models.
- **[Rate Limiting](internal/rate-limiting.md)**: Throughput controls.
- **[Endpoint Registry](internal/registry.md)**: Route mapping.

## Getting Started
Ensure you have `Java 21` and `Maven` installed.

1. **Configure Application**: Check [Configuration Guide](resources/configuration.md).
2. **Database Setup**: Refer to [Database Schema](resources/database.md).
3. **Run Application**:
   ```bash
   mvn spring-boot:run
   ```
