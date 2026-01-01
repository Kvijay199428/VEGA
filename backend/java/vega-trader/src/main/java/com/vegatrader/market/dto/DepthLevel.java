package com.vegatrader.market.dto;

/**
 * Represents a single level in the order book (Price, Quantity, Orders).
 */
public class DepthLevel {
    private double price;
    private long quantity;
    private int orders;

    public DepthLevel() {
    }

    public DepthLevel(double price, long quantity, int orders) {
        this.price = price;
        this.quantity = quantity;
        this.orders = orders;
    }

    // Compatibility constructor for existing code calling without orders
    public DepthLevel(double price, long quantity) {
        this(price, quantity, 0);
    }

    // Getters
    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public int getOrders() {
        return orders;
    }

    // Setters
    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }
}
