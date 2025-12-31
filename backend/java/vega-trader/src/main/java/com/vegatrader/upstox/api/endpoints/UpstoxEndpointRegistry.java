package com.vegatrader.upstox.api.endpoints;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Central registry for all Upstox API endpoints.
 * <p>
 * This registry provides methods to:
 * <ul>
 * <li>Get all available endpoints</li>
 * <li>Filter endpoints by category</li>
 * <li>Find endpoints by path and method</li>
 * <li>Get endpoint statistics</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Get all endpoints
 * List<UpstoxEndpoint> allEndpoints = UpstoxEndpointRegistry.getAllEndpoints();
 * 
 * // Filter by category
 * List<UpstoxEndpoint> orderEndpoints = UpstoxEndpointRegistry.getEndpointsByCategory("Orders");
 * 
 * // Find specific endpoint
 * Optional<UpstoxEndpoint> endpoint = UpstoxEndpointRegistry.findEndpoint("/order/place", HttpMethod.POST);
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 * @see UpstoxEndpoint
 */
public final class UpstoxEndpointRegistry {

    private static final List<UpstoxEndpoint> ALL_ENDPOINTS;
    private static final Map<String, List<UpstoxEndpoint>> ENDPOINTS_BY_CATEGORY;

    static {
        // Collect all endpoints from all enum classes
        ALL_ENDPOINTS = Collections.unmodifiableList(
                Stream.of(
                        Arrays.asList(AuthenticationEndpoints.values()),
                        Arrays.asList(UserProfileEndpoints.values()),
                        Arrays.asList(OrderEndpoints.values()),
                        Arrays.asList(PortfolioEndpoints.values()),
                        Arrays.asList(MarketDataEndpoints.values()),
                        Arrays.asList(OptionChainEndpoints.values()),
                        Arrays.asList(WebSocketEndpoints.values()))
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));

        // Group endpoints by category
        ENDPOINTS_BY_CATEGORY = Collections.unmodifiableMap(
                ALL_ENDPOINTS.stream()
                        .collect(Collectors.groupingBy(UpstoxEndpoint::getCategory)));
    }

    // Private constructor to prevent instantiation
    private UpstoxEndpointRegistry() {
        throw new AssertionError("UpstoxEndpointRegistry cannot be instantiated");
    }

    /**
     * Gets all registered Upstox endpoints.
     *
     * @return an immutable list of all endpoints
     */
    public static List<UpstoxEndpoint> getAllEndpoints() {
        return ALL_ENDPOINTS;
    }

    /**
     * Gets all unique categories.
     *
     * @return a set of category names
     */
    public static Set<String> getAllCategories() {
        return ENDPOINTS_BY_CATEGORY.keySet();
    }

    /**
     * Gets endpoints for a specific category.
     *
     * @param category the category name (case-sensitive)
     * @return a list of endpoints in the category, or empty list if category not
     *         found
     */
    public static List<UpstoxEndpoint> getEndpointsByCategory(String category) {
        return ENDPOINTS_BY_CATEGORY.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Finds an endpoint by its path and HTTP method.
     *
     * @param path   the endpoint path
     * @param method the HTTP method
     * @return an Optional containing the endpoint if found
     */
    public static Optional<UpstoxEndpoint> findEndpoint(String path, UpstoxEndpoint.HttpMethod method) {
        return ALL_ENDPOINTS.stream()
                .filter(e -> e.getPath().equals(path) && e.getMethod() == method)
                .findFirst();
    }

    /**
     * Finds endpoints by path (regardless of HTTP method).
     *
     * @param path the endpoint path
     * @return a list of endpoints with the given path
     */
    public static List<UpstoxEndpoint> findEndpointsByPath(String path) {
        return ALL_ENDPOINTS.stream()
                .filter(e -> e.getPath().equals(path))
                .collect(Collectors.toList());
    }

    /**
     * Finds endpoints by HTTP method.
     *
     * @param method the HTTP method
     * @return a list of endpoints using the given method
     */
    public static List<UpstoxEndpoint> findEndpointsByMethod(UpstoxEndpoint.HttpMethod method) {
        return ALL_ENDPOINTS.stream()
                .filter(e -> e.getMethod() == method)
                .collect(Collectors.toList());
    }

    /**
     * Gets the total number of registered endpoints.
     *
     * @return the total endpoint count
     */
    public static int getTotalEndpointCount() {
        return ALL_ENDPOINTS.size();
    }

    /**
     * Gets endpoint count by category.
     *
     * @return a map of category names to endpoint counts
     */
    public static Map<String, Integer> getEndpointCountByCategory() {
        return ENDPOINTS_BY_CATEGORY.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()));
    }

    /**
     * Gets endpoint count by HTTP method.
     *
     * @return a map of HTTP methods to endpoint counts
     */
    public static Map<UpstoxEndpoint.HttpMethod, Long> getEndpointCountByMethod() {
        return ALL_ENDPOINTS.stream()
                .collect(Collectors.groupingBy(
                        UpstoxEndpoint::getMethod,
                        Collectors.counting()));
    }

    /**
     * Returns a formatted string with registry statistics.
     *
     * @return registry statistics
     */
    public static String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Upstox Endpoint Registry Statistics:\n");
        sb.append("====================================\n");
        sb.append(String.format("Total Endpoints: %d\n\n", getTotalEndpointCount()));

        sb.append("Endpoints by Category:\n");
        getEndpointCountByCategory()
                .forEach((category, count) -> sb.append(String.format("  %-20s: %d endpoints\n", category, count)));

        sb.append("\nEndpoints by HTTP Method:\n");
        getEndpointCountByMethod()
                .forEach((method, count) -> sb.append(String.format("  %-10s: %d endpoints\n", method, count)));

        return sb.toString();
    }

    /**
     * Prints registry statistics to standard output.
     */
    public static void printStatistics() {
        System.out.println(getStatistics());
    }
}
