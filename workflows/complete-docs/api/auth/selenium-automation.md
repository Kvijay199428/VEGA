# Selenium Automation

The Selenium module automates the human interaction required for OAuth 2.0 login.

## Workflow: OAuth Login Automation
**Class**: `com.vegatrader.upstox.auth.selenium.workflow.OAuthLoginAutomation`

Automates the complete Upstox Login Flow:
1. **Navigate**: Opens Authorization URL.
2. **Mobile/OTP**: Enters mobile number -> "Get OTP".
3. **TOTP**: Generates Time-based OTP (using stored secret) -> "Continue".
4. **PIN**: Enters 6-digit PIN.
5. **Consent**: Clicks "Allow" on the OAuth consent page (if shown).
6. **Code Capture**: Intercepts the Redirect URI to extract the `code` parameter.

## Resilience
- **Screenshot on Failure**: Captures full-page screenshots if any step fails, saved to `logs/screenshots/`.
- **Driver Management**: Auto-quits the ChromeDriver to prevent zombie processes.
- **Headless Support**: Configurable via `SeleniumConfig` (uses `chrome-headless-shell` for speed).
