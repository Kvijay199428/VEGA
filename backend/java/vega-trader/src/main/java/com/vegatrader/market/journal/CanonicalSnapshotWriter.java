package com.vegatrader.market.journal;

import com.vegatrader.market.depth.model.BookLevel;
import com.vegatrader.market.depth.model.L30OrderBook;
import sun.misc.Unsafe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * High-Performance Off-Heap Canonical Snapshot Writer.
 * Writes FULL_D30 snapshots to append-only journal without GC.
 */
public final class CanonicalSnapshotWriter implements AutoCloseable {

    private static final int HEADER_SIZE = EventHeader.BYTES;
    private static final int SNAPSHOT_SIZE = L2SnapshotPayload.BYTES;
    private static final int RECORD_SIZE = HEADER_SIZE + SNAPSHOT_SIZE;

    private final FileChannel channel;
    private final long bufferAddr;
    private long sequence = 0;
    private final Unsafe U = UnsafeAccess.U;

    // Helper object for wrapping address as generic ByteBuffer for channel write
    // In production we might avoid DirectBuffer allocation per write, but
    // FileChannel needs ByteBuffer
    // We can reuse a single DirectByteBuffer that points to our address if we were
    // using JNI,
    // but here we allocate a real DirectBuffer once and use its address.
    private final ByteBuffer directBuffer;

    public CanonicalSnapshotWriter(FileChannel channel) {
        this.channel = channel;
        // Allocate off-heap memory
        this.directBuffer = ByteBuffer.allocateDirect(RECORD_SIZE);
        // Get the address of the direct buffer - this is safe because we keep a strong
        // ref to directBuffer
        // Note: The correct way to get address is via reflection or internal API if not
        // using JNI
        // For simplicity/robustness in pure Java, we can just use Unsafe to put into
        // the buffer's memory
        // IF we knew the address. DirectByteBuffer usually exposes address.
        // HOWEVER, standard way: allocate via Unsafe, create DirectBuffer wrapper?
        // Or allocate DirectBuffer and use Unsafe to write into it?
        // Let's rely on Unsafe.allocateMemory and create a DirectBuffer wrapper for the
        // channel write if needed,
        // OR just allocate a DirectBuffer and find its address.

        // Simpler approach for reliability:
        // 1. Allocate DirectBuffer (standard).
        // 2. Get its address via reflection (standard hack).
        // 3. Use Unsafe to write to that address.
        // 4. Use channel.write(buffer).

        this.bufferAddr = getDirectBufferAddress(this.directBuffer);
    }

    // Fallback or specific implementation to get address
    private long getDirectBufferAddress(ByteBuffer buffer) {
        try {
            java.lang.reflect.Field addressField = java.nio.Buffer.class.getDeclaredField("address");
            addressField.setAccessible(true);
            return addressField.getLong(buffer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get DirectBuffer address", e);
        }
    }

    public void write(L30OrderBook book, long exchangeTs, long receiveTs, int instrumentId) throws IOException {
        long addr = bufferAddr;

        // 1. HEADER
        U.putLong(addr + EventHeader.SEQ_OFFSET, ++sequence);
        U.putLong(addr + EventHeader.EX_TS_OFFSET, exchangeTs);
        U.putLong(addr + EventHeader.RX_TS_OFFSET, receiveTs);
        U.putInt(addr + EventHeader.INSTR_OFFSET, instrumentId);
        U.putShort(addr + EventHeader.TYPE_OFFSET, EventHeader.EVT_L2_SNAPSHOT);
        U.putShort(addr + EventHeader.SIZE_OFFSET, (short) SNAPSHOT_SIZE);
        // Zero out reserved
        U.putInt(addr + EventHeader.SOURCE_OFFSET, 0);
        U.putInt(addr + EventHeader.FLAGS_OFFSET, 0);
        U.putLong(addr + EventHeader.RSVD1_OFFSET, 0);
        U.putLong(addr + EventHeader.RSVD2_OFFSET, 0);

        // 2. PAYLOAD
        long payloadAddr = addr + HEADER_SIZE;

        // Depth
        U.putByte(payloadAddr + L2SnapshotPayload.OFF_DEPTH, (byte) 30);

        // Bids
        writeSide(payloadAddr + L2SnapshotPayload.OFF_BIDS, book.getBids());

        // Asks
        writeSide(payloadAddr + L2SnapshotPayload.OFF_ASKS, book.getAsks());

        // 3. FLUSH
        directBuffer.clear();
        channel.write(directBuffer);
    }

    private void writeSide(long startAddr, java.util.List<BookLevel> levels) {
        int size = levels != null ? Math.min(levels.size(), L2SnapshotPayload.DEPTH) : 0;

        for (int i = 0; i < L2SnapshotPayload.DEPTH; i++) {
            long levelAddr = startAddr + (i * L2SnapshotPayload.LEVEL_BYTES);

            if (i < size) {
                BookLevel level = levels.get(i);
                // Assumption: Price and Qty are safe to convert to long (scaled)
                // In BookLevel, price is double, qty is long.
                // We multiply price by 100 for scaling as per spec (or 10000)
                // suggestion2.md says "price * 100", lets use that.
                long scaledPrice = (long) (level.getPrice() * 100);
                long qty = level.getQuantity();

                U.putLong(levelAddr + L2SnapshotPayload.LEVEL_OFF_PRICE, scaledPrice);
                U.putLong(levelAddr + L2SnapshotPayload.LEVEL_OFF_QTY, qty);
            } else {
                // Zero fill empty levels
                U.putLong(levelAddr + L2SnapshotPayload.LEVEL_OFF_PRICE, 0);
                U.putLong(levelAddr + L2SnapshotPayload.LEVEL_OFF_QTY, 0);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        // DirectBuffer GC will handle deallocation eventually
    }
}
