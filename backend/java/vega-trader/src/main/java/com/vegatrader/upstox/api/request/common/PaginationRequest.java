package com.vegatrader.upstox.api.request.common;

/**
 * Common request DTO for pagination parameters.
 *
 * @since 2.0.0
 */
public class PaginationRequest {

    private Integer pageNumber;
    private Integer pageSize;

    public PaginationRequest() {
        this.pageNumber = 1;
        this.pageSize = 10;
    }

    public PaginationRequest(Integer pageNumber, Integer pageSize) {
        this.pageNumber = pageNumber != null ? pageNumber : 1;
        this.pageSize = pageSize != null ? pageSize : 10;
    }

    public static PaginationRequestBuilder builder() {
        return new PaginationRequestBuilder();
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    public void validate() {
        if (pageNumber == null || pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be >= 1");
        }
        if (pageSize == null || pageSize < 1) {
            throw new IllegalArgumentException("Page size must be >= 1");
        }
        if (pageSize > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }

    public static class PaginationRequestBuilder {
        private Integer pageNumber = 1;
        private Integer pageSize = 10;

        public PaginationRequestBuilder pageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PaginationRequestBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginationRequestBuilder firstPage() {
            this.pageNumber = 1;
            return this;
        }

        public PaginationRequestBuilder nextPage(int currentPage) {
            this.pageNumber = currentPage + 1;
            return this;
        }

        public PaginationRequest build() {
            return new PaginationRequest(pageNumber, pageSize);
        }
    }

    @Override
    public String toString() {
        return String.format("Pagination{page=%d, size=%d, offset=%d}",
                pageNumber, pageSize, getOffset());
    }
}
