package com.vegatrader.upstox.auth.selenium.v2;

import com.vegatrader.upstox.auth.selenium.v2.control.HumanGate;
import com.vegatrader.upstox.auth.selenium.v2.exception.CaptchaDetectedException;
import com.vegatrader.upstox.auth.selenium.v2.security.CloudflareCaptchaDetector;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for Upstox login page (V2).
 * Implements Selenium automation for login flow using documented XPath
 * selectors.
 * 
 * Flow: Mobile → Get OTP → TOTP → Continue → PIN → Continue
 *
 * @since 2.1.0
 */
public class LoginPageV2 {

    private static final Logger logger = LoggerFactory.getLogger(LoginPageV2.class);

    // XPath Selectors (from part3_selenium_automation.md)
    private static final By MOBILE_NUMBER_INPUT = By.xpath("//*[@id='mobileNum']");
    private static final By GET_OTP_BUTTON = By.xpath("//*[@id='getOtp']");
    private static final By OTP_TOTP_INPUT = By.xpath("//*[@id='otpNum']");
    private static final By CONTINUE_BUTTON = By.xpath("//*[@id='continueBtn']");
    private static final By PIN_INPUT = By.xpath("//*[@id='pinCode']");
    // Fallback PIN locators per c2.md (Upstox changes IDs frequently)
    private static final By PIN_INPUT_FALLBACK1 = By.cssSelector("input[type='password']");
    private static final By PIN_INPUT_FALLBACK2 = By.xpath("//input[contains(@placeholder,'PIN')]");
    private static final By PIN_CONTINUE_BUTTON = By.xpath("//*[@id='pinContinueBtn']");
    private static final By ERROR_MESSAGE = By.className("error-message");

    // CAPTCHA detection selectors
    private static final By CAPTCHA_ID = By.id("captcha");
    private static final By CAPTCHA_IMAGE = By.id("captcha_image");
    private static final By CAPTCHA_INPUT = By.id("captcha_input");
    private static final By CLOUDFLARE_TURNSTILE = By.className("cf-turnstile");
    private static final By RECAPTCHA = By.className("g-recaptcha");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration defaultTimeout;

    /**
     * Create LoginPageV2 with default 10-second timeout (optimized for speed).
     * 
     * @param driver WebDriver instance
     */
    public LoginPageV2(WebDriver driver) {
        this(driver, Duration.ofSeconds(10));
    }

    /**
     * Create LoginPageV2 with custom timeout.
     * 
     * @param driver  WebDriver instance
     * @param timeout wait timeout duration
     */
    public LoginPageV2(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.defaultTimeout = timeout;
        this.wait = new WebDriverWait(driver, timeout);
    }

    /**
     * Navigate to Upstox OAuth authorization URL.
     * 
     * @param authorizationUrl complete OAuth URL with client_id and redirect_uri
     */
    public void navigateToAuthUrl(String authorizationUrl) {
        logger.info("Navigating to authorization URL");
        logger.debug("URL: {}", authorizationUrl);

        driver.get(authorizationUrl);

        // Wait for mobile number input to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(MOBILE_NUMBER_INPUT));

        // Apply kiosk mode styling - hide non-essential elements
        applyKioskStyling();

        // Check for CAPTCHA immediately after page load
        detectCaptcha();

        logger.info("✓ Login page loaded (kiosk mode)");
    }

    /**
     * Apply kiosk styling to hide non-essential elements.
     * Focuses display on login form only.
     */
    private void applyKioskStyling() {
        try {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

            // Hide QR code section and other non-essential elements
            String hideScript = "try {" +
                    "  // Hide QR code login section" +
                    "  document.querySelectorAll('[class*=\"eh\"]').forEach(e => e.style.display='none');" +
                    "  // Hide help link" +
                    "  document.querySelectorAll('[href*=\"contact-us\"]').forEach(e => e.style.display='none');" +
                    "  // Make login container full width" +
                    "  document.querySelectorAll('[class*=\"ch ci\"]').forEach(e => {" +
                    "    e.style.width = '100%';" +
                    "    e.style.maxWidth = '100%';" +
                    "  });" +
                    "  // Center the main form" +
                    "  document.body.style.overflow = 'hidden';" +
                    "} catch(e) { console.log('Kiosk styling error:', e); }";

            js.executeScript(hideScript);
            logger.info("✓ Kiosk styling applied - non-essential elements hidden");
        } catch (Exception e) {
            logger.debug("Kiosk styling not applied: {}", e.getMessage());
        }
    }

    /**
     * Enter mobile number in the input field.
     * 
     * @param mobileNumber 10-digit mobile number
     */
    public void enterMobileNumber(String mobileNumber) {
        logger.info("Entering mobile number: {}****{}",
                mobileNumber.substring(0, 2),
                mobileNumber.substring(8));

        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(MOBILE_NUMBER_INPUT));
        input.clear();
        input.sendKeys(mobileNumber);

        logger.info("✓ Mobile number entered");
    }

    /**
     * Click "Get OTP" button after entering mobile number.
     * Includes CAPTCHA detection with human pause/resume.
     */
    public void clickGetOtp() {
        logger.info("Clicking Get OTP button");

        // Quick check for page load
        sleep(500);

        // Check for Cloudflare CAPTCHA using enterprise detector
        if (CloudflareCaptchaDetector.isCaptchaPresent(driver)) {
            logger.warn("⚠️ Cloudflare CAPTCHA detected before OTP request");

            // Pause for human to solve CAPTCHA
            HumanGate.waitForHuman("Cloudflare CAPTCHA detected. Please solve it in the browser window.");

            // Wait for human to complete CAPTCHA resolution
            if (!CloudflareCaptchaDetector.waitForCaptchaResolution(driver, 5)) {
                throw new CaptchaDetectedException(
                        "CAPTCHA not resolved within timeout. Manual intervention required.",
                        "cloudflare-turnstile");
            }
        }

        // Now attempt to click Get OTP button
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(GET_OTP_BUTTON));
        button.click();

        // Wait for OTP/TOTP input field to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(OTP_TOTP_INPUT));

        // Quick delay for page transition
        sleep(500);

        // Check for CAPTCHA after clicking Get OTP
        if (CloudflareCaptchaDetector.isCaptchaPresent(driver)) {
            logger.warn("⚠️ Cloudflare CAPTCHA detected after OTP request");
            HumanGate.waitForHuman("Cloudflare CAPTCHA appeared after OTP request.");
        }

        logger.info("✓ Get OTP clicked, OTP input visible");
    }

    /**
     * Enter TOTP/OTP code.
     * 
     * @param code 6-digit TOTP or OTP code
     */
    public void enterTotp(String code) {
        logger.info("Entering TOTP code");

        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(OTP_TOTP_INPUT));
        input.clear();
        input.sendKeys(code);

        logger.info("✓ TOTP code entered");
    }

    /**
     * Click Continue button after entering OTP/TOTP.
     */
    public void clickContinueAfterOtp() {
        logger.info("Clicking Continue after OTP/TOTP");

        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(CONTINUE_BUTTON));
        button.click();

        // Wait for PIN input to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(PIN_INPUT));

        // Quick delay for page transition
        sleep(500);

        // Check for CAPTCHA after OTP submission
        detectCaptcha();

        logger.info("✓ Continue clicked, PIN input visible");
    }

    /**
     * Enter 6-digit PIN.
     * 
     * @param pin 6-digit PIN
     */
    public void enterPin(String pin) {
        logger.info("Entering PIN");

        WebElement input = null;
        try {
            // Try primary locator first
            input = wait.until(ExpectedConditions.elementToBeClickable(PIN_INPUT));
        } catch (Exception e) {
            // Try fallback locators (Upstox changes IDs frequently)
            logger.debug("Primary PIN locator failed, trying fallbacks...");
            try {
                input = wait.until(ExpectedConditions.elementToBeClickable(PIN_INPUT_FALLBACK1));
            } catch (Exception e2) {
                input = wait.until(ExpectedConditions.elementToBeClickable(PIN_INPUT_FALLBACK2));
            }
        }
        input.clear();
        input.sendKeys(pin);

        logger.info("✓ PIN entered");
    }

    /**
     * Click Continue button after entering PIN (final submit).
     */
    public void clickContinueAfterPin() {
        logger.info("Clicking Continue after PIN");

        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(PIN_CONTINUE_BUTTON));
        button.click();

        // Quick delay for redirect
        sleep(500);

        // Final CAPTCHA check
        detectCaptcha();

        logger.info("✓ Login submitted");
    }

    /**
     * Perform complete login flow.
     * 
     * @param credentials login credentials with mobile, PIN, and TOTP secret
     * @throws RuntimeException if login fails
     */
    public void performLogin(LoginCredentialsV2 credentials) {
        credentials.validate();

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Starting Upstox Login Flow (V2)");
        logger.info("═══════════════════════════════════════════════════════");

        // Step 1: Enter mobile number
        enterMobileNumber(credentials.getMobileNumber());

        // Step 2: Click Get OTP
        clickGetOtp();

        // Step 3: Generate and enter TOTP
        String totpCode = credentials.generateTotpCode();
        if (totpCode == null) {
            throw new RuntimeException("Failed to generate TOTP code");
        }
        logger.info("Generated TOTP: {}", totpCode);
        enterTotp(totpCode);

        // Step 4: Click Continue after TOTP
        clickContinueAfterOtp();

        // Step 5: Enter PIN
        enterPin(credentials.getPin());

        // Step 6: Click Continue after PIN
        clickContinueAfterPin();

        // Check for errors
        if (isErrorDisplayed()) {
            String errorMsg = getErrorMessage();
            logger.error("Login error: {}", errorMsg);
            throw new RuntimeException("Login failed: " + errorMsg);
        }

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("✓ Login flow completed successfully");
        logger.info("═══════════════════════════════════════════════════════");
    }

    /**
     * Check if error message is displayed on the page.
     * 
     * @return true if error is displayed
     */
    public boolean isErrorDisplayed() {
        try {
            return !driver.findElements(ERROR_MESSAGE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get error message text.
     * 
     * @return error message or empty string
     */
    public String getErrorMessage() {
        try {
            WebElement error = driver.findElement(ERROR_MESSAGE);
            return error.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get current page URL.
     * 
     * @return current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Get page title.
     * 
     * @return page title
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Sleep for specified milliseconds.
     * 
     * @param millis milliseconds to sleep
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Detect CAPTCHA on the current page.
     * Throws CaptchaDetectedException if any CAPTCHA is found.
     * 
     * CRITICAL: CAPTCHA = ABORT immediately, never retry.
     * 
     * @throws CaptchaDetectedException if CAPTCHA is detected
     */
    private void detectCaptcha() {
        // Check for various CAPTCHA types
        if (isElementPresent(CAPTCHA_ID)) {
            logger.error("⚠️ CAPTCHA detected (id='captcha')");
            throw new CaptchaDetectedException(
                    "CAPTCHA detected. Manual intervention required.", "upstox-captcha");
        }

        if (isElementPresent(CAPTCHA_IMAGE)) {
            logger.error("⚠️ CAPTCHA image detected");
            throw new CaptchaDetectedException(
                    "CAPTCHA image detected. Manual intervention required.", "captcha-image");
        }

        if (isElementPresent(CAPTCHA_INPUT)) {
            logger.error("⚠️ CAPTCHA input field detected");
            throw new CaptchaDetectedException(
                    "CAPTCHA input detected. Manual intervention required.", "captcha-input");
        }

        // Check for Cloudflare Turnstile error state
        if (isTurnstileError()) {
            logger.error("⚠️ Cloudflare Turnstile verification FAILED - automation blocked");
            throw new CaptchaDetectedException(
                    "Cloudflare Turnstile blocked automation. Manual intervention required.",
                    "cloudflare-turnstile-error");
        }

        if (isElementPresent(RECAPTCHA)) {
            logger.error("⚠️ Google reCAPTCHA detected");
            throw new CaptchaDetectedException(
                    "Google reCAPTCHA detected. Manual intervention required.", "google-recaptcha");
        }
    }

    /**
     * Check if Cloudflare Turnstile has failed/errored.
     * Looks for error messages, failed state, or disabled Get OTP button due to
     * Turnstile.
     */
    private boolean isTurnstileError() {
        try {
            // Check for Turnstile iframe with error
            if (isElementPresent(CLOUDFLARE_TURNSTILE)) {
                // Look for error text near Turnstile
                List<WebElement> errorElements = driver.findElements(By.xpath("//*[contains(text(), 'Error')]"));
                for (WebElement elem : errorElements) {
                    if (elem.isDisplayed()) {
                        logger.warn("Found Turnstile error element: {}", elem.getText());
                        return true;
                    }
                }

                // Check for "Having trouble?" or similar error messages
                if (isElementPresent(By.xpath("//*[contains(text(), 'Having trouble')]"))) {
                    return true;
                }
            }

            // Check if Get OTP button is disabled due to Turnstile failure
            try {
                WebElement getOtpButton = driver.findElement(GET_OTP_BUTTON);
                if (getOtpButton != null) {
                    String disabled = getOtpButton.getAttribute("disabled");
                    String ariaDisabled = getOtpButton.getAttribute("aria-disabled");
                    String className = getOtpButton.getAttribute("class");

                    // If Turnstile is present and button is disabled, Turnstile has failed
                    if (isElementPresent(CLOUDFLARE_TURNSTILE)) {
                        if ("true".equals(disabled) || "true".equals(ariaDisabled)
                                || (className != null && className.contains("disabled"))) {
                            logger.warn("Get OTP button disabled - Turnstile verification failed");
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                // Button not found, not an error
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an element is present on the page.
     * 
     * @param locator element locator
     * @return true if element is present
     */
    private boolean isElementPresent(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            return !elements.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
