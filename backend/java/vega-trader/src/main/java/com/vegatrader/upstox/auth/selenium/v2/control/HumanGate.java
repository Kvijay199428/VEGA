package com.vegatrader.upstox.auth.selenium.v2.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Human-in-the-loop gate for CAPTCHA resolution.
 * 
 * This class provides pause/resume functionality when human intervention
 * is required (CAPTCHA solving, manual verification, etc.)
 * 
 * DESIGN RULE: Never attempt to bypass CAPTCHA automatically.
 * Detect → Halt → Human solves → Resume
 *
 * @since 2.2.0
 */
public final class HumanGate {

    private static final Logger logger = LoggerFactory.getLogger(HumanGate.class);

    private HumanGate() {
    }

    /**
     * Wait for human confirmation via console input.
     * Blocks until user presses ENTER.
     * 
     * @param reason reason for requiring human action
     */
    public static void waitForHuman(String reason) {
        logger.warn("╔══════════════════════════════════════════════════════════════╗");
        logger.warn("║              HUMAN ACTION REQUIRED                           ║");
        logger.warn("╚══════════════════════════════════════════════════════════════╝");
        logger.warn("Reason: {}", reason);
        logger.warn("Complete CAPTCHA in browser window.");
        logger.warn("Press ENTER in console to continue...");
        logger.warn("══════════════════════════════════════════════════════════════");

        System.out.println();
        System.out.println("========================================");
        System.out.println("  HUMAN ACTION REQUIRED");
        System.out.println("========================================");
        System.out.println("Reason: " + reason);
        System.out.println();
        System.out.println(">> Complete CAPTCHA in browser window");
        System.out.println(">> Press ENTER to continue...");
        System.out.println("========================================");
        System.out.println();

        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
            logger.info("✓ Human confirmed, resuming automation");
        } catch (Exception e) {
            logger.error("❌ Human confirmation failed: {}", e.getMessage());
            throw new RuntimeException("Human confirmation failed", e);
        }
    }

    /**
     * Wait for human with timeout auto-continue option.
     * If no input received within timeout, auto-continues.
     * 
     * @param reason            reason for requiring human action
     * @param autoResumeSeconds seconds before auto-resume (0 = wait forever)
     */
    public static void waitForHumanWithTimeout(String reason, int autoResumeSeconds) {
        if (autoResumeSeconds <= 0) {
            waitForHuman(reason);
            return;
        }

        logger.warn("╔══════════════════════════════════════════════════════════════╗");
        logger.warn("║              HUMAN ACTION REQUIRED                           ║");
        logger.warn("╚══════════════════════════════════════════════════════════════╝");
        logger.warn("Reason: {}", reason);
        logger.warn("Auto-resume in {} seconds if no action...", autoResumeSeconds);
        logger.warn("══════════════════════════════════════════════════════════════");

        System.out.println();
        System.out.println("========================================");
        System.out.println("  HUMAN ACTION REQUIRED");
        System.out.println("========================================");
        System.out.println("Reason: " + reason);
        System.out.println();
        System.out.println(">> Complete CAPTCHA in browser window");
        System.out.println(">> Auto-resume in " + autoResumeSeconds + " seconds...");
        System.out.println(">> Or press ENTER to continue immediately");
        System.out.println("========================================");
        System.out.println();

        try {
            long endTime = System.currentTimeMillis() + (autoResumeSeconds * 1000L);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (System.currentTimeMillis() < endTime) {
                if (reader.ready()) {
                    reader.readLine();
                    logger.info("✓ Human confirmed via ENTER, resuming automation");
                    return;
                }
                Thread.sleep(500);
            }

            logger.info("⏰ Auto-resume timeout reached, continuing automation");

        } catch (Exception e) {
            logger.warn("Human gate interrupted: {}", e.getMessage());
        }
    }

    /**
     * Non-blocking human notification (just logs, doesn't wait).
     * Useful when you want to alert but not block.
     * 
     * @param reason reason for notification
     */
    public static void notifyHuman(String reason) {
        logger.warn("╔══════════════════════════════════════════════════════════════╗");
        logger.warn("║              ATTENTION REQUIRED                              ║");
        logger.warn("╚══════════════════════════════════════════════════════════════╝");
        logger.warn("Reason: {}", reason);
        logger.warn("══════════════════════════════════════════════════════════════");

        System.out.println();
        System.out.println("========================================");
        System.out.println("  ATTENTION: " + reason);
        System.out.println("========================================");
        System.out.println();
    }
}
