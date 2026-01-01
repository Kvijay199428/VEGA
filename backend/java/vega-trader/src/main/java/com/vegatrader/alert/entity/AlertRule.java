package com.vegatrader.alert.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Alert Rule Entity.
 * Defines a condition to monitor on a specific instrument.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    private String id = UUID.randomUUID().toString();
    private String clientId;
    private String instrumentKey;

    private AlertType type;
    private double threshold;
    private ComparisonOperator operator;

    private String message;
    private boolean active = true;
    private boolean oneShot = true; // Delete after firing?

    // Manual Getters
    public String getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public AlertType getType() {
        return type;
    }

    public double getThreshold() {
        return threshold;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public String getMessage() {
        return message;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOneShot() {
        return oneShot;
    }

    // Manual Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setOperator(ComparisonOperator operator) {
        this.operator = operator;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setOneShot(boolean oneShot) {
        this.oneShot = oneShot;
    }

    // Manual Builder
    public static AlertRuleBuilder builder() {
        return new AlertRuleBuilder();
    }

    public static class AlertRuleBuilder {
        private AlertRule rule = new AlertRule();

        public AlertRuleBuilder id(String id) {
            rule.setId(id);
            return this;
        }

        public AlertRuleBuilder clientId(String clientId) {
            rule.setClientId(clientId);
            return this;
        }

        public AlertRuleBuilder instrumentKey(String instrumentKey) {
            rule.setInstrumentKey(instrumentKey);
            return this;
        }

        public AlertRuleBuilder type(AlertType type) {
            rule.setType(type);
            return this;
        }

        public AlertRuleBuilder threshold(double threshold) {
            rule.setThreshold(threshold);
            return this;
        }

        public AlertRuleBuilder operator(ComparisonOperator operator) {
            rule.setOperator(operator);
            return this;
        }

        public AlertRuleBuilder message(String message) {
            rule.setMessage(message);
            return this;
        }

        public AlertRuleBuilder active(boolean active) {
            rule.setActive(active);
            return this;
        }

        public AlertRuleBuilder oneShot(boolean oneShot) {
            rule.setOneShot(oneShot);
            return this;
        }

        public AlertRule build() {
            return rule;
        }
    }

    public enum AlertType {
        PRICE_ABOVE, PRICE_BELOW, CHANGE_PERCENT_GREATER, CHANGE_PERCENT_LESS, VOLUME_GREATER
    }

    public enum ComparisonOperator {
        GT, LT, EQ, GTE, LTE
    }
}
