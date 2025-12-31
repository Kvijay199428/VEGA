# Admin Module

The Admin module provides capabilities for system monitoring, broker management, and audit logging.

## Services

### 1. Admin Dashboard Service
**Class**: `com.vegatrader.upstox.api.admin.service.AdminDashboardService`

Provides real-time system metrics and audit logs for the administrative dashboard.

**Key Features**:
- **Summary Metrics**: Counts of disabled strikes, active brokers, and audit actions.
- **Recent Activity**: Stream of recent admin actions (last 24h).
- **Performance**: Track top administrative users.
- **Audit Logs**: Query logs by type (`EMERGENCY`, `RECOVERY`) or time range.

**Dashboard DTO**: `DashboardSummary`
```json
{
  "disabledStrikes": 12,
  "activeBrokers": 5,
  "recentActions24h": 45,
  "failedActions24h": 0
}
```

### 2. Broker Priority Service
**Class**: `com.vegatrader.upstox.api.admin.service.BrokerPriorityService`

Manages the routing priority of brokers for different exchanges and instrument types.

**Key Features**:
- **Priority Management**: Reorder brokers (1 to N) for specific exchanges (NSE, BSE).
- **Activation**: Enable or disable specific brokers (`activateBroker`, `deactivateBroker`).
- **Registration**: Onboard new brokers into the system.

**Usage**:
Used by the Order Management System (OMS) to decide which broker adapter to use for placing an order.

### 3. Audit Logging
**Entity**: `AdminActionAuditEntity`
**Repository**: `AdminActionAuditRepository`

All administrative actions are strictly audited with:
- `actionType` (e.g., `UPDATE_SETTINGS`)
- `performedBy` (Admin User ID)
- `payload` (What changed)
- `timestamp`
