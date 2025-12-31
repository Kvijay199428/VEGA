package com.vegatrader.upstox.auth.selenium.v2;

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
 * Page Object for Upstox OAuth consent page.
 * Handles the consent/authorization screen that may appear after login.
 *
 * @since 2.1.0
 */
public class ConsentPageV2 {

    private static final Logger logger = LoggerFactory.getLogger(ConsentPageV2.class);

    // Consent button selectors (multiple possible selectors)
    private static final By AUTHORIZE_BUTTON = By.id("authorize-btn");
    private static final By AUTHORIZE_BUTTON_ALT = By.xpath("//button[contains(text(), 'Authorize')]");
    private static final By ALLOW_BUTTON = By.xpath("//button[contains(text(), 'Allow')]");
    private static final By CONTINUE_BUTTON = By.xpath("//button[contains(text(), 'Continue')]");

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Create ConsentPageV2 with default 10-second timeout.
     * 
     * @param driver WebDriver instance
     */
    public ConsentPageV2(WebDriver driver) {
        this(driver, Duration.ofSeconds(10));
    }

    /**
     * Create ConsentPageV2 with custom timeout.
     * 
     * @param driver  WebDriver instance
     * @param timeout wait timeout duration
     */
    public ConsentPageV2(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    /**
     * Handle consent page if present.
     * Attempts to click any visible authorize/allow/continue button.
     * 
     * @return true if consent was granted, false if page was not present
     */
    public boolean grantConsentIfPresent() {
        logger.info("Checking for consent page");

        try {
            // Try each button type
            if (tryClickButton(AUTHORIZE_BUTTON, "Authorize")) {
                return true;
            }
            if (tryClickButton(AUTHORIZE_BUTTON_ALT, "Authorize (alt)")) {
                return true;
            }
            if (tryClickButton(ALLOW_BUTTON, "Allow")) {
                return true;
            }
            if (tryClickButton(CONTINUE_BUTTON, "Continue")) {
                return true;
            }

            logger.info("No consent page detected - continuing");
            return false;

        } catch (Exception e) {
            logger.debug("Consent page not found or already handled: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Try to click a button identified by locator.
     * 
     * @param locator    button locator
     * @param buttonName name for logging
     * @return true if button was clicked
     */
    private boolean tryClickButton(By locator, String buttonName) {
        try {
            List<WebElement> buttons = driver.findElements(locator);
            if (!buttons.isEmpty()) {
                WebElement button = buttons.get(0);
                if (button.isDisplayed() && button.isEnabled()) {
                    logger.info("Found {} button - clicking", buttonName);
                    button.click();

                    // Wait a moment for page transition
                    sleep(1000);

                    logger.info("✓ Consent granted via {} button", buttonName);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.debug("Could not click {} button: {}", buttonName, e.getMessage());
        }
        return false;
    }

    /**
     * Wait explicitly for consent page and grant consent.
     * Use this when you know consent page will appear.
     */
    public void waitAndGrantConsent() {
        logger.info("Waiting for consent page");

        try {
            // Wait for any consent button to appear
            WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(
                            "//button[contains(text(), 'Authorize') or contains(text(), 'Allow') or contains(text(), 'Continue')]")));

            wait.until(ExpectedConditions.elementToBeClickable(button));
            button.click();

            sleep(1000);
            logger.info("✓ Consent granted");

        } catch (Exception e) {
            logger.warn("Consent page wait timed out - may not be required");
        }
    }

    /**
     * Check if consent page is currently displayed.
     * 
     * @return true if consent page is visible
     */
    public boolean isConsentPageDisplayed() {
        try {
            List<WebElement> authorizeButtons = driver.findElements(AUTHORIZE_BUTTON);
            List<WebElement> authorizeAltButtons = driver.findElements(AUTHORIZE_BUTTON_ALT);
            List<WebElement> allowButtons = driver.findElements(ALLOW_BUTTON);

            return !authorizeButtons.isEmpty() || !authorizeAltButtons.isEmpty() || !allowButtons.isEmpty();
        } catch (Exception e) {
            return false;
        }
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
}
