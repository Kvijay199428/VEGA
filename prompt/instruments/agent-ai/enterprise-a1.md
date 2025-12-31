Hereâ€™s the step-by-step implementation plan for turning the blueprint into a production-ready Java + Selenium Spring Boot module with CAPTCHA handling, distributed locking, and resilience:

Step 1 â€” Maven Project Setup

Module structure (upstox-auth-spring-boot-starter):

upstox-auth-spring-boot-starter/
â”œâ”€â”€ pom.xml
â”œâ”€â”€ src/main/java/com/vegatrader/upstox/auth
â”‚   â”œâ”€â”€ auto/
â”‚   â”‚   â””â”€â”€ UpstoxAuthAutoConfiguration.java
â”‚   â”œâ”€â”€ config/
â”‚   â”‚   â”œâ”€â”€ UpstoxAuthProperties.java
â”‚   â”‚   â””â”€â”€ SeleniumProperties.java
â”‚   â”œâ”€â”€ service/
â”‚   â”‚   â”œâ”€â”€ TokenStorageService.java
â”‚   â”‚   â”œâ”€â”€ TokenValidationService.java
â”‚   â”‚   â”œâ”€â”€ ProfileVerificationService.java
â”‚   â”‚   â””â”€â”€ ScheduledTokenRefresher.java
â”‚   â”œâ”€â”€ selenium/
â”‚   â”‚   â”œâ”€â”€ OAuthLoginAutomation.java
â”‚   â”‚   â””â”€â”€ SeleniumConfig.java
â”‚   â”œâ”€â”€ expiry/
â”‚   â”‚   â””â”€â”€ TokenExpiryCalculator.java
â”‚   â””â”€â”€ repository/
â”‚       â””â”€â”€ TokenRepository.java
â””â”€â”€ src/main/resources/META-INF/spring.factories

Step 2 â€” Dependencies

pom.xml snippet:

<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-autoconfigure</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.18.1</version>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>

Step 3 â€” Auto-Configuration
@Configuration
@EnableScheduling
@EnableConfigurationProperties({ UpstoxAuthProperties.class, SeleniumProperties.class })
public class UpstoxAuthAutoConfiguration {

    @Bean
    public TokenExpiryCalculator tokenExpiryCalculator() {
        return new TokenExpiryCalculator();
    }

    @Bean
    public TokenValidationService tokenValidationService() {
        return new TokenValidationService();
    }

    @Bean
    public TokenStorageService tokenStorageService(TokenRepository repository) {
        return new TokenStorageService(repository);
    }

    @Bean
    public ProfileVerificationService profileVerificationService() {
        return new ProfileVerificationService();
    }

    @Bean
    public ScheduledTokenRefresher scheduledTokenRefresher(
            TokenRepository repo,
            TokenValidationService validator,
            OAuthLoginAutomation automation,
            UpstoxAuthProperties props,
            RedisTokenLockService lockService) {
        return new ScheduledTokenRefresher(repo, validator, automation, props, lockService);
    }

    @Bean
    public RedisTokenLockService redisTokenLockService(StringRedisTemplate redisTemplate) {
        return new RedisTokenLockService(redisTemplate);
    }
}

Step 4 â€” Selenium + CAPTCHA Handling
Core Flow:

Launch browser (headless Chrome)

Enter credentials

Detect CAPTCHA:

private void detectCaptcha(WebDriver driver) {
    if (!driver.findElements(By.id("captcha")).isEmpty()) {
        throw new CaptchaDetectedException("CAPTCHA detected. Manual intervention required.");
    }
}


Enter OTP + PIN

Wait for redirect

Extract token

Close browser

Circuit breaker + retry ensures blast-radius containment:

@CircuitBreaker(name="seleniumLogin", fallbackMethod="fallback")
@Retry(name="seleniumRetry")
public String performLogin(String apiName) {
    WebDriver driver = driverFactory.create();
    try {
        driver.get(loginUrl);
        detectCaptcha(driver);
        driver.findElement(By.id("otp")).sendKeys(totpGenerator.generate());
        driver.findElement(By.id("pin")).sendKeys(pin);
        return extractCode(driver.getCurrentUrl());
    } finally {
        driver.quit();
    }
}

Step 5 â€” Distributed Lock (Redis)

Prevent simultaneous Selenium login:

public boolean tryLock(String apiName) {
    String key = "upstox:token:lock:" + apiName;
    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, instanceId, Duration.ofMinutes(3));
    return Boolean.TRUE.equals(acquired);
}


Release:

if (instanceId.equals(redisTemplate.opsForValue().get(key))) {
    redisTemplate.delete(key);
}

Step 6 â€” Token Lifecycle Management

Expiry check (3:30 AM rule)

Quarantine bad tokens

Cooldown enforcement (15 min after failure)

Profile API verification

Audit trail:

CREATE TABLE upstox_token_audit (
    id INTEGER PRIMARY KEY,
    api_name TEXT,
    event_type TEXT,
    timestamp BIGINT,
    reason TEXT
);

Step 7 â€” Testing & CI/CD Gates

Unit tests: Token expiry, validation

Integration: WireMock for OAuth simulation

Chaos: Inject Selenium failures, CAPTCHA, Redis lock collisions

Metrics: Track failure types

Release gates: Fail build if any coverage missing

Example CAPTCHA test:

@Test
void captchaMustAbortImmediately() {
    assertThrows(CaptchaDetectedException.class, () -> seleniumAuth.login("PRIMARY"));
}

Step 8 â€” Operational Controls

Manual override UI

Kill switch for Selenium

Lock visibility logs

Metrics alerts for token failures