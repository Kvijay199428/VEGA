Let’s design a full Bloomberg-style ultra-low-latency physical layout for VEGA’s market data feed system, showing:

Multiple feeds and symbols

Shared memory bus with concurrent clients

Lock-free ring buffer with wrap-around

Delta-compressed data

Hex offsets for memory-mapped regions and file storage

Here’s a Mermaid diagram representing the complete physical-layer architecture:

flowchart TD
    %% ----------------- Market Data Feeds -----------------
    subgraph Market_Feeds["Market Data Feeds (Multi-Symbol)"]
        A1[NSE_FO|61755] --> B1[Protobuf Bytes Stream]
        A2[NSE_EQ|500325] --> B2[Protobuf Bytes Stream]
        A3[NSE_FO|61756] --> B3[Protobuf Bytes Stream]
        B1 --> C[Off-Heap Ring Buffer]
        B2 --> C
        B3 --> C
    end

    %% ----------------- Off-Heap Ring Buffer Layer -----------------
    subgraph OffHeap["Off-Heap Lock-Free Ring Buffer (Direct ByteBuffer)"]
        C --> D1[Slot 0: Feed Record 1]
        C --> D2[Slot 1: Feed Record 2]
        C --> D3[Slot 2: Feed Record 3]
        C --> D4[Slot n: Feed Record n]
        D1 --> E[Delta Compression Layer]
        D2 --> E
        D3 --> E
        D4 --> E
    end

    %% ----------------- Shared Memory Bus -----------------
    subgraph SharedMemory["Bloomberg-style Shared Memory Bus"]
        E --> F1[Client Terminal 1 (Read Offset 0x0000)]
        E --> F2[Client Terminal 2 (Read Offset 0x20000)]
        E --> F3[Analytics / Dashboard (Read Offset 0x40000)]
        F1 --> G[Memory-Mapped Region 1]
        F2 --> G
        F3 --> G
    end

    %% ----------------- File Storage (Append-Only) -----------------
    subgraph BinaryStorage["Persistent Binary Storage"]
        E --> H[Binary File: Append-Only Journal (.bin)]
        H --> H1[Header Block (0x0000 - 0x0030)]
        H1 --> H1a[Version: 1 byte]
        H1 --> H1b[Feed Type: 1 byte]
        H1 --> H1c[Symbol ID: 4 bytes]
        H1 --> H1d[Timestamp: 8 bytes]
        H --> H2[Data Blocks (0x0030 - 0xF000)]
        H2 --> H2a[Record 1: LTP + Bid/Ask + OI + Greeks + OHLC]
        H2 --> H2b[Record 2 ... Record n]
        H2 --> H2c[Delta-Encoded Fields]
        H --> H3[Block Index Table (0xF000 - 0xF100)]
        H3 --> H3a[Start Offset: 8 bytes]
        H3 --> H3b[End Offset: 8 bytes]
        H3 --> H3c[Timestamp Mapping: 8 bytes]
    end

    %% ----------------- Replay / Time-Seek Engine -----------------
    subgraph ReplayEngine["Replay & Historical Access"]
        H3 --> I[Time Seek Index Lookup]
        I --> J[Load Specific Data Block(s)]
        J --> K[Delta Decode to Full State]
        K --> F3
    end

    %% ----------------- Ring Buffer Wrap-Around -----------------
    subgraph WrapAround["Ring Buffer Wrap-Around"]
        D4 --> D1
    end

    %% ----------------- Styles -----------------
    style Market_Feeds fill:#f9f,stroke:#333,stroke-width:2px
    style OffHeap fill:#9cf,stroke:#333,stroke-width:2px
    style SharedMemory fill:#fc9,stroke:#333,stroke-width:2px
    style BinaryStorage fill:#c9f,stroke:#333,stroke-width:2px
    style ReplayEngine fill:#9f9,stroke:#333,stroke-width:2px
    style WrapAround fill:#f96,stroke:#333,stroke-width:2px

✅ Key Highlights:

Multi-Feed, Multi-Symbol Support

Each symbol feed enters the off-heap ring buffer independently.

Protobuf frames are stored raw, no decoding until client access.

Off-Heap Lock-Free Ring Buffer

Each slot contains one feed record.

Ring buffer wraps around to overwrite old records (circular buffer).

Delta Compression

Only changed fields are stored per update, saving memory and disk I/O.

Full state reconstructed on demand.

Shared Memory Bus

Clients access memory-mapped regions directly.

Multiple terminals can read concurrently without blocking writers.

Binary Append-Only Storage

Includes headers, data blocks, delta-encoded records, and index tables.

Hex offsets provide precise physical layout.

Replay / Historical Engine

Uses block index table to seek exact timestamps.

Delta decoding reconstructs full market state for playback.

Ring Buffer Wrap-Around

Ensures constant memory footprint.

Maintains ultra-low latency for live market feed.