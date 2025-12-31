# VEGA TRADER - Upstox OAuth v2 Enterprise Authentication System

**Official Documentation Guide**

---

## Table of Contents

| # | Document | Description |
|---|----------|-------------|
| 1 | [README.md](./README.md) | This file - Overview & Quick Start |
| 2 | [ARCHITECTURE.md](./ARCHITECTURE.md) | System architecture & design |
| 3 | [AUTHENTICATION_FLOW.md](./AUTHENTICATION_FLOW.md) | Complete OAuth flow |
| 4 | [API_REFERENCE.md](./API_REFERENCE.md) | REST endpoints & contracts |
| 5 | [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) | SQLite tables & schema |
| 6 | [CLI_USAGE.md](./CLI_USAGE.md) | Command-line usage |
| 7 | [FRONTEND_INTEGRATION.md](./FRONTEND_INTEGRATION.md) | React/Vite integration |
| 8 | [OPERATOR_RUNBOOK.md](./OPERATOR_RUNBOOK.md) | Daily operations SOP |
| 9 | [SECURITY.md](./SECURITY.md) | Security considerations |
| 10 | [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) | Error handling & FAQ |

---

## 1. Purpose

This documentation defines the complete OAuth authentication lifecycle for **VEGA Trader**'s Upstox integration:

- ✅ Access token generation (6 API configurations)
- ✅ Token validation & lifecycle management
- ✅ Database persistence (SQLite)
- ✅ CAPTCHA handling (human-in-the-loop)
- ✅ Multi-API orchestration
- ✅ CLI and frontend parity

---

## 2. Target Audience

| Audience | Primary Documents |
|----------|-------------------|
| Backend Engineers | ARCHITECTURE, API_REFERENCE |
| Trading Architects | AUTHENTICATION_FLOW, DATABASE |
| DevOps/Operations | OPERATOR_RUNBOOK, CLI_USAGE |
| Compliance/Audit | SECURITY, AUTHENTICATION_FLOW |
| Frontend Developers | FRONTEND_INTEGRATION, API_REFERENCE |

---

## 3. Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Chrome browser (for Selenium)
- `.env` file with Upstox credentials

### Run Token Generation
```bash
cd backend/java/vega-trader

# Resume-capable (recommended)
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.ResumeTokenTest

# Standard test
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.TokenPersistenceTest
```

### Verify Tokens
```bash
# Check database
sqlite3 database/vega_trade.db "SELECT api_name, is_active, validity_at FROM upstox_tokens"
```

---

## 4. System Capabilities

| Capability | Status |
|------------|--------|
| Selenium-assisted OAuth login | ✅ |
| Zero-delay token reuse | ✅ |
| Multi-API orchestration (6 keys) | ✅ |
| SQLite persistence | ✅ |
| Token audit trail | ✅ |
| 11-minute broker cooldown | ✅ |
| Resume-from-failure | ✅ |
| CLI & frontend parity | ✅ |

---

## 5. API Configuration

| Index | API Name | Purpose |
|-------|----------|---------|
| 0 | PRIMARY | Main trading operations |
| 1 | WEBSOCKET1 | Market data stream 1 |
| 2 | WEBSOCKET2 | Market data stream 2 |
| 3 | WEBSOCKET3 | Market data stream 3 |
| 4 | OPTIONCHAIN1 | Option chain data 1 |
| 5 | OPTIONCHAIN2 | Option chain data 2 |

---

## 6. Design Principles

1. **Compliance First** - No CAPTCHA bypass, no OTP automation abuse
2. **Fail Fast** - No retries on sensitive authentication actions
3. **Human-in-the-Loop** - Operator confirmation for critical steps
4. **Single Source of Truth** - Database is authoritative
5. **Zero Unnecessary Regeneration** - Only regenerate when invalid

---

## 7. File Structure

```
backend/java/vega-trader/
├── database/
│   └── vega_trade.db          # SQLite database
├── logs/auth/
│   ├── token_generation.log   # Token gen events
│   ├── cooldown_events.log    # Broker cooldowns
│   └── resume_flow.log        # Resume operations
└── src/main/java/com/vegatrader/upstox/auth/
    ├── db/                    # Database layer
    ├── service/               # Business logic
    ├── controller/            # REST endpoints
    ├── dto/                   # Data transfer objects
    └── selenium/v2/           # Selenium automation
```

---

## 8. Version History

| Version | Date | Changes |
|---------|------|---------|
| 2.4.0 | 2025-12-29 | Resume-from-failure, 11-min cooldown |
| 2.3.0 | 2025-12-29 | HotTokenRegistry, LoginSuccess page |
| 2.2.0 | 2025-12-29 | Multi-API, single Selenium |
| 2.1.0 | 2025-12-28 | V2 Selenium automation |
| 2.0.0 | 2025-12-27 | Enterprise auth system |

---

**Document Status:** Final  
**Last Updated:** 2025-12-29  
**Maintainer:** VEGA Trader Team
