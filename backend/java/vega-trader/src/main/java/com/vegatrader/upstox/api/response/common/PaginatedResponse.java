package com.vegatrader.upstox.api.response.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Generic paginated response wrapper for API endpoints that return paginated
 * data.
 * <p>
 * This class provides standardized pagination metadata along with the data,
 * making it easy to navigate through large datasets.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * PaginatedResponse<Order> orders = new PaginatedResponse<>();
 * orders.setData(orderList);
 * orders.setPageNumber(1);
 * orders.setPageSize(10);
 * orders.setTotalPages(5);
 * orders.setTotalElements(50);
 * 
 * // Check pagination
 * if (orders.hasNext()) {
 *     // Fetch next page
 * }
 * }</pre>
 * </p>
 *
 * @param <T> the type of elements in the paginated list
 * @since 2.0.0
 */
public class PaginatedResponse<T> {

    @SerializedName("data")
    private List<T> data;

    @SerializedName("page_number")
    private Integer pageNumber;

    @SerializedName("page_size")
    private Integer pageSize;

    @SerializedName("total_pages")
    private Integer totalPages;

    @SerializedName("total_elements")
    private Long totalElements;

    @SerializedName("first")
    private Boolean first;

    @SerializedName("last")
    private Boolean last;

    @SerializedName("has_next")
    private Boolean hasNext;

    @SerializedName("has_previous")
    private Boolean hasPrevious;

    /**
     * Default constructor for JSON deserialization.
     */
    public PaginatedResponse() {
    }

    /**
     * Creates a new paginated response.
     *
     * @param data          the list of data elements
     * @param pageNumber    the current page number (0-indexed or 1-indexed based on
     *                      API)
     * @param pageSize      the number of elements per page
     * @param totalElements the total number of elements across all pages
     */
    public PaginatedResponse(List<T> data, Integer pageNumber, Integer pageSize, Long totalElements) {
        this.data = data;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;

        // Calculate derived fields
        if (totalElements != null && pageSize != null && pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
            this.first = pageNumber != null && pageNumber <= 1;
            this.last = pageNumber != null && pageNumber >= totalPages;
            this.hasNext = !this.last;
            this.hasPrevious = !this.first;
        }
    }

    /**
     * Builder for creating paginated responses.
     *
     * @param <T> the type of elements
     * @return a new PaginatedResponseBuilder
     */
    public static <T> PaginatedResponseBuilder<T> builder() {
        return new PaginatedResponseBuilder<>();
    }

    /**
     * Gets the list of data elements for the current page.
     *
     * @return the data list
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Sets the data list.
     *
     * @param data the data to set
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * Gets the current page number.
     *
     * @return the page number
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * Sets the page number.
     *
     * @param pageNumber the page number to set
     */
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Gets the page size (number of elements per page).
     *
     * @return the page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize the page size to set
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Gets the total number of pages.
     *
     * @return the total pages
     */
    public Integer getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total pages.
     *
     * @param totalPages the total pages to set
     */
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Gets the total number of elements across all pages.
     *
     * @return the total elements
     */
    public Long getTotalElements() {
        return totalElements;
    }

    /**
     * Sets the total elements.
     *
     * @param totalElements the total elements to set
     */
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Returns true if this is the first page.
     *
     * @return true if first page
     */
    public Boolean isFirst() {
        return first != null && first;
    }

    /**
     * Sets the first page flag.
     *
     * @param first the flag to set
     */
    public void setFirst(Boolean first) {
        this.first = first;
    }

    /**
     * Returns true if this is the last page.
     *
     * @return true if last page
     */
    public Boolean isLast() {
        return last != null && last;
    }

    /**
     * Sets the last page flag.
     *
     * @param last the flag to set
     */
    public void setLast(Boolean last) {
        this.last = last;
    }

    /**
     * Returns true if there is a next page.
     *
     * @return true if has next page
     */
    public Boolean hasNext() {
        return hasNext != null && hasNext;
    }

    /**
     * Sets the has next flag.
     *
     * @param hasNext the flag to set
     */
    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    /**
     * Returns true if there is a previous page.
     *
     * @return true if has previous page
     */
    public Boolean hasPrevious() {
        return hasPrevious != null && hasPrevious;
    }

    /**
     * Sets the has previous flag.
     *
     * @param hasPrevious the flag to set
     */
    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    /**
     * Gets the number of elements in the current page.
     *
     * @return the current page element count
     */
    public int getCurrentPageElementCount() {
        return data != null ? data.size() : 0;
    }

    /**
     * Returns true if the current page is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("PaginatedResponse{page=%d/%d, size=%d, total=%d, elements=%d}",
                pageNumber, totalPages, pageSize, totalElements, getCurrentPageElementCount());
    }

    /**
     * Builder class for PaginatedResponse.
     *
     * @param <T> the type of elements
     */
    public static class PaginatedResponseBuilder<T> {
        private List<T> data;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalElements;

        public PaginatedResponseBuilder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public PaginatedResponseBuilder<T> pageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PaginatedResponseBuilder<T> pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginatedResponseBuilder<T> totalElements(Long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public PaginatedResponse<T> build() {
            return new PaginatedResponse<>(data, pageNumber, pageSize, totalElements);
        }
    }
}
