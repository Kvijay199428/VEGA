package com.vegatrader.risk;

public class RiskSnapshot {
    private String clientId;
    private double usedMargin;
    private double availableMargin;
    private boolean riskBreached;
    private String reason;

    public RiskSnapshot() {
    }

    // Getters
    public String getClientId() {
        return clientId;
    }

    public double getUsedMargin() {
        return usedMargin;
    }

    public double getAvailableMargin() {
        return availableMargin;
    }

    public boolean isRiskBreached() {
        return riskBreached;
    }

    public String getReason() {
        return reason;
    }

    // Setters
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setUsedMargin(double usedMargin) {
        this.usedMargin = usedMargin;
    }

    public void setAvailableMargin(double availableMargin) {
        this.availableMargin = availableMargin;
    }

    public void setRiskBreached(boolean riskBreached) {
        this.riskBreached = riskBreached;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Builder
    public static RiskSnapshotBuilder builder() {
        return new RiskSnapshotBuilder();
    }

    public static class RiskSnapshotBuilder {
        private RiskSnapshot snapshot = new RiskSnapshot();

        public RiskSnapshotBuilder clientId(String c) {
            snapshot.setClientId(c);
            return this;
        }

        public RiskSnapshotBuilder usedMargin(double u) {
            snapshot.setUsedMargin(u);
            return this;
        }

        public RiskSnapshotBuilder availableMargin(double a) {
            snapshot.setAvailableMargin(a);
            return this;
        }

        public RiskSnapshotBuilder isRiskBreached(boolean b) {
            snapshot.setRiskBreached(b);
            return this;
        }

        public RiskSnapshotBuilder reason(String r) {
            snapshot.setReason(r);
            return this;
        }

        public RiskSnapshot build() {
            return snapshot;
        }
    }
}
