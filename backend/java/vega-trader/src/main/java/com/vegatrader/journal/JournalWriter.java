package com.vegatrader.journal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Append-only Journal Writer for raw wire frames.
 * Uses FileChannel for high-performance sequential writes.
 */
@Service
public class JournalWriter {

    private static final Logger logger = LoggerFactory.getLogger(JournalWriter.class);

    private static final String BASE_DIR = "marketdata/journal";
    private static final int BUFFER_SIZE = 1024 * 1024; // 1MB buffer

    private FileChannel channel;
    private final ByteBuffer headerBuffer = ByteBuffer.allocateDirect(WireFrameHeader.SIZE_BYTES);
    private final AtomicInteger connectionIdCounter = new AtomicInteger(1);

    // Current connection ID for this session
    private int currentConnectionId;

    public JournalWriter() {
        this.currentConnectionId = connectionIdCounter.getAndIncrement();
        initChannel();
    }

    private void initChannel() {
        try {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            File dir = new File(BASE_DIR, dateStr);
            if (!dir.exists())
                dir.mkdirs();

            // Journal file: market-v3-{timestamp}.bin
            File file = new File(dir, "market-v3-" + System.currentTimeMillis() + ".bin");
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();

            logger.info("Journal initialized: {}", file.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Failed to initialize journal", e);
        }
    }

    /**
     * Append raw wire frame to journal.
     * Thread-safe via synchronized (could be optimized with spin-lock or
     * disruptor).
     */
    public synchronized void append(byte[] payload) {
        if (channel == null || payload == null)
            return;

        try {
            // Prepare header
            headerBuffer.clear();
            WireFrameHeader.write(headerBuffer, System.nanoTime(), currentConnectionId, payload.length,
                    WireFrameHeader.PROTO_WS, WireFrameHeader.COMPRESSION_NONE);
            headerBuffer.flip();

            // Write Header + Payload
            // Optimization: scatter-gather write if payload was ByteBuffer,
            // but payload is byte[] here.

            channel.write(headerBuffer);
            channel.write(ByteBuffer.wrap(payload));

        } catch (Exception e) {
            logger.error("Journal write failed", e);
        }
    }

    public void close() {
        try {
            if (channel != null)
                channel.close();
        } catch (Exception e) {
            // ignore
        }
    }
}
