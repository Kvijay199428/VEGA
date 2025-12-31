package com.vegatrader.upstox.api.generator;

import com.vegatrader.upstox.api.endpoints.UpstoxEndpoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Fluent URL builder for constructing Upstox API URLs.
 * <p>
 * This builder provides a type-safe, fluent API for constructing complete URLs
 * with base URL, endpoint path, path parameters, and query parameters.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * String url = UpstoxUrlBuilder.create()
 *         .baseUrl("https://api.upstox.com/v2")
 *         .endpoint(OrderEndpoints.GET_ORDER_DETAILS)
 *         .pathParam("order_id", "240127000123456")
 *         .queryParam("segment", "NSE")
 *         .build();
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpointGenerator
 */
public final class UpstoxUrlBuilder {

    private String baseUrl;
    private String path;
    private final Map<String, String> pathParams = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();

    private UpstoxUrlBuilder() {
        // Private constructor to enforce factory pattern
    }

    /**
     * Creates a new URL builder instance.
     *
     * @return a new UpstoxUrlBuilder
     */
    public static UpstoxUrlBuilder create() {
        return new UpstoxUrlBuilder();
    }

    /**
     * Sets the base URL.
     *
     * @param baseUrl the base URL (e.g., "https://api.upstox.com/v2")
     * @return this builder
     * @throws IllegalArgumentException if baseUrl is null or empty
     */
    public UpstoxUrlBuilder baseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        // Remove trailing slash if present
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return this;
    }

    /**
     * Sets the endpoint.
     *
     * @param endpoint the endpoint
     * @return this builder
     * @throws IllegalArgumentException if endpoint is null
     */
    public UpstoxUrlBuilder endpoint(UpstoxEndpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoint cannot be null");
        }
        return path(endpoint.getPath());
    }

    /**
     * Sets the path directly.
     *
     * @param path the path (e.g., "/order/place")
     * @return this builder
     * @throws IllegalArgumentException if path is null or empty
     */
    public UpstoxUrlBuilder path(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        // Ensure path starts with /
        this.path = path.startsWith("/") ? path : "/" + path;
        return this;
    }

    /**
     * Adds a path parameter.
     * <p>
     * Path parameters are used to replace placeholders in the path like {order_id}.
     * </p>
     *
     * @param name  the parameter name (without curly braces)
     * @param value the parameter value
     * @return this builder
     * @throws IllegalArgumentException if name or value is null/empty
     */
    public UpstoxUrlBuilder pathParam(String name, String value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Path parameter name cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Path parameter value cannot be null");
        }
        pathParams.put(name, value);
        return this;
    }

    /**
     * Adds multiple path parameters.
     *
     * @param params map of parameter names to values
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    public UpstoxUrlBuilder pathParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Path parameters map cannot be null");
        }
        params.forEach(this::pathParam);
        return this;
    }

    /**
     * Adds a query parameter.
     * <p>
     * Query parameters are appended to the URL as ?key=value&key2=value2.
     * Values are automatically URL encoded.
     * </p>
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this builder
     * @throws IllegalArgumentException if name is null/empty
     */
    public UpstoxUrlBuilder queryParam(String name, String value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter name cannot be null or empty");
        }
        if (value != null) {
            queryParams.put(name, value);
        }
        return this;
    }

    /**
     * Adds multiple query parameters.
     *
     * @param params map of parameter names to values
     * @return this builder
     * @throws IllegalArgumentException if params is null
     */
    public UpstoxUrlBuilder queryParams(Map<String, String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Query parameters map cannot be null");
        }
        params.forEach(this::queryParam);
        return this;
    }

    /**
     * Builds the complete URL.
     *
     * @return the complete URL string
     * @throws IllegalStateException if baseUrl or path is not set
     * @throws RuntimeException      if URL encoding fails
     */
    public String build() {
        if (baseUrl == null) {
            throw new IllegalStateException("Base URL must be set");
        }
        if (path == null) {
            throw new IllegalStateException("Path must be set");
        }

        StringBuilder url = new StringBuilder(baseUrl);

        // Replace path parameters
        String finalPath = path;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (finalPath.contains(placeholder)) {
                finalPath = finalPath.replace(placeholder, urlEncode(entry.getValue()));
            }
        }

        // Check if all placeholders are replaced
        if (finalPath.contains("{") && finalPath.contains("}")) {
            throw new IllegalStateException("Not all path parameters were provided: " + finalPath);
        }

        url.append(finalPath);

        // Add query parameters
        if (!queryParams.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    url.append("&");
                }
                url.append(entry.getKey()).append("=").append(urlEncode(entry.getValue()));
                first = false;
            }
        }

        return url.toString();
    }

    /**
     * URL encodes a string value.
     *
     * @param value the value to encode
     * @return the URL encoded value
     * @throws RuntimeException if encoding fails
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported
            throw new RuntimeException("Failed to URL encode value: " + value, e);
        }
    }

    /**
     * Clears all parameters and resets the builder.
     *
     * @return this builder
     */
    public UpstoxUrlBuilder reset() {
        this.baseUrl = null;
        this.path = null;
        this.pathParams.clear();
        this.queryParams.clear();
        return this;
    }
}
