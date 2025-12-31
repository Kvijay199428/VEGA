package com.vegatrader.upstox.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for token generation with database persistence.
 * 
 * Usage: Run as main class to test token generation and DB storage.
 *
 * @since 2.3.0
 */
public class TokenPersistenceTest {

    private static final Logger logger = LoggerFactory.getLogger(TokenPersistenceTest.class);

    public static void main(String[] args) {
        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║        TOKEN PERSISTENCE TEST - STARTED                        ║");
        logger.info("╚═══════════════════════════════════════════════════════════════╝");

        try {
            // Create orchestrator
            AsyncTokenOrchestrator orchestrator = new AsyncTokenOrchestrator();

            // Execute with INVALID_ONLY mode (generate only missing/invalid tokens)
            TokenExecutionRequest request = TokenExecutionRequest.invalidOnly();

            logger.info("Executing token refresh with INVALID_ONLY mode...");

            // Execute synchronously for test
            TokenExecutionResult result = orchestrator.executeSync(request);

            // Print results
            logger.info("");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("                    TEST RESULTS");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("Success:      {}", result.isSuccess());
            logger.info("Fast Exit:    {}", result.isNoop());
            logger.info("Generated:    {}", result.getSuccessCount());
            logger.info("Failed:       {}", result.getFailedCount());
            logger.info("Duration:     {} ms", result.getDurationMs());
            logger.info("Exit Code:    {}", result.getExitCode());

            if (result.getGenerationResults() != null) {
                logger.info("");
                logger.info("Generation Results:");
                for (TokenGenerationService.GenerationResult gr : result.getGenerationResults()) {
                    logger.info("  {} : {}", gr.getApiName(), gr.isSuccess() ? "✓ SUCCESS" : "✗ " + gr.getMessage());
                }
            }

            if (result.getReport() != null) {
                logger.info("");
                logger.info("Token Status Report:");
                logger.info("  Valid:   {}", result.getReport().getValid());
                logger.info("  Invalid: {}", result.getReport().getInvalid());
                logger.info("  Missing: {}", result.getReport().getMissing());
            }

            logger.info("");
            logger.info("═══════════════════════════════════════════════════════════════");
            logger.info("                    TEST COMPLETE");
            logger.info("═══════════════════════════════════════════════════════════════");

            // Shutdown
            orchestrator.shutdown();

            // Exit with appropriate code
            System.exit(result.getExitCode());

        } catch (Exception e) {
            logger.error("Test failed with exception: {}", e.getMessage(), e);
            System.exit(3);
        }
    }
}
