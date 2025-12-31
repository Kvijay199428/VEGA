# Enterprise-Grade V2 Login Automation Upgrade

## Background

Upgrade the existing V2 login automation module (`auth/selenium/v2/`) with enterprise-grade features based on documentation. This adds institutional-level resilience, CAPTCHA handling, and audit capabilities.

## Key Enterprise Features to Add

| Feature | Description |
|---------|-------------|
| **CAPTCHA Detection** | Abort immediately, never retry, quarantine token |
| **Quarantine System** | Bad tokens blocked from regeneration |
| **Cooldown Enforcement** | 15-minute wait after failure |
| **Failure Taxonomy** | Classified exceptions (CAPTCHA, TIMEOUT, CREDENTIALS, etc.) |
| **Circuit Breaker** | Resilience4j fail-fast for Selenium |
| **Retry Policy** | Selective retry for network timeouts only |
| **Audit Logging** | Track GENERATED, REFRESHED, FAILED, QUARANTINED events |
| **Scheduled Refresh** | 2:30 AM cron (1 hour before 3:30 AM expiry) |

---

## Proposed Changes

### Exception Classes

#### [NEW] [CaptchaDetectedException.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/exception/CaptchaDetectedException.java)

CAPTCHA detection exception - triggers immediate abort and token quarantine.

```java
public class CaptchaDetectedException extends RuntimeException {
    // Never retry, always quarantine
}
```

---

#### [NEW] [TokenFailureReason.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/exception/TokenFailureReason.java)

Failure taxonomy enum:

```java
public enum TokenFailureReason {
    NETWORK_TIMEOUT,
    INVALID_CREDENTIALS,
    CAPTCHA,
    RATE_LIMIT,
    SELENIUM_DOM_CHANGE,
    BROWSER_CRASH,
    UNKNOWN
}
```

---

#### [NEW] [AuthException.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/exception/AuthException.java)

Base exception with failure classification.

---

### Quarantine & Cooldown

#### [NEW] [TokenStateV2.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/TokenStateV2.java)

Token state tracking for quarantine/cooldown:

```java
public class TokenStateV2 {
    private boolean quarantined;
    private Instant lastFailureAt;
    private TokenFailureReason lastFailureReason;
    private int consecutiveFailures;
    
    public boolean isInCooldown(); // 15-minute check
    public void markFailure(TokenFailureReason reason);
    public void clearQuarantine();
}
```

---

### CAPTCHA Handling

#### [MODIFY] [LoginPageV2.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/LoginPageV2.java)

Add CAPTCHA detection after each page transition:

```java
private void detectCaptcha() {
    List<WebElement> captchaElements = driver.findElements(By.id("captcha"));
    if (!captchaElements.isEmpty()) {
        throw new CaptchaDetectedException("CAPTCHA detected. Manual intervention required.");
    }
    // Also check for Cloudflare turnstile
    if (!driver.findElements(By.className("cf-turnstile")).isEmpty()) {
        throw new CaptchaDetectedException("Cloudflare CAPTCHA detected.");
    }
}
```

---

### Audit Logging

#### [NEW] [TokenAuditService.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/TokenAuditService.java)

Audit event logging:

```java
public class TokenAuditService {
    public void logEvent(String apiName, TokenAuditEvent event, String reason);
}

public enum TokenAuditEvent {
    GENERATED, REFRESHED, FAILED, QUARANTINED, UNQUARANTINED, MANUAL_OVERRIDE
}
```

---

### Failure Classification

#### [MODIFY] [OAuthLoginAutomationV2.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/OAuthLoginAutomationV2.java)

Add failure classification and quarantine logic:

```java
private RuntimeException classifyException(Exception e) {
    if (e instanceof CaptchaDetectedException) return e;
    if (e instanceof TimeoutException) return new NetworkTimeoutException(e);
    if (e instanceof NoSuchElementException) return new SeleniumDomException(e);
    return new UnknownAuthException(e);
}

private void handleFailure(LoginConfigV2 config, Exception e) {
    TokenFailureReason reason = classifyFailureReason(e);
    
    if (reason == TokenFailureReason.CAPTCHA || 
        reason == TokenFailureReason.INVALID_CREDENTIALS) {
        // Quarantine token - never auto-retry
        tokenStateManager.quarantine(config.getApiName(), reason);
    }
    
    auditService.logEvent(config.getApiName(), TokenAuditEvent.FAILED, reason.name());
}
```

---

### Kill Switch & Configuration

#### [NEW] [AuthConfigV2.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/AuthConfigV2.java)

Enterprise configuration with kill switch:

```java
public class AuthConfigV2 {
    private boolean seleniumEnabled = true;      // Kill switch
    private int cooldownMinutes = 15;
    private int maxConsecutiveFailures = 3;
    private boolean screenshotOnFailureOnly = true;
}
```

---

### Scheduled Refresh

#### [NEW] [ScheduledTokenRefresherV2.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/ScheduledTokenRefresherV2.java)

Cron-based token refresh at 2:30 AM (1 hour before expiry):

```java
public class ScheduledTokenRefresherV2 {
    // @Scheduled(cron = "0 30 2 * * *")
    public void refreshAll() {
        for (TokenState token : getAllActive()) {
            if (token.isQuarantined()) continue;
            if (token.isInCooldown()) continue;
            if (!needsRefresh(token)) continue;
            
            // Perform refresh
            OAuthLoginAutomationV2 automation = new OAuthLoginAutomationV2();
            LoginResultV2 result = automation.performLogin(buildConfig(token));
            
            if (result.isSuccess()) {
                auditService.logEvent(token.getApiName(), REFRESHED, "scheduled");
            }
        }
    }
}
```

---

### Enhanced Error Screenshots

#### [MODIFY] [OAuthLoginAutomationV2.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/v2/OAuthLoginAutomationV2.java)

Enhanced screenshot capture with failure context:

```java
private void captureFailureScreenshot(String apiName, TokenFailureReason reason) {
    String filename = String.format("error_%s_%s_%s.png", 
        apiName, reason, Instant.now().toString());
    // Save to logs/screenshots/
}
```

---

## File Summary

| Component | Files |
|-----------|-------|
| **Exceptions** | `CaptchaDetectedException.java`, `TokenFailureReason.java`, `AuthException.java`, `NetworkTimeoutException.java`, `SeleniumDomException.java` |
| **State** | `TokenStateV2.java`, `TokenStateManagerV2.java` |
| **Audit** | `TokenAuditService.java`, `TokenAuditEvent.java` |
| **Config** | `AuthConfigV2.java` |
| **Modified** | `LoginPageV2.java` (CAPTCHA detection), `OAuthLoginAutomationV2.java` (failure handling) |

---

## Verification Plan

### 1. Compilation Test

```bash
cd d:\projects\VEGA TRADER\backend\java\vega-trader
mvn compile -q
```

### 2. CAPTCHA Detection Test

Verify CAPTCHA detection throws correct exception and quarantines token.

### 3. Cooldown Test

Verify tokens in cooldown are skipped during refresh.

### 4. Audit Log Test

Verify events are logged correctly.

---

## CAPTCHA Policy (Critical)

> [!CAUTION]
> **CAPTCHA detected = ABORT IMMEDIATELY**
> - Never retry after CAPTCHA
> - Token is quarantined
> - Manual intervention required
> - Alert triggered

This is the institutional-safe approach per enterprise documentation.
