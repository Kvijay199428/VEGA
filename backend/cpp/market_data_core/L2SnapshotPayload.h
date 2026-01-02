#ifndef L2_SNAPSHOT_PAYLOAD_H
#define L2_SNAPSHOT_PAYLOAD_H

#include <cstdint>
#include "EventHeader.h"

#pragma pack(push, 1)

// L2 Level Structure (16 bytes)
typedef struct {
    int64_t price;    // Price * 100 (scaled integer)
    int64_t quantity; // Quantity
} L2Level;

// L2 Snapshot Payload (30 Depth)
// Layout: [Depth(1)] [Reserved(7)] [Bids(30*16)] [Asks(30*16)]
// Total: 8 + 480 + 480 = 968 bytes
typedef struct {
    uint8_t depth;        // Current depth (e.g. 30)
    uint8_t reserved[7];  // Alignment padding
    L2Level bids[30];     // Bid levels
    L2Level asks[30];     // Ask levels
} L2SnapshotPayload;

#pragma pack(pop)

static_assert(sizeof(L2Level) == 16, "L2Level must be 16 bytes");
static_assert(sizeof(L2SnapshotPayload) == 968, "L2SnapshotPayload must be 968 bytes");

#endif // L2_SNAPSHOT_PAYLOAD_H
