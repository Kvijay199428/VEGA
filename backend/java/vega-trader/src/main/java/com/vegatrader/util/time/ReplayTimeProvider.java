package com.vegatrader.util.time;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Replay/deterministic time provider for Market Replay.
 * 
 * <p>
 * Provides timestamps from a pre-recorded journal or test sequence,
 * enabling deterministic backtesting and replay validation.
 * 
 * <p>
 * Thread-safe via AtomicReference for high-frequency access.
 * 
 * @since 5.0.0
 */
public class ReplayTimeProvider implements TimeProvider {

    private final Iterator<Instant> replayIterator;
    private final AtomicReference<Instant> lastTime = new AtomicReference<>(Instant.EPOCH);

    /**
     * Create replay provider from list of timestamps.
     * 
     * @param replayTimestamps ordered list of journal timestamps
     */
    public ReplayTimeProvider(List<Instant> replayTimestamps) {
        this.replayIterator = replayTimestamps.iterator();
        if (replayIterator.hasNext()) {
            lastTime.set(replayIterator.next());
        }
    }

    /**
     * Create replay provider from iterator.
     * 
     * @param iterator timestamp iterator
     */
    public ReplayTimeProvider(Iterator<Instant> iterator) {
        this.replayIterator = iterator;
        if (replayIterator.hasNext()) {
            lastTime.set(replayIterator.next());
        }
    }

    @Override
    public Instant now() {
        return lastTime.get();
    }

    /**
     * Advance to next timestamp in the replay sequence.
     * 
     * @return the new current time, or last time if sequence exhausted
     */
    public Instant advance() {
        if (replayIterator.hasNext()) {
            Instant next = replayIterator.next();
            lastTime.set(next);
            return next;
        }
        return lastTime.get();
    }

    /**
     * Check if more timestamps are available.
     * 
     * @return true if more timestamps exist
     */
    public boolean hasMore() {
        return replayIterator.hasNext();
    }

    /**
     * Set time directly (for testing or manual control).
     * 
     * @param instant the time to set
     */
    public void setTime(Instant instant) {
        lastTime.set(instant);
    }
}
