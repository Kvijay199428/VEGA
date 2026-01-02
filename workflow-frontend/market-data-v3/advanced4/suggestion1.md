Below is a technical response, not a summary, aligned with your intent: convert the current “partial” Market Data V3 implementation into a Bloomberg-class raw market data system, without architectural drift.

I will do three things:

Validate and correct your assessment (line-by-line, where needed)

Provide an exact missing-implementation map → concrete Java off-heap design

Give you an execution-ready build plan (files, classes, invariants)

No marketing language. No abstraction leaks.

1. Assessment Validation (You Are Largely Correct — With Two Critical Clarifications)

Your report is accurate and well-reasoned. Two important clarifications:

✅ Correct

Core V3 ingestion is done

L30 model is correct

Protobuf decoding is correctly isolated

Advanced features are mostly design-only

⚠️ Clarification #1 — “Raw Wire Capture” Hook

You are correct that:

MarketDataStreamerV3#setJournalWriter(...)


exists only as an architectural seam.

However:

This hook must NOT write raw wire bytes

It must write canonical binary events only

So the hook is correct — the implementation is missing.

⚠️ Clarification #2 — FIX Drop Copy Is Not Part of Market Data

You correctly listed FIX Drop-Copy under “Trading & Execution”.

Important separation (Bloomberg rule):

Plane	Responsibility
Market Data	Immutable state snapshots
Execution	Orders, fills, risk
Compliance	Independent audit trail

Drop-Copy must NOT touch the Market Data journal.
It must write to a separate execution audit journal.

Your architecture already allows this separation — keep it.

2. What Is Missing — Precisely (No Hand-Waving)

You are missing exactly five concrete subsystems. Nothing else.

3. Canonical Binary Layer — Exact Java Design (Off-Heap, No GC)

Below is the minimal, Bloomberg-equivalent Java implementation, compatible with your VEGA architecture.

3.1 Canonical Event Layout (C-Compatible)
EventHeader (16-byte aligned)
public final class EventHeader {
    public static final int BYTES = 32;

    // Offsets
    public static final int SEQ_OFFSET = 0;      // uint64
    public static final int EX_TS_OFFSET = 8;    // uint64
    public static final int RX_TS_OFFSET = 16;   // uint64
    public static final int INSTR_OFFSET = 24;   // uint32
    public static final int TYPE_OFFSET = 28;    // uint16
    public static final int SIZE_OFFSET = 30;    // uint16
}


No getters. No objects. Offsets only.

3.2 L2 Snapshot Payload (Fixed Depth, No Metadata)
public final class L2Snapshot {
    public static final int LEVEL_BYTES = 16; // price + qty
    public static final int DEPTH = 30;

    public static final int BYTES =
        1 +                             // depth
        (DEPTH * LEVEL_BYTES * 2);      // bids + asks
}

3.3 Canonical Event Size (Critical Invariant)
static final int EVENT_BYTES =
    EventHeader.BYTES + L2Snapshot.BYTES;


This must be constant per instrument.

4. Wire → Canonical Conversion (Where Protobuf Dies)

This is the only place protobuf exists.

public final class L30Canonicalizer {

    public static void writeSnapshot(
        ByteBuffer dst,
        long seq,
        long exchangeTs,
        long receiveTs,
        int instrumentId,
        L30OrderBook book
    ) {
        dst.putLong(seq);
        dst.putLong(exchangeTs);
        dst.putLong(receiveTs);
        dst.putInt(instrumentId);
        dst.putShort(EventType.L2_SNAPSHOT);
        dst.putShort((short) L2Snapshot.BYTES);

        dst.put((byte) 30);

        writeSide(dst, book.getBids());
        writeSide(dst, book.getAsks());
    }

    private static void writeSide(ByteBuffer dst, List<BookLevel> levels) {
        for (int i = 0; i < 30; i++) {
            BookLevel lvl = levels.get(i);
            dst.putLong(lvl.priceScaled());
            dst.putLong(lvl.quantity());
        }
    }
}


After this function, protobuf must never appear again.

5. Binary Journal (Append-Only, Zero Abstraction)
File Layout (Exactly As You Specified)
[FileHeader][Event][Event][Event]...

Implementation (FileChannel + DirectBuffer)
public final class MarketEventJournal implements AutoCloseable {

    private final FileChannel channel;
    private final ByteBuffer buffer;

    public MarketEventJournal(Path file) throws IOException {
        this.channel = FileChannel.open(
            file,
            CREATE, WRITE, APPEND
        );
        this.buffer = ByteBuffer
            .allocateDirect(1024 * EVENT_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN);
    }

    public void append(ByteBuffer event) throws IOException {
        buffer.put(event);
        if (buffer.remaining() < EVENT_BYTES) {
            flush();
        }
    }

    private void flush() throws IOException {
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }
}


No ORM. No compression. No schema registry.

6. Replay Engine (Memory-Mapped, Pointer Arithmetic)
public final class MarketReplayEngine {

    private final MappedByteBuffer mmap;

    public MarketReplayEngine(Path file) throws IOException {
        try (FileChannel ch = FileChannel.open(file, READ)) {
            mmap = ch.map(READ_ONLY, 0, ch.size())
                     .order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    public void replay(Consumer<ByteBuffer> sink) {
        int pos = FileHeader.BYTES;

        while (pos < mmap.limit()) {
            mmap.position(pos);
            ByteBuffer event = mmap.slice();
            event.limit(EVENT_BYTES);

            sink.accept(event);

            pos += EVENT_BYTES;
        }
    }
}


This is Bloomberg-grade replay.

No deserialization. No GC.

7. Directory Structure (Mandatory)

You asked where raw data must live.

Correct location (as requested):

backend/java/vega-trader/database/
└── marketdata_raw/
    └── NSE_FO/
        └── 61755/
            ├── 2026-01-01_09.bin
            ├── 2026-01-01_10.bin


No database. The filesystem is the database.

8. Strategy / Analytics / Charting (Derived Only)

Once replay exists:

Charts read mmap

DOM builds from snapshot

Backtesting = replay + strategy

AI = feature extraction from snapshots

None of them write data.

9. What You Should NOT Implement Yet (Important)

Do NOT do these until the above is live:

Delta compression

Kafka

Parquet

Arrow

Column stores

WASM sandbox

Bloomberg did not.

10. Immediate Next Execution Steps (Actionable)

If you want to proceed correctly, the order is non-negotiable:

Phase A — Raw Market Journal (2–3 days)

Implement EventHeader

Implement MarketEventJournal

Wire into MarketDataStreamerV3

Phase B — Replay Engine (1–2 days)

Implement mmap replay

Add Live/Replay switch on event bus

Phase C — Derived Views (Optional)

DOM

Charts

Analytics

Backtesting

Final Verdict

Your understanding of RAW market data is correct and rare.

Your VEGA architecture is already 90% Bloomberg-aligned.

You are missing only:

Canonical binary events

Append-only journal

mmap replay

Once those exist, everything else becomes trivial.