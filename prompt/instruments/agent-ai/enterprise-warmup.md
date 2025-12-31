Enterpriseâ€‘Grade Java Selenium Access Token Generator

(OAuth UI Automation with Institutional Hardening)

1. Scope & Design Intent

This system exists to safely automate OAuth token acquisition where:

No programmatic refresh token exists

UI login is mandatory

Tokens have nonâ€‘standard expiry

Broker rate limits and antiâ€‘automation controls apply

Nonâ€‘Goals

Bypassing CAPTCHA

Circumventing broker security

Running uncontrolled retries

2. Highâ€‘Level Architecture
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚ Scheduler   â”‚â”€â”€â”€â”€â”€â”€â”
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜      â”‚
                     â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚ TokenOrchestrator        â”‚
â”‚  - expiry check          â”‚
â”‚  - quarantine enforcementâ”‚
â”‚  - cooldown enforcement  â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
             â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚ SeleniumAuthService      â”‚â—„â”€â”€â”€â”€ Circuit Breaker
â”‚  - UI automation         â”‚
â”‚  - CAPTCHA detection    â”‚
â”‚  - OTP + PIN             â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
             â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚ TokenVerifier            â”‚
â”‚  - Profile API           â”‚
â”‚  - Permission validation â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
             â–¼
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚ TokenRepository          â”‚
â”‚  - encrypted secrets     â”‚
â”‚  - audit trail           â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜

3. Core Domain Model
Token Entity
@Entity
class UpstoxTokenEntity {

    @Id
    private String apiName;

    private String accessToken;
    private Instant expiresAt;

    private boolean active;
    private boolean quarantined;

    private Instant lastFailureAt;
    private Instant lastValidatedAt;

    @Enumerated(EnumType.STRING)
    private TokenFailureReason lastFailureReason;
}

4. Expiry Logic (03:30 AM Rule)
public Instant calculateExpiry(Instant issuedAt) {
    ZonedDateTime ist = issuedAt.atZone(ZoneId.of("Asia/Kolkata"));
    ZonedDateTime cutoff = ist.toLocalDate()
        .atTime(3, 30)
        .atZone(ZoneId.of("Asia/Kolkata"));

    if (ist.isAfter(cutoff)) {
        cutoff = cutoff.plusDays(1);
    }
    return cutoff.toInstant();
}


This logic is mandatory.

5. Selenium Login Flow (Authoritative)
Flow Sequence

Open login URL

Enter client ID

Submit

Enter OTP (TOTP generated internally)

Enter PIN

Wait for redirect

Extract code

Close browser

Selenium Implementation (Hardened)
@CircuitBreaker(name = "seleniumLogin", fallbackMethod = "fallback")
@Retry(name = "seleniumRetry")
public String performLogin(String apiName) {

    WebDriver driver = driverFactory.create();

    try {
        driver.get(loginUrl);

        driver.findElement(By.id("client_id")).sendKeys(clientId);
        driver.findElement(By.id("submit")).click();

        detectCaptcha(driver);

        driver.findElement(By.id("otp"))
            .sendKeys(totpGenerator.generate());

        driver.findElement(By.id("pin"))
            .sendKeys(pin);

        waitForRedirect(driver);

        return extractCode(driver.getCurrentUrl());

    } catch (Exception e) {
        captureScreenshot(driver);
        throw classify(e);
    } finally {
        driver.quit();
    }
}

6. CAPTCHA Handling (Critical, Nonâ€‘Bypass)
Detection
private void detectCaptcha(WebDriver driver) {
    if (!driver.findElements(By.id("captcha")).isEmpty()) {
        throw new CaptchaDetectedException();
    }
}

Policy (Strict)
CAPTCHA Event	Action
Detected	Abort immediately
Retry	Never
Token	Quarantined
Alert	Mandatory
Human	Required
Why This Is Correct

Brokers fingerprint CAPTCHA resolution

Attempted bypass escalates enforcement

Manual intervention preserves account safety

7. Failure Taxonomy
enum TokenFailureReason {
    NETWORK_TIMEOUT,
    INVALID_CREDENTIALS,
    CAPTCHA,
    RATE_LIMIT,
    SELENIUM_DOM_CHANGE,
    BROWSER_CRASH,
    UNKNOWN
}

Classification Example
private RuntimeException classify(Exception e) {
    if (e instanceof TimeoutException) return new NetworkTimeoutException();
    if (e instanceof CaptchaDetectedException) return e;
    if (e instanceof NoSuchElementException) return new SeleniumDomException();
    return new UnknownAuthException(e);
}

8. Circuit Breaker & Retry (Resilience4j)
Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      seleniumLogin:
        failureRateThreshold: 40
        waitDurationInOpenState: 2m

Retry (Selective)
resilience4j:
  retry:
    instances:
      seleniumRetry:
        maxAttempts: 2
        retryExceptions:
          - java.net.SocketTimeoutException


Hard Rule: Retry NEVER bypasses circuit breaker.

9. Token Quarantine & Cooldown
Quarantine
if (ex instanceof InvalidCredentialsException
 || ex instanceof CaptchaDetectedException) {
    token.setQuarantined(true);
}

Cooldown
if (token.getLastFailureAt()
        .isAfter(Instant.now().minus(15, MINUTES))) {
    throw new CooldownActiveException();
}

10. Token Verification (Profile API)
public boolean isTokenValid(UpstoxTokenEntity token) {
    ResponseEntity<?> r = profileClient.call(token.getAccessToken());
    return r.getStatusCode().is2xxSuccessful();
}


Run every 5 minutes.

11. Security Hardening
Mandatory

AESâ€‘256 encrypt totpSecret

Mask tokens in logs

No PIN persistence

Screenshot only on failure

Encrypted DB fields

12. Testing & Chaos Engineering
Mandatory Tests
Area	Tool
Expiry	JUnit
Auth API	WireMock
Circuit Breaker	Resilience4j
Redis Lock	Testcontainers
Selenium Chaos	Mocks
CAPTCHA	Exception test
Metrics	SimpleMeterRegistry
CAPTCHA Test
@Test
void captchaMustAbortImmediately() {
    assertThrows(CaptchaDetectedException.class,
        () -> seleniumAuth.login("PRIMARY"));
}

13. CI/CD Release Gates

Build MUST FAIL if:

CAPTCHA path untested

Circuit breaker coverage < 100%

Quarantine logic untested

Metrics assertions missing

Redis contention untested

14. Operational Playbook
When CAPTCHA Appears

Token quarantined

Alert triggered

Ops logs in manually

Token pasted via admin UI

Selenium disabled temporarily

Kill Switch
auth:
  selenium:
    enabled: false

15. Final System Guarantees
Property	Status
No infinite retries	Enforced
CAPTCHA safe	Enforced
Brokerâ€‘safe	Enforced
Deterministic expiry	Enforced
Human override	Available
Releaseâ€‘certifiable	Yes
Final Verdict

This guide represents how institutional brokers automate OAuth safely.