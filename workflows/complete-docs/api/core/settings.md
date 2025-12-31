# Settings Module

The Settings module handles configuration management at both the system (Admin) and user levels.

## 1. Admin Settings (System Configuration)
**Service**: `com.vegatrader.upstox.api.settings.service.AdminSettingsService`

Manages global application settings that can be changed at runtime without deployment.

### Features
- **Dynamic Updates**: Change validation parameters, risk limits, and feature flags live.
- **Validation**: Strict validation based on `SettingDefinition` (Type, Min, Max).
- **Audit Trail**: All changes are logged with `reasonCode` and `comment`.

### Key Settings
| Key | Default | Description |
|-----|---------|-------------|
| `rms.killSwitch.enabled` | `false` | Global emergency kill switch to stop all trading. |
| `trading.maxOrderQty` | `1800` | Maximum quantity per order allowed by system. |
| `ws.binaryEncoding` | `true` | Use binary Protobuf encoding for WebSockets. |

### Kill Switch
The module provides a dedicated Emergency Kill Switch (`enableKillSwitch`) that immediately stops all trading activities.

## 2. User Settings (Preferences)
**Service**: `com.vegatrader.upstox.api.settings.service.UserSettingsService`
**Resolver**: `com.vegatrader.upstox.api.settings.service.SettingsResolver`

Manages user-specific preferences with a hierarchical resolution strategy.

### Resolution Hierarchy
Settings are resolved in the following precedence (highest to lowest):
1. **Regulatory Rules**: Hardcoded compliance rules (cannot be overridden).
2. **Exchange Rules**: Exchange-specific constraints.
3. **System Defaults**: Global defaults (`UserPrioritySettings.defaults()`).
4. **User Preferences**: Stored in DB/Cache for the specific user.
5. **Session Overrides**: Temporary overrides for the current login session.

### Configurable Preferences
- **Broker Routing**: Preferred broker order.
- **Product Type**: Default (`INTRADAY` vs `DELIVERY`).
- **Exchange**: Default Exchange (`NSE` vs `BSE`).
- **Confirmations**: `confirmBeforePlace` (UI toggle).
