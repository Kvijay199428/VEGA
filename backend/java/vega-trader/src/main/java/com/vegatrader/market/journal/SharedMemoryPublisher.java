package com.vegatrader.market.journal;

import sun.misc.Unsafe;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared Memory Publisher (Ring Buffer).
 * Publishes events to a memory-mapped ring buffer for UI consumption
 * (Zero-Serialization).
 * 
 * Layout:
 * [WriteSeq (8b)] [Size (8b)] [Data Ring ...]
 */
public final class SharedMemoryPublisher implements AutoCloseable {

    private static final int HEADER_BYTES = 16;
    private static final int RING_SIZE = 1024 * 1024 * 64; // 64 MB ring
    private static final String SHM_FILE = "market_data_shm.bin";

    // Offsets within Header
    private static final int OFF_SEQ = 0;
    private static final int OFF_SIZE = 8;

    private final FileChannel channel;
    private final MappedByteBuffer mmap;
    private final long baseAddr;
    private final long ringDataAddr;
    private final Unsafe U = UnsafeAccess.U;

    // Local sequencing
    private final AtomicLong writeSequence = new AtomicLong(0);

    public SharedMemoryPublisher() {
        try {
            Path path = Paths.get(SHM_FILE);
            // Create file if not exists
            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
                raf.setLength(HEADER_BYTES + RING_SIZE);
            }

            this.channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
            this.mmap = channel.map(FileChannel.MapMode.READ_WRITE, 0, HEADER_BYTES + RING_SIZE);
            this.baseAddr = getDirectBufferAddress(mmap);
            this.ringDataAddr = baseAddr + HEADER_BYTES;

            // Initialize header
            U.putLong(baseAddr + OFF_SIZE, RING_SIZE);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Shared Memory Bus", e);
        }
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
     * Publish a raw canonical record to the ring buffer.
     * 
     * @param srcAddr Pointer to the record in off-heap memory
     * @param length  Length of the record
     */
    public void publish(long srcAddr, int length) {
        long seq = writeSequence.getAndIncrement();

        // Ring buffer position
        long offset = (seq * length) % RING_SIZE;
        long targetAddr = ringDataAddr + offset;

        // Copy memory (memcpy)
        U.copyMemory(srcAddr, targetAddr, length);

        // Update Write Sequence (Release semantics)
        U.putLongVolatile(null, baseAddr + OFF_SEQ, seq);
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }
}
