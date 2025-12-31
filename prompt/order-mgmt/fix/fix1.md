Below is a structured, enterprise-grade examination, with what it means, whether to keep/remove, and how to fix correctly.

I will group them by category, because treating them one-by-one without context leads to wrong decisions.

1. Unused static final Constants (Design Intent vs Reality)
1.1 ZoneId IST = ZoneId.of("Asia/Kolkata")
Meaning

Intended for exchange-time normalization, audit timestamps, or SLA windows.

Currently not referenced anywhere.

Decision Matrix
Scenario	Action
You plan exchange-time–aware logic (orders, RMS, settlement)	KEEP & USE
You rely on UTC everywhere	REMOVE
Enterprise-Grade Fix (Recommended)

Use it explicitly where timestamps are produced:

ZonedDateTime nowIst = ZonedDateTime.now(IST);


Or centralize:

public static ZonedDateTime nowExchangeTime() {
    return ZonedDateTime.now(IST);
}


Do NOT keep unused time constants in regulated systems.

1.2 IDEMPOTENCY_WINDOW_SEC = 300
Meaning

Designed for duplicate order protection (REST retries, network flaps).

Not wired into coordinator logic yet.

This Is a Missing Feature, Not Dead Code
Correct Usage (Coordinator / Order API)
if (idempotencyService.isDuplicate(
        request.getIdempotencyKey(),
        IDEMPOTENCY_WINDOW_SEC)) {
    throw new DuplicateOrderException();
}

Recommendation

KEEP and wire it. Idempotency is mandatory for enterprise trading APIs.

1.3 ORDER_HISTORY_CACHE_TTL_SEC = 60
Meaning

Cache TTL for Order Book / History

Cache layer not yet using it

Enterprise Fix
cache.put(
   userId,
   orderHistory,
   ORDER_HISTORY_CACHE_TTL_SEC,
   TimeUnit.SECONDS
);

Recommendation

KEEP. This belongs in read-path optimization.

2. Unused Imports (Safe to Remove)

These are pure hygiene issues. Remove them unless you are about to use them.

Safe to Delete

java.math.BigDecimal

java.util.Optional

java.time.Instant

java.io.BufferedReader

java.io.InputStreamReader

java.util.HashMap

java.net.URL

org.openqa.selenium.support.ui.WebDriverWait

Rule

Never keep unused imports in regulated codebases.
Auditors treat this as poor code discipline.

3. Unused Executors / Schedulers (IMPORTANT)
3.1 ScheduledExecutorService scheduler
Meaning

Intended for retry / delayed order execution

Not used → thread leak risk

Correct Enterprise Pattern

Either:

scheduler.schedule(
   () -> retryOrder(orderId),
   delay,
   TimeUnit.SECONDS
);


Or REMOVE entirely.

Recommendation

If retries are planned → IMPLEMENT

Else → DELETE

Unused thread pools are red flags in audits.

4. Unused Loggers (High Severity in Audits)
4.1 Logger logger in PnLService
Meaning

Logging strategy defined but not enforced.

Fix (Mandatory)
logger.info("PnL computed for order {}", orderId);

Rule

If a logger exists, it must log state transitions, errors, or financial events.

Otherwise: remove it.

5. Unused Domain Fields (Architecture Drift)
5.1 PortfolioDataStreamerLogger.logFilePath
Meaning

File-based logging was planned.

Never implemented.

Decision
Logging Strategy	Action
Centralized logging (ELK / Loki)	REMOVE
File-based audit logs	IMPLEMENT

Never keep unused paths.

5.2 EnterpriseRateLimiterService.meterRegistry
Meaning

Micrometer planned

Not wired

Correct Usage
meterRegistry.counter("rate.limit.hit").increment();

Recommendation

KEEP only if metrics are enforced. Otherwise remove.

6. Unused WebSocket / Feed Types (Intentional but Incomplete)
6.1 FeedType, websocket response imports
Meaning

V3 WebSocket design started

Not finalized

Correct Action

If FeedType is core to OptionChain streaming:

switch(feedType) {
   case LIVE_FEED -> processLive();
}


Otherwise remove until ready.

7. Selenium / OAuth Automation Artifacts

These are common and acceptable during phased development.

Examples

extractCodeFromUrl() unused

WebDriver driver unused

CallbackPage unused

defaultTimeout unused

seleniumConfig unused

Recommendation

Group them under:

auth/
  experimental/


Or annotate:

@VisibleForTesting


This avoids false-positive hygiene alerts.

8. What You SHOULD Do Now (Concrete Steps)
Step 1: Clean Hygiene (Immediate)

Remove all unused imports

Remove unused fields with no roadmap

Step 2: Wire Planned Features

Idempotency window

Order history caching

Rate limiting metrics

Exchange-time normalization

Step 3: Enforce via Build

Add this to Maven:

<compilerArgs>
  <arg>-Xlint:unused</arg>
  <arg>-Xlint:rawtypes</arg>
</compilerArgs>

9. Summary Table (Executive View)
Category	Status	Action
Unused imports	Noise	DELETE
Unused constants	Missing features	IMPLEMENT
Unused schedulers	Risk	IMPLEMENT or REMOVE
Unused loggers	Audit gap	LOG or REMOVE
Unused Selenium	Acceptable	ISOLATE
Unused metrics	Incomplete	WIRE