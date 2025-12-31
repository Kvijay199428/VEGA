package com.vegatrader.upstox.auth.selenium.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for capturing screenshots during automation.
 *
 * @since 2.0.0
 */
public final class ScreenshotCapture {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotCapture.class);

    private static final String SCREENSHOT_DIR = "logs/selenium/screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotCapture() {
        // Utility class
    }

    /**
     * Capture screenshot and save to file.
     *
     * @param driver WebDriver instance
     * @param name   screenshot name
     * @return path to saved screenshot
     */
    public static String captureScreenshot(WebDriver driver, String name) {
        if (driver == null) {
            logger.warn("Cannot capture screenshot - driver is null");
            return null;
        }

        try {
            // Create directory if not exists
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }

            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = String.format("%s_%s.png", name, timestamp);
            Path screenshotPath = screenshotDir.resolve(filename);

            // Capture screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File screenshot = takesScreenshot.getScreenshotAs(OutputType.FILE);

            // Save to file
            Files.copy(screenshot.toPath(), screenshotPath);

            logger.info("✓ Screenshot saved: {}", screenshotPath.toAbsolutePath());
            return screenshotPath.toAbsolutePath().toString();

        } catch (IOException e) {
            logger.error("Failed to capture screenshot", e);
            return null;
        }
    }

    /**
     * Capture screenshot on error with auto-generated name.
     *
     * @param driver WebDriver instance
     * @return path to saved screenshot
     */
    public static String captureErrorScreenshot(WebDriver driver) {
        return captureScreenshot(driver, "error");
    }
}
