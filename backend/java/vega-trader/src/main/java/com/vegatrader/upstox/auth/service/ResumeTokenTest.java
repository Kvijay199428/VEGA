package com.vegatrader.upstox.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resume Token Test - Enterprise test with resume-from-failure.
 * 
 * Features:
 * - Resume from exact failure point
 * - 11-minute cooldown on broker throttling
 * - State persistence for crash recovery
 *
 * @since 2.4.0
 */
public class ResumeTokenTest {

    private static final Logger logger = LoggerFactory.getLogger(ResumeTokenTest.class);

    public static void main(String[] args) {
        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║        RESUME TOKEN TEST - STARTED                            ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");

        try {
            ResumeOrchestrator orchestrator = new ResumeOrchestrator();

            // Check if there's a resumable state
            TokenExecutionState resumableState = orchestrator.getResumableState();

            TokenExecutionResult result;

            if (resumableState != null) {
                logger.info("Found resumable state: {}", resumableState);
                logger.info("Status: {}", resumableState.getStatus());
                logger.info("Next API: {}", resumableState.getNextApiToGenerate());

                // Check cooldown
                CooldownManager cooldown = new CooldownManager();
                if (resumableState.isInCooldown() && !cooldown.canResumeNow(resumableState)) {
                    logger.info("Cooldown remaining: {}", cooldown.getRemainingTimeFormatted(resumableState));
                }

                result = orchestrator.resumeFromState(resumableState);
            } else {
                logger.info("No resumable state found. Starting fresh.");
                result = orchestrator.executeWithResume();
            }

            // Print results
            logger.info("");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("                    TEST RESULTS");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("Success:      {}", result.isSuccess());
            logger.info("Generated:    {}", result.getSuccessCount());
            logger.info("Failed:       {}", result.getFailedCount());
            logger.info("Exit Code:    {}", result.getExitCode());

            logger.info("");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("                    TEST COMPLETE");
            logger.info("═══════════════════════════════════════════════════════════════");

            System.exit(result.getExitCode());

        } catch (Exception e) {
            logger.error("Test failed with exception: {}", e.getMessage(), e);
            System.exit(3);
        }
    }
}
