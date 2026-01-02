package com.vegatrader.market.journal;

import java.io.File;

public class JniSanityCheck {
    public static void main(String[] args) {
        System.out.println("=== JNI Sanity Check ===");

        try {
            // Manually load the library from the known build location for testing
            File libFile = new File("../../../backend/cpp/market_data_core/market_data_core.dll");
            System.out.println("Loading library from: " + libFile.getAbsolutePath());

            if (!libFile.exists()) {
                System.err.println("ERROR: DLL not found at expected path!");
                System.exit(1);
            }

            System.load(libFile.getAbsolutePath());
            System.out.println("SUCCESS: market_data_core.dll loaded successfully.");

            // Try to initialize a writer (native call)
            // capacity 1MB, temp file
            File tempJournal = File.createTempFile("msg_journal_test", ".dat");
            tempJournal.deleteOnExit();

            System.out.println("Attempting NativeJournal.initWriter...");
            long ptr = NativeJournal.initWriter(tempJournal.getAbsolutePath(), 1024 * 1024);
            System.out.println("Native pointer returned: " + ptr);

            if (ptr == 0) {
                System.err.println("FAILED: initWriter returned 0 (NULL)");
            } else {
                System.out.println("SUCCESS: Native Writer created.");
                NativeJournal.closeWriter(ptr);
                System.out.println("SUCCESS: Native Writer closed.");
            }

        } catch (Throwable t) {
            System.err.println("JNI LOAD FAILURE:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
