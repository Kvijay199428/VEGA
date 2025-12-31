package com.vegatrader.upstox.api.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class for JSON serialization/deserialization.
 *
 * @since 2.0.0
 */
public final class JsonUtils {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private static final Gson COMPACT_GSON = new GsonBuilder()
            .create();

    private JsonUtils() {
        // Utility class - no instantiation
    }

    /**
     * Converts object to JSON string (pretty printed).
     *
     * @param obj the object
     * @return JSON string
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * Converts object to compact JSON string.
     *
     * @param obj the object
     * @return compact JSON string
     */
    public static String toCompactJson(Object obj) {
        return COMPACT_GSON.toJson(obj);
    }

    /**
     * Parses JSON string to object.
     *
     * @param json  the JSON string
     * @param clazz the target class
     * @param <T>   the type
     * @return parsed object
     * @throws JsonSyntaxException if JSON is invalid
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Parses JSON string to object (safe - returns null on error).
     *
     * @param json  the JSON string
     * @param clazz the target class
     * @param <T>   the type
     * @return parsed object or null
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return GSON.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Checks if string is valid JSON.
     *
     * @param json the JSON string
     * @return true if valid
     */
    public static boolean isValidJson(String json) {
        try {
            GSON.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}
