package com.vegatrader.execution.dto;

public class OrderRequest {
    private String instrumentKey;
    private int quantity;
    private double price;
    private String orderType; // LIMIT, MARKET
    private String transactionType; // BUY, SELL
    private String product; // I, D
    private String validity; // DAY, IOC

    public OrderRequest() {
    }

    // Getters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getProduct() {
        return product;
    }

    public String getValidity() {
        return validity;
    }

    // Setters
    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    // Builder
    public static OrderRequestBuilder builder() {
        return new OrderRequestBuilder();
    }

    public static class OrderRequestBuilder {
        private OrderRequest request = new OrderRequest();

        public OrderRequestBuilder instrumentKey(String k) {
            request.setInstrumentKey(k);
            return this;
        }

        public OrderRequestBuilder quantity(int q) {
            request.setQuantity(q);
            return this;
        }

        public OrderRequestBuilder price(double p) {
            request.setPrice(p);
            return this;
        }

        public OrderRequestBuilder orderType(String t) {
            request.setOrderType(t);
            return this;
        }

        public OrderRequestBuilder transactionType(String t) {
            request.setTransactionType(t);
            return this;
        }

        public OrderRequestBuilder product(String p) {
            request.setProduct(p);
            return this;
        }

        public OrderRequestBuilder validity(String v) {
            request.setValidity(v);
            return this;
        }

        public OrderRequest build() {
            return request;
        }
    }
}
