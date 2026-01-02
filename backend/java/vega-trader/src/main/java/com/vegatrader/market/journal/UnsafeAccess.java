package com.vegatrader.market.journal;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

/**
 * Access to sun.misc.Unsafe for off-heap memory operations.
 * Critical for high-frequency binary journaling.
 */
public final class UnsafeAccess {
    public static final Unsafe U;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("CRITICAL: Failed to acquire Unsafe access", e);
        }
    }

    private UnsafeAccess() {
        // Static utility
    }
}
