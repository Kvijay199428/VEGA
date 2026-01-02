package com.vegatrader.market.journal;

/**
 * JNI Bridge to the C++ Market Data Core.
 * Provides access to the JournalWriter and ReplayEngine.
 */
public class NativeJournal {

    static {
        loadNative();

        int abi = nativeAbiVersion();
        if (abi != 0xDEAD1701) {
            throw new UnsatisfiedLinkError(
                    "Native ABI mismatch. Expected 0xDEAD1701, got " + Integer.toHexString(abi));
        }
    }

    private static void loadNative() {
        try {
            // Priority:
            // 1. System property "vega.native.path"
            // 2. Default: backend/cpp/market_data_core/market_data_core.dll (relative to
            // project root)

            String defaultPath = java.nio.file.Paths.get("backend", "cpp", "market_data_core", "market_data_core.dll")
                    .toAbsolutePath().toString();
            String pathStr = System.getProperty("vega.native.path", defaultPath);

            java.io.File libFile = new java.io.File(pathStr);
            if (!libFile.exists()) {
                // Try fallback: ../../../backend/cpp... if running from
                // backend/java/vega-trader
                libFile = new java.io.File("../../../backend/cpp/market_data_core/market_data_core.dll");
                if (libFile.exists()) {
                    System.out.println("NativeJournal: Found DLL at fallback location: " + libFile.getAbsolutePath());
                } else {
                    throw new IllegalStateException("Native DLL not found at: " + pathStr + " or fallback.");
                }
            } else {
                System.out.println("NativeJournal: Found DLL at: " + libFile.getAbsolutePath());
            }

            System.load(libFile.getAbsolutePath());
        } catch (Throwable t) {
            System.err.println("NativeJournal: Failed to load library: " + t.getMessage());
            throw new RuntimeException(t);
        }
    }

    /**
     * Check ABI Version.
     * 
     * @return 0xVEGA1701 for current version
     */
    public static native int nativeAbiVersion();

    /**
     * Initialize a new Journal Writer.
     * 
     * @param filepath Path to the journal file
     * @param capacity Max size in bytes
     * @return Pointer to the native JournalWriter object
     */
    public static native long initWriter(String filepath, long capacity);

    /**
     * Append a canonical snapshot to the journal.
     * 
     * @param writerPtr    Pointer to JournalWriter
     * @param instrumentId Instrument ID
     * @param exchangeTs   Exchange Timestamp
     * @param receiveTs    Receive Timestamp
     * @param eventType    Event Type
     * @param payload      Byte array containing the payload struct
     */
    public static native void append(long writerPtr, int instrumentId, long exchangeTs, long receiveTs, int eventType,
            byte[] payload);

    /**
     * Close and free the Journal Writer.
     * 
     * @param writerPtr Pointer to JournalWriter
     */
    public static native void closeWriter(long writerPtr);

    // Replay Methods
    public static native long initReplay(String filepath);

    public static native long seek(long replayPtr, long timestamp);

    public static native void closeReplay(long replayPtr);
}
