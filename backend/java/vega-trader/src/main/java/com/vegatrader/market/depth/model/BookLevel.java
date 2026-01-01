package com.vegatrader.market.depth.model;

public class BookLevel {
    private double price;
    private long quantity;
    private int orders;

    public BookLevel() {
    }

    public BookLevel(double price, long quantity, int orders) {
        this.price = price;
        this.quantity = quantity;
        this.orders = orders;
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

    // Builder
    public static BookLevelBuilder builder() {
        return new BookLevelBuilder();
    }

    public static class BookLevelBuilder {
        private BookLevel level = new BookLevel();

        public BookLevelBuilder price(double p) {
            level.setPrice(p);
            return this;
        }

        public BookLevelBuilder quantity(long q) {
            level.setQuantity(q);
            return this;
        }

        public BookLevelBuilder orders(int o) {
            level.setOrders(o);
            return this;
        }

        public BookLevel build() {
            return level;
        }
    }
}
