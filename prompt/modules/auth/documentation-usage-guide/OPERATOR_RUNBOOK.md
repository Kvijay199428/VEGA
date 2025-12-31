# Operator Runbook

## 1. Daily Operations

### 1.1 Morning Token Check (Before 9:00 AM IST)

```bash
# 1. Check token status
sqlite3 database/vega_trade.db "SELECT api_name, is_active, validity_at FROM upstox_tokens"

# 2. Run token generation if needed
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.ResumeTokenTest
```

**Expected Output:**
```
  PRIMARY         : ✓ VALID
  WEBSOCKET1      : ✓ VALID
  WEBSOCKET2      : ✓ VALID
  WEBSOCKET3      : ✓ VALID
  OPTIONCHAIN1    : ✓ VALID
  OPTIONCHAIN2    : ✓ VALID
```

---

### 1.2 Token Regeneration Window

| Time (IST) | Status |
|------------|--------|
| 03:30 AM | Tokens expire |
| 03:30 - 09:00 AM | Regeneration window |
| 09:00 AM | Market opens |

**⚠️ Always regenerate before market opens.**

---

## 2. Common Scenarios

### 2.1 All Tokens Valid
```
╔═══════════════════════════════════════════════════════════════╗
║       FAST EXIT: All 6 tokens valid. No regeneration.        ║
╚═══════════════════════════════════════════════════════════════╝
```
**Action:** None required.

---

### 2.2 Some Tokens Invalid
```
Generating 3 tokens: [WEBSOCKET2, WEBSOCKET3, OPTIONCHAIN1]
```
**Action:** Monitor browser window, wait for completion.

---

### 2.3 Cooldown Triggered
```
╔═══════════════════════════════════════════════════════════════╗
║              BROKER COOLDOWN ACTIVE                           ║
╚═══════════════════════════════════════════════════════════════╝
Last failed API: WEBSOCKET2
Cooldown until: 2025-12-29 21:39:00
```
**Action:** Wait 11 minutes. System will auto-resume.

| ❌ DO NOT | ✅ DO |
|-----------|-------|
| Restart application | Wait patiently |
| Retry manually | Monitor logs |
| Change credentials | Check cooldown_events.log |

---

### 2.4 CAPTCHA Detected
```
⚠ CAPTCHA detected - manual intervention required
```
**Action:** Solve CAPTCHA in visible browser window.

---

## 3. Log Files to Monitor

| Log File | Contents |
|----------|----------|
| `logs/auth/token_generation.log` | Token gen events |
| `logs/auth/cooldown_events.log` | Broker cooldowns |
| `logs/auth/resume_flow.log` | Resume operations |
| `logs/vega-trader.log` | General application logs |

### 3.1 Tail Logs in Real-time
```bash
# Windows PowerShell
Get-Content logs/auth/token_generation.log -Tail 50 -Wait

# Linux/Mac
tail -f logs/auth/token_generation.log
```

---

## 4. Incident Response

### 4.1 CAPTCHA Loop (Multiple CAPTCHAs)
```
Symptom: CAPTCHA appears repeatedly
Cause: IP flagged by Upstox
Action:
  1. Stop automation
  2. Wait 30 minutes
  3. Consider VPN or different network
  4. Retry
```

### 4.2 Token Invalidation (Mid-Day)
```
Symptom: 401 errors during trading
Cause: Token revoked by Upstox
Action:
  1. Check Upstox app for security alerts
  2. Regenerate affected token only
  3. Update trading systems
```

### 4.3 Broker Downtime
```
Symptom: Auth URL not loading
Cause: Upstox maintenance
Action:
  1. Check https://status.upstox.com
  2. Wait for recovery
  3. Retry after maintenance window
```

### 4.4 Database Locked
```
Symptom: "database is locked"
Cause: Concurrent access
Action:
  1. Stop all processes
  2. Check for sqlite3 shell sessions
  3. Retry
```

---

## 5. Recovery Procedures

### 5.1 Resume After Failure
```bash
# System auto-resumes if state was saved
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.ResumeTokenTest
```

### 5.2 Force Fresh Start
```bash
# Clear execution state
sqlite3 database/vega_trade.db "DELETE FROM token_execution_state"

# Regenerate all
mvn exec:java -Dexec.mainClass=com.vegatrader.upstox.auth.service.TokenPersistenceTest
```

### 5.3 Database Recovery
```bash
# Restore from backup
cp database/backup_20251229.db database/vega_trade.db
```

---

## 6. Monitoring Checklist

| Item | Frequency | Command |
|------|-----------|---------|
| Token count | Daily | `SELECT COUNT(*) FROM upstox_tokens WHERE is_active=1` |
| Expiry times | Daily | `SELECT api_name, validity_at FROM upstox_tokens` |
| Cooldown events | After failure | `cat logs/auth/cooldown_events.log` |
| Disk space | Weekly | `du -sh database/` |

---

## 7. Emergency Contacts

| Issue | Contact |
|-------|---------|
| Upstox API issues | support@upstox.com |
| System bugs | [Create GitHub Issue] |
| Urgent trading issues | [Internal escalation] |

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
