package com.vegatrader.upstox.api.response.user;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for account information.
 *
 * @since 2.0.0
 */
public class AccountInfoResponse {

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("demat_account_number")
    private String dematAccountNumber;

    @SerializedName("trading_account_number")
    private String tradingAccountNumber;

    @SerializedName("account_status")
    private String accountStatus;

    @SerializedName("enabled_exchanges")
    private List<String> enabledExchanges;

    @SerializedName("enabled_products")
    private List<String> enabledProducts;

    @SerializedName("account_type")
    private String accountType;

    public AccountInfoResponse() {
    }

    // Getters/Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getDematAccountNumber() {
        return dematAccountNumber;
    }

    public void setDematAccountNumber(String dematAccountNumber) {
        this.dematAccountNumber = dematAccountNumber;
    }

    public String getTradingAccountNumber() {
        return tradingAccountNumber;
    }

    public void setTradingAccountNumber(String tradingAccountNumber) {
        this.tradingAccountNumber = tradingAccountNumber;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public List<String> getEnabledExchanges() {
        return enabledExchanges;
    }

    public void setEnabledExchanges(List<String> enabledExchanges) {
        this.enabledExchanges = enabledExchanges;
    }

    public List<String> getEnabledProducts() {
        return enabledProducts;
    }

    public void setEnabledProducts(List<String> enabledProducts) {
        this.enabledProducts = enabledProducts;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(accountStatus);
    }

    public boolean hasExchange(String exchange) {
        return enabledExchanges != null && enabledExchanges.contains(exchange);
    }

    public boolean hasProduct(String product) {
        return enabledProducts != null && enabledProducts.contains(product);
    }

    @Override
    public String toString() {
        return String.format("Account{id='%s', name='%s', status='%s'}",
                clientId, name, accountStatus);
    }
}
