package com.vegatrader.journal;

import java.nio.ByteBuffer;

/**
 * Binary Wire Frame Header.
 * 
 * struct WireFrameHeader {
 * uint64_t recv_ts_ns; // local receive time
 * uint32_t conn_id; // websocket/session id
 * uint32_t payload_size; // size of raw payload
 * uint16_t protocol; // 1=WS, 2=TCP
 * uint16_t compression; // 0=none, 1=gzip
 * };
 * Total Size: 8 + 4 + 4 + 2 + 2 = 20 bytes.
 */
public class WireFrameHeader {

    public static final int SIZE_BYTES = 20;

    public static final int PROTO_WS = 1;
    public static final int PROTO_TCP = 2;

    public static final int COMPRESSION_NONE = 0;
    public static final int COMPRESSION_GZIP = 1;

    public static void write(ByteBuffer buffer, long recvTsNs, int connId, int payloadSize, int protocol,
            int compression) {
        buffer.putLong(recvTsNs);
        buffer.putInt(connId);
        buffer.putInt(payloadSize);
        buffer.putShort((short) protocol);
        buffer.putShort((short) compression);
    }
}
