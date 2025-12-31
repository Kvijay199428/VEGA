package com.vegatrader.upstox.auth.selenium.config;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Arrays;

/**
 * Browser options builder for Chrome and Firefox.
 * Configures browser settings for automation.
 *
 * @since 2.0.0
 */
public final class BrowserOptions {

    private BrowserOptions() {
        // Utility class
    }

    /**
     * Build Chrome options with recommended settings.
     *
     * @param headless whether to run in headless mode
     * @return configured ChromeOptions
     */
    public static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        // Headless mode
        if (headless) {
            options.addArguments("--headless=new"); // New headless mode
        }

        // Performance and stability options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");

        // Privacy options
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // Window size (important for headless)
        options.addArguments("--window-size=1920,1080");

        // User agent (appear as normal browser)
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        return options;
    }

    /**
     * Build Firefox options with recommended settings.
     *
     * @param headless whether to run in headless mode
     * @return configured FirefoxOptions
     */
    public static FirefoxOptions buildFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();

        // Headless mode
        if (headless) {
            options.addArguments("--headless");
        }

        // Window size
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        // Performance options
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("dom.push.enabled", false);

        return options;
    }
}
