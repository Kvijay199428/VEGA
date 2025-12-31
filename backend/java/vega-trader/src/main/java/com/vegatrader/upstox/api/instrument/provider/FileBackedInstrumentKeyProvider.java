package com.vegatrader.upstox.api.instrument.provider;

import com.vegatrader.upstox.api.instrument.service.InstrumentEnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * File-backed implementation of InstrumentKeyProvider.
 * 
 * <p>
 * Delegates to {@link InstrumentEnrollmentService} to:
 * <ul>
 * <li>Load instrument master files from designated path</li>
 * <li>Parse and index instruments</li>
 * <li>Filter eligible instruments</li>
 * <li>Return subscription-ready keys</li>
 * </ul>
 * 
 * <p>
 * This implementation maintains clean separation:
 * - Instrument enrollment logic stays in enrollment package
 * - MarketDataStreamerV3 only depends on InstrumentKeyProvider interface
 * - Bounded contexts preserved
 * 
 * @since 3.1.0
 */
public final class FileBackedInstrumentKeyProvider implements InstrumentKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(FileBackedInstrumentKeyProvider.class);

    private final InstrumentEnrollmentService enrollmentService;

    /**
     * Creates provider backed by enrollment service.
     * 
     * @param enrollmentService service that manages instrument enrollment
     */
    public FileBackedInstrumentKeyProvider(InstrumentEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
        logger.info("FileBackedInstrumentKeyProvider initialized");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Delegates to enrollment service to get configured instruments.
     */
    @Override
    public Set<String> getInstrumentKeys() {
        Set<String> keys = enrollmentService.enrollConfiguredInstruments();
        logger.debug("Providing {} instrument keys", keys.size());
        return keys;
    }
}
