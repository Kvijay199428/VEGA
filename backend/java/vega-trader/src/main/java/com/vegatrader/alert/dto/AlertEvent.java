package com.vegatrader.alert.dto;

public class AlertEvent {
    private String id;
    private String ruleId;
    private String instrumentKey;
    private String message;
    private double value;
    private long timestamp;

    public AlertEvent() {
    }

    public AlertEvent(String id, String ruleId, String instrumentKey, String message, double value, long timestamp) {
        this.id = id;
        this.ruleId = ruleId;
        this.instrumentKey = instrumentKey;
        this.message = message;
        this.value = value;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public String getMessage() {
        return message;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Builder
    public static AlertEventBuilder builder() {
        return new AlertEventBuilder();
    }

    public static class AlertEventBuilder {
        private AlertEvent event = new AlertEvent();

        public AlertEventBuilder id(String id) {
            event.setId(id);
            return this;
        }

        public AlertEventBuilder ruleId(String ruleId) {
            event.setRuleId(ruleId);
            return this;
        }

        public AlertEventBuilder instrumentKey(String instrumentKey) {
            event.setInstrumentKey(instrumentKey);
            return this;
        }

        public AlertEventBuilder message(String message) {
            event.setMessage(message);
            return this;
        }

        public AlertEventBuilder value(double value) {
            event.setValue(value);
            return this;
        }

        public AlertEventBuilder timestamp(long timestamp) {
            event.setTimestamp(timestamp);
            return this;
        }

        public AlertEvent build() {
            return event;
        }
    }
}
