package com.vegatrader.upstox.auth.selenium.workflow;

import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.pages.CallbackPage;
import com.vegatrader.upstox.auth.selenium.pages.ConsentPage;
import com.vegatrader.upstox.auth.selenium.pages.LoginPage;
import com.vegatrader.upstox.auth.selenium.utils.ScreenshotCapture;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main OAuth login automation orchestrator.
 * Coordinates complete login flow: navigate → login → consent → capture code.
 *
 * @since 2.0.0
 */
public class OAuthLoginAutomation {

    private static final Logger logger = LoggerFactory.getLogger(OAuthLoginAutomation.class);

    private final SeleniumConfig seleniumConfig;
    private WebDriver driver;

    public OAuthLoginAutomation(SeleniumConfig seleniumConfig) {
        this.seleniumConfig = seleniumConfig;
    }

    /**
     * Perform complete OAuth login and return authorization code.
     *
     * @param authorizationUrl complete OAuth authorization URL
     * @param credentials      login credentials
     * @param redirectUri      expected redirect URI
     * @return authorization code
     */
    public String performLogin(String authorizationUrl, LoginCredentials credentials,
            String redirectUri) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Starting OAuth Login Automation");
        logger.info("═══════════════════════════════════════════════════════");

        try {
            // Create WebDriver
            driver = seleniumConfig.createWebDriver();
            logger.info("✓ Browser launched");

            // Page objects
            LoginPage loginPage = new LoginPage(driver);
            ConsentPage consentPage = new ConsentPage(driver);
            AuthCodeCapture authCodeCapture = new AuthCodeCapture(driver);

            // Step 1: Navigate to authorization URL
            logger.info("Step 1: Navigating to authorization URL");
            loginPage.navigateToAuthUrl(authorizationUrl);

            // Step 2: Perform login with complete flow
            logger.info("Step 2: Starting login flow");
            credentials.validate();

            // 2a: Enter mobile number and click Get OTP
            logger.info("  2a: Entering mobile number and clicking Get OTP");
            loginPage.enterUsername(credentials.getUsername());
            loginPage.clickGetOtpButton();

            // 2b: Generate TOTP and enter
            logger.info("  2b: Generating and entering TOTP");
            String totpCode = credentials.generateTOTPCode();
            if (totpCode == null) {
                throw new RuntimeException("Failed to generate TOTP code");
            }
            logger.info("  Generated TOTP: {}", totpCode);
            loginPage.enterTOTP(totpCode);

            // 2c: Click Continue after TOTP
            logger.info("  2c: Clicking Continue after TOTP");
            loginPage.clickContinueOtpButton();

            // 2d: Enter PIN and click Continue
            logger.info("  2d: Entering PIN and clicking Continue");
            loginPage.enterPassword(credentials.getPassword());
            loginPage.clickLoginButton();

            // Step 3: Handle consent (if required)
            logger.info("Step 3: Handling OAuth consent");
            consentPage.grantConsent();

            // Step 4: Capture authorization code
            logger.info("Step 4: Capturing authorization code");
            String authCode = authCodeCapture.captureAuthCode(redirectUri);

            logger.info("═══════════════════════════════════════════════════════");
            logger.info("✓ OAuth Login Automation Completed Successfully");
            logger.info("═══════════════════════════════════════════════════════");

            return authCode;

        } catch (Exception e) {
            logger.error("✗ OAuth login automation failed", e);

            // Capture screenshot on error
            if (driver != null) {
                try {
                    String screenshotPath = ScreenshotCapture.captureScreenshot(driver, "login_error");
                    logger.error("Screenshot saved: {}", screenshotPath);
                } catch (Exception screenshotError) {
                    logger.error("Failed to capture screenshot", screenshotError);
                }
            }

            throw new RuntimeException("OAuth login automation failed: " + e.getMessage(), e);

        } finally {
            // Always close browser
            if (driver != null) {
                SeleniumConfig.quitDriver(driver);
            }
        }
    }

    /**
     * Get WebDriver instance (for testing/debugging).
     *
     * @return WebDriver or null
     */
    public WebDriver getDriver() {
        return driver;
    }
}
