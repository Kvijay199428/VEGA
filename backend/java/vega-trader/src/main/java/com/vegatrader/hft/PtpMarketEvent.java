package com.vegatrader.hft;

import com.vegatrader.market.dto.LiveMarketSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

/**
 * PTP-Timestamped Market Event.
 * Carries hardware timestamp (from NIC) and software timestamp (kernel/user).
 */
public class PtpMarketEvent {

    /** Hardware timestamp from PTP clock (nanoseconds) */
    private long ptpNs;

    /** Local receive timestamp (nanoseconds, CLOCK_REALTIME or CLOCK_MONOTONIC) */
    private long recvNs;

    /** Instrument Identifier (Internal int ID for speed) */
    private int instrumentId;

    /** The actual payload (snapshot) */
    private LiveMarketSnapshot snapshot;

    public PtpMarketEvent() {
    }

    public PtpMarketEvent(long ptpNs, long recvNs, int instrumentId, LiveMarketSnapshot snapshot) {
        this.ptpNs = ptpNs;
        this.recvNs = recvNs;
        this.instrumentId = instrumentId;
        this.snapshot = snapshot;
    }

    // Getters
    public long getPtpNs() {
        return ptpNs;
    }

    public long getRecvNs() {
        return recvNs;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public LiveMarketSnapshot getSnapshot() {
        return snapshot;
    }

    // Setters
    public void setPtpNs(long ptpNs) {
        this.ptpNs = ptpNs;
    }

    public void setRecvNs(long recvNs) {
        this.recvNs = recvNs;
    }

    public void setInstrumentId(int instrumentId) {
        this.instrumentId = instrumentId;
    }

    public void setSnapshot(LiveMarketSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Serialize to ByteBuffer for ring buffer or journaling.
     */
    public ByteBuffer serialize() {
        // Estimate size: 8 (ptp) + 8 (recv) + 4 (id) + snapshot size
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);
        bb.putLong(ptpNs);
        bb.putLong(recvNs);
        bb.putInt(instrumentId);

        // Simple serialization of snapshot fields (for demo)
        // In real HFT, we'd copy primitive fields directly
        if (snapshot != null) {
            bb.putDouble(snapshot.getLtp());
            bb.putLong(snapshot.getVolume());
        }

        bb.flip();
        return bb;
    }

    // Builder
    public static PtpMarketEventBuilder builder() {
        return new PtpMarketEventBuilder();
    }

    public static class PtpMarketEventBuilder {
        private PtpMarketEvent event = new PtpMarketEvent();

        public PtpMarketEventBuilder ptpNs(long ns) {
            event.setPtpNs(ns);
            return this;
        }

        public PtpMarketEventBuilder recvNs(long ns) {
            event.setRecvNs(ns);
            return this;
        }

        public PtpMarketEventBuilder instrumentId(int id) {
            event.setInstrumentId(id);
            return this;
        }

        public PtpMarketEventBuilder snapshot(LiveMarketSnapshot s) {
            event.setSnapshot(s);
            return this;
        }

        public PtpMarketEvent build() {
            return event;
        }
    }
}
