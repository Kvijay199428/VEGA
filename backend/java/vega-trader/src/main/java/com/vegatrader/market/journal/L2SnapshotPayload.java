package com.vegatrader.market.journal;

/**
 * L2 Snapshot Payload Layout (Fixed 30 Depth).
 * Total Size: 968 bytes.
 */
public final class L2SnapshotPayload {
    public static final int BYTES = 968;
    public static final int DEPTH = 30;

    // Offsets
    // [0-0] Depth (uint8)
    // [1-7] Reserved/Alignment (padding)
    // [8-487] Bids (30 * 16 bytes)
    // [488-967] Asks (30 * 16 bytes)

    public static final int OFF_DEPTH = 0;
    public static final int OFF_BIDS = 8;
    public static final int SINGLE_SIDE_BYTES = DEPTH * 16;
    public static final int OFF_ASKS = OFF_BIDS + SINGLE_SIDE_BYTES;

    // Level Offsets (relative to start of Bids/Asks array)
    // struct L2Level { int64_t price; int64_t qty; }
    public static final int LEVEL_BYTES = 16;
    public static final int LEVEL_OFF_PRICE = 0;
    public static final int LEVEL_OFF_QTY = 8;

    private L2SnapshotPayload() {
    }
}
