package com.vegatrader.upstox.api.response.user;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for user funds and margin information.
 *
 * @since 2.0.0
 */
public class FundsResponse {

    @SerializedName("equity")
    private FundSegment equity;

    @SerializedName("commodity")
    private FundSegment commodity;

    @SerializedName("active_orders_count")
    private Integer activeOrdersCount;

    public FundsResponse() {
    }

    public FundSegment getEquity() {
        return equity;
    }

    public void setEquity(FundSegment equity) {
        this.equity = equity;
    }

    public FundSegment getCommodity() {
        return commodity;
    }

    public void setCommodity(FundSegment commodity) {
        this.commodity = commodity;
    }

    public Integer getActiveOrdersCount() {
        return activeOrdersCount;
    }

    public void setActiveOrdersCount(Integer activeOrdersCount) {
        this.activeOrdersCount = activeOrdersCount;
    }

    public static class FundSegment {
        @SerializedName("available_balance")
        private Double availableBalance;

        @SerializedName("used_balance")
        private Double usedBalance;

        @SerializedName("balance")
        private Double balance;

        public Double getAvailableBalance() {
            return availableBalance;
        }

        public void setAvailableBalance(Double availableBalance) {
            this.availableBalance = availableBalance;
        }

        public Double getUsedBalance() {
            return usedBalance;
        }

        public void setUsedBalance(Double usedBalance) {
            this.usedBalance = usedBalance;
        }

        public Double getBalance() {
            return balance;
        }

        public void setBalance(Double balance) {
            this.balance = balance;
        }

        @Override
        public String toString() {
            return String.format("Funds{total=%.2f, available=%.2f, used=%.2f}",
                    balance, availableBalance, usedBalance);
        }
    }
}
