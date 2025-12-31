package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for GTT order listing.
 *
 * @since 2.0.0
 */
public class GTTListResponse {

    @SerializedName("gtt_orders")
    private List<GTTResponse> gttOrders;

    public GTTListResponse() {
    }

    public List<GTTResponse> getGttOrders() {
        return gttOrders;
    }

    public void setGttOrders(List<GTTResponse> gttOrders) {
        this.gttOrders = gttOrders;
    }

    public int getTotalCount() {
        return gttOrders != null ? gttOrders.size() : 0;
    }

    public long getActiveCount() {
        if (gttOrders == null)
            return 0;
        return gttOrders.stream()
                .filter(GTTResponse::isActive)
                .count();
    }

    public long getTriggeredCount() {
        if (gttOrders == null)
            return 0;
        return gttOrders.stream()
                .filter(GTTResponse::isTriggered)
                .count();
    }

    @Override
    public String toString() {
        return String.format("GTTList{total=%d, active=%d, triggered=%d}",
                getTotalCount(), getActiveCount(), getTriggeredCount());
    }
}
