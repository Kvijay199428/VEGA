package com.vegatrader.upstox.api.instrument.provider;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMasterEntity;
import com.vegatrader.upstox.api.instrument.repository.InstrumentMasterRepository;
import com.vegatrader.upstox.api.instrument.repository.InstrumentSuspensionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Database-backed implementation of InstrumentKeyProvider.
 * 
 * <p>
 * Provides instrument keys directly from the database instead of
 * loading from files. This ensures:
 * <ul>
 * <li>Keys are always from current BOD data</li>
 * <li>Suspended instruments are excluded</li>
 * <li>No file I/O during subscription</li>
 * </ul>
 * 
 * @since 4.0.0
 */
@Component
public class DatabaseBackedInstrumentKeyProvider implements InstrumentKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackedInstrumentKeyProvider.class);

    private final InstrumentMasterRepository masterRepository;
    private final InstrumentSuspensionRepository suspensionRepository;

    public DatabaseBackedInstrumentKeyProvider(
            InstrumentMasterRepository masterRepository,
            InstrumentSuspensionRepository suspensionRepository) {
        this.masterRepository = masterRepository;
        this.suspensionRepository = suspensionRepository;
        logger.info("DatabaseBackedInstrumentKeyProvider initialized");
    }

    @Override
    public Set<String> getInstrumentKeys() {
        // Get all active instruments excluding suspended ones
        Set<String> suspendedKeys = suspensionRepository.findAll().stream()
                .map(s -> s.getInstrumentKey())
                .collect(Collectors.toSet());

        Set<String> keys = masterRepository.findAll().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .map(InstrumentMasterEntity::getInstrumentKey)
                .filter(key -> !suspendedKeys.contains(key))
                .collect(Collectors.toSet());

        logger.debug("Providing {} instrument keys from database (excluded {} suspended)",
                keys.size(), suspendedKeys.size());

        return keys;
    }

    /**
     * Gets instrument keys for a specific segment.
     * 
     * @param segment the market segment (e.g., NSE_EQ, NSE_FO)
     * @return set of instrument keys
     */
    public Set<String> getInstrumentKeysBySegment(String segment) {
        Set<String> suspendedKeys = suspensionRepository.findAll().stream()
                .map(s -> s.getInstrumentKey())
                .collect(Collectors.toSet());

        Set<String> keys = masterRepository.findBySegmentAndInstrumentType(segment, null).stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .map(InstrumentMasterEntity::getInstrumentKey)
                .filter(key -> !suspendedKeys.contains(key))
                .collect(Collectors.toSet());

        logger.debug("Providing {} instrument keys for segment {} from database",
                keys.size(), segment);

        return keys;
    }

    /**
     * Gets instrument keys for a specific segment and type.
     * 
     * @param segment        the market segment
     * @param instrumentType the instrument type (EQ, FUT, CE, PE, INDEX)
     * @return set of instrument keys
     */
    public Set<String> getInstrumentKeysBySegmentAndType(String segment, String instrumentType) {
        Set<String> suspendedKeys = suspensionRepository.findAll().stream()
                .map(s -> s.getInstrumentKey())
                .collect(Collectors.toSet());

        Set<String> keys = masterRepository.findBySegmentAndInstrumentType(segment, instrumentType).stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .map(InstrumentMasterEntity::getInstrumentKey)
                .filter(key -> !suspendedKeys.contains(key))
                .collect(Collectors.toSet());

        logger.debug("Providing {} instrument keys for {}/{} from database",
                keys.size(), segment, instrumentType);

        return keys;
    }

    /**
     * Gets count of available instruments.
     */
    public long getInstrumentCount() {
        return masterRepository.count() - suspensionRepository.count();
    }
}
