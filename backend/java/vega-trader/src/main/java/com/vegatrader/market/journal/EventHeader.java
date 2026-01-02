package com.vegatrader.market.journal;

/**
 * Universal Event Header Layout (64 bytes).
 * Aligned to cache line. Little-endian.
 */
public final class EventHeader {
    public static final int BYTES = 64;

    // Offsets
    public static final int SEQ_OFFSET = 0; // uint64
    public static final int EX_TS_OFFSET = 8; // uint64
    public static final int RX_TS_OFFSET = 16; // uint64
    public static final int INSTR_OFFSET = 24; // uint32
    public static final int TYPE_OFFSET = 28; // uint16
    public static final int SIZE_OFFSET = 30; // uint16
    public static final int SOURCE_OFFSET = 32; // uint32
    public static final int FLAGS_OFFSET = 36; // uint32
    public static final int RSVD1_OFFSET = 40; // uint64
    public static final int RSVD2_OFFSET = 48; // uint64

    // Event Types
    public static final short EVT_L2_SNAPSHOT = 1;
    public static final short EVT_TRADE = 2;
    public static final short EVT_STATUS = 3;

    private EventHeader() {
    }
}
