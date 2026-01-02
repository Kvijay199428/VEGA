package com.vegatrader.market.journal;

import sun.misc.Unsafe;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.LongConsumer;

/**
 * High-Performance Memory-Mapped Replay Engine.
 * Supports zero-copy reading and random seek by timestamp.
 */
@org.springframework.stereotype.Service
public final class MarketReplayEngine implements AutoCloseable {

    private FileChannel channel;
    private MappedByteBuffer mmap;
    private long baseAddr;
    private long length;
    private final Unsafe U = UnsafeAccess.U;

    // Constants from our layout
    private static final int HEADER_SIZE = EventHeader.BYTES;
    private static final int SNAPSHOT_SIZE = L2SnapshotPayload.BYTES;
    private static final int RECORD_SIZE = HEADER_SIZE + SNAPSHOT_SIZE;

    public MarketReplayEngine() {
        // Default constructor for Spring
    }

    public synchronized void loadJournal(Path file) throws IOException {
        close(); // Close existing if any

        this.length = Files.size(file);
        this.channel = FileChannel.open(file, StandardOpenOption.READ);
        this.mmap = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
        this.baseAddr = getDirectBufferAddress(mmap);
    }

    private long getDirectBufferAddress(java.nio.Buffer buffer) {
        try {
            java.lang.reflect.Field addressField = java.nio.Buffer.class.getDeclaredField("address");
            addressField.setAccessible(true);
            return addressField.getLong(buffer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get buffer address", e);
        }
    }

    /**
     * Seek to the first record with exchange timestamp >= targetTs.
     * 
     * @param targetTs Exchange timestamp (epoch nanos or micros depending on
     *                 source)
     * @return pointer address to the record, or -1 if not found.
     */
    public long seekByTimestamp(long targetTs) {
        long ptr = baseAddr;
        long endAddr = baseAddr + length;

        while (ptr + RECORD_SIZE <= endAddr) {
            // Read exchange timestamp from header (Offset 8)
            long ts = U.getLong(ptr + EventHeader.EX_TS_OFFSET);
            if (ts >= targetTs) {
                return ptr;
            }
            ptr += RECORD_SIZE;
        }
        return -1;
    }

    /**
     * Replay from startPtr to end of file, feeding pointers to the sink.
     * 
     * @param startPtr Address to start replay from
     * @param sink     Consumer for the record pointer
     */
    public void replay(long startPtr, LongConsumer sink) {
        long endAddr = baseAddr + length;
        // Validate startPtr
        if (startPtr < baseAddr || startPtr >= endAddr) {
            return;
        }

        // Align check (optional but good for safety)
        long offset = startPtr - baseAddr;
        if (offset % RECORD_SIZE != 0) {
            // Align
            startPtr -= (offset % RECORD_SIZE);
        }

        for (long p = startPtr; p + RECORD_SIZE <= endAddr; p += RECORD_SIZE) {
            sink.accept(p);
        }
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
        // mmap remains valid until GC unmaps it, but channel is closed.
    }
}
