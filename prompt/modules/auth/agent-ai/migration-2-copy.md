Below is a structured, upgrade-focused guide that maps your existing Selenium-based login to the current Upstox OAuth + Token Request architecture, without breaking your enterprise hardening (a1–a6).
This is written as a migration guide, not generic documentation.

Upstox Login Process Upgrade Guide

From Legacy Selenium Login → OAuth Authorization + Token Request (v2 / v3)

This guide assumes:

You already have a production-grade Selenium automation

You manage 6 API keys (PRIMARY, WS1–3, OC1–2)

You already enforce captcha pause, Redis locks, expiry logic, audit, resilience

You want to align strictly with official Upstox flows, not fight them

1. What Actually Changed (Critical Understanding)
Old Mental Model (Legacy Selenium)
Selenium Login
 → OTP
 → PIN
 → Access Token scraped or intercepted

New Official Model (Correct)
Authorize (browser redirect)
 → User login + CAPTCHA + OTP + PIN (Upstox-controlled)
 → Redirect with authorization_code
 → POST /v2/login/authorization/token
 → Access token + profile


OR (Initiator Flow – v3)

POST /v3/login/auth/token/request/{client_id}
 → User approves in app / WhatsApp
 → Token delivered via webhook


Key Point:
👉 Selenium is now only for STEP 1 (Authorize)
👉 Token generation is API-only

2. Two Supported Login Paths (You Must Support Both)
Flow	When to Use	Selenium Needed
OAuth Authorization Code (v2)	Fully automated ops	✅ Yes
Access Token Request + Webhook (v3)	Institutional / fallback	❌ No

Your system should prefer v2, fallback to v3.

3. Updated High-Level Architecture
┌──────────────┐
│ Scheduler /  │
│ Manual UI    │
└──────┬───────┘
       │
       ▼
┌──────────────────────┐
│ Token Orchestrator   │
│ (Redis lock enforced)│
└──────┬───────────────┘
       │
       ├── v2 OAuth Flow (Preferred)
       │     ├─ Selenium → /authorize
       │     ├─ CAPTCHA pause
       │     ├─ Capture ?code=
       │     └─ POST /v2/token
       │
       └── v3 Token Request (Fallback)
             ├─ POST /v3/token/request
             ├─ User approves in app
             └─ Webhook receives token

4. OAuth Authorization (v2) — How to Upgrade Selenium
4.1 Authorization URL (Per API Key)
GET https://api.upstox.com/v2/login/authorization/dialog
    ?client_id=UPSTOX_CLIENT_ID_N
    &redirect_uri=UPSTOX_REDIRECT_URI
    &response_type=code
    &state=random_csrf_token


Rules

redirect_uri MUST match app config

state MUST be generated + validated

QR login is NOT supported → Selenium must use mobile + OTP

4.2 Selenium Responsibilities (Reduced Scope)

Selenium now does ONLY THIS:

Open authorize URL

Detect CAPTCHA → pause

Enter mobile number

Enter OTP

Enter PIN

WAIT for redirect

CAPTURE code from redirect URL

EXIT

❌ Selenium must NOT:

Call token APIs

Store tokens

Retry endlessly

4.3 Capturing Authorization Code (Exact Java)
public String waitForAuthCode(WebDriver driver) {

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));

    wait.until(d -> d.getCurrentUrl().startsWith(REDIRECT_URI));

    URI uri = URI.create(driver.getCurrentUrl());
    Map<String, String> params =
        Arrays.stream(uri.getQuery().split("&"))
              .map(s -> s.split("="))
              .collect(Collectors.toMap(a -> a[0], a -> a[1]));

    String stateReturned = params.get("state");
    validateState(stateReturned);

    return params.get("code");
}


Hard rule:
If code is not received → ABORT (do not retry).

5. Token Exchange (v2) — API Only

Once code is obtained:

POST https://api.upstox.com/v2/login/authorization/token
Content-Type: application/x-www-form-urlencoded

code=AUTH_CODE
client_id=CLIENT_ID
client_secret=CLIENT_SECRET
redirect_uri=REDIRECT_URI
grant_type=authorization_code

Important Rules

code is single-use

Token always expires at 03:30 AM

Response already includes profile data

Store access_token and extended_token separately

6. Handling 6 API Keys (Your .env Design Is Correct)
Mapping Strategy
API Index	Purpose	Flow
0	PRIMARY	OAuth v2
1–3	WEBSOCKET	OAuth v2
4–5	OPTIONCHAIN	OAuth v2
Loop Design (Pseudo)
for (ApiConfig api : apiConfigs) {
    withRedisLock(api.name(), () -> {
        String code = seleniumAuthorize(api);
        TokenResponse token = exchangeToken(api, code);
        validateProfile(token);
        storeToken(api.name(), token);
    });
}


Never parallelize Selenium.

7. CAPTCHA Handling (Still Required)

Upstox uses Cloudflare → CAPTCHA will happen.

Correct Behavior

Detect CAPTCHA

Pause automation

Operator solves it

Resume

Anything else will get IP-blocked.

You already implemented this correctly — no change required.

8. v3 Access Token Request (Fallback / Institutional Mode)

This flow does NOT require Selenium.

When to Use

CAPTCHA loops

Broker enforcement

Scheduled overnight approval

Compliance-heavy environments

8.1 Initiate Request
POST /v3/login/auth/token/request/{client_id}
{
  "client_secret": "..."
}


Result:

User notified in Upstox app + WhatsApp

Approval window valid until 03:30 AM

8.2 Receive Token via Webhook

Webhook payload:

{
  "client_id": "...",
  "user_id": "...",
  "access_token": "...",
  "expires_at": "...",
  "message_type": "access_token"
}

Webhook Rules

Verify signature (if enabled)

Match client_id

Store token

Mark API key ACTIVE

9. Logout API (Optional but Recommended)

Before regenerating tokens aggressively:

DELETE /v2/logout
Authorization: Bearer <old_token>


Use this when:

Rotating credentials

Recovering from corruption

Manual operator intervention

10. Updated SOP (Operational)
Automated Night Refresh (02:30)

Redis lock

Check expiry

OAuth authorize (Selenium)

CAPTCHA pause

Capture code

Token API

Profile verify

Audit log

Close browser

Manual / Fallback

Initiate v3 token request

User approves in app

Webhook stores token

Mark ACTIVE

11. What You Must NOT Do

❌ Do not scrape tokens
❌ Do not bypass CAPTCHA
❌ Do not reuse authorization codes
❌ Do not parallelize Selenium
❌ Do not store client_secret in frontend

12. Final Upgrade Summary
Area	Status
Selenium usage	Reduced, safer
CAPTCHA handling	Correct
OAuth compliance	Full
Multi-API support	Native
Expiry modeling	Correct
Institutional fallback	Implemented