package com.vegatrader.upstox.api.websocket.disruptor;

/**
 * Pre-allocated event for LMAX Disruptor ring buffer.
 * 
 * <p>
 * Design principles:
 * <ul>
 * <li>Mutable fields (reused, not allocated per event)</li>
 * <li>Binary payload (pre-serialized upstream)</li>
 * <li>Minimal fields (cache-friendly)</li>
 * <li>Clear method for reset</li>
 * </ul>
 * 
 * <p>
 * This enables:
 * <ul>
 * <li>Zero GC pressure</li>
 * <li>Cache-line efficiency</li>
 * <li>Deterministic latency</li>
 * </ul>
 * 
 * @since 3.1.0
 */
public class MarketEvent {

    /** Instrument key (e.g., "NSE_EQ|INE002A01018") */
    public String instrumentKey;

    /** Pre-serialized payload (JSON, Protobuf, or MessagePack) */
    public byte[] payload;

    /** Exchange timestamp in milliseconds */
    public long exchangeTimestamp;

    /** True if this is a snapshot event */
    public boolean snapshot;

    /**
     * Clears the event for reuse.
     * Called by Disruptor after processing.
     */
    public void clear() {
        this.instrumentKey = null;
        this.payload = null;
        this.exchangeTimestamp = 0;
        this.snapshot = false;
    }

    @Override
    public String toString() {
        return "MarketEvent{" +
                "instrumentKey='" + instrumentKey + '\'' +
                ", payloadSize=" + (payload != null ? payload.length : 0) +
                ", exchangeTs=" + exchangeTimestamp +
                ", snapshot=" + snapshot +
                '}';
    }
}
