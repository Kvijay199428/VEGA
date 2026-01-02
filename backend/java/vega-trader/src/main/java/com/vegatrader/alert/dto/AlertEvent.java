package com.vegatrader.alert.dto;

public class AlertEvent {
    private String clientId;
    private String ruleId;
    private String instrumentKey;
    private String message;
    private double triggerValue;
    private long timestamp;

    public AlertEvent() {
    }

    public AlertEvent(String clientId, String ruleId, String instrumentKey, String message, double triggerValue,
            long timestamp) {
        this.clientId = clientId;
        this.ruleId = ruleId;
        this.instrumentKey = instrumentKey;
        this.message = message;
        this.triggerValue = triggerValue;
        this.timestamp = timestamp;
    }

    // Getters
    public String getClientId() {
        return clientId;
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

    public double getTriggerValue() {
        return triggerValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public void setTriggerValue(double triggerValue) {
        this.triggerValue = triggerValue;
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

        public AlertEventBuilder clientId(String clientId) {
            event.setClientId(clientId);
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

        public AlertEventBuilder triggerValue(double triggerValue) {
            event.setTriggerValue(triggerValue);
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
