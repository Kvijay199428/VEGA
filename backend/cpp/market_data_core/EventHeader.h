#ifndef EVENT_HEADER_H
#define EVENT_HEADER_H

#include <cstdint>

#pragma pack(push, 1)

/**
 * Universal Event Header (64 bytes)
 * 
 * Layout:
 * [Sequence (8)] [ExTs (8)] [RxTs (8)] [InstID (4)] [Type (2)] [Size (2)] [Padding (32)]
 */
typedef struct {
    uint64_t sequence;        // Monotonic sequence number
    uint64_t exchange_ts_ns;  // Exchange timestamp (nanoseconds)
    uint64_t receive_ts_ns;   // Receive timestamp (nanoseconds)
    uint32_t instrument_id;   // Unique internal instrument ID
    uint16_t event_type;      // Event type (e.g. 1=L2_SNAPSHOT, 2=TRADE)
    uint16_t payload_size;    // Size of the payload following this header
    uint8_t  reserved[32];    // Padding to reach 64 bytes cache-line alignment
} EventHeader;

#pragma pack(pop)

static_assert(sizeof(EventHeader) == 64, "EventHeader must be 64 bytes");

#endif // EVENT_HEADER_H
