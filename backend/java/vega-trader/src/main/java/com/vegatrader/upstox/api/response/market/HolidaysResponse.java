package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for market holidays.
 *
 * @since 2.0.0
 */
public class HolidaysResponse {

    @SerializedName("holidays")
    private List<Holiday> holidays;

    public HolidaysResponse() {
    }

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<Holiday> holidays) {
        this.holidays = holidays;
    }

    public int getCount() {
        return holidays != null ? holidays.size() : 0;
    }

    public static class Holiday {
        @SerializedName("date")
        private String date;

        @SerializedName("description")
        private String description;

        @SerializedName("day")
        private String day;

        @SerializedName("exchange")
        private String exchange;

        @SerializedName("type")
        private String type;

        public Holiday() {
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isNSE() {
            return "NSE".equalsIgnoreCase(exchange);
        }

        public boolean isBSE() {
            return "BSE".equalsIgnoreCase(exchange);
        }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)", date, description, exchange);
        }
    }

    @Override
    public String toString() {
        return String.format("Holidays{count=%d}", getCount());
    }
}
