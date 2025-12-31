# Admin & Compliance TODOs

This document details the pending tasks related to administrative tools, compliance checks, and audit logging in the Vega Trader backend.

## 1. AdminActionService.java

**Location**: `com.vegatrader.upstox.api.admin.service.AdminActionService`

### Dependency Injection
-   **Line 22**: `// TODO: Inject repositories`
    -   **Context**: The service currently logs actions but does not interact with a database. It needs repositories to persist changes and logs.
    -   **Task**: Inject `DisabledStrikeRepository`, `AdminAuditRepository`, and `BrokerRegistryRepository` (or equivalent DAO components).

### Disable Strike Logic
-   **Line 35**: `// TODO: Insert into disabled_strikes table`
    -   **Context**: `disableStrike` method.
    -   **Task**: Create a record in the `disabled_strikes` table with `active=true`, `underlying`, `expiry`, `strike`, `reason`, and `admin_user`.
-   **Line 36**: `// TODO: Log to admin_actions_audit`
    -   **Context**: Audit trail for strike disablement.
    -   **Task**: Insert an audit record with `ACTION_TYPE='STRIKE_DISABLE'`, timestamp, user info, and details.

### Enable Strike Logic
-   **Line 52**: `// TODO: Update disabled_strikes set active = false`
    -   **Context**: `enableStrike` method.
    -   **Task**: execute update query to allow trading on the previously disabled strike.
-   **Line 53**: `// TODO: Log to admin_actions_audit`
    -   **Task**: Insert audit record for `STRIKE_ENABLE`.

### Broker Priority Logic
-   **Line 69**: `// TODO: Update broker_registry priorities`
    -   **Context**: `updateBrokerPriority` method.
    -   **Task**: Reflect the new priority order in the `broker_registry` configuration table.
-   **Line 70**: `// TODO: Log to admin_actions_audit`
    -   **Task**: Insert audit record for `BROKER_PRIORITY_UPDATE`.

### Contract Rollback Logic
-   **Line 85**: `// TODO: Update broker_symbol_mapping active flags`
    -   **Context**: `rollbackContract` method. Used when a new contract version is erroneous.
    -   **Task**: Set current version to inactive and previous version to active in `broker_symbol_mapping`.
-   **Line 86**: `// TODO: Log to contract_version_history`
    -   **Task**: Record the rollback event in the version history table.
-   **Line 87**: `// TODO: Log to admin_actions_audit`
    -   **Task**: Insert audit record for `CONTRACT_ROLLBACK`.

### Reporting
-   **Line 100**: `// TODO: Query admin_actions_audit ORDER BY performed_at DESC LIMIT ?`
    -   **Context**: `getRecentActions` method.
    -   **Task**: Implement the SQL query to fetch the most recent actions for the admin dashboard.
-   **Line 109**: `// TODO: Insert into admin_actions_audit`
    -   **Context**: `logAuditAction` helper method.
    -   **Task**: Centralized logic to write to the audit table.

## 2. AuditExportService.java

**Location**: `com.vegatrader.upstox.api.order.audit.AuditExportService`

### Export Functionality
-   **Line 99**: `// TODO: Implement PDF generation using iText or similar`
    -   **Task**: Add library dependency (e.g., iText or OpenPDF) and implement logic to generate a PDF report of the order trail.
-   **Line 176**: `// TODO: Track export history in DB`
    -   **Task**: Log who exported what data and when, for security compliance.

## 3. TokenAuditService.java

**Location**: `com.vegatrader.upstox.auth.selenium.v2.TokenAuditService`

### Persistence
-   **Line 69**: `// TODO: Persist to database audit table`
    -   **Task**: When a new token is generated or a refresh occurs, save this event to a persistent store (SQLite/Postgres) to track token lifecycle.
