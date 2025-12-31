# CLI Usage Guide

## 1. Prerequisites

```bash
# Required
- Java 21+
- Maven 3.9+
- Chrome browser (for Selenium)
- .env file configured

# Directory
cd backend/java/vega-trader
```

---

## 2. Available Commands

### 2.1 Resume Token Test (Recommended)

Continues from last failure, handles 11-minute cooldowns.

```bash
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.ResumeTokenTest
```

**Output:**
```
╔═══════════════════════════════════════════════════════════════╗
║        RESUME TOKEN TEST - STARTED                            ║
╚═══════════════════════════════════════════════════════════════╝

Found resumable state: COOLDOWN
Cooldown remaining: 3 min 45 sec

<!-- After cooldown -->

═══════════════════════════════════════════════════════
        RESUME ORCHESTRATOR - STARTED
═══════════════════════════════════════════════════════
Generating 4 tokens: [WEBSOCKET2, WEBSOCKET3, OPTIONCHAIN1, OPTIONCHAIN2]
→ Generating: WEBSOCKET2
✓ Generated: WEBSOCKET2
...
```

---

### 2.2 Standard Token Test

Simple test without resume capability.

```bash
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.TokenPersistenceTest
```

---

### 2.3 With Logging to File

```bash
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.ResumeTokenTest 2>&1 | `
  Tee-Object -FilePath "prompt/modules/auth/test-log/token_test_$timestamp.log"
```

---

## 3. Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success (all tokens valid or generated) |
| 1 | Partial success (some tokens failed) |
| 2 | CAPTCHA required (human intervention) |
| 3 | Fatal failure |

---

## 4. Environment Variables

Required in `.env` file:

```properties
# Core Credentials
UPSTOX_MOBILE_NUMBER=9XXXXXXXXX
UPSTOX_TOTP=BASE32_SECRET_KEY
UPSTOX_PIN=123456
UPSTOX_REDIRECT_URI=http://localhost:28020/api/v1/auth/upstox/callback

# API Config 0 (PRIMARY)
UPSTOX_CLIENT_ID_0=4871f658-a594-47b8-8176-d0d77578ef02
UPSTOX_CLIENT_SECRET_0=xxxxxxxxx

# API Config 1 (WEBSOCKET1)
UPSTOX_CLIENT_ID_1=9aff3af3-325f-41a2-85d9-30ea2fdf2106
UPSTOX_CLIENT_SECRET_1=xxxxxxxxx

# API Config 2 (WEBSOCKET2)
UPSTOX_CLIENT_ID_2=17bb10d9-3f61-4cd8-8aa4-0b0ee82448f8
UPSTOX_CLIENT_SECRET_2=xxxxxxxxx

# API Config 3 (WEBSOCKET3)
UPSTOX_CLIENT_ID_3=a24c2f20-f3aa-4e87-94ef-0a7a1b3f8a21
UPSTOX_CLIENT_SECRET_3=xxxxxxxxx

# API Config 4 (OPTIONCHAIN1)
UPSTOX_CLIENT_ID_4=04362a8f-f775-4a9e-a18b-67308f0794fb
UPSTOX_CLIENT_SECRET_4=xxxxxxxxx

# API Config 5 (OPTIONCHAIN2)
UPSTOX_CLIENT_ID_5=b5c17d91-e5c4-4a9b-8c67-12345abcdef0
UPSTOX_CLIENT_SECRET_5=xxxxxxxxx
```

---

## 5. Headless vs Visible Mode

### Visible Mode (Default)
Browser window appears for operator verification.

```java
// In SeleniumConfigV2
headless = false  // Default
```

### Headless Mode
For CI/CD or automated environments (use with caution).

```bash
# Set environment variable
UPSTOX_HEADLESS=true mvn exec:java ...
```

---

## 6. Database Inspection

```bash
# View all tokens
sqlite3 database/vega_trade.db "SELECT api_name, is_active, validity_at FROM upstox_tokens"

# View execution state
sqlite3 database/vega_trade.db "SELECT * FROM token_execution_state ORDER BY updated_at DESC LIMIT 1"

# Count active tokens
sqlite3 database/vega_trade.db "SELECT COUNT(*) as active FROM upstox_tokens WHERE is_active=1"
```

---

## 7. Log Files

```
logs/auth/
├── token_generation.log   # Token generation events
├── cooldown_events.log    # Broker cooldown triggers
└── resume_flow.log        # Resume operations
```

---

## 8. Troubleshooting

### PIN Timeout Error
```
Symptom: "Timeout waiting for PIN input"
Cause: Broker throttling
Action: Wait 11 minutes, then run ResumeTokenTest
```

### CAPTCHA Detected
```
Symptom: "CAPTCHA detected - manual intervention required"
Action: Solve CAPTCHA manually in browser window
```

### Token Upsert Failed
```
Symptom: "Failed to upsert token"
Cause: Database locked or permission issue
Action: Check database/vega_trade.db permissions
```

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
