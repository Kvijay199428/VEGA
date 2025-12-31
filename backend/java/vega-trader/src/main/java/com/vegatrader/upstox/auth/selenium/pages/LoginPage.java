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
 * Page Object for Upstox login page.
 * Handles username/password entry and login button click.
 *
 * @since 2.0.0
 */
public class LoginPage {

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators (matching Python login.py.backup - see LOGIN_FLOW_ANALYSIS.md)
    private static final By USERNAME_FIELD = By.id("mobileNum");
    private static final By GET_OTP_BUTTON = By.id("getOtp"); // ID, not button text
    private static final By TOTP_FIELD = By.id("otpNum");
    private static final By CONTINUE_OTP_BUTTON = By.id("continueBtn"); // Continue after TOTP
    private static final By PASSWORD_FIELD = By.id("pinCode");
    private static final By CONTINUE_PIN_BUTTON = By.id("pinContinueBtn"); // Continue after PIN
    private static final By ERROR_MESSAGE = By.className("error-message");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Navigate to Upstox OAuth authorization URL.
     *
     * @param authorizationUrl complete authorization URL
     */
    public void navigateToAuthUrl(String authorizationUrl) {
        logger.info("Navigating to: {}", authorizationUrl);
        driver.get(authorizationUrl);

        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_FIELD));
    }

    /**
     * Enter mobile number.
     *
     * @param mobileNumber mobile number
     */
    public void enterUsername(String mobileNumber) {
        logger.info("Entering mobile number: {}", mobileNumber);

        WebElement mobileField = wait.until(
                ExpectedConditions.elementToBeClickable(USERNAME_FIELD));
        mobileField.clear();
        mobileField.sendKeys(mobileNumber);
    }

    /**
     * Click "Get OTP" button after entering mobile number.
     */
    public void clickGetOtpButton() {
        logger.info("Clicking Get OTP button");

        WebElement getOtpButton = wait.until(
                ExpectedConditions.elementToBeClickable(GET_OTP_BUTTON));
        getOtpButton.click();

        // Wait for OTP field to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(TOTP_FIELD));

        // Wait a moment for page transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Enter TOTP code.
     *
     * @param totpCode TOTP code
     */
    public void enterTOTP(String totpCode) {
        logger.info("Entering TOTP code");

        WebElement totpField = wait.until(
                ExpectedConditions.elementToBeClickable(TOTP_FIELD));
        totpField.clear();
        totpField.sendKeys(totpCode);
    }

    /**
     * Click Continue button after TOTP entry.
     */
    public void clickContinueOtpButton() {
        logger.info("Clicking Continue button after TOTP");

        WebElement continueButton = wait.until(
                ExpectedConditions.elementToBeClickable(CONTINUE_OTP_BUTTON));
        continueButton.click();

        // Wait a moment for page transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Enter PIN (6-digit password).
     *
     * @param pin 6-digit PIN
     */
    public void enterPassword(String pin) {
        logger.info("Entering PIN");

        WebElement pinField = wait.until(
                ExpectedConditions.elementToBeClickable(PASSWORD_FIELD));
        pinField.clear();
        pinField.sendKeys(pin);
    }

    /**
     * Click Continue button after PIN entry (pinContinueBtn).
     */
    public void clickLoginButton() {
        logger.info("Clicking Continue button after PIN");

        WebElement continueButton = wait.until(
                ExpectedConditions.elementToBeClickable(CONTINUE_PIN_BUTTON));
        continueButton.click();

        // Wait a moment for page transition
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if error message is displayed.
     *
     * @return true if error exists
     */
    public boolean hasError() {
        try {
            return driver.findElements(ERROR_MESSAGE).size() > 0;
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
     * Perform complete login sequence with new Upstox flow.
     * Flow: Enter mobile → Click Get OTP → Enter TOTP → Click Continue → Enter PIN
     * → Click Continue
     *
     * @param mobileNumber mobile number
     * @param pin          6-digit PIN
     */
    public void login(String mobileNumber, String pin) {
        enterUsername(mobileNumber);
        clickGetOtpButton();

        // TOTP will be entered by calling code via enterTOTP()
        // Then clickContinueOtpButton() must be called

        enterPassword(pin);
        clickLoginButton();

        if (hasError()) {
            String errorMsg = getErrorMessage();
            logger.error("Login error: {}", errorMsg);
            throw new RuntimeException("Login failed: " + errorMsg);
        }
    }
}
