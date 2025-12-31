Letâ€™s go step-by-step for deploying your Selenium-based Upstox token generator in an enterprise-grade environment.

1. Build the Starter JAR

Assuming your Maven module structure is ready:

cd upstox-auth-spring-boot-starter
mvn clean install


Result:
target/upstox-auth-spring-boot-starter-1.0.0.jar

This JAR can now be included as a dependency in any Spring Boot project.

2. Configure Selenium & ChromeDriver

Download ChromeDriver compatible with your Chrome version.
Place it in a known path, e.g., /opt/chromedriver or C:\chromedriver.exe.

Set system property in SeleniumConfig.java or application.yml:

upstox:
  selenium:
    driver-path: "/opt/chromedriver"
    browser: chrome
    headless: true
    timeout-seconds: 60


SeleniumConfig.java Example:

@Configuration
public class SeleniumConfig {

    @Value("${upstox.selenium.driver-path}")
    private String driverPath;

    @Value("${upstox.selenium.headless}")
    private boolean headless;

    @Bean
    public WebDriver webDriver() {
        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        if(headless) options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        return new ChromeDriver(options);
    }
}

3. Integrate Token Starter in Spring Boot

Add dependency:

<dependency>
    <groupId>com.vegatrader</groupId>
    <artifactId>upstox-auth-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>


Autowire the services:

@Autowired
TokenStorageService tokenStorageService;

@Autowired
ScheduledTokenRefresher tokenRefresher;

@Autowired
RedisTokenLockService lockService;


Fetch token safely:

String primaryToken = tokenStorageService.getToken("PRIMARY")
    .orElseThrow(() -> new RuntimeException("Token not available"))
    .getAccessToken();

4. Enable Scheduled Refreshes

Your ScheduledTokenRefresher already contains:

@Scheduled(cron = "0 30 2 * * *")
public void refreshIfRequired() {
    for (UpstoxTokenEntity token : repository.findAllActive()) {
        if (!lockService.tryLock(token.getApiName())) continue;

        try {
            if (validator.needsRefresh(token)) {
                automation.regenerate(token.getApiName());
            }
        } finally {
            lockService.releaseLock(token.getApiName());
        }
    }
}


âœ… This ensures:

Only one instance refreshes a token

Refresh aligns with 3:30 AM cutoff

Safe in multi-node deployments

5. Handling CAPTCHA / MFA

Selenium detects CAPTCHA via element presence:

WebElement captcha = driver.findElement(By.id("captcha_image"));
if(captcha.isDisplayed()) throw new CaptchaDetectedException();


Flow:

If CAPTCHA detected â†’ pause automation â†’ notify operator

Operator completes CAPTCHA â†’ call manual OTP submission endpoint

Once CAPTCHA cleared â†’ automation resumes token regeneration

Recommended: Expose a REST endpoint:

@PostMapping("/token/manual-captcha")
public ResponseEntity<String> submitCaptcha(@RequestBody CaptchaSubmission submission) {
    automation.submitCaptcha(submission);
    return ResponseEntity.ok("Captcha submitted successfully");
}

6. Redis Lock Safety

Ensure multi-instance safety:

if(!lockService.tryLock(apiName)) {
    log.info("Token regeneration already in progress for {}", apiName);
    return;
}


Lock TTL = 3 minutes (auto-release on crash)

Prevents parallel Selenium launches

7. Audit Logging & Metrics

Track events in upstox_token_audit table:

GENERATED

REFRESHED

QUARANTINED

REVOKED

Increment Prometheus metrics:

Counter.builder("upstox_token_failure_total")
    .tag("api", apiName)
    .tag("type", failureType)
    .register(registry)
    .increment();

8. CI/CD Deployment Gate

Before production deployment:

Run all unit tests (TokenExpiry, TOTP, Validation)

Run integration tests (WireMock simulates Upstox API)

Run Redis concurrency tests (Testcontainers)

Run chaos tests (simulate Selenium failure, CAPTCHA detection, cooldown)

âœ… Build fails if any test fails, enforcing release safety.

9. Production Safety Checklist
Task	Status
TOTP secrets encrypted	âœ…
Selenium headless	âœ…
Redis locks	âœ…
Token quarantine	âœ…
Circuit breakers	âœ…
Cooldown enforcement	âœ…
CAPTCHA detection	âœ…
Audit & metrics	âœ…

This completes the enterprise-grade Java Selenium-based token generator with full production safety, CAPTCHA handling, and Redis-based multi-instance locking.