package com.vegatrader.journal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

/**
 * Reader for binary market data journals.
 * Used for replay and verification.
 */
@Service
public class JournalReader {

    private static final Logger logger = LoggerFactory.getLogger(JournalReader.class);

    /**
     * Replay a journal file.
     * 
     * @param journalPath Absolute path to .bin file
     * @param callback    Callback for each raw payload
     */
    public void replay(String journalPath, Consumer<byte[]> callback) {
        File file = new File(journalPath);
        if (!file.exists()) {
            logger.error("Journal file not found: {}", journalPath);
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
                FileChannel channel = raf.getChannel()) {

            ByteBuffer headerBuffer = ByteBuffer.allocate(WireFrameHeader.SIZE_BYTES);

            logger.info("Starting replay of: {}", journalPath);
            int records = 0;

            while (channel.read(headerBuffer) == WireFrameHeader.SIZE_BYTES) {
                headerBuffer.flip();

                // Parse Header
                long recvTs = headerBuffer.getLong();
                int connId = headerBuffer.getInt();
                int payloadSize = headerBuffer.getInt();
                short proto = headerBuffer.getShort();
                short compress = headerBuffer.getShort();

                headerBuffer.clear();

                // Validation
                if (payloadSize <= 0 || payloadSize > 10 * 1024 * 1024) {
                    logger.error("Corrupt journal frame: size={}", payloadSize);
                    break;
                }

                // Read Payload
                ByteBuffer payloadBuffer = ByteBuffer.allocate(payloadSize);
                while (payloadBuffer.hasRemaining()) {
                    if (channel.read(payloadBuffer) < 0)
                        break; // EOF unexpected
                }

                if (payloadBuffer.position() == payloadSize) {
                    callback.accept(payloadBuffer.array());
                    records++;
                } else {
                    logger.warn("Incomplete payload reading record #{}", records);
                    break;
                }
            }

            logger.info("Replay complete. Records processed: {}", records);

        } catch (Exception e) {
            logger.error("Failed to replay journal", e);
        }
    }
}
