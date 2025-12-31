package com.vegatrader.upstox.api.request.order;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for modifying a pending order.
 *
 * @since 2.0.0
 */
public class ModifyOrderRequest {

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("validity")
    private String validity;

    @SerializedName("price")
    private Double price;

    @SerializedName("order_type")
    private String orderType;

    @SerializedName("disclosed_quantity")
    private Integer disclosedQuantity;

    @SerializedName("trigger_price")
    private Double triggerPrice;

    public ModifyOrderRequest() {
    }

    public static ModifyOrderRequestBuilder builder() {
        return new ModifyOrderRequestBuilder();
    }

    // Getters/Setters
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getDisclosedQuantity() {
        return disclosedQuantity;
    }

    public void setDisclosedQuantity(Integer disclosedQuantity) {
        this.disclosedQuantity = disclosedQuantity;
    }

    public Double getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(Double triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public static class ModifyOrderRequestBuilder {
        private Integer quantity;
        private String validity, orderType;
        private Double price, triggerPrice;
        private Integer disclosedQuantity;

        public ModifyOrderRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ModifyOrderRequestBuilder validity(String validity) {
            this.validity = validity;
            return this;
        }

        public ModifyOrderRequestBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public ModifyOrderRequestBuilder orderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public ModifyOrderRequestBuilder disclosedQuantity(Integer qty) {
            this.disclosedQuantity = qty;
            return this;
        }

        public ModifyOrderRequestBuilder triggerPrice(Double triggerPrice) {
            this.triggerPrice = triggerPrice;
            return this;
        }

        public ModifyOrderRequest build() {
            ModifyOrderRequest request = new ModifyOrderRequest();
            request.quantity = this.quantity;
            request.validity = this.validity;
            request.price = this.price;
            request.orderType = this.orderType;
            request.disclosedQuantity = this.disclosedQuantity;
            request.triggerPrice = this.triggerPrice;
            return request;
        }
    }
}
