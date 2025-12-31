# Security Considerations

## 1. Data Protection

### 1.1 What Is NEVER Logged

| Data | Why |
|------|-----|
| Access Token | Bearer token exposure |
| OTP/TOTP code | One-time secret |
| PIN | User credential |
| Client Secret | API credential |

**Code Enforcement:**
```java
// Tokens are masked in logs
logger.info("Token obtained (length: {})", token.length());
// NOT: logger.info("Token: {}", token);
```

---

### 1.2 What IS Logged

| Data | Purpose |
|------|---------|
| API Name | Operation context |
| User ID | Audit trail |
| Timestamps | Debugging |
| Success/Failure | Monitoring |

---

## 2. Storage Security

### 2.1 SQLite File Permissions

```bash
# Set restrictive permissions (Unix/Mac)
chmod 600 database/vega_trade.db

# Verify
ls -la database/vega_trade.db
# -rw------- 1 user user 12288 Dec 29 21:10 vega_trade.db
```

### 2.2 .env File Security

```bash
# Must not be committed to Git
echo ".env" >> .gitignore

# Set permissions
chmod 600 .env
```

---

## 3. Credential Handling

### 3.1 Environment Variables (Recommended)

```properties
# .env file
UPSTOX_CLIENT_SECRET_0=your_secret_here
```

**Never hardcode secrets in source code.**

### 3.2 Optional: Encrypted Storage

For production environments, consider:
- AWS Secrets Manager
- HashiCorp Vault
- Azure Key Vault

---

## 4. Selenium Security

### 4.1 Single Instance Policy

Only ONE Selenium browser runs at a time.

**Why:**
- Prevents session conflicts
- Avoids OTP race conditions
- Broker-safe

### 4.2 Browser Cleanup

```java
// Always close browser after use
finally {
    driver.quit();
}
```

### 4.3 No Credentials in URLs

```java
// Credentials are typed, not passed in URL
driver.findElement(By.id("pinCode")).sendKeys(pin);
// NOT: driver.get("https://...?pin=" + pin);
```

---

## 5. Network Security

### 5.1 HTTPS Only

All Upstox API calls use HTTPS:
```
https://api.upstox.com/v2/...
```

### 5.2 Redirect URI Validation

```java
// Redirect only to known URI
if (!currentUrl.startsWith(redirectUri)) {
    throw new SecurityException("Invalid redirect");
}
```

---

## 6. Audit Trail

### 6.1 Token Audit Log

```
[AUDIT] 2025-12-29 21:10:15 | API: PRIMARY | Event: GENERATED | userId=7EAHBJ
[AUDIT] 2025-12-29 21:10:15 | API: PRIMARY | Event: VERIFIED | status=SUCCESS
[AUDIT] 2025-12-29 21:10:15 | API: PRIMARY | Event: PERSISTED | db=SUCCESS
```

### 6.2 Database Audit Columns

```sql
generated_at TEXT  -- When token was created
updated_at INTEGER -- Last modification
```

---

## 7. Compliance Requirements

### 7.1 No CAPTCHA Bypass

- System pauses for human CAPTCHA solving
- No OCR or ML-based bypass
- Maintains broker trust

### 7.2 No OTP/PIN Abuse

- Maximum 1 login attempt per API
- 11-minute cooldown on failure
- No rapid-fire retries

### 7.3 Single Selenium Instance

- Prevents session flooding
- Respects broker rate limits
- Human-in-the-loop design

---

## 8. Recommended Hardening

| Level | Action | Priority |
|-------|--------|----------|
| Basic | File permissions 600 | ✅ Done |
| Basic | .gitignore .env | ✅ Done |
| Medium | Log rotation | ✅ Done |
| Medium | Database backups | Recommended |
| Advanced | Secret manager | Optional |
| Advanced | Token encryption at rest | Optional |

---

## 9. Security Checklist

- [ ] `.env` not in Git
- [ ] Database permissions set to 600
- [ ] No tokens in logs
- [ ] Single Selenium instance enforced
- [ ] HTTPS for all API calls
- [ ] Audit trail enabled
- [ ] Backup strategy in place

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
