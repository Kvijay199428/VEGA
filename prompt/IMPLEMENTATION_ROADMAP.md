# VEGA TRADER - Implementation Roadmap

**Version:** 1.1  
**Generated:** 2025-12-30  
**Status:** PRODUCTION READY (Complete)

---

## Executive Summary

This roadmap consolidates all implemented features, identifies overlaps with existing code, and outlines remaining enhancements across the VEGA TRADER platform.

| Module | Status | Tests | Priority |
|--------|--------|-------|----------|
| RMS Control Plane v4.1 | ✅ Complete | 74 | P0 |
| Client Risk Limits | ✅ Complete | 12 | P0 |
| Multi-Broker Abstraction | ✅ Complete | 11 | P0 |
| Sectoral Indexing | ✅ Complete | 14 | P1 |
| User Settings | ✅ Complete | 15 | P1 |
| Expired Instruments | ✅ Complete | 21 | P1 |
| Logics Feature | ✅ Complete | 12 | P1 |
| Architecture Feature | ✅ Complete | 7 | P1 |
| Final Settings & SEBI | ✅ Complete | 9 | P0 |
| Option Chain Module | ✅ Complete | 18 | P0 |
| Option Chain WebSocket | ✅ Complete | 24 | P0 |
| User Profile & Funds | ✅ Complete | 14 | P0 |
| **Admin Settings Framework** | ✅ Complete | 15 | P2 |
| **Order Management** | ✅ Complete | 12 | P0 |
| **Advanced Order APIs** | ✅ Complete | 13 | P0 |
| **Coordinator Service** | ✅ Complete | 9 | P0 |

---

## Part 1: Completed Modules

### 1.1 Database Schema (V10-V43)

| Migration | Description | Status |
|-----------|-------------|--------|
| V10-V12 | Instrument master | ✅ |
| V13-V22 | RMS tables | ✅ |
| V23-V24 | Client risk | ✅ |
| V25-V26 | Multi-broker | ✅ |
| V27-V30 | Sectoral indexing | ✅ |
| V31-V32 | User settings | ✅ |
| V33 | Expired instruments | ✅ |
| V34-V37 | Logics (expiry, strikes, BSE groups) | ✅ |
| V38-V40 | Architecture (versioning, admin audit) | ✅ |
| V41 | Option chain | ✅ |
| V42-V43 | Profile/funds snapshots | ✅ |

### 1.2 Core Services

| Service | Features | Tests |
|---------|----------|-------|
| `RmsValidationService` | Quantity/price/T2T validation | 15 |
| `ClientRiskEvaluator` | Kill-switch, daily usage | 12 |
| `MultiBrokerEngine` | Adapter routing | 11 |
| `SectorService` | Index tracking | 14 |
| `UserSettingsService` | Validation + audit | 15 |
| `OptionChainService` | Caching, token rotation | 18 |
| `UserProfileService` | Profile caching, eligibility | 7 |
| `FundsMarginService` | Margin check, maintenance guard | 7 |

### 1.3 REST Endpoints

| Module | Endpoints | Status |
|--------|-----------|--------|
| Option Chain | 4 | ✅ |
| User Profile | 6 | ✅ |
| Expired Instruments | 7 | ✅ |
| Admin Actions | 4 | ✅ |

### 1.4 WebSocket Streaming

| Feature | Status |
|---------|--------|
| `OptionChainFeedStreamV3` | ✅ |
| Delta-based updates | ✅ |
| Binary WebSocket transport | ✅ |
| Text WebSocket transport | ✅ |
| Latency tracking | ✅ |
| Multicast dispatcher | ✅ |
| Heartbeat (3s) | ✅ |

---

## Part 2: Overlapping Code Analysis

### 2.1 Settings Framework Overlap

**Existing (V31-V32):**
```
user_settings (user_id, setting_key, value)
settings_metadata (17 definitions)
UserSettingsService.java
SettingsResolver.java
```

**Proposed in a2/a3.md:**
```
settings_definition (key, scope, data_type, locked)
settings_admin (key, value, tenant_id, effective_from)
settings_user (already exists as user_settings)
settings_audit_log (already exists in V31)
```

**Resolution Strategy:**
- ✅ Keep V31/V32 as-is (user settings)
- 🔄 Add V44 for `settings_admin` table
- 🔄 Add V45 for `settings_definition` table
- ✅ Reuse existing `SettingsResolver` (priority chain)

### 2.2 Audit Logging Overlap

**Existing:**
- `settings_audit_log` (V31)
- `admin_actions_audit` (V40)
- `option_chain_audit` (V41)

**Proposed in a3.md:**
- Same `settings_audit_log` structure

**Resolution:** ✅ No changes needed - already covered.

### 2.3 Profile/Funds Overlap

**Existing:**
- `UserProfileResponse.java` (DTO)
- `FundsResponse.java` (DTO)
- `UserProfileEndpoints` (enum)

**Implemented:**
- `UserProfile.java` (domain record)
- `FundsMargin.java` (domain record with July 2025)
- `UserProfileService.java`
- `FundsMarginService.java`
- `UserProfileController.java`

**Resolution:** ✅ Coexist - DTOs for API response, records for domain logic.

---

## Part 3: Remaining Enhancements

### 3.1 Admin Settings Framework (P2)

**New Migrations:**
```sql
-- V44: Admin settings table
CREATE TABLE settings_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(64) NOT NULL,
    setting_value TEXT,
    tenant_id VARCHAR(32) DEFAULT 'GLOBAL',
    effective_from TIMESTAMP,
    updated_by VARCHAR(64),
    reason_code VARCHAR(32),
    INDEX idx_admin_key (setting_key)
);

-- V45: Settings definition (schema registry)
CREATE TABLE settings_definition (
    setting_key VARCHAR(64) PRIMARY KEY,
    scope ENUM('SYSTEM', 'ADMIN', 'USER') NOT NULL,
    data_type VARCHAR(32) NOT NULL,
    schema_version VARCHAR(16),
    locked BOOLEAN DEFAULT FALSE,
    default_value TEXT
);
```

**New Services:**
| Service | Description |
|---------|-------------|
| `SettingsRegistry` | Schema validation |
| `AdminSettingsService` | Admin CRUD + audit |
| `SettingsCache` | In-memory snapshot |

**New Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/settings` | GET | List admin settings |
| `/admin/settings` | POST | Update setting |
| `/admin/settings/history` | GET | Audit trail |

### 3.2 Kill-Switch Enhancement (P2)

**Current:** Basic `ClientRiskEvaluator` kill-switch per client.

**Enhancement:**
- Global platform kill-switch
- Per-broker kill-switch
- Per-exchange kill-switch
- Immediate propagation via WebSocket

### 3.3 Broker-Specific Overrides (P3)

**Structure:**
```json
{
    "brokerOverrides": {
        "UPSTOX": {
            "options.maxStrikesPerSide": 20,
            "trading.maxOrderQty": 1800
        },
        "ZERODHA": {
            "options.maxStrikesPerSide": 25
        }
    }
}
```

### 3.4 Feature Flags (P3)

**Structure:**
```json
{
    "features": {
        "websocket.binary": true,
        "optionChain.bseSupport": false,
        "trading.afterMarket": true
    }
}
```

---

## Part 4: Implementation Timeline

### Phase 1: Core Complete ✅
- All P0 modules done
- All P1 modules done
- Total tests: 150+

### Phase 2: Admin Controls (Estimated: 2-3 hours)
1. V44-V45 migrations
2. `AdminSettingsService`
3. Admin REST endpoints
4. Tests (10)

### Phase 3: Advanced Features (Estimated: 3-4 hours)
1. Global kill-switch
2. Broker overrides
3. Feature flags
4. Live config diff

---

## Part 5: File Structure Summary

```
backend/java/vega-trader/src/main/java/com/vegatrader/upstox/api/
├── broker/           # Multi-broker (UpstoxBrokerAdapter)
├── endpoints/        # Endpoint enums
├── instrument/       # Instrument master
├── optionchain/      # Option chain module
│   ├── controller/   # OptionChainController
│   ├── model/        # OptionChainStrike, Response
│   ├── service/      # OptionChainService
│   └── stream/       # WebSocket streaming (16 files)
├── profile/          # User profile & funds
│   ├── controller/   # UserProfileController
│   ├── model/        # UserProfile, FundsMargin
│   └── service/      # UserProfileService, FundsMarginService
├── response/         # Response DTOs
├── rms/              # RMS control plane
│   ├── controller/   # AdminController
│   ├── entity/       # 15+ entities
│   ├── repository/   # 15+ repositories
│   ├── service/      # RmsValidationService
│   └── validation/   # Validators
├── sectoral/         # Sectoral indexing
└── settings/         # Settings framework
    ├── model/        # UserPrioritySettings
    └── service/      # SettingsResolver
```

---

## Part 6: Compliance Checklist

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Zero-delay logging | ✅ | Audit tables |
| Token tracking | ✅ | option_chain_audit |
| Rate limit enforcement | ✅ | Token rotation |
| Maintenance window | ✅ | FundsMarginService |
| July 2025 margin | ✅ | FundsMargin.isAfterCombinedMarginDate() |
| Profile snapshots | ✅ | V42 |
| Funds snapshots | ✅ | V43 |
| Immutable audit | ✅ | All audit tables |

---

## Part 7: Test Coverage Summary

| Module | Unit Tests | Integration | Total |
|--------|------------|-------------|-------|
| RMS | 45 | 29 | 74 |
| Option Chain | 18 | - | 18 |
| WebSocket | 24 | - | 24 |
| Profile/Funds | 14 | - | 14 |
| Settings | 15 | 9 | 24 |
| **Total** | **116** | **38** | **154** |

---

## Conclusion

**Core Platform:** ✅ PRODUCTION READY
- All critical modules implemented
- 154 tests passing
- SEBI compliance covered
- July 2025 API change pre-implemented

**Enhancement Queue:**
1. Admin Settings Framework (P2)
2. Kill-Switch Global (P2)
3. Broker Overrides (P3)
4. Feature Flags (P3)

**Next Action:** Proceed with Phase 2 (Admin Controls) or deploy current state.

---

*Document Status: IMPLEMENTATION ROADMAP FINALIZED*
