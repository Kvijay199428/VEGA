package com.vegatrader.upstox.api.response.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for user profile information.
 *
 * @since 2.0.0
 */
public class UserProfileResponse {

    @SerializedName("email")
    private String email;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("name")
    private String name;

    @SerializedName("pan")
    private String pan;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("broker_name")
    private String brokerName;

    @SerializedName("exchanges")
    private List<String> exchanges;

    @SerializedName("products")
    private List<String> products;

    @SerializedName("poa")
    private Boolean poa;

    @SerializedName("is_active")
    private Boolean isActive;

    public UserProfileResponse() {
    }

    // Getters/Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
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

    public Boolean getPoa() {
        return poa;
    }

    public void setPoa(Boolean poa) {
        this.poa = poa;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return String.format("UserProfile{userId='%s', name='%s', exchanges=%s}",
                userId, name, exchanges);
    }
}
