package com.vegatrader.execution.dto;

public class OrderResponse {
    private String orderId;
    private String instrumentKey;
    private String status; // ACCEPTED, REJECTED, COMPLETE
    private String message;
    private int filledQuantity;
    private double avgPrice;

    public OrderResponse() {
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getFilledQuantity() {
        return filledQuantity;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFilledQuantity(int filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    // Builder
    public static OrderResponseBuilder builder() {
        return new OrderResponseBuilder();
    }

    public static class OrderResponseBuilder {
        private OrderResponse response = new OrderResponse();

        public OrderResponseBuilder orderId(String id) {
            response.setOrderId(id);
            return this;
        }

        public OrderResponseBuilder instrumentKey(String k) {
            response.setInstrumentKey(k);
            return this;
        }

        public OrderResponseBuilder status(String s) {
            response.setStatus(s);
            return this;
        }

        public OrderResponseBuilder message(String m) {
            response.setMessage(m);
            return this;
        }

        public OrderResponseBuilder filledQuantity(int q) {
            response.setFilledQuantity(q);
            return this;
        }

        public OrderResponseBuilder avgPrice(double p) {
            response.setAvgPrice(p);
            return this;
        }

        public OrderResponse build() {
            return response;
        }
    }
}
