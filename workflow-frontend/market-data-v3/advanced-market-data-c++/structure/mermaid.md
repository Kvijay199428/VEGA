Here’s a detailed Mermaid diagram representing the VEGA market data system architecture similar to Bloomberg terminals:

flowchart TD
    %% Data Ingestion
    A[WebSocket Market Data Stream] --> B[Raw Byte Feed (Protobuf)]

    %% Off-Heap Storage
    B --> C[Java ByteBuffer / Off-Heap Memory]
    
    %% Lock-Free Ring Buffer
    C --> D[Lock-Free Ring Buffer]
    
    %% Delta Compression
    D --> E[Delta-Encoded Compression Layer]
    
    %% Persistent Storage
    E --> F[Binary Append-Only Journal (Versioned, with Headers & Metadata)]
    
    %% Shared Memory Bus for Multiple Clients
    D --> G[Shared Memory Bus]
    
    %% Replay Engine
    F --> H[Replay Engine with Time Seek]
    G --> H
    
    %% Analytics & Terminal UI
    H --> I[Terminal UI / Analytics Dashboard]
    G --> I
    
    %% Optional caching layer
    D --> J[In-Memory Cache for Fast Access]
    J --> I


This diagram captures:

Ingestion layer: WebSocket feed → raw protobuf.

Memory layer: off-heap ByteBuffer + lock-free ring buffer for high-speed streaming.

Compression & storage: delta-encoded compression → append-only journal with headers, metadata, and versioning.

Shared memory bus: allows multiple clients to access real-time data without extra copies.

Replay & analytics: replay engine with time seek → terminal UI for analytics.

Caching: optional memory cache for instant UI updates.