package com.vegatrader.upstox.auth.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Page Object for OAuth consent page.
 * Handles permission approval.
 *
 * @since 2.0.0
 */
public class ConsentPage {

    private static final Logger logger = LoggerFactory.getLogger(ConsentPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators for consent page
    private static final By ALLOW_BUTTON = By
            .xpath("//button[contains(text(), 'Allow') or contains(text(), 'Authorize')]");
    private static final By DENY_BUTTON = By.xpath("//button[contains(text(), 'Deny') or contains(text(), 'Cancel')]");
    private static final By PERMISSIONS_LIST = By.className("permissions");

    public ConsentPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Wait for consent page to load.
     *
     * @return true if consent page appeared
     */
    public boolean waitForConsentPage() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(ALLOW_BUTTON),
                    ExpectedConditions.urlContains("redirect_uri")));

            // Check if we're on consent page or already redirected
            return driver.findElements(ALLOW_BUTTON).size() > 0;
        } catch (Exception e) {
            logger.warn("Consent page did not appear or already redirected");
            return false;
        }
    }

    /**
     * Click allow/authorize button.
     */
    public void clickAllow() {
        logger.info("Clicking Allow button on consent page");

        try {
            WebElement allowButton = wait.until(
                    ExpectedConditions.elementToBeClickable(ALLOW_BUTTON));
            allowButton.click();

            logger.info("✓ Consent granted");
        } catch (Exception e) {
            logger.warn("Allow button not found - may have auto-consented");
        }
    }

    /**
     * Click deny button (for testing).
     */
    public void clickDeny() {
        logger.info("Clicking Deny button on consent page");

        WebElement denyButton = wait.until(
                ExpectedConditions.elementToBeClickable(DENY_BUTTON));
        denyButton.click();

        logger.info("✓ Consent denied");
    }

    /**
     * Check if consent page is displayed.
     *
     * @return true if on consent page
     */
    public boolean isConsentPageDisplayed() {
        return driver.findElements(ALLOW_BUTTON).size() > 0;
    }

    /**
     * Grant consent (allow OAuth permissions).
     */
    public void grantConsent() {
        if (waitForConsentPage()) {
            clickAllow();
        } else {
            logger.info("Consent page not required or already passed");
        }
    }
}
