package com.vegatrader.upstox.api.endpoints;

/**
 * Base interface for all Upstox API endpoints.
 * <p>
 * Each endpoint implementation provides:
 * <ul>
 * <li>The HTTP path (e.g., "/order/place")</li>
 * <li>The HTTP method (GET, POST, PUT, DELETE)</li>
 * <li>The category (e.g., "Orders", "Portfolio")</li>
 * <li>A human-readable description</li>
 * </ul>
 * </p>
 * <p>
 * This interface is typically implemented by enums that group related endpoints
 * together.
 * </p>
 *
 * @since 1.0.0
 * @see AuthenticationEndpoints
 * @see OrderEndpoints
 * @see PortfolioEndpoints
 */
public interface UpstoxEndpoint {

    /**
     * HTTP methods supported by Upstox API.
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }

    /**
     * Gets the endpoint path (e.g., "/order/place").
     * <p>
     * Path may contain placeholders like {order_id} that need to be replaced
     * with actual values when building the complete URL.
     * </p>
     *
     * @return the endpoint path
     */
    String getPath();

    /**
     * Gets the HTTP method for this endpoint.
     *
     * @return the HTTP method
     */
    HttpMethod getMethod();

    /**
     * Gets the category this endpoint belongs to (e.g., "Orders", "Portfolio").
     *
     * @return the category name
     */
    String getCategory();

    /**
     * Gets a human-readable description of this endpoint.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Returns true if this endpoint requires path parameters.
     * <p>
     * Default implementation checks if the path contains curly braces.
     * </p>
     *
     * @return true if path parameters are required
     */
    default boolean requiresPathParams() {
        return getPath().contains("{") && getPath().contains("}");
    }

    /**
     * Returns true if this is a GET endpoint.
     *
     * @return true if HTTP method is GET
     */
    default boolean isGetMethod() {
        return getMethod() == HttpMethod.GET;
    }

    /**
     * Returns true if this is a POST endpoint.
     *
     * @return true if HTTP method is POST
     */
    default boolean isPostMethod() {
        return getMethod() == HttpMethod.POST;
    }

    /**
     * Returns true if this is a PUT endpoint.
     *
     * @return true if HTTP method is PUT
     */
    default boolean isPutMethod() {
        return getMethod() == HttpMethod.PUT;
    }

    /**
     * Returns true if this is a DELETE endpoint.
     *
     * @return true if HTTP method is DELETE
     */
    default boolean isDeleteMethod() {
        return getMethod() == HttpMethod.DELETE;
    }
}
