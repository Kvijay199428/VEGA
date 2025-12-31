package com.vegatrader.upstox.auth.selenium.v2.security;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Cloudflare CAPTCHA/Turnstile Detection Utility.
 * 
 * DESIGN PRINCIPLES (Non-Negotiable):
 * - Never attempt to bypass CAPTCHA
 * - Detect → Halt → Human solves → Resume
 * - Must survive DOM mutation, iframe challenges, Turnstile upgrades
 * 
 * FAIL-SAFE RULE: If detection fails → assume CAPTCHA exists
 *
 * @since 2.2.0
 */
public final class CloudflareCaptchaDetector {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareCaptchaDetector.class);

    private CloudflareCaptchaDetector() {
    }

    /**
     * Check if Cloudflare CAPTCHA/Turnstile is present on the page.
     * 
     * @param driver WebDriver instance
     * @return true if CAPTCHA is detected or uncertain (fail-safe)
     */
    public static boolean isCaptchaPresent(WebDriver driver) {
        try {
            // 1. Check for Cloudflare challenge iframe
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement iframe : iframes) {
                String src = iframe.getAttribute("src");
                if (src != null && src.contains("challenges.cloudflare.com")) {
                    logger.warn("⚠️ Cloudflare challenge iframe detected: {}",
                            src.substring(0, Math.min(src.length(), 80)));
                    return true;
                }
            }

            // 2. Check known Cloudflare DOM markers
            if (!driver.findElements(By.cssSelector("[id*='cf'], [class*='cf-turnstile']")).isEmpty()) {
                logger.warn("⚠️ Cloudflare DOM marker detected (cf/cf-turnstile)");
                return true;
            }

            // 3. Check for error text in Turnstile widget
            List<WebElement> errorElements = driver.findElements(By.xpath("//*[contains(text(), 'Error')]"));
            for (WebElement elem : errorElements) {
                if (elem.isDisplayed()) {
                    // Check if near a Cloudflare element
                    try {
                        WebElement parent = elem.findElement(By.xpath("./.."));
                        String parentHtml = parent.getAttribute("outerHTML");
                        if (parentHtml != null
                                && (parentHtml.contains("cloudflare") || parentHtml.contains("turnstile"))) {
                            logger.warn("⚠️ Cloudflare Turnstile error detected");
                            return true;
                        }
                    } catch (Exception e) {
                        // Continue checking
                    }
                }
            }

            // 4. Check page text hints
            String bodyText = driver.findElement(By.tagName("body")).getText();
            if (bodyText.contains("Checking your browser")
                    || bodyText.contains("Verify you are human")
                    || bodyText.contains("Having trouble?")) {
                logger.warn("⚠️ CAPTCHA challenge text detected on page");
                return true;
            }

            // 5. Check for disabled Get OTP button with Turnstile present
            try {
                WebElement getOtpButton = driver.findElement(By.id("getOtp"));
                if (getOtpButton != null) {
                    String disabled = getOtpButton.getAttribute("disabled");
                    String ariaDisabled = getOtpButton.getAttribute("aria-disabled");
                    String className = getOtpButton.getAttribute("class");

                    boolean isDisabled = "true".equals(disabled) || "true".equals(ariaDisabled)
                            || (className != null && className.contains("disabled"));

                    if (isDisabled && !iframes.isEmpty()) {
                        logger.warn("⚠️ Get OTP button disabled with Cloudflare iframe present - Turnstile failed");
                        return true;
                    }
                }
            } catch (Exception e) {
                // Button not found, continue
            }

            logger.info("✓ No CAPTCHA detected");
            return false;

        } catch (Exception e) {
            // FAIL-SAFE: Assume CAPTCHA if uncertain
            logger.warn("⚠️ CAPTCHA detection failed, assuming CAPTCHA present (fail-safe): {}", e.getMessage());
            return true;
        }
    }

    /**
     * Wait for CAPTCHA to be resolved (human intervention).
     * Polls every 2 seconds until CAPTCHA is gone or max wait exceeded.
     * 
     * @param driver         WebDriver instance
     * @param maxWaitSeconds maximum wait time in seconds
     * @return true if CAPTCHA was resolved, false if timeout
     */
    public static boolean waitForCaptchaResolution(WebDriver driver, int maxWaitSeconds) {
        logger.info("⏳ Waiting for human to solve CAPTCHA (max {} seconds)...", maxWaitSeconds);

        long startTime = System.currentTimeMillis();
        long maxWaitMs = maxWaitSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            if (!isCaptchaPresent(driver)) {
                logger.info("✓ CAPTCHA resolved by human");
                return true;
            }

            try {
                Thread.sleep(2000); // Poll every 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.error("❌ CAPTCHA resolution timeout after {} seconds", maxWaitSeconds);
        return false;
    }
}
