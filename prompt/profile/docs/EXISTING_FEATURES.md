# User Profile & Funds/Margin Module - Existing Documentation

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## 1. Existing Components Overview

### 1.1 Endpoint Definitions

| Enum | Endpoint | Description |
|------|----------|-------------|
| `UserProfileEndpoints.USER_PROFILE` | `GET /user/profile` | User profile (name, email, exchanges, products) |
| `UserProfileEndpoints.USER_FUNDS` | `GET /user/get-funds-and-active-orders` | Funds and margin details |

### 1.2 Response DTOs

#### UserProfileResponse

| Field | Type | Description |
|-------|------|-------------|
| `email` | String | User email |
| `mobile` | String | Mobile number |
| `name` | String | Full name |
| `pan` | String | PAN (masked) |
| `userId` | String | Upstox client ID |
| `brokerName` | String | "UPSTOX" |
| `exchanges` | List<String> | NSE, BSE, NFO enabled |
| `products` | List<String> | I, D, CO enabled |
| `poa` | Boolean | Power of Attorney status |
| `isActive` | Boolean | Account active status |

#### FundsResponse

| Field | Type | Description |
|-------|------|-------------|
| `equity` | FundSegment | Equity segment funds |
| `commodity` | FundSegment | Commodity segment funds |
| `activeOrdersCount` | Integer | Active orders |

#### FundSegment

| Field | Type | Description |
|-------|------|-------------|
| `availableBalance` | Double | Available margin |
| `usedBalance` | Double | Used margin |
| `balance` | Double | Total balance |

### 1.3 RMS Integration

#### MarginProfile (record)

| Field | Type | Description |
|-------|------|-------------|
| `intradayMarginPct` | double | Margin percentage |
| `intradayLeverage` | double | Leverage multiplier |
| `requiredMargin` | double | Calculated margin |

---

## 2. Missing Features (Per a1.md)

| Feature | Status | Priority |
|---------|--------|----------|
| UserProfileService (with caching) | ❌ Missing | P0 |
| FundsMarginService (with caching) | ❌ Missing | P0 |
| Profile snapshot persistence | ❌ Missing | P1 |
| Funds snapshot persistence | ❌ Missing | P1 |
| Maintenance window guard | ❌ Missing | P0 |
| Combined equity margin (July 2025) | ❌ Missing | P0 |
| Profile-driven eligibility gating | ❌ Missing | P1 |
| REST controller for frontend | ❌ Missing | P1 |

---

## 3. July 2025 API Change Alert

**Effective Date:** July 19, 2025

### Before
```json
{
  "equity": { "available_balance": 10000 },
  "commodity": { "available_balance": 5000 }
}
```

### After
```json
{
  "equity": { "available_balance": 15000 },  // Combined
  "commodity": { "available_balance": 0 }     // Always zero
}
```

**Action Required:**
- All margin logic must use `data.equity` only
- Commodity object becomes dummy (zeros)
- Enforce in code, not just docs

---

## 4. Implementation Checklist

### Phase 1: Core Services
- [ ] V42 - user_profile_snapshot table
- [ ] V43 - funds_margin_snapshot table
- [ ] UserProfile domain record
- [ ] FundsMargin domain record
- [ ] UserProfileService (fetch + cache)
- [ ] FundsMarginService (fetch + cache + maintenance guard)

### Phase 2: RMS Integration
- [ ] Profile-driven eligibility checks
- [ ] Margin sufficiency validation
- [ ] Rejection codes (RMS-P-*, RMS-M-*)

### Phase 3: REST API
- [ ] UserProfileController
- [ ] GET /api/user/profile
- [ ] GET /api/user/funds

---

*Document Status: EXISTING FEATURE ANALYSIS*
