package com.vegatrader.upstox.auth.selenium.v2;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Selenium WebDriver configuration for V2 login automation.
 * Supports Chrome and Firefox browsers with headless mode.
 *
 * @since 2.1.0
 */
public class SeleniumConfigV2 {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumConfigV2.class);

    private final String browser;
    private final boolean headless;
    private final int timeoutSeconds;

    public SeleniumConfigV2() {
        this("chrome", false, 60);
    }

    public SeleniumConfigV2(String browser, boolean headless) {
        this(browser, headless, 60);
    }

    public SeleniumConfigV2(String browser, boolean headless, int timeoutSeconds) {
        this.browser = browser.toLowerCase();
        this.headless = headless;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Create and configure WebDriver instance.
     * 
     * @return configured WebDriver
     */
    public WebDriver createWebDriver() {
        WebDriver driver;

        switch (browser) {
            case "firefox":
                driver = createFirefoxDriver();
                break;
            case "chrome":
            default:
                driver = createChromeDriver();
                break;
        }

        // Configure timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeoutSeconds));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

        // Maximize window for better visibility
        if (!headless) {
            driver.manage().window().maximize();
        }

        logger.info("✓ WebDriver created: browser={}, headless={}", browser, headless);
        return driver;
    }

    /**
     * Create Chrome WebDriver with kiosk-style compact window.
     */
    private WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        // Kiosk-style compact window for focused login display
        options.addArguments("--window-size=888,382");
        options.addArguments("--window-position=100,100");
        options.addArguments("--app=data:text/html,Loading..."); // App mode (no address bar)

        // Essential options
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");

        // Disable automation detection
        options.addArguments("--disable-blink-features=AutomationControlled");

        logger.info("Creating Chrome WebDriver in kiosk mode (888x382)");

        return new ChromeDriver(options);
    }

    /**
     * Create Firefox WebDriver.
     */
    private WebDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        return new FirefoxDriver(options);
    }

    /**
     * Safely quit WebDriver.
     * 
     * @param driver WebDriver to quit
     */
    public static void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
                logger.info("✓ Browser closed");
            } catch (Exception e) {
                logger.warn("Error closing browser: {}", e.getMessage());
            }
        }
    }

    // Getters

    public String getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
