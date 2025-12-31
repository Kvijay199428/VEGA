# User & Settings TODOs

This document details the pending tasks related to user profiles, configuration, and settings management.

## 1. UserProfileService.java

**Location**: `com.vegatrader.upstox.api.profile.service.UserProfileService`

### Broker Integration
-   **Line 109**: `// TODO: Implement actual Upstox API call via BrokerAdapter`
    -   **Context**: `fetchFromBroker` method.
    -   **Task**: Replace the hardcoded mock profile with a real call to Upstox's "Get Profile" endpoint.
    -   **Data to Map**: User ID, Name, Email, Exchanges (NSE/BSE), Products (Intraday/Delivery), Order Types.

## 2. SettingsResolver.java

**Location**: `com.vegatrader.upstox.api.settings.service.SettingsResolver`

### Persistence
-   **Line 78**: `// TODO: Persist to database`
    -   **Context**: `saveUserSettings` method.
    -   **Task**: Save the `UserPrioritySettings` object to the `user_settings` table.
-   **Line 79**: `// TODO: Log to audit table`
    -   **Context**: Settings change audit.
    -   **Task**: Log who changed what setting and when.

## 3. FundsMarginService.java

**Location**: `com.vegatrader.upstox.api.profile.service.FundsMarginService`

### Broker Integration
-   **Line 100**: `// TODO: Implement actual Upstox API call via BrokerAdapter`
    -   **Context**: Fetching funds and margin availability.
    -   **Task**: Call Upstox API to get `available_margin`, `used_margin`, etc., to allow for local validation of order placement.

## 4. MultiBrokerResolver.java

**Location**: `com.vegatrader.upstox.api.broker.service.MultiBrokerResolver`

### Registry Integration
-   **Line 79**: `// TODO: Read from broker_registry table`
    -   **Context**: Resolving available brokers.
    -   **Task**: Don't just rely on configuration files; read proper broker connection details (status, priority) from the `broker_registry` database table.
