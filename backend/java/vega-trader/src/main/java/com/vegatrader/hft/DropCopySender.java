package com.vegatrader.hft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Drop-Copy FIX Capture.
 * Simulates capturing execution reports and sending to a compliance Drop-Copy
 * session.
 */
@Component
public class DropCopySender {

    private static final Logger logger = LoggerFactory.getLogger(DropCopySender.class);

    // In real implementation: private SessionID dropCopySession;

    public DropCopySender() {
        logger.info("DropCopySender initialized (FIX 4.4)");
    }

    /**
     * Capture an execution report and forward to drop-copy.
     * 
     * @param executionReport Raw FIX message or object
     */
    public void onExecutionReport(Object executionReport) {
        try {
            // Validate sequence
            // Send to Target (dropCopySession)
            if (logger.isDebugEnabled()) {
                logger.debug("Drop-Copy forwarded: {}", executionReport);
            }
        } catch (Exception e) {
            logger.error("Drop-Copy failure", e);
        }
    }
}
