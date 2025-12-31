package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for order history.
 *
 * @since 2.0.0
 */
public class OrderHistoryResponse {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("history")
    private List<OrderHistoryEntry> history;

    public OrderHistoryResponse() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<OrderHistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<OrderHistoryEntry> history) {
        this.history = history;
    }

    public int getHistoryCount() {
        return history != null ? history.size() : 0;
    }

    public static class OrderHistoryEntry {
        @SerializedName("timestamp")
        private Long timestamp;

        @SerializedName("state")
        private String state;

        @SerializedName("message")
        private String message;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("filled_quantity")
        private Integer filledQuantity;

        @SerializedName("price")
        private Double price;

        @SerializedName("average_price")
        private Double averagePrice;

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getFilledQuantity() {
            return filledQuantity;
        }

        public void setFilledQuantity(Integer filledQuantity) {
            this.filledQuantity = filledQuantity;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getAveragePrice() {
            return averagePrice;
        }

        public void setAveragePrice(Double averagePrice) {
            this.averagePrice = averagePrice;
        }

        @Override
        public String toString() {
            return String.format("%s: %s (filled: %d)", state, message, filledQuantity);
        }
    }

    @Override
    public String toString() {
        return String.format("OrderHistory{orderId='%s', entries=%d}",
                orderId, getHistoryCount());
    }
}
