package com.vegatrader.upstox.auth.dto;

import java.util.List;

/**
 * Profile view DTO for login success page.
 * Mapped from Get Profile API response.
 *
 * @since 2.3.0
 */
public class ProfileView {

    private String userId;
    private String userName;
    private String email;
    private String broker;
    private String userType;

    private List<String> exchanges;
    private List<String> products;
    private List<String> orderTypes;

    private boolean poa;
    private boolean ddpi;
    private boolean isActive;

    public ProfileView() {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public List<String> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<String> exchanges) {
        this.exchanges = exchanges;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setProducts(List<String> products) {
        this.products = products;
    }

    public List<String> getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(List<String> orderTypes) {
        this.orderTypes = orderTypes;
    }

    public boolean isPoa() {
        return poa;
    }

    public void setPoa(boolean poa) {
        this.poa = poa;
    }

    public boolean isDdpi() {
        return ddpi;
    }

    public void setDdpi(boolean ddpi) {
        this.ddpi = ddpi;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Builder for ProfileView.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ProfileView view = new ProfileView();

        public Builder userId(String v) {
            view.userId = v;
            return this;
        }

        public Builder userName(String v) {
            view.userName = v;
            return this;
        }

        public Builder email(String v) {
            view.email = v;
            return this;
        }

        public Builder broker(String v) {
            view.broker = v;
            return this;
        }

        public Builder userType(String v) {
            view.userType = v;
            return this;
        }

        public Builder exchanges(List<String> v) {
            view.exchanges = v;
            return this;
        }

        public Builder products(List<String> v) {
            view.products = v;
            return this;
        }

        public Builder orderTypes(List<String> v) {
            view.orderTypes = v;
            return this;
        }

        public Builder poa(boolean v) {
            view.poa = v;
            return this;
        }

        public Builder ddpi(boolean v) {
            view.ddpi = v;
            return this;
        }

        public Builder active(boolean v) {
            view.isActive = v;
            return this;
        }

        public ProfileView build() {
            return view;
        }
    }
}
