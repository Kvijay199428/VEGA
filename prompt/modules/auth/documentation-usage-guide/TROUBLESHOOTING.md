# Troubleshooting & FAQ

## 1. Error Taxonomy

### 1.1 Authentication Errors

| Error | Cause | Action |
|-------|-------|--------|
| PIN timeout | Broker throttling | Wait 11 minutes |
| CAPTCHA detected | Anti-automation | Solve manually |
| Invalid credentials | Wrong .env values | Check credentials |
| OTP not received | Mobile issue | Check SMS |

### 1.2 Network Errors

| Error | Cause | Action |
|-------|-------|--------|
| Connection refused | Upstox down | Check status.upstox.com |
| Socket timeout | Network issue | Retry |
| SSL handshake failed | Certificate issue | Update Java/drivers |

### 1.3 Database Errors

| Error | Cause | Action |
|-------|-------|--------|
| Database locked | Concurrent access | Close other sessions |
| No such table | First run | Auto-creates on start |
| Disk full | Storage issue | Free disk space |

### 1.4 Selenium Errors

| Error | Cause | Action |
|-------|-------|--------|
| NoSuchElementException | DOM changed | Update locators |
| WebDriverException | Chrome crashed | Restart test |
| SessionNotCreatedException | Driver mismatch | Update ChromeDriver |

---

## 2. Common Issues & Solutions

### 2.1 "PIN redirect timeout"
```
Problem: Selenium stuck after entering PIN
Cause: Broker session throttling
Solution:
  1. Wait 11 minutes (auto-enforced)
  2. Run ResumeTokenTest
  3. System resumes from failed API
```

### 2.2 "CAPTCHA detected"
```
Problem: Browser shows CAPTCHA
Cause: Too many attempts or suspicious behavior
Solution:
  1. Solve CAPTCHA manually in visible browser
  2. Automation continues automatically
  3. If persistent, wait 30 minutes
```

### 2.3 "Token upsert failed"
```
Problem: Database write failed
Cause: Table doesn't exist or permissions
Solution:
  1. Check database/vega_trade.db exists
  2. Tables auto-create on first run
  3. Check file permissions
```

### 2.4 "No such table: upstox_tokens"
```
Problem: Table not created
Cause: First run or database deleted
Solution:
  # Tables auto-create, but you can force:
  sqlite3 database/vega_trade.db < create_tables.sql
```

### 2.5 "Unable to find CDP implementation"
```
Problem: CDP version mismatch warning
Cause: Chrome/Selenium version mismatch
Solution: Can be safely ignored (non-blocking)
```

---

## 3. Retry Rules

| Scenario | Retry? | Wait Time |
|----------|--------|-----------|
| CAPTCHA | NO | Human solves |
| PIN timeout | YES | 11 minutes |
| Network timeout | YES | Immediate |
| Invalid credentials | NO | Fix .env |
| Profile API 401 | NO | Regenerate token |

---

## 4. Diagnostic Commands

### 4.1 Check Token Status
```bash
sqlite3 database/vega_trade.db "SELECT api_name, is_active, validity_at FROM upstox_tokens"
```

### 4.2 Check Execution State
```bash
sqlite3 database/vega_trade.db "SELECT * FROM token_execution_state ORDER BY updated_at DESC LIMIT 1"
```

### 4.3 Clear Stuck State
```bash
sqlite3 database/vega_trade.db "DELETE FROM token_execution_state WHERE status='RUNNING'"
```

### 4.4 View Recent Logs
```bash
# Token generation
tail -50 logs/auth/token_generation.log

# Cooldowns
cat logs/auth/cooldown_events.log

# Resume flow
cat logs/auth/resume_flow.log
```

---

## 5. FAQ

### Q: Why not auto-refresh tokens?

**A:** Upstox tokens require human authentication (OTP, PIN, CAPTCHA). Auto-refresh would require storing these credentials, which is a security risk and violates broker terms.

---

### Q: Why human gate / CAPTCHA pause?

**A:** Legal compliance. Bypassing CAPTCHA is against Upstox terms of service and potentially illegal.

---

### Q: Why not bypass CAPTCHA with ML/OCR?

**A:** Three reasons:
1. Against Upstox ToS
2. Potentially illegal
3. Cloudflare Turnstile is designed to resist automation

---

### Q: Why use Profile API for validation?

**A:** Profile API is the authoritative way to verify token validity. Database timestamps can be stale if tokens are revoked remotely.

---

### Q: Why 11 minutes cooldown?

**A:** Upstox enforces ~10 minute session throttling. We add 1 minute buffer for safety.

---

### Q: Can I add more than 6 APIs?

**A:** Yes, extend `ApiName` enum and add corresponding `.env` variables. Broker must approve additional API keys.

---

### Q: Why single Selenium instance?

**A:** Multiple concurrent logins with the same credentials cause OTP/PIN conflicts and can trigger account security measures.

---

### Q: Can I run in headless mode?

**A:** Yes, but not recommended. Headless mode cannot handle CAPTCHA requiring human intervention.

---

## 6. Support Escalation

| Issue | First Action | Escalate To |
|-------|--------------|-------------|
| CAPTCHA loop | Wait 30 min | Check IP reputation |
| Account locked | Contact Upstox | support@upstox.com |
| System bug | Check logs | Create GitHub issue |
| Data loss | Check backups | DBA team |

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
