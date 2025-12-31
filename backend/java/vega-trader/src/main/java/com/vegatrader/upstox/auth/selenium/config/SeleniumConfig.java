package com.vegatrader.upstox.auth.selenium.config;

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
 * Selenium WebDriver configuration and factory.
 * Manages WebDriver lifecycle and browser options.
 *
 * @since 2.0.0
 */
public class SeleniumConfig {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumConfig.class);

    private static final String DEFAULT_BROWSER = "chrome";
    private static final boolean DEFAULT_HEADLESS = false;
    private static final int IMPLICIT_WAIT_SECONDS = 10;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;

    private String browser;
    private boolean headless;

    public SeleniumConfig() {
        this.browser = DEFAULT_BROWSER;
        this.headless = DEFAULT_HEADLESS;
    }

    public SeleniumConfig(String browser, boolean headless) {
        this.browser = browser != null ? browser : DEFAULT_BROWSER;
        this.headless = headless;
    }

    /**
     * Create and configure WebDriver instance.
     *
     * @return configured WebDriver
     */
    public WebDriver createWebDriver() {
        logger.info("Creating WebDriver: browser={}, headless={}", browser, headless);

        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "firefox":
                driver = createFirefoxDriver();
                break;
            case "chrome":
            default:
                driver = createChromeDriver();
                break;
        }

        // Configure timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));

        // Maximize window (unless headless)
        if (!headless) {
            driver.manage().window().maximize();
        }

        logger.info("✓ WebDriver created successfully");
        return driver;
    }

    /**
     * Create Chrome WebDriver with configured options.
     */
    private WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = BrowserOptions.buildChromeOptions(headless);
        return new ChromeDriver(options);
    }

    /**
     * Create Firefox WebDriver with configured options.
     */
    private WebDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = BrowserOptions.buildFirefoxOptions(headless);
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
                logger.info("✓ WebDriver quit successfully");
            } catch (Exception e) {
                logger.error("Error quitting WebDriver", e);
            }
        }
    }

    // Getters/Setters
    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }
}
